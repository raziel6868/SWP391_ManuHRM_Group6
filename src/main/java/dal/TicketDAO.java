/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.PasswordReset;

/**
 *
 * @author Khanh Manh
 */
public class TicketDAO {

    public String sendPasswordResetTicket(String employeeCode) {
        // 1. Lấy id từ bảng users dựa vào employee_code
        String checkUserSql = "SELECT id FROM users WHERE employee_code = ?";

        // 2. Kiểm tra xem user_id này đã có yêu cầu PENDING nào chưa
        String checkTicketSql = "SELECT id FROM password_resets WHERE user_id = ? AND status = 'PENDING'";

        // 3. Chèn dữ liệu mới vào bảng password_resets với user_id tìm được
        String insertTicketSql = "INSERT INTO password_resets (user_id, status) VALUES (?, 'PENDING')";

        Connection conn = DBContext.getConnection();
        if (conn == null) {
            return "Lỗi kết nối cơ sở dữ liệu!";
        }

        try {
            long userId = -1;

            // BƯỚC 1: Kiểm tra xem Employee Code có tồn tại không và lấy ra user_id
            try (PreparedStatement psCheckUser = conn.prepareStatement(checkUserSql)) {
                psCheckUser.setString(1, employeeCode);
                try (ResultSet rs = psCheckUser.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getLong("id"); // Lấy ra ID của người dùng
                    } else {
                        return "Mã nhân viên (Employee Code) không tồn tại trên hệ thống!";
                    }
                }
            }

            // BƯỚC 2: Kiểm tra trùng lặp (Spam) bằng dữ liệu user_id vừa tìm được
            try (PreparedStatement psCheckTicket = conn.prepareStatement(checkTicketSql)) {
                psCheckTicket.setLong(1, userId);
                try (ResultSet rs = psCheckTicket.executeQuery()) {
                    if (rs.next()) {
                        return "Yêu cầu của bạn đang ở trạng thái chờ duyệt. Vui lòng không gửi lại liên tục!";
                    }
                }
            }

            // BƯỚC 3: Tiến hành tạo yêu cầu reset mật khẩu vào bảng password_resets
            try (PreparedStatement psInsert = conn.prepareStatement(insertTicketSql)) {
                psInsert.setLong(1, userId);
                int rows = psInsert.executeUpdate();
                if (rows > 0) {
                    return "SUCCESS"; // Trả về trạng thái thành công cho Controller xử lý tiếp
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close(); // Đóng kết nối để tránh tràn bộ nhớ kết nối (Connection Leak)
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return "Đã xảy ra lỗi hệ thống trong quá trình tạo ticket!";
    }
    public boolean updateTicketStatus(long ticketId, long adminId, String action) {
        // 1. Cập nhật trạng thái ticket và lưu id người xử lý trực tiếp
        String updateTicketSql = "UPDATE password_resets SET status = ?, resolved_by = ? WHERE id = ?";

        // 2. Cập nhật mật khẩu của user dựa theo user_id liên kết từ ticket đó (Chỉ chạy khi APPROVE)
        String updateUserPassSql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP "
                                 + "WHERE id = (SELECT user_id FROM password_resets WHERE id = ?)";

        Connection conn = DBContext.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            // Bật chế độ Transaction nhằm đảm bảo tính toàn vẹn dữ liệu
            conn.setAutoCommit(false);

            String targetStatus = action.equals("APPROVE") ? "RESOLVED" : "REJECTED";

            // Bước 1: Nếu hành động là APPROVE, thực hiện reset mật khẩu user về mặc định "123456"
            if (action.equals("APPROVE")) {
                String defaultHash = "$2a$12$0wwAa21is/sQAN8BtCCkQuDqbYwfRLTxYJFPWjTiIW4EpSXCOdjTS";
                try (PreparedStatement psUser = conn.prepareStatement(updateUserPassSql)) {
                    psUser.setString(1, defaultHash);
                    psUser.setLong(2, ticketId);
                    psUser.executeUpdate();
                }
            }

            // Bước 2: Cập nhật trạng thái Ticket thành trạng thái đích và ghi nhận Admin xử lý
            try (PreparedStatement psTicket = conn.prepareStatement(updateTicketSql)) {
                psTicket.setString(1, targetStatus);
                psTicket.setLong(2, adminId);
                psTicket.setLong(3, ticketId);
                int affectedRows = psTicket.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Thành công toàn bộ -> Lưu vào DB
                    return true;
                }
            }

            conn.rollback(); 
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close(); // Đóng kết nối an toàn
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<PasswordReset> getAllManageableTickets() {
        List<PasswordReset> list = new ArrayList<>();

        // Lấy toàn bộ ticket ở trạng thái PENDING hoặc REJECTED để có thể duyệt/duyệt lại
        String sql = "SELECT t.id, t.user_id, t.status, t.created_at, u.employee_code, u.full_name "
                   + "FROM password_resets t "
                   + "INNER JOIN users u ON t.user_id = u.id "
                   + "WHERE t.status IN ('PENDING', 'REJECTED') "
                   + "ORDER BY CASE t.status WHEN 'PENDING' THEN 1 WHEN 'REJECTED' THEN 2 END, t.created_at DESC";

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PasswordReset ticket = new PasswordReset();

                ticket.setId(rs.getLong("id")); 
                ticket.setUserId(rs.getLong("user_id"));
                
                String dbStatus = rs.getString("status");
                ticket.setStatus(PasswordReset.Status.valueOf(dbStatus));
                
                ticket.setCreatedAt(rs.getTimestamp("created_at"));
                ticket.setEmployeeCode(rs.getString("employee_code"));
                ticket.setFullName(rs.getString("full_name"));

                list.add(ticket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
