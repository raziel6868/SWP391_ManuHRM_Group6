package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.User;
import util.PasswordUtil;

public class UserDAO {

	public List<User> searchAndFilter(String keyword, Long departmentId, Long roleId, Boolean isActive, int offset,
			int limit) {

		List<User> users = new ArrayList<>();

		StringBuilder sql = new StringBuilder("""
				SELECT u.id, u.employee_code, u.username, u.full_name,
				       u.phone, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id,
				       d.name  AS department_name,
				       r.name  AS role_name,
				       r.display_name AS role_display_name
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN roles r       ON u.role_id       = r.id
				WHERE 1=1
				""");

		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.isBlank()) {
			sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
			String like = "%" + keyword.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (roleId != null) {
			sql.append(" AND u.role_id = ?");
			params.add(roleId);
		}
		if (isActive != null) {
			sql.append(" AND u.is_active = ?");
			params.add(isActive);
		}

		sql.append(" ORDER BY u.id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				users.add(mapRow(rs));
			}

		} catch (SQLException e) {
			System.err.println("UserDAO.searchAndFilter() ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		return users;
	}

	public int countSearchAndFilter(String keyword, Long departmentId, Long roleId, Boolean isActive) {

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.isBlank()) {
			sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
			String like = "%" + keyword.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (roleId != null) {
			sql.append(" AND u.role_id = ?");
			params.add(roleId);
		}
		if (isActive != null) {
			sql.append(" AND u.is_active = ?");
			params.add(isActive);
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			System.err.println("UserDAO.countSearchAndFilter() ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}

	public User getById(Long id) {
		String sql = """
				SELECT u.id, u.employee_code, u.username, u.full_name,
				       u.phone, u.dob, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id, u.manager_id,
				       u.created_at, u.updated_at,
				       d.name         AS department_name,
				       r.name         AS role_name,
				       r.display_name AS role_display_name,
				       m.full_name    AS manager_name
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN roles r       ON u.role_id       = r.id
				LEFT JOIN users m       ON u.manager_id    = m.id
				WHERE u.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				User user = mapRow(rs);
				user.setDob(rs.getDate("dob"));
				user.setManagerId(rs.getLong("manager_id"));
				user.setManagerName(rs.getString("manager_name"));
				user.setCreatedAt(rs.getTimestamp("created_at"));
				user.setUpdatedAt(rs.getTimestamp("updated_at"));
				return user;
			}

		} catch (SQLException e) {
			System.err.println("UserDAO.getById() ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public User getByUsername(String username) {
		return null;
	}

	public User findActiveUserByLogin(String identifier, String plainPassword) throws SQLException {
		String sql = """
				SELECT u.id, u.employee_code, u.username, u.password_hash, u.full_name,
				       u.phone, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id,
				       d.name AS department_name,
				       r.name AS role_name,
				       r.display_name AS role_display_name
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN roles r ON u.role_id = r.id
				WHERE (u.username = ? OR u.employee_code = ?)
				  AND u.is_active = TRUE
				  AND r.is_active = TRUE
				LIMIT 1
				""";

		try (Connection conn = DBContext.getConnection()) {
			if (conn == null) {
				throw new SQLException("Database connection is not available");
			}

			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, identifier);
				ps.setString(2, identifier);

				ResultSet rs = ps.executeQuery();
				if (rs.next() && isPasswordMatched(plainPassword, rs.getString("password_hash"))) {
					return mapRow(rs);
				}
			}
		}

		return null;
	}

	public boolean insert(User user) {
		return false;
	}

	public boolean updateProfile(User user) {
		return false;
	}

	public boolean updatePassword(Long id, String newPasswordHash) {
		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		String sql = "UPDATE users SET is_active = ? WHERE id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			System.err.println("UserDAO.updateStatus() ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	private User mapRow(ResultSet rs) throws SQLException {
		User user = new User();
		user.setId(rs.getLong("id"));
		user.setEmployeeCode(rs.getString("employee_code"));
		user.setUsername(rs.getString("username"));
		user.setFullName(rs.getString("full_name"));
		user.setPhone(rs.getString("phone"));
		user.setJobTitle(rs.getString("job_title"));
		user.setIsActive(rs.getBoolean("is_active"));
		user.setDepartmentId(rs.getLong("department_id"));
		user.setRoleId(rs.getLong("role_id"));

		String empType = rs.getString("employee_type");
		if (empType != null) {
			user.setEmployeeType(User.EmployeeType.valueOf(empType));
		}

		user.setDepartmentName(rs.getString("department_name"));
		user.setRoleName(rs.getString("role_name"));
		user.setRoleDisplayName(rs.getString("role_display_name"));

		return user;
	}

	private boolean isPasswordMatched(String plainPassword, String passwordHash) {
		if (plainPassword == null || passwordHash == null || passwordHash.isBlank()) {
			return false;
		}

		try {
			return PasswordUtil.checkPassword(plainPassword, passwordHash);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
