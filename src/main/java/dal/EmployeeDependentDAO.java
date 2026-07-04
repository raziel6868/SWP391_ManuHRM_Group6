package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.EmployeeDependent;

public class EmployeeDependentDAO {

	private static final String SELECT_BASE = """
			SELECT ed.id, ed.user_id, ed.full_name, ed.relationship, ed.tax_code,
			       ed.date_of_birth, ed.effective_from, ed.effective_to, ed.is_active,
			       ed.created_at, ed.updated_at,
			       u.full_name AS user_full_name, u.employee_code, d.name AS department_name
			FROM employee_dependents ed
			JOIN users u ON ed.user_id = u.id
			LEFT JOIN departments d ON u.department_id = d.id
			""";

	public List<EmployeeDependent> search(Long departmentId, String keyword, int offset, int limit) {
		List<EmployeeDependent> dependents = new ArrayList<>();
		StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1 = 1");
		List<Object> params = new ArrayList<>();
		appendFilters(sql, params, departmentId, keyword);
		sql.append(" ORDER BY u.full_name ASC, ed.full_name ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					dependents.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.search() ERROR: " + e.getMessage());
		}
		return dependents;
	}

	public int count(Long departmentId, String keyword) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM employee_dependents ed
				JOIN users u ON ed.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();
		appendFilters(sql, params, departmentId, keyword);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.count() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public EmployeeDependent getById(Long id) {
		if (id == null) {
			return null;
		}
		String sql = SELECT_BASE + " WHERE ed.id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean create(EmployeeDependent dependent) {
		if (!isMutationValid(dependent)) {
			return false;
		}
		String sql = """
				INSERT INTO employee_dependents
				    (user_id, full_name, relationship, tax_code, date_of_birth,
				     effective_from, effective_to, is_active)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, dependent);
			ps.setBoolean(8, dependent.getIsActive() == null || dependent.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.create() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean update(EmployeeDependent dependent) {
		if (!isMutationValid(dependent) || dependent.getId() == null) {
			return false;
		}
		String sql = """
				UPDATE employee_dependents
				SET user_id = ?,
				    full_name = ?,
				    relationship = ?,
				    tax_code = ?,
				    date_of_birth = ?,
				    effective_from = ?,
				    effective_to = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, dependent);
			ps.setLong(8, dependent.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.update() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}
		String sql = "UPDATE employee_dependents SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.updateStatus() ERROR: " + e.getMessage());
		}
		return false;
	}

	public int countActiveDependents(Long userId, int year, int month) {
		if (userId == null) {
			return 0;
		}
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());
		String sql = """
				SELECT COUNT(*)
				FROM employee_dependents
				WHERE user_id = ?
				  AND is_active = TRUE
				  AND effective_from <= ?
				  AND (effective_to IS NULL OR effective_to >= ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, lastDay);
			ps.setDate(3, firstDay);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeDependentDAO.countActiveDependents() ERROR: " + e.getMessage());
		}
		return 0;
	}

	private boolean isMutationValid(EmployeeDependent dependent) {
		return dependent != null && dependent.getUserId() != null && dependent.getFullName() != null
				&& !dependent.getFullName().isBlank() && dependent.getEffectiveFrom() != null;
	}

	private void appendFilters(StringBuilder sql, List<Object> params, Long departmentId, String keyword) {
		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (keyword != null && !keyword.isBlank()) {
			String likeKeyword = "%" + keyword.trim() + "%";
			sql.append(
					" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR ed.full_name LIKE ? OR COALESCE(ed.tax_code, '') LIKE ?)");
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}
	}

	private void setMutationParams(PreparedStatement ps, EmployeeDependent dependent) throws SQLException {
		ps.setLong(1, dependent.getUserId());
		ps.setString(2, dependent.getFullName());
		ps.setString(3, dependent.getRelationship());
		ps.setString(4, dependent.getTaxCode());
		if (dependent.getDateOfBirth() != null) {
			ps.setDate(5, dependent.getDateOfBirth());
		} else {
			ps.setNull(5, java.sql.Types.DATE);
		}
		ps.setDate(6, dependent.getEffectiveFrom());
		if (dependent.getEffectiveTo() != null) {
			ps.setDate(7, dependent.getEffectiveTo());
		} else {
			ps.setNull(7, java.sql.Types.DATE);
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private EmployeeDependent mapRow(ResultSet rs) throws SQLException {
		EmployeeDependent dependent = new EmployeeDependent();
		dependent.setId(rs.getLong("id"));
		dependent.setUserId(rs.getLong("user_id"));
		dependent.setFullName(rs.getString("full_name"));
		dependent.setRelationship(rs.getString("relationship"));
		dependent.setTaxCode(rs.getString("tax_code"));
		dependent.setDateOfBirth(rs.getDate("date_of_birth"));
		dependent.setEffectiveFrom(rs.getDate("effective_from"));
		dependent.setEffectiveTo(rs.getDate("effective_to"));
		dependent.setIsActive(rs.getBoolean("is_active"));
		dependent.setCreatedAt(rs.getTimestamp("created_at"));
		dependent.setUpdatedAt(rs.getTimestamp("updated_at"));
		dependent.setUserFullName(rs.getString("user_full_name"));
		dependent.setEmployeeCode(rs.getString("employee_code"));
		dependent.setDepartmentName(rs.getString("department_name"));
		return dependent;
	}
}
