package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.PasswordReset;

public class TicketDAO {

	public String sendPasswordResetTicket(String employeeCode) {
		String checkUserSql = "SELECT id FROM users WHERE employee_code = ?";
		String checkTicketSql = "SELECT id FROM password_resets WHERE user_id = ? AND status = 'PENDING'";
		String insertTicketSql = "INSERT INTO password_resets (user_id, status) VALUES (?, 'PENDING')";

		try (Connection conn = DBContext.getConnection()) {
			if (conn == null) {
				return "Lỗi kết nối cơ sở dữ liệu!";
			}

			long userId = -1;

			// BƯỚC 1: Kiểm tra xem Employee Code có tồn tại không và lấy ra user_id
			try (PreparedStatement psCheckUser = conn.prepareStatement(checkUserSql)) {
				psCheckUser.setString(1, employeeCode);
				try (ResultSet rs = psCheckUser.executeQuery()) {
					if (rs.next()) {
						userId = rs.getLong("id");
					} else {
						return "Mã nhân viên (Employee Code) không tồn tại trên hệ thống!";
					}
				}
			}

			// BƯỚC 2: Kiểm tra trùng lặp (Spam)
			try (PreparedStatement psCheckTicket = conn.prepareStatement(checkTicketSql)) {
				psCheckTicket.setLong(1, userId);
				try (ResultSet rs = psCheckTicket.executeQuery()) {
					if (rs.next()) {
						return "Yêu cầu của bạn đang ở trạng thái chờ duyệt. Vui lòng không gửi lại liên tục!";
					}
				}
			}

			// BƯỚC 3: Tạo yêu cầu
			try (PreparedStatement psInsert = conn.prepareStatement(insertTicketSql)) {
				psInsert.setLong(1, userId);
				if (psInsert.executeUpdate() > 0) {
					return "SUCCESS";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Đã xảy ra lỗi hệ thống trong quá trình tạo ticket!";
	}

	public boolean updateTicketStatus(long ticketId, long adminId, String action) {
		String updateTicketSql = """
				UPDATE password_resets
				SET status = ?, resolved_by = ?
				WHERE id = ?
				""";
		String updateUserPassSql = """
				UPDATE users
				SET password_hash = ?, updated_at = CURRENT_TIMESTAMP
				WHERE id = (SELECT user_id FROM password_resets WHERE id = ?)
				""";

		Connection conn = DBContext.getConnection();
		if (conn == null) {
			return false;
		}

		try {
			conn.setAutoCommit(false);
			String targetStatus = action.equals("APPROVE") ? "RESOLVED" : "REJECTED";

			if (action.equals("APPROVE")) {
				// Default hash for "123456"
				String defaultHash = "$2a$12$0wwAa21is/sQAN8BtCCkQuDqbYwfRLTxYJFPWjTiIW4EpSXCOdjTS";
				try (PreparedStatement psUser = conn.prepareStatement(updateUserPassSql)) {
					psUser.setString(1, defaultHash);
					psUser.setLong(2, ticketId);
					psUser.executeUpdate();
				}
			}

			try (PreparedStatement psTicket = conn.prepareStatement(updateTicketSql)) {
				psTicket.setString(1, targetStatus);
				psTicket.setLong(2, adminId);
				psTicket.setLong(3, ticketId);

				if (psTicket.executeUpdate() > 0) {
					conn.commit();
					return true;
				}
			}

			conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				if (conn != null)
					conn.rollback();
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

	public List<PasswordReset> getAllManageableTickets() {
		List<PasswordReset> list = new ArrayList<>();
		String sql = """
				SELECT t.id, t.user_id, t.status, t.created_at, u.employee_code, u.full_name
				FROM password_resets t
				INNER JOIN users u ON t.user_id = u.id
				WHERE t.status IN ('PENDING', 'REJECTED')
				ORDER BY CASE t.status
				         WHEN 'PENDING' THEN 1
				         WHEN 'REJECTED' THEN 2
				         END,
				         t.created_at DESC
				""";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				PasswordReset ticket = new PasswordReset();
				ticket.setId(rs.getLong("id"));
				ticket.setUserId(rs.getLong("user_id"));
				ticket.setStatus(PasswordReset.Status.valueOf(rs.getString("status")));
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
