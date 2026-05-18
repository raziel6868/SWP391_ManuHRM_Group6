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

	public User findActiveUserByLogin(String identifier, String plainPassword) throws SQLException {
		String sql = """
				SELECT u.id, u.employee_code, u.username, u.password_hash, u.full_name,
				       u.phone, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id, u.manager_id,
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

				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next() && isPasswordMatched(plainPassword, rs.getString("password_hash"))) {
						return mapRow(rs);
					}
				}
			}
		}

		return null;
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
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					User user = mapRow(rs);
					user.setDob(rs.getDate("dob"));
					user.setManagerName(rs.getString("manager_name"));
					user.setCreatedAt(rs.getTimestamp("created_at"));
					user.setUpdatedAt(rs.getTimestamp("updated_at"));
					return user;
				}
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public List<User> searchAndFilter(String keyword, Long departmentId, Long roleId, Boolean isActive,
			String employeeType, int offset, int limit) {
		List<User> users = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT u.id, u.employee_code, u.username, u.full_name,
				       u.phone, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id, u.manager_id,
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
		if (employeeType != null && !employeeType.isBlank()) {
			sql.append(" AND u.employee_type = ?");
			params.add(employeeType.trim().toUpperCase());
		}

		sql.append(" ORDER BY u.id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					users.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.searchAndFilter() ERROR: " + e.getMessage());
		}
		return users;
	}

	public int countSearchAndFilter(String keyword, Long departmentId, Long roleId, Boolean isActive,
			String employeeType) {
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
		if (employeeType != null && !employeeType.isBlank()) {
			sql.append(" AND u.employee_type = ?");
			params.add(employeeType.trim().toUpperCase());
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.countSearchAndFilter() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public boolean insertUser(User user) {
		String sql = """
				INSERT INTO users (
				    employee_code, username, password_hash, full_name,
				    phone, dob, job_title, department_id,
				    employee_type, role_id, manager_id, is_active
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, user.getEmployeeCode());
			ps.setString(2, user.getUsername());
			ps.setString(3, user.getPasswordHash());
			ps.setString(4, user.getFullName());
			ps.setString(5, user.getPhone());

			if (user.getDob() != null) {
				ps.setDate(6, new java.sql.Date(user.getDob().getTime()));
			} else {
				ps.setNull(6, java.sql.Types.DATE);
			}

			ps.setString(7, user.getJobTitle());

			if (user.getDepartmentId() != null && user.getDepartmentId() > 0) {
				ps.setLong(8, user.getDepartmentId());
			} else {
				ps.setNull(8, java.sql.Types.BIGINT);
			}

			ps.setString(9, user.getEmployeeType() != null ? user.getEmployeeType().name() : "OFFICE");

			if (user.getRoleId() != null && user.getRoleId() > 0) {
				ps.setLong(10, user.getRoleId());
			} else {
				ps.setNull(10, java.sql.Types.BIGINT);
			}

			if (user.getManagerId() != null && user.getManagerId() > 0) {
				ps.setLong(11, user.getManagerId());
			} else {
				ps.setNull(11, java.sql.Types.BIGINT);
			}

			ps.setBoolean(12, user.getIsActive() != null ? user.getIsActive() : true);

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.insertUser() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean updateUserByAdmin(User user, String optionalNewPassword) {
		boolean updatePassword = optionalNewPassword != null && !optionalNewPassword.trim().isEmpty();

		StringBuilder sql = new StringBuilder("""
				UPDATE users SET full_name = ?, phone = ?, dob = ?, job_title = ?,
				department_id = ?, employee_type = ?, role_id = ?, manager_id = ?
				""");

		if (updatePassword) {
			sql.append(", password_hash = ?");
		}
		sql.append(", updated_at = CURRENT_TIMESTAMP WHERE id = ?");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			int index = 1;
			ps.setString(index++, user.getFullName());
			ps.setString(index++, user.getPhone());

			if (user.getDob() != null) {
				ps.setDate(index++, new java.sql.Date(user.getDob().getTime()));
			} else {
				ps.setNull(index++, java.sql.Types.DATE);
			}

			ps.setString(index++, user.getJobTitle());

			if (user.getDepartmentId() != null && user.getDepartmentId() > 0) {
				ps.setLong(index++, user.getDepartmentId());
			} else {
				ps.setNull(index++, java.sql.Types.BIGINT);
			}

			ps.setString(index++, user.getEmployeeType() != null ? user.getEmployeeType().name() : "OFFICE");

			if (user.getRoleId() != null && user.getRoleId() > 0) {
				ps.setLong(index++, user.getRoleId());
			} else {
				ps.setNull(index++, java.sql.Types.BIGINT);
			}

			if (user.getManagerId() != null && user.getManagerId() > 0) {
				ps.setLong(index++, user.getManagerId());
			} else {
				ps.setNull(index++, java.sql.Types.BIGINT);
			}

			if (updatePassword) {
				ps.setString(index++, PasswordUtil.hashPassword(optionalNewPassword));
			}

			ps.setLong(index, user.getId());

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.updateUserByAdmin() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean updateProfile(Long id, String fullName, String phone, java.util.Date dob) {
		String sql = "UPDATE users SET full_name = ?, phone = ?, dob = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, fullName);
			ps.setString(2, phone);

			if (dob != null) {
				ps.setDate(3, new java.sql.Date(dob.getTime()));
			} else {
				ps.setNull(3, java.sql.Types.DATE);
			}

			ps.setLong(4, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.updateProfile() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean changePassword(Long userId, String currentPassword, String newPassword) {
		String selectSql = "SELECT password_hash FROM users WHERE id = ?";
		String updateSql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

		try (Connection conn = DBContext.getConnection()) {
			String storedHash = null;
			try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
				psSelect.setLong(1, userId);
				try (ResultSet rs = psSelect.executeQuery()) {
					if (rs.next()) {
						storedHash = rs.getString("password_hash");
					}
				}
			}

			if (!isPasswordMatched(currentPassword, storedHash)) {
				return false;
			}

			try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
				psUpdate.setString(1, PasswordUtil.hashPassword(newPassword));
				psUpdate.setLong(2, userId);
				return psUpdate.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.changePassword() ERROR: " + e.getMessage());
		}
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

		long deptId = rs.getLong("department_id");
		if (!rs.wasNull()) {
			user.setDepartmentId(deptId);
		}

		long roleId = rs.getLong("role_id");
		if (!rs.wasNull()) {
			user.setRoleId(roleId);
		}

		try {
			long managerId = rs.getLong("manager_id");
			if (!rs.wasNull()) {
				user.setManagerId(managerId);
			}
		} catch (SQLException ignored) {
		}

		try {
			String empType = rs.getString("employee_type");
			if (empType != null) {
				user.setEmployeeType(User.EmployeeType.valueOf(empType));
			}
		} catch (SQLException ignored) {
		}

		try {
			user.setDepartmentName(rs.getString("department_name"));
		} catch (SQLException ignored) {
		}
		try {
			user.setRoleName(rs.getString("role_name"));
		} catch (SQLException ignored) {
		}
		try {
			user.setRoleDisplayName(rs.getString("role_display_name"));
		} catch (SQLException ignored) {
		}

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
