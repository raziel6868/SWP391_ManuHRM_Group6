package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Permission;

public class PermissionDAO {
	/**
	 * Lấy danh sách toàn bộ các quyền hạn (Permissions) hiện có trong hệ thống,
	 * thường dùng để hiển thị trên màn hình Phân quyền động (Ma trận Checkbox).
	 *
	 * @return Danh sách tất cả quyền
	 */
	public List<Permission> getAllPermissions() {
		return null;
	}

	/**
	 * Lấy danh sách các ID quyền hạn mà một Role đang sở hữu để tick sẵn vào
	 * Checkbox.
	 *
	 * @param roleId
	 *            ID của Role
	 * @return Danh sách các Permission ID
	 */
	public List<Long> getPermissionIdsByRoleId(Long roleId) {
		return null;
	}

	/**
	 * Cập nhật toàn bộ quyền cho một Role (Xóa hết quyền cũ và Insert lại các quyền
	 * mới).
	 *
	 * @param roleId
	 *            ID của Role cần phân quyền
	 * @param permissionIds
	 *            Danh sách các Permission ID mới được cấp
	 * @return true nếu phân quyền thành công, false nếu thất bại
	 */
	public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
		return false;
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

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Permission permission = new Permission();
				permission.setId(rs.getLong("id"));
				permission.setCode(rs.getString("code"));
				permission.setName(rs.getString("name"));
				permission.setUrlPattern(rs.getString("url_pattern"));
				permission.setModule(rs.getString("module"));
				permissions.add(permission);
			}

		} catch (SQLException e) {
			System.err.println("PermissionDAO.getPermissionsByRoleId() ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		return permissions;
	}
}
