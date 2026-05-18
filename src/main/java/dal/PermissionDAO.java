package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Permission;

public class PermissionDAO {

	public List<Permission> getAllPermissions() {
		List<Permission> list = new ArrayList<>();
		String sql = """
				SELECT id, code, name, url_pattern, module
				FROM permissions
				ORDER BY module, name
				""";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("PermissionDAO.getAllPermissions() ERROR: " + e.getMessage());
		}
		return list;
	}

	public List<Long> getPermissionIdsByRoleId(Long roleId) {
		List<Long> list = new ArrayList<>();
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
		String sql = """
				SELECT p.id, p.code, p.name, p.url_pattern, p.module
				FROM permissions p
				INNER JOIN role_permissions rp ON rp.permission_id = p.id
				WHERE rp.role_id = ?
				ORDER BY p.module, p.name
				""";

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
		String deleteSql = "DELETE FROM role_permissions WHERE role_id = ?";
		String insertSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

		Connection conn = DBContext.getConnection();
		if (conn == null) {
			return false;
		}

		try {
			conn.setAutoCommit(false);

			try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
				psDelete.setLong(1, roleId);
				psDelete.executeUpdate();
			}

			if (permissionIds != null && !permissionIds.isEmpty()) {
				try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
					for (Long permissionId : permissionIds) {
						psInsert.setLong(1, roleId);
						psInsert.setLong(2, permissionId);
						psInsert.addBatch();
					}
					psInsert.executeBatch();
				}
			}

			conn.commit();
			return true;
		} catch (SQLException e) {
			System.err.println("PermissionDAO.updateRolePermissions() ERROR: " + e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException rollbackException) {
				System.err.println(
						"PermissionDAO.updateRolePermissions() ROLLBACK ERROR: " + rollbackException.getMessage());
			}
		} finally {
			try {
				conn.setAutoCommit(true);
				conn.close();
			} catch (SQLException e) {
				System.err.println("PermissionDAO.updateRolePermissions() CLOSE ERROR: " + e.getMessage());
			}
		}
		return false;
	}

	private Permission mapRow(ResultSet rs) throws SQLException {
		Permission permission = new Permission();
		permission.setId(rs.getLong("id"));
		permission.setCode(rs.getString("code"));
		permission.setName(rs.getString("name"));
		permission.setUrlPattern(rs.getString("url_pattern"));
		permission.setModule(rs.getString("module"));
		return permission;
	}
}
