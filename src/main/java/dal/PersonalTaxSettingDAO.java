package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.PersonalTaxSetting;

public class PersonalTaxSettingDAO {

	private static final String SELECT_COLUMNS = """
			id,
			personal_deduction,
			dependent_deduction,
			effective_from,
			effective_to,
			created_at,
			updated_at
			""";

	public PersonalTaxSetting getActiveForPeriod(int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());
		String sql = """
				SELECT %s
				FROM personal_tax_settings
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
			System.err.println("PersonalTaxSettingDAO.getActiveForPeriod() ERROR: " + e.getMessage());
		}
		return null;
	}

	public List<PersonalTaxSetting> getAll() {
		List<PersonalTaxSetting> settings = new ArrayList<>();
		String sql = """
				SELECT %s
				FROM personal_tax_settings
				ORDER BY effective_from DESC, id DESC
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				settings.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("PersonalTaxSettingDAO.getAll() ERROR: " + e.getMessage());
		}
		return settings;
	}

	public PersonalTaxSetting getById(Long id) {
		if (id == null) {
			return null;
		}
		String sql = """
				SELECT %s
				FROM personal_tax_settings
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
			System.err.println("PersonalTaxSettingDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean hasBlockingPeriod(Long excludeId, Date effectiveFrom, Date effectiveTo) {
		if (effectiveFrom == null) {
			return false;
		}
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM personal_tax_settings
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
			System.err.println("PersonalTaxSettingDAO.hasBlockingPeriod() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean upsert(PersonalTaxSetting setting) {
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
				System.err.println("PersonalTaxSettingDAO.upsert() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("PersonalTaxSettingDAO.upsert() connection ERROR: " + e.getMessage());
		}
		return false;
	}

	private boolean hasBlockingPeriod(Connection conn, Long excludeId, Date effectiveFrom, Date effectiveTo)
			throws SQLException {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM personal_tax_settings
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
				UPDATE personal_tax_settings
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

	private boolean insert(Connection conn, PersonalTaxSetting setting) throws SQLException {
		String sql = """
				INSERT INTO personal_tax_settings
				    (personal_deduction, dependent_deduction, effective_from, effective_to)
				VALUES (?, ?, ?, ?)
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, setting);
			return ps.executeUpdate() > 0;
		}
	}

	private boolean update(Connection conn, PersonalTaxSetting setting) throws SQLException {
		String sql = """
				UPDATE personal_tax_settings
				SET personal_deduction = ?,
				    dependent_deduction = ?,
				    effective_from = ?,
				    effective_to = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, setting);
			ps.setLong(5, setting.getId());
			return ps.executeUpdate() > 0;
		}
	}

	private void setMutationParams(PreparedStatement ps, PersonalTaxSetting setting) throws SQLException {
		ps.setBigDecimal(1, setting.getPersonalDeduction());
		ps.setBigDecimal(2, setting.getDependentDeduction());
		ps.setDate(3, setting.getEffectiveFrom());
		if (setting.getEffectiveTo() != null) {
			ps.setDate(4, setting.getEffectiveTo());
		} else {
			ps.setNull(4, java.sql.Types.DATE);
		}
	}

	private PersonalTaxSetting mapRow(ResultSet rs) throws SQLException {
		PersonalTaxSetting setting = new PersonalTaxSetting();
		setting.setId(rs.getLong("id"));
		setting.setPersonalDeduction(rs.getBigDecimal("personal_deduction"));
		setting.setDependentDeduction(rs.getBigDecimal("dependent_deduction"));
		setting.setEffectiveFrom(rs.getDate("effective_from"));
		setting.setEffectiveTo(rs.getDate("effective_to"));
		setting.setCreatedAt(rs.getTimestamp("created_at"));
		setting.setUpdatedAt(rs.getTimestamp("updated_at"));
		return setting;
	}
}
