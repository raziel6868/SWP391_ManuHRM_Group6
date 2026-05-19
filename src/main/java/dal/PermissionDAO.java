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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			conn.setAutoCommit(false); // Begin transaction

			// 1. Delete old permissions
			try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
				psDelete.setLong(1, roleId);
				psDelete.executeUpdate();
			}

			// 2. Insert new permissions using Batch Insert
			if (permissionIds != null && !permissionIds.isEmpty()) {
				try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
					for (Long permId : permissionIds) {
						psInsert.setLong(1, roleId);
						psInsert.setLong(2, permId);
						psInsert.addBatch();
					}
					psInsert.executeBatch();
				}
			}

			conn.commit(); // Commit transaction
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				if (conn != null)
					conn.rollback(); // Rollback on error
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				if (conn != null) {
					conn.setAutoCommit(true);
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
}