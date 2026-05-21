package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.PasswordReset;
import util.PasswordUtil;

public class TicketDAO {

	public int countPendingTickets() {
		String sql = "SELECT COUNT(*) FROM password_resets WHERE status = 'PENDING'";
		try (Connection conn = DBContext.getConnection()) {
			if (conn == null)
				return 0;
			try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("TicketDAO.countPendingTickets() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public String createResetTicket(String employeeCode) {
		if (employeeCode == null || employeeCode.isBlank()) {
			return "Mã nhân viên/tài khoản không hợp lệ!";
		}

		String checkUserSql = "SELECT id FROM users WHERE employee_code = ? OR username = ?";
		String checkTicketSql = "SELECT id FROM password_resets WHERE user_id = ? AND status = 'PENDING'";
		String insertTicketSql = "INSERT INTO password_resets (user_id, status) VALUES (?, 'PENDING')";

		try (Connection conn = DBContext.getConnection()) {
			if (conn == null)
				return "Lỗi kết nối cơ sở dữ liệu!";

			long userId = -1;
			try (PreparedStatement ps = conn.prepareStatement(checkUserSql)) {
				ps.setString(1, employeeCode);
				ps.setString(2, employeeCode);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						userId = rs.getLong("id");
					} else {
						return "Mã nhân viên hoặc tài khoản không tồn tại trên hệ thống!";
					}
				}
			}

			try (PreparedStatement ps = conn.prepareStatement(checkTicketSql)) {
				ps.setLong(1, userId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						return "Yêu cầu của bạn đang ở trạng thái chờ duyệt. Vui lòng không gửi lại liên tục!";
					}
				}
			}

			try (PreparedStatement ps = conn.prepareStatement(insertTicketSql)) {
				ps.setLong(1, userId);
				if (ps.executeUpdate() > 0) {
					return "SUCCESS";
				}
			}
		} catch (SQLException e) {
			System.err.println("TicketDAO.createResetTicket() ERROR: " + e.getMessage());
		}
		return "Đã xảy ra lỗi hệ thống trong quá trình tạo ticket!";
	}

	public PasswordResetResult processTicket(long ticketId, long adminId, String newPassword) {
		if (ticketId <= 0 || adminId <= 0 || newPassword == null || newPassword.isBlank()) {
			return new PasswordResetResult(false, "Thông tin không hợp lệ!");
		}

		String getUserIdSql = "SELECT user_id FROM password_resets WHERE id = ? AND status = 'PENDING'";
		String updateTicketSql = "UPDATE password_resets SET status = 'RESOLVED', resolved_by = ?, new_password = ? WHERE id = ?";
		String updateUserPassSql = "UPDATE users SET password_hash = ?, must_change_password = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

		try (Connection conn = DBContext.getConnection()) {
			if (conn == null)
				return new PasswordResetResult(false, "Lỗi kết nối cơ sở dữ liệu!");

			long userId = -1;
			try (PreparedStatement ps = conn.prepareStatement(getUserIdSql)) {
				ps.setLong(1, ticketId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						userId = rs.getLong("user_id");
					} else {
						return new PasswordResetResult(false, "Ticket không tồn tại hoặc đã được xử lý!");
					}
				}
			}

			String passwordHash = PasswordUtil.hashPassword(newPassword);

			conn.setAutoCommit(false);
			try {
				try (PreparedStatement ps = conn.prepareStatement(updateUserPassSql)) {
					ps.setString(1, passwordHash);
					ps.setLong(2, userId);
					ps.executeUpdate();
				}

				try (PreparedStatement ps = conn.prepareStatement(updateTicketSql)) {
					ps.setLong(1, adminId);
					ps.setString(2, newPassword);
					ps.setLong(3, ticketId);
					if (ps.executeUpdate() > 0) {
						conn.commit();
						return new PasswordResetResult(true, newPassword);
					}
				}
				conn.rollback();
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("TicketDAO.processTicket() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("TicketDAO.processTicket() ERROR: " + e.getMessage());
		}
		return new PasswordResetResult(false, "Đã xảy ra lỗi hệ thống!");
	}

	public boolean rejectTicket(long ticketId, long adminId) {
		if (ticketId <= 0 || adminId <= 0)
			return false;

		String updateSql = "UPDATE password_resets SET status = 'REJECTED', resolved_by = ? WHERE id = ? AND status = 'PENDING'";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(updateSql)) {
			ps.setLong(1, adminId);
			ps.setLong(2, ticketId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("TicketDAO.rejectTicket() ERROR: " + e.getMessage());
			return false;
		}
	}

	public List<PasswordReset> getPendingTickets() {
		List<PasswordReset> list = new ArrayList<>();
		String sql = """
				SELECT t.id, t.user_id, t.status, t.created_at, u.employee_code, u.full_name
				FROM password_resets t
				INNER JOIN users u ON t.user_id = u.id
				WHERE t.status IN ('PENDING', 'REJECTED')
				ORDER BY CASE t.status WHEN 'PENDING' THEN 1 WHEN 'REJECTED' THEN 2 END, t.created_at DESC""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
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
			}
		} catch (SQLException e) {
			System.err.println("TicketDAO.getPendingTickets() ERROR: " + e.getMessage());
		}
		return list;
	}

}
