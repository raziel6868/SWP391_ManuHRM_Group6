package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Role;

public class RoleDAO {

    /** Lấy danh sách tất cả role đang active để đổ vào Dropdown lọc. */
    public List<Role> getActiveRoles() {
        List<Role> list = new ArrayList<>();
        String sql =
                "SELECT id, name, display_name, description, is_active, is_system FROM roles WHERE is_active = TRUE ORDER BY id ASC";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("RoleDAO.getActiveRoles() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy danh sách các vai trò (Role), hỗ trợ tìm kiếm và phân trang cho màn hình Role List.
     *
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
     *
     * @param id ID của Role cần lấy
     * @return Đối tượng Role nếu tìm thấy, ngược lại trả về null
     */
    public Role getById(Long id) {
        return null;
    }

    /**
     * Cập nhật thông tin cơ bản của Role (tên hiển thị, mô tả).
     *
     * @param role Đối tượng Role chứa dữ liệu mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean update(Role role) {
        return false;
    }

    /**
     * Khóa hoặc mở khóa một Role (Atomic operation).
     *
     * @param id ID của Role cần thay đổi trạng thái
     * @param isActive Trạng thái mới (true = mở khóa, false = khóa)
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateStatus(Long id, boolean isActive) {
        return false;
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
