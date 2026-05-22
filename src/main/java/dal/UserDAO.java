package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import model.User;
import util.PasswordUtil;

public class UserDAO {

	// === XÁC THỰC ===

	public User findActiveUserByLogin(String identifier, String plainPassword) {
		String sql = """
				SELECT u.id, u.employee_code, u.username, u.password_hash, u.full_name,
				       u.phone, u.job_title, u.employee_type, u.is_active, u.must_change_password,
				       u.department_id, u.role_id, u.manager_id,
				       d.name AS department_name, r.name AS role_name, r.display_name AS role_display_name,
				       COALESCE(r.rank, 1) AS role_rank
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN roles r ON u.role_id = r.id
				WHERE (u.username = ? OR u.employee_code = ?) AND u.is_active = TRUE AND r.is_active = TRUE
				LIMIT 1""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, identifier);
			ps.setString(2, identifier);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String storedHash = rs.getString("password_hash");
					if (storedHash != null && !storedHash.isBlank()
							&& PasswordUtil.checkPassword(plainPassword, storedHash)) {
						return mapRow(rs);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.findActiveUserByLogin() ERROR: " + e.getMessage());
		}
		return null;
	}

	// === TRUY VẤN ĐƠN LẺ ===

	public User getById(Long id) {
		if (id == null)
			return null;
		String sql = """
				SELECT u.id, u.employee_code, u.username, u.full_name,
				       u.phone, u.dob, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id, u.manager_id, u.created_at, u.updated_at,
				       d.name AS department_name, r.name AS role_name, r.display_name AS role_display_name,
				       COALESCE(r.rank, 1) AS role_rank,
				       m.full_name AS manager_name
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN roles r ON u.role_id = r.id
				LEFT JOIN users m ON u.manager_id = m.id
				WHERE u.id = ?""";

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

	// === TÌM KIẾM & PHÂN TRANG ===

	public List<User> searchUsers(String keyword, Long departmentId, Long roleId, Boolean isActive, String employeeType,
			int offset, int limit) {
		return searchUsers(keyword, departmentId, roleId, isActive, employeeType, offset, limit, null);
	}

	public List<User> searchUsers(String keyword, Long departmentId, Long roleId, Boolean isActive, String employeeType,
			int offset, int limit, Long managerId) {
		List<User> users = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT u.id, u.employee_code, u.username, u.full_name,
				       u.phone, u.job_title, u.employee_type, u.is_active,
				       u.department_id, u.role_id, u.manager_id,
				       d.name AS department_name, r.name AS role_name, r.display_name AS role_display_name,
				       COALESCE(r.rank, 1) AS role_rank
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN roles r ON u.role_id = r.id
				WHERE 1=1""");

		List<Object> params = new ArrayList<>();
		appendSearchConditions(sql, params, keyword, departmentId, roleId, isActive, employeeType);

		// Line Manager: chỉ thấy user dưới quyền
		if (managerId != null) {
			sql.append(" AND u.manager_id = ?");
			params.add(managerId);
		}

