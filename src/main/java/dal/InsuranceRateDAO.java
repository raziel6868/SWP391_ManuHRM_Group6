package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.InsuranceRate;

public class InsuranceRateDAO {

	private static final String SELECT_COLUMNS = """
			id,
			social_insurance_employee_rate,
			health_insurance_employee_rate,
			unemployment_insurance_employee_rate,
			social_insurance_employer_rate,
			health_insurance_employer_rate,
			unemployment_insurance_employer_rate,
			social_health_insurance_cap,
			unemployment_insurance_cap,
			effective_from,
			effective_to,
			created_at,
			updated_at
			""";

	public InsuranceRate getActiveForPeriod(int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());

		String sql = """
				SELECT %s
				FROM insurance_rates
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
			System.err.println("InsuranceRateDAO.getActiveForPeriod() ERROR: " + e.getMessage());
		}

		return null;
	}

	public List<InsuranceRate> getAll() {
		List<InsuranceRate> insuranceRates = new ArrayList<>();
		String sql = """
				SELECT %s
				FROM insurance_rates
				ORDER BY effective_from DESC, id DESC
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				insuranceRates.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("InsuranceRateDAO.getAll() ERROR: " + e.getMessage());
		}

		return insuranceRates;
	}

	public InsuranceRate getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT %s
				FROM insurance_rates
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
			System.err.println("InsuranceRateDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean hasBlockingPeriod(Long excludeId, Date effectiveFrom, Date effectiveTo) {
		if (effectiveFrom == null) {
			return false;
		}

		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM insurance_rates
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
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("InsuranceRateDAO.hasBlockingPeriod() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean upsert(InsuranceRate insuranceRate) {
		if (insuranceRate == null || insuranceRate.getEffectiveFrom() == null) {
			return false;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				if (hasBlockingPeriod(conn, insuranceRate.getId(), insuranceRate.getEffectiveFrom(),
						insuranceRate.getEffectiveTo())) {
					conn.rollback();
					return false;
				}

				closePreviousOverlappingRate(conn, insuranceRate.getId(), insuranceRate.getEffectiveFrom());
				boolean success = insuranceRate.getId() == null
						? insert(conn, insuranceRate)
						: update(conn, insuranceRate);
				if (success) {
					conn.commit();
					return true;
				}
				conn.rollback();
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("InsuranceRateDAO.upsert() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("InsuranceRateDAO.upsert() connection ERROR: " + e.getMessage());
		}

		return false;
	}

	private boolean hasBlockingPeriod(Connection conn, Long excludeId, Date effectiveFrom, Date effectiveTo)
			throws SQLException {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM insurance_rates
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

	private void closePreviousOverlappingRate(Connection conn, Long excludeId, Date effectiveFrom) throws SQLException {
		String sql = """
				UPDATE insurance_rates
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

	private boolean insert(Connection conn, InsuranceRate insuranceRate) throws SQLException {
		String sql = """
				INSERT INTO insurance_rates
				    (social_insurance_employee_rate, health_insurance_employee_rate,
				     unemployment_insurance_employee_rate, social_insurance_employer_rate,
				     health_insurance_employer_rate, unemployment_insurance_employer_rate,
				     social_health_insurance_cap, unemployment_insurance_cap,
				     effective_from, effective_to)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, insuranceRate);
			return ps.executeUpdate() > 0;
		}
	}

	private boolean update(Connection conn, InsuranceRate insuranceRate) throws SQLException {
		String sql = """
				UPDATE insurance_rates
				SET social_insurance_employee_rate = ?,
				    health_insurance_employee_rate = ?,
				    unemployment_insurance_employee_rate = ?,
				    social_insurance_employer_rate = ?,
				    health_insurance_employer_rate = ?,
				    unemployment_insurance_employer_rate = ?,
				    social_health_insurance_cap = ?,
				    unemployment_insurance_cap = ?,
				    effective_from = ?,
				    effective_to = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, insuranceRate);
			ps.setLong(11, insuranceRate.getId());
			return ps.executeUpdate() > 0;
		}
	}

	private void setMutationParams(PreparedStatement ps, InsuranceRate insuranceRate) throws SQLException {
		ps.setBigDecimal(1, insuranceRate.getSocialInsuranceEmployeeRate());
		ps.setBigDecimal(2, insuranceRate.getHealthInsuranceEmployeeRate());
		ps.setBigDecimal(3, insuranceRate.getUnemploymentInsuranceEmployeeRate());
		ps.setBigDecimal(4, insuranceRate.getSocialInsuranceEmployerRate());
		ps.setBigDecimal(5, insuranceRate.getHealthInsuranceEmployerRate());
		ps.setBigDecimal(6, insuranceRate.getUnemploymentInsuranceEmployerRate());
		if (insuranceRate.getSocialHealthInsuranceCap() != null) {
			ps.setBigDecimal(7, insuranceRate.getSocialHealthInsuranceCap());
		} else {
			ps.setNull(7, java.sql.Types.DECIMAL);
		}
		if (insuranceRate.getUnemploymentInsuranceCap() != null) {
			ps.setBigDecimal(8, insuranceRate.getUnemploymentInsuranceCap());
		} else {
			ps.setNull(8, java.sql.Types.DECIMAL);
		}
		ps.setDate(9, insuranceRate.getEffectiveFrom());
		if (insuranceRate.getEffectiveTo() != null) {
			ps.setDate(10, insuranceRate.getEffectiveTo());
		} else {
			ps.setNull(10, java.sql.Types.DATE);
		}
	}

	private InsuranceRate mapRow(ResultSet rs) throws SQLException {
		InsuranceRate insuranceRate = new InsuranceRate();
		insuranceRate.setId(rs.getLong("id"));
		insuranceRate.setSocialInsuranceEmployeeRate(rs.getBigDecimal("social_insurance_employee_rate"));
		insuranceRate.setHealthInsuranceEmployeeRate(rs.getBigDecimal("health_insurance_employee_rate"));
		insuranceRate.setUnemploymentInsuranceEmployeeRate(rs.getBigDecimal("unemployment_insurance_employee_rate"));
		insuranceRate.setSocialInsuranceEmployerRate(rs.getBigDecimal("social_insurance_employer_rate"));
		insuranceRate.setHealthInsuranceEmployerRate(rs.getBigDecimal("health_insurance_employer_rate"));
		insuranceRate.setUnemploymentInsuranceEmployerRate(rs.getBigDecimal("unemployment_insurance_employer_rate"));
		insuranceRate.setSocialHealthInsuranceCap(rs.getBigDecimal("social_health_insurance_cap"));
		insuranceRate.setUnemploymentInsuranceCap(rs.getBigDecimal("unemployment_insurance_cap"));
		insuranceRate.setEffectiveFrom(rs.getDate("effective_from"));
		insuranceRate.setEffectiveTo(rs.getDate("effective_to"));
		insuranceRate.setCreatedAt(rs.getTimestamp("created_at"));
		insuranceRate.setUpdatedAt(rs.getTimestamp("updated_at"));
		return insuranceRate;
	}
}
