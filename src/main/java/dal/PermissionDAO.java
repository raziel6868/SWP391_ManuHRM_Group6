package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Permission;

public class PermissionDAO {

	public List<Permission> getAllPermissions() {
		List<Permission> list = new ArrayList<>();
		String sql = "SELECT id, code, name, url_pattern, module FROM permissions ORDER BY module, name";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("PermissionDAO.getAllPermissions() ERROR: " + e.getMessage());
		}
		return list;
	}

	public List<Long> getPermissionIdsByRoleId(Long roleId) {
		List<Long> list = new ArrayList<>();
		if (roleId == null)
			return list;

		String sql = "SELECT permission_id FROM role_permissions WHERE role_id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, roleId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(rs.getLong("permission_id"));
				}
			}
		} catch (SQLException e) {
			System.err.println("PermissionDAO.getPermissionIdsByRoleId() ERROR: " + e.getMessage());
		}
		return list;
	}

	public List<Permission> getPermissionsByRoleId(Long roleId) {
		List<Permission> permissions = new ArrayList<>();
		if (roleId == null)
			return permissions;

		String sql = """
				SELECT p.id, p.code, p.name, p.url_pattern, p.module
				FROM permissions p
				INNER JOIN role_permissions rp ON rp.permission_id = p.id
				WHERE rp.role_id = ?
				ORDER BY p.module, p.name""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, roleId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					permissions.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("PermissionDAO.getPermissionsByRoleId() ERROR: " + e.getMessage());
		}
		return permissions;
	}

	public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
		if (roleId == null)
			return false;

		String deleteSql = "DELETE FROM role_permissions WHERE role_id = ?";
		String insertSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
					ps.setLong(1, roleId);
					ps.executeUpdate();
				}

				if (permissionIds != null && !permissionIds.isEmpty()) {
					try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
						for (Long permId : permissionIds) {
							ps.setLong(1, roleId);
							ps.setLong(2, permId);
							ps.addBatch();
						}
						ps.executeBatch();
					}
				}

				conn.commit();
				return true;
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("PermissionDAO.updateRolePermissions() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("PermissionDAO.updateRolePermissions() ERROR: " + e.getMessage());
		}
		return false;
	}

	private Permission mapRow(ResultSet rs) throws SQLException {
		Permission p = new Permission();
		p.setId(rs.getLong("id"));
		p.setCode(rs.getString("code"));
		p.setName(rs.getString("name"));
		p.setUrlPattern(rs.getString("url_pattern"));
		p.setModule(rs.getString("module"));
		return p;
	}

	/**
	 * Lấy danh sách URL patterns mà một role được phép truy cập. Dùng cho
	 * AuthFilter kiểm tra quyền động theo URL.
	 *
	 * @param roleId
	 *            ID của role
	 * @return Set chứa các URL patterns (dạng "/user-list", "/role-permission",
	 *         ...)
	 */
	public Set<String> getAllowedUrlsByRoleId(Long roleId) {
		Set<String> urls = new HashSet<>();
		if (roleId == null)
			return urls;

		String sql = """
				SELECT p.url_pattern
				FROM permissions p
				INNER JOIN role_permissions rp ON rp.permission_id = p.id
				WHERE rp.role_id = ?""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, roleId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String urlPattern = rs.getString("url_pattern");
					if (urlPattern != null && !urlPattern.trim().isEmpty()) {
						urls.add(urlPattern.trim());
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("PermissionDAO.getAllowedUrlsByRoleId() ERROR: " + e.getMessage());
		}
		return urls;
	}
}