		sql.append(" ORDER BY u.id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					users.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.searchUsers() ERROR: " + e.getMessage());
		}
		return users;
	}

	public int countUsers(String keyword, Long departmentId, Long roleId, Boolean isActive, String employeeType) {
		return countUsers(keyword, departmentId, roleId, isActive, employeeType, null);
	}

	public int countUsers(String keyword, Long departmentId, Long roleId, Boolean isActive, String employeeType,
			Long managerId) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1");
		List<Object> params = new ArrayList<>();
		appendSearchConditions(sql, params, keyword, departmentId, roleId, isActive, employeeType);

		// Line Manager: chỉ đếm user dưới quyền
		if (managerId != null) {
			sql.append(" AND u.manager_id = ?");
			params.add(managerId);
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.countUsers() ERROR: " + e.getMessage());
		}
		return 0;
	}

	private void appendSearchConditions(StringBuilder sql, List<Object> params, String keyword, Long departmentId,
			Long roleId, Boolean isActive, String employeeType) {
		if (keyword != null && !keyword.isBlank()) {
			String like = "%" + keyword.trim() + "%";
			sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
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
	}

	// === CRUD ===

	public boolean insert(User user) {
		if (user == null || user.getEmployeeCode() == null)
			return false;
		String sql = """
				INSERT INTO users (employee_code, username, password_hash, full_name, phone, dob,
				                   job_title, department_id, employee_type, role_id, manager_id, is_active)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			int i = 1;
			ps.setString(i++, user.getEmployeeCode());
			ps.setString(i++, user.getUsername());
			ps.setString(i++, user.getPasswordHash());
			ps.setString(i++, user.getFullName());
			ps.setString(i++, user.getPhone());
			ps.setDate(i++, user.getDob() != null ? new Date(user.getDob().getTime()) : null);
			ps.setString(i++, user.getJobTitle());
			ps.setObject(i++, user.getDepartmentId());
			ps.setString(i++, user.getEmployeeType() != null ? user.getEmployeeType().name() : "OFFICE");
			ps.setObject(i++, user.getRoleId());
			ps.setObject(i++, user.getManagerId());
			ps.setBoolean(i++, user.getIsActive() != null && user.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.insert() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateByAdmin(User user, String optionalNewPassword) {
		if (user == null || user.getId() == null)
			return false;
		boolean updatePassword = optionalNewPassword != null && !optionalNewPassword.trim().isEmpty();

		StringBuilder sql = new StringBuilder("""
				UPDATE users SET full_name = ?, phone = ?, dob = ?, job_title = ?,
				                 department_id = ?, employee_type = ?, role_id = ?, manager_id = ?""");
		if (updatePassword)
			sql.append(", password_hash = ?");
		sql.append(", updated_at = CURRENT_TIMESTAMP WHERE id = ?");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			int i = 1;
			ps.setString(i++, user.getFullName());
			ps.setString(i++, user.getPhone());
			ps.setDate(i++, user.getDob() != null ? new Date(user.getDob().getTime()) : null);
			ps.setString(i++, user.getJobTitle());
			ps.setObject(i++, user.getDepartmentId());
			ps.setString(i++, user.getEmployeeType() != null ? user.getEmployeeType().name() : "OFFICE");
			ps.setObject(i++, user.getRoleId());
			ps.setObject(i++, user.getManagerId());
			if (updatePassword) {
				ps.setString(i++, PasswordUtil.hashPassword(optionalNewPassword));
			}
			ps.setLong(i++, user.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.updateByAdmin() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateProfile(Long id, String fullName, String phone, java.util.Date dob) {
		if (id == null)
			return false;
		String sql = "UPDATE users SET full_name = ?, phone = ?, dob = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, fullName);
			ps.setString(2, phone);
			ps.setDate(3, dob != null ? new Date(dob.getTime()) : null);
			ps.setLong(4, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.updateProfile() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null)
			return false;
		String sql = "UPDATE users SET is_active = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.updateStatus() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean resetPassword(Long userId, String newPasswordHash, boolean mustChangePassword) {
		if (userId == null)
			return false;
		String sql = "UPDATE users SET password_hash = ?, must_change_password = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, newPasswordHash);
			ps.setBoolean(2, mustChangePassword);
			ps.setLong(3, userId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("UserDAO.resetPassword() ERROR: " + e.getMessage());
			return false;
		}
	}

	// === KIỂM TRA TỒN TẠI ===

	public boolean existsByEmployeeCode(String employeeCode) {
		if (employeeCode == null)
			return false;
		return count("SELECT COUNT(*) FROM users WHERE employee_code = ?", List.of(employeeCode)) > 0;
	}

	public boolean existsByUsername(String username) {
		if (username == null)
			return false;
		return count("SELECT COUNT(*) FROM users WHERE username = ?", List.of(username)) > 0;
	}

	public int countActiveUsersByRoleId(Long roleId) {
		if (roleId == null)
			return 0;
		return count("SELECT COUNT(*) FROM users WHERE role_id = ? AND is_active = TRUE", List.of(roleId));
	}

	// === PRIVATE HELPERS ===

	private int count(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("UserDAO.count() ERROR: " + e.getMessage());
		}
		return 0;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		if (params == null || params.isEmpty())
			return;
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
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

		try {
			int mustChange = rs.getInt("must_change_password");
			user.setMustChangePassword(rs.wasNull() ? false : mustChange == 1);
		} catch (SQLException ignore) {
		}

		long deptId = rs.getLong("department_id");
		if (!rs.wasNull())
			user.setDepartmentId(deptId);

		long roleId = rs.getLong("role_id");
		if (!rs.wasNull())
			user.setRoleId(roleId);

		try {
			long managerId = rs.getLong("manager_id");
			if (!rs.wasNull())
				user.setManagerId(managerId);
		} catch (SQLException ignore) {
		}

		try {
			String empType = rs.getString("employee_type");
			if (empType != null) {
				user.setEmployeeType(User.EmployeeType.valueOf(empType));
			}
		} catch (SQLException ignore) {
		}

		try {
			user.setDepartmentName(rs.getString("department_name"));
		} catch (SQLException ignore) {
		}
		try {
			user.setRoleName(rs.getString("role_name"));
		} catch (SQLException ignore) {
		}
		try {
			user.setRoleDisplayName(rs.getString("role_display_name"));
		} catch (SQLException ignore) {
		}
		try {
			Object rankObj = rs.getObject("role_rank");
			user.setRoleRank(rankObj != null ? ((Number) rankObj).intValue() : 1);
		} catch (SQLException ignore) {
		}
		return user;
	}
}
