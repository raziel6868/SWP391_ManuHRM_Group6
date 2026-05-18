package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.PasswordTicket;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    public List<User> searchAndFilter(
            String keyword,
            Long departmentId,
            Long roleId,
            Boolean isActive,
            int offset,
            int limit) {

        List<User> users = new ArrayList<>();

        StringBuilder sql
                = new StringBuilder(
                        """
                SELECT u.id, u.employee_code, u.username, u.full_name,
                       u.phone, u.job_title, u.employee_type, u.is_active,
                       u.department_id, u.role_id,
                       d.name  AS department_name,
                       r.name  AS role_name,
                       r.display_name AS role_display_name
                FROM users u
                LEFT JOIN departments d ON u.department_id = d.id
                LEFT JOIN roles r       ON u.role_id       = r.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
            String like = "%" + keyword.trim() + "%";
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

        sql.append(" ORDER BY u.id ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.searchAndFilter() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    public int countSearchAndFilter(
            String keyword, Long departmentId, Long roleId, Boolean isActive) {

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
            String like = "%" + keyword.trim() + "%";
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

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.countSearchAndFilter() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public User getById(Long id) {
        String sql
                = """
                SELECT u.id, u.employee_code, u.username, u.full_name,
                       u.phone, u.dob, u.job_title, u.employee_type, u.is_active,
                       u.department_id, u.role_id, u.manager_id,
                       u.created_at, u.updated_at,
                       d.name         AS department_name,
                       r.name         AS role_name,
                       r.display_name AS role_display_name,
                       m.full_name    AS manager_name
                FROM users u
                LEFT JOIN departments d ON u.department_id = d.id
                LEFT JOIN roles r       ON u.role_id       = r.id
                LEFT JOIN users m       ON u.manager_id    = m.id
                WHERE u.id = ?
                """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapRow(rs);
                u.setDob(rs.getDate("dob"));
                u.setManagerId(rs.getLong("manager_id"));
                u.setManagerName(rs.getString("manager_name"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                u.setUpdatedAt(rs.getTimestamp("updated_at"));
                return u;
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.getById() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public User getByUsername(String username) {
        return null;
    }

    public boolean insert(User user) {
        return false;
    }

    public boolean updateProfile(User user) {
        return false;
    }

    public boolean updatePassword(Long id, String newPasswordHash) {
        return false;
    }

    public boolean updateStatus(Long id, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.updateStatus() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmployeeCode(rs.getString("employee_code"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setJobTitle(rs.getString("job_title"));
        u.setIsActive(rs.getBoolean("is_active"));
        u.setDepartmentId(rs.getLong("department_id"));
        u.setRoleId(rs.getLong("role_id"));

        String empType = rs.getString("employee_type");
        if (empType != null) {
            u.setEmployeeType(User.EmployeeType.valueOf(empType));
        }

        u.setDepartmentName(rs.getString("department_name"));
        u.setRoleName(rs.getString("role_name"));
        u.setRoleDisplayName(rs.getString("role_display_name"));

        return u;
    }

    public boolean updateProfile(Long id, String fullName, String phone, java.util.Date dob) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, dob = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        // Sử dụng khối bảo vệ an toàn kết nối DB
        Connection conn = DBContext.getConnection();
        if (conn == null) {
            System.err.println("UserDAO.updateProfile() CANNOT proceed because DB connection is null!");
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);

            // Kiểm tra tránh lỗi NullPointerException nếu người dùng không chọn ngày sinh
            if (dob != null) {
                ps.setDate(3, new java.sql.Date(dob.getTime()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            ps.setLong(4, id);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0; // Trả về true nếu cập nhật thành công ít nhất 1 dòng
        } catch (SQLException e) {
            System.err.println("Error inside UserDAO.updateProfile:");
            e.printStackTrace();
        }
        return false;
    }
    // Bạn nhớ import thư viện Bcrypt mà dự án đang dùng ở đầu file nhé (ví dụ: org.mindrot.jbcrypt.BCrypt hoặc tương đương)
// import org.mindrot.jbcrypt.BCrypt;

    public static void main(String[] args) {
        System.out.println(new UserDAO().getAllPendingTickets().size());
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        String selectSql = "SELECT password_hash FROM users WHERE id = ?";
        String updateSql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        Connection conn = DBContext.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            // Bước 1: Lấy chuỗi hash mật khẩu hiện tại từ Database
            String storedHash = null;
            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setLong(1, userId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        storedHash = rs.getString("password_hash");
                    }
                }
            }

            // Nếu không tìm thấy user hoặc chuỗi hash trống
            if (storedHash == null) {
                return false;
            }

            // Bước 2: Phá/Kiểm tra hash (Verify) mật khẩu cũ người dùng nhập vào
            // Hàm checkpw() sẽ tự giải mã thuật toán Bcrypt dựa trên Salt để đối chiếu
            if (!BCrypt.checkpw(currentPassword, storedHash)) {
                System.out.println("Mật khẩu hiện tại không trùng khớp với dữ liệu mã hóa!");
                return false; // Mật khẩu cũ nhập vào bị sai
            }

            // Bước 3: Thêm hash (Mã hóa Bcrypt) cho mật khẩu mới trước khi lưu
            // Số 12 ở đây tương ứng với độ phức tạp $2a$12$ giống hệt chuỗi hash bạn gửi lúc trước
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));

            // Bước 4: Cập nhật chuỗi hash mới này vào Database
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, newPasswordHash);
                psUpdate.setLong(2, userId);
                return psUpdate.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String sendPasswordResetTicket(String employeeCode) {
        String checkUserSql = "SELECT id FROM users WHERE employee_code = ?";
        String checkTicketSql = "SELECT id FROM password_tickets WHERE employee_code = ? AND status = 'PENDING'";
        String insertTicketSql = "INSERT INTO password_tickets (employee_code, status) VALUES (?, 'PENDING')";

        Connection conn = DBContext.getConnection();
        if (conn == null) {
            return "Lỗi kết nối cơ sở dữ liệu!";
        }

        try {
            // 1. Kiểm tra xem Employee Code có tồn tại trong hệ thống không
            try (PreparedStatement psCheckUser = conn.prepareStatement(checkUserSql)) {
                psCheckUser.setString(1, employeeCode);
                try (ResultSet rs = psCheckUser.executeQuery()) {
                    if (!rs.next()) {
                        return "Mã nhân viên (Employee Code) không tồn tại trên hệ thống!";
                    }
                }
            }

            // 2. Kiểm tra xem nhân viên này đã có ticket nào đang chờ duyệt sẵn chưa (tránh spam gửi liên tục)
            try (PreparedStatement psCheckTicket = conn.prepareStatement(checkTicketSql)) {
                psCheckTicket.setString(1, employeeCode);
                try (ResultSet rs = psCheckTicket.executeQuery()) {
                    if (rs.next()) {
                        return "Yêu cầu của bạn đang ở trạng thái chờ duyệt. Vui lòng không gửi lại liên tục!";
                    }
                }
            }

            // 3. Tiến hành tạo ticket yêu cầu reset mật khẩu
            try (PreparedStatement psInsert = conn.prepareStatement(insertTicketSql)) {
                psInsert.setString(1, employeeCode);
                int rows = psInsert.executeUpdate();
                if (rows > 0) {
                    return "SUCCESS"; // Thành công
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Đã xảy ra lỗi hệ thống trong quá trình tạo ticket!";
    }
// Thêm hàm này vào Class UserDAO.java của bạn

    public boolean resolvePasswordTicket(int ticketId, String employeeCode) {
        String updateTicketSql = "UPDATE password_tickets SET status = 'RESOLVED', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String updateUserPassSql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE employee_code = ?";

        Connection conn = DBContext.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            // Bật chế độ Transaction để đảm bảo cả 2 lệnh cùng thành công hoặc cùng thất bại
            conn.setAutoCommit(false);

            // 1. Reset mật khẩu user về mặc định "123456" (Chuỗi hash Bcrypt round 12)
            String defaultHash = "$2a$12$0wwAa21is/sQAN8BtCCkQuDqbYwfRLTxYJFPWjTiIW4EpSXCOdjTS";
            try (PreparedStatement psUser = conn.prepareStatement(updateUserPassSql)) {
                psUser.setString(1, defaultHash);
                psUser.setString(2, employeeCode);
                psUser.executeUpdate();
            }

            // 2. Cập nhật trạng thái Ticket thành RESOLVED
            try (PreparedStatement psTicket = conn.prepareStatement(updateTicketSql)) {
                psTicket.setInt(1, ticketId);
                int affectedRows = psTicket.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Lưu thay đổi vào DB
                    return true;
                }
            }

            conn.rollback();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

// Hàm bổ trợ để lấy danh sách ticket hiển thị lên trang Admin
    public java.util.List<PasswordTicket> getAllPendingTickets() {
        java.util.List<model.PasswordTicket> list = new java.util.ArrayList<>();
        String sql = "SELECT t.id, t.employee_code, t.status, t.created_at, u.full_name "
                + "FROM password_tickets t JOIN users u ON t.employee_code = u.employee_code "
                + "WHERE t.status = 'PENDING' ORDER BY t.created_at DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Khởi tạo đối tượng và gán data (Bạn có thể tạo nhanh Class model tương ứng)
                model.PasswordTicket ticket = new model.PasswordTicket();
                ticket.setId(rs.getInt("id"));
                ticket.setEmployeeCode(rs.getString("employee_code"));
                ticket.setFullName(rs.getString("full_name")); // Thêm field này vào model nếu cần hiển thị tên
                ticket.setStatus(rs.getString("status"));
                ticket.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(ticket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
   
}
