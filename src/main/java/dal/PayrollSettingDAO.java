package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.PayrollSetting;

public class PayrollSettingDAO {

	private static final String SELECT_COLUMNS = """
			id,
			standard_work_days,
			standard_work_hours_per_day,
			normal_overtime_rate,
			effective_from,
			effective_to,
			created_at,
			updated_at
			""";
	private static final BigDecimal DEFAULT_WORK_DAYS = new BigDecimal("22");
	private static final BigDecimal DEFAULT_HOURS_PER_DAY = new BigDecimal("8");
	private static final BigDecimal DEFAULT_OT_RATE = new BigDecimal("1.5");

	public PayrollSetting getActiveForPeriod(int year, int month) {
		PayrollSetting setting = getConfiguredForPeriod(year, month);
		return setting != null ? setting : defaultSetting();
	}

	public PayrollSetting getConfiguredForPeriod(int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());

		String sql = """
				SELECT %s
				FROM payroll_settings
				WHERE effective_from <= ?
				  AND (effective_to IS NULL OR effective_to >= ?)
				ORDER BY effective_from DESC, id DESC
				LIMIT 1
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, lastDay);
			ps.setDate(2, firstDay);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("PayrollSettingDAO.getActiveForPeriod() ERROR: " + e.getMessage());
		}

		return null;
	}

	public List<PayrollSetting> getAll() {
		List<PayrollSetting> settings = new ArrayList<>();
		String sql = """
				SELECT %s
				FROM payroll_settings
				ORDER BY effective_from DESC, id DESC
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				settings.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("PayrollSettingDAO.getAll() ERROR: " + e.getMessage());
		}

		return settings;
	}

	public PayrollSetting getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT %s
				FROM payroll_settings
				WHERE id = ?
				""".formatted(SELECT_COLUMNS);
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("PayrollSettingDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean hasBlockingPeriod(Long excludeId, Date effectiveFrom, Date effectiveTo) {
		if (effectiveFrom == null) {
			return false;
		}

		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM payroll_settings
				WHERE effective_from >= ?
				  AND id <> ?
				""");
		if (effectiveTo != null) {
			sql.append(" AND effective_from <= ?");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setDate(1, effectiveFrom);
			ps.setLong(2, excludeId != null ? excludeId : -1L);
			if (effectiveTo != null) {
				ps.setDate(3, effectiveTo);
			}
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("PayrollSettingDAO.hasBlockingPeriod() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean upsert(PayrollSetting setting) {
		if (setting == null || setting.getEffectiveFrom() == null) {
			return false;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				if (hasBlockingPeriod(conn, setting.getId(), setting.getEffectiveFrom(), setting.getEffectiveTo())) {
					conn.rollback();
					return false;
				}

				closePreviousOverlappingSetting(conn, setting.getId(), setting.getEffectiveFrom());
				boolean success = setting.getId() == null ? insert(conn, setting) : update(conn, setting);
				if (success) {
					conn.commit();
					return true;
				}
				conn.rollback();
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("PayrollSettingDAO.upsert() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("PayrollSettingDAO.upsert() connection ERROR: " + e.getMessage());
		}

		return false;
	}

	private PayrollSetting defaultSetting() {
		PayrollSetting setting = new PayrollSetting();
		setting.setStandardWorkDays(DEFAULT_WORK_DAYS);
		setting.setStandardWorkHoursPerDay(DEFAULT_HOURS_PER_DAY);
		setting.setNormalOvertimeRate(DEFAULT_OT_RATE);
		return setting;
	}

	private boolean hasBlockingPeriod(Connection conn, Long excludeId, Date effectiveFrom, Date effectiveTo)
			throws SQLException {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM payroll_settings
				WHERE effective_from >= ?
				  AND id <> ?
				""");
		if (effectiveTo != null) {
			sql.append(" AND effective_from <= ?");
		}

		try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setDate(1, effectiveFrom);
			ps.setLong(2, excludeId != null ? excludeId : -1L);
			if (effectiveTo != null) {
				ps.setDate(3, effectiveTo);
			}
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	private void closePreviousOverlappingSetting(Connection conn, Long excludeId, Date effectiveFrom)
			throws SQLException {
		String sql = """
				UPDATE payroll_settings
				SET effective_to = DATE_SUB(?, INTERVAL 1 DAY),
				    updated_at = CURRENT_TIMESTAMP
				WHERE effective_from < ?
				  AND (effective_to IS NULL OR effective_to >= ?)
				  AND id <> ?
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, effectiveFrom);
			ps.setDate(2, effectiveFrom);
			ps.setDate(3, effectiveFrom);
			ps.setLong(4, excludeId != null ? excludeId : -1L);
			ps.executeUpdate();
		}
	}

	private boolean insert(Connection conn, PayrollSetting setting) throws SQLException {
		String sql = """
				INSERT INTO payroll_settings
				    (standard_work_days, standard_work_hours_per_day, normal_overtime_rate, effective_from, effective_to)
				VALUES (?, ?, ?, ?, ?)
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, setting);
			return ps.executeUpdate() > 0;
		}
	}

	private boolean update(Connection conn, PayrollSetting setting) throws SQLException {
		String sql = """
				UPDATE payroll_settings
				SET standard_work_days = ?,
				    standard_work_hours_per_day = ?,
				    normal_overtime_rate = ?,
				    effective_from = ?,
				    effective_to = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, setting);
			ps.setLong(6, setting.getId());
			return ps.executeUpdate() > 0;
		}
	}

	private void setMutationParams(PreparedStatement ps, PayrollSetting setting) throws SQLException {
		ps.setBigDecimal(1, setting.getStandardWorkDays());
		ps.setBigDecimal(2, setting.getStandardWorkHoursPerDay());
		ps.setBigDecimal(3, setting.getNormalOvertimeRate());
		ps.setDate(4, setting.getEffectiveFrom());
		if (setting.getEffectiveTo() != null) {
			ps.setDate(5, setting.getEffectiveTo());
		} else {
			ps.setNull(5, java.sql.Types.DATE);
		}
	}

	private PayrollSetting mapRow(ResultSet rs) throws SQLException {
		PayrollSetting setting = new PayrollSetting();
		setting.setId(rs.getLong("id"));
		setting.setStandardWorkDays(rs.getBigDecimal("standard_work_days"));
		setting.setStandardWorkHoursPerDay(rs.getBigDecimal("standard_work_hours_per_day"));
		setting.setNormalOvertimeRate(rs.getBigDecimal("normal_overtime_rate"));
		setting.setEffectiveFrom(rs.getDate("effective_from"));
		setting.setEffectiveTo(rs.getDate("effective_to"));
		setting.setCreatedAt(rs.getTimestamp("created_at"));
		setting.setUpdatedAt(rs.getTimestamp("updated_at"));
		return setting;
	}
}
