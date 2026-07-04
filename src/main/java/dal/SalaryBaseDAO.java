package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.SalaryBase;

public class SalaryBaseDAO {

	public List<SalaryBase> searchSalaryBases(Long departmentId, int offset, int limit) {
		List<SalaryBase> salaryBases = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT sb.id, sb.user_id, sb.base_salary, sb.insurance_salary, sb.effective_from, sb.effective_to,
				       sb.created_at, sb.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name
				FROM salary_bases sb
				JOIN users u ON sb.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE sb.effective_to IS NULL
				""");
		List<Object> params = new ArrayList<>();

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY u.full_name LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					salaryBases.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error searching salary bases: " + e.getMessage());
		}

		return salaryBases;
	}

	public SalaryBase getCurrentByUserId(Long userId) {
		String sql = """
				SELECT sb.id, sb.user_id, sb.base_salary, sb.insurance_salary, sb.effective_from, sb.effective_to,
				       sb.created_at, sb.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name
				FROM salary_bases sb
				JOIN users u ON sb.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE sb.user_id = ?
				  AND sb.effective_from <= CURRENT_DATE
				  AND (sb.effective_to IS NULL OR sb.effective_to >= CURRENT_DATE)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting salary base for user: " + e.getMessage());
		}

		return null;
	}

	public boolean upsert(Long userId, BigDecimal baseSalary, BigDecimal insuranceSalary, java.sql.Date effectiveFrom) {
		String checkSql = "SELECT id FROM salary_bases WHERE user_id = ? AND effective_to IS NULL";
		String updateSql = """
				UPDATE salary_bases
				SET base_salary = ?, insurance_salary = ?, effective_from = ?, updated_at = CURRENT_TIMESTAMP
				WHERE user_id = ? AND effective_to IS NULL
				""";
		String insertSql = """
				INSERT INTO salary_bases (user_id, base_salary, insurance_salary, effective_from, created_at)
				VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
				""";

		try (Connection conn = DBContext.getConnection()) {
			try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
				checkPs.setLong(1, userId);
				try (ResultSet rs = checkPs.executeQuery()) {
					if (rs.next()) {
						try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
							updatePs.setBigDecimal(1, baseSalary);
							if (insuranceSalary != null) {
								updatePs.setBigDecimal(2, insuranceSalary);
							} else {
								updatePs.setNull(2, java.sql.Types.DECIMAL);
							}
							updatePs.setDate(3, effectiveFrom);
							updatePs.setLong(4, userId);
							return updatePs.executeUpdate() > 0;
						}
					} else {
						try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
							insertPs.setLong(1, userId);
							insertPs.setBigDecimal(2, baseSalary);
							if (insuranceSalary != null) {
								insertPs.setBigDecimal(3, insuranceSalary);
							} else {
								insertPs.setNull(3, java.sql.Types.DECIMAL);
							}
							insertPs.setDate(4, effectiveFrom);
							return insertPs.executeUpdate() > 0;
						}
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error upserting salary base: " + e.getMessage());
		}

		return false;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private SalaryBase mapRow(ResultSet rs) throws SQLException {
		SalaryBase sb = new SalaryBase();
		sb.setId(rs.getLong("id"));
		sb.setUserId(rs.getLong("user_id"));
		sb.setBaseSalary(rs.getBigDecimal("base_salary"));
		sb.setInsuranceSalary(rs.getBigDecimal("insurance_salary"));
		sb.setEffectiveFrom(rs.getDate("effective_from"));
		sb.setEffectiveTo(rs.getDate("effective_to"));
		sb.setCreatedAt(rs.getTimestamp("created_at"));
		sb.setUpdatedAt(rs.getTimestamp("updated_at"));
		sb.setUserFullName(rs.getString("user_full_name"));
		sb.setEmployeeCode(rs.getString("employee_code"));
		sb.setDepartmentName(rs.getString("department_name"));
		return sb;
	}
}
