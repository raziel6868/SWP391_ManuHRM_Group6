package dal;

import model.Permission;
import java.util.List;

public class PermissionDAO {
    /**
     * Lấy danh sách toàn bộ các quyền hạn (Permissions) hiện có trong hệ thống,
     * thường dùng để hiển thị trên màn hình Phân quyền động (Ma trận Checkbox).
     * @return Danh sách tất cả quyền
     */
    public List<Permission> getAllPermissions() {
        return null;
    }

    /**
     * Lấy danh sách các ID quyền hạn mà một Role đang sở hữu để tick sẵn vào Checkbox.
     * @param roleId ID của Role
     * @return Danh sách các Permission ID
     */
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return null;
    }

    /**
     * Cập nhật toàn bộ quyền cho một Role (Xóa hết quyền cũ và Insert lại các quyền mới).
     * @param roleId ID của Role cần phân quyền
     * @param permissionIds Danh sách các Permission ID mới được cấp
     * @return true nếu phân quyền thành công, false nếu thất bại
     */
    public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
        return false;
    }
}
