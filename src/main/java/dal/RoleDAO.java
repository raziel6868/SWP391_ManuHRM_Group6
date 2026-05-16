package dal;

import model.Role;
import java.util.List;

public class RoleDAO {
    /**
     * Lấy danh sách các vai trò (Role), hỗ trợ tìm kiếm và phân trang cho màn hình Role List.
     * @param keyword Từ khóa tìm kiếm theo tên hoặc mô tả
     * @param offset Vị trí bắt đầu lấy dữ liệu (dùng cho phân trang)
     * @param limit Số lượng bản ghi tối đa trên một trang
     * @return Danh sách các vai trò phù hợp
     */
    public List<Role> searchAndFilter(String keyword, int offset, int limit) {
        return null;
    }

    /**
     * Lấy thông tin chi tiết của một Role theo ID để hiển thị lên Form chỉnh sửa.
     * @param id ID của Role cần lấy
     * @return Đối tượng Role nếu tìm thấy, ngược lại trả về null
     */
    public Role getById(Long id) {
        return null;
    }

    /**
     * Cập nhật thông tin cơ bản của Role (tên hiển thị, mô tả).
     * @param role Đối tượng Role chứa dữ liệu mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean update(Role role) {
        return false;
    }

    /**
     * Khóa hoặc mở khóa một Role (Atomic operation).
     * @param id ID của Role cần thay đổi trạng thái
     * @param isActive Trạng thái mới (true = mở khóa, false = khóa)
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateStatus(Long id, boolean isActive) {
        return false;
    }
}
