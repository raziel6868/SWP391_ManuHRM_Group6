package dal;

import model.User;
import java.util.List;

public class UserDAO {
    /**
     * Tìm kiếm và lọc danh sách nhân viên cho màn hình User List.
     * Chứa tất cả các tiêu chí lọc trong một hàm duy nhất để dễ maintain.
     * @param keyword Từ khóa tìm kiếm (Mã NV, Tên, Username)
     * @param departmentId Lọc theo phòng ban (truyền null nếu lấy tất cả)
     * @param roleId Lọc theo vai trò (truyền null nếu lấy tất cả)
     * @param isActive Lọc theo trạng thái (truyền null nếu lấy cả nhân viên đã nghỉ)
     * @param offset Vị trí bắt đầu (phân trang)
     * @param limit Số lượng bản ghi trên một trang
     * @return Danh sách User thỏa mãn điều kiện
     */
    public List<User> searchAndFilter(String keyword, Long departmentId, Long roleId, Boolean isActive, int offset, int limit) {
        return null;
    }

    /**
     * Lấy thông tin chi tiết của một nhân viên theo ID.
     * @param id ID của nhân viên
     * @return Đối tượng User hoặc null
     */
    public User getById(Long id) {
        return null;
    }

    /**
     * Lấy thông tin User theo Username (hoặc Mã NV) dùng cho chức năng Đăng nhập.
     * @param username Tên đăng nhập hoặc Mã NV
     * @return Đối tượng User hoặc null
     */
    public User getByUsername(String username) {
        return null;
    }

    /**
     * Thêm mới một nhân sự vào hệ thống (Onboarding).
     * @param user Đối tượng User chứa đầy đủ thông tin khởi tạo
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insert(User user) {
        return false;
    }

    /**
     * Cập nhật thông tin hồ sơ nhân sự (Profile, Department, Role...).
     * TUYỆT ĐỐI KHÔNG cập nhật Password hay Status trong hàm này (nguyên tắc Atomic).
     * @param user Đối tượng User chứa dữ liệu cập nhật
     * @return true nếu thành công
     */
    public boolean updateProfile(User user) {
        return false;
    }

    /**
     * Cập nhật mật khẩu của nhân viên (Atomic operation). Dùng cho tính năng Đổi mật khẩu hoặc Quên mật khẩu.
     * @param id ID của User
     * @param newPasswordHash Mật khẩu mới đã được băm (BCrypt)
     * @return true nếu đổi pass thành công
     */
    public boolean updatePassword(Long id, String newPasswordHash) {
        return false;
    }

    /**
     * Khóa hoặc Mở khóa tài khoản nhân viên (Atomic operation).
     * @param id ID của User
     * @param isActive Trạng thái (false = khóa, true = mở)
     * @return true nếu cập nhật thành công
     */
    public boolean updateStatus(Long id, boolean isActive) {
        return false;
    }
}
