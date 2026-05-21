package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Role;

public class RoleDAO {

	// === QUERY: Lấy danh sách Role ===

	public List<Role> getAllRoles() {
		return getRoles("SELECT id, name, display_name, description, is_active, is_system FROM roles ORDER BY id ASC",
				null);
	}

	public List<Role> getActiveRoles() {
		return getRoles("""
				SELECT id, name, display_name, description, is_active, is_system
				FROM roles WHERE is_active = TRUE ORDER BY id ASC""", null);
	}

	public List<Role> searchRoles(String keyword, int offset, int limit) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return getRoles(
					"SELECT id, name, display_name, description, is_active, is_system FROM roles ORDER BY id ASC LIMIT ? OFFSET ?",
					List.of(limit, offset));
		}
		String like = "%" + keyword.trim() + "%";
		String sql = """
				SELECT id, name, display_name, description, is_active, is_system
				FROM roles
				WHERE name LIKE ? OR display_name LIKE ? OR description LIKE ?
				ORDER BY id ASC LIMIT ? OFFSET ?""";
		return getRoles(sql, List.of(like, like, like, limit, offset));
	}

	public int countRoles(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return count("SELECT COUNT(*) FROM roles", null);
		}
		String like = "%" + keyword.trim() + "%";
		return count("""
				SELECT COUNT(*) FROM roles
				WHERE name LIKE ? OR display_name LIKE ? OR description LIKE ?""", List.of(like, like, like));
	}

	// === QUERY: Lấy một Role ===

	public Role getById(Long id) {
		if (id == null)
			return null;
		String sql = "SELECT id, name, display_name, description, is_active, is_system FROM roles WHERE id = ?";
		return getRole(sql, List.of(id));
	}

	// === QUERY: Kiểm tra tồn tại ===

	public boolean existsByName(String name) {
		if (name == null || name.isBlank())
			return false;
		return count("SELECT COUNT(*) FROM roles WHERE name = ?", List.of(name)) > 0;
	}

	// === CRUD: Thêm, Sửa, Xóa ===

	public boolean insert(Role role) {
		if (role == null || role.getName() == null)
			return false;
		String sql = """
				INSERT INTO roles (name, display_name, description, is_active, is_system)
				VALUES (?, ?, ?, ?, ?)""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, role.getName());
			ps.setString(2, role.getDisplayName());
			ps.setString(3, role.getDescription());
			ps.setBoolean(4, role.getIsActive() != null && role.getIsActive());
			ps.setBoolean(5, role.getIsSystem() != null && role.getIsSystem());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("RoleDAO.insert() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean update(Role role) {
		if (role == null || role.getId() == null)
			return false;
		String sql = """
				UPDATE roles SET name = ?, display_name = ?, description = ?
				WHERE id = ? AND is_system = FALSE""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, role.getName());
			ps.setString(2, role.getDisplayName());
			ps.setString(3, role.getDescription());
			ps.setLong(4, role.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("RoleDAO.update() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null)
			return false;
		String sql = "UPDATE roles SET is_active = ? WHERE id = ? AND is_system = FALSE";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("RoleDAO.updateStatus() ERROR: " + e.getMessage());
			return false;
		}
	}

	// === Private helper methods ===

	private List<Role> getRoles(String sql, List<Object> params) {
		List<Role> list = new ArrayList<>();
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("RoleDAO.getRoles() ERROR: " + e.getMessage());
		}
		return list;
	}

	private Role getRole(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("RoleDAO.getRole() ERROR: " + e.getMessage());
		}
		return null;
	}

	private int count(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("RoleDAO.count() ERROR: " + e.getMessage());
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

	private Role mapRow(ResultSet rs) throws SQLException {
		Role r = new Role();
		r.setId(rs.getLong("id"));
		r.setName(rs.getString("name"));
		r.setDisplayName(rs.getString("display_name"));
		r.setDescription(rs.getString("description"));
		r.setIsActive(rs.getBoolean("is_active"));
		r.setIsSystem(rs.getBoolean("is_system"));
		return r;
	}
}
