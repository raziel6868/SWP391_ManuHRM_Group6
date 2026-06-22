package controller.attendancecorrection;

import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import dal.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import model.AttendanceCorrection;
import model.User;

@WebServlet(name = "AttendanceCorrectionApproveServlet", urlPatterns = {"/attendance-correction-approve"})
public class AttendanceCorrectionApproveServlet extends HttpServlet {

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		String redirectUrl = request.getContextPath() + "/attendance-correction-list";

		if (id == null) {
			session.setAttribute("errorMsg", "Yêu cầu điều chỉnh không hợp lệ.");
			response.sendRedirect(redirectUrl);
			return;
		}

		AttendanceCorrection correction = correctionDAO.getById(id);
		if (correction == null) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu điều chỉnh.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (!"PENDING".equals(correction.getStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý trước đó.");
			response.sendRedirect(redirectUrl);
			return;
		}

		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			if (conn == null) {
				throw new SQLException("Không thể kết nối database.");
			}
			conn.setAutoCommit(false);

			boolean approved = correctionDAO.approve(conn, id, authUser.getId());
			if (!approved) {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể duyệt yêu cầu (có thể đã được xử lý bởi người khác).");
				response.sendRedirect(redirectUrl);
				return;
			}

			boolean updated = attendanceDAO.updateAfterCorrection(conn, correction.getAttendanceRecordId(),
					correction.getNewCheckIn(), correction.getNewCheckOut());
			if (!updated) {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể cập nhật bản ghi chấm công.");
				response.sendRedirect(redirectUrl);
				return;
			}

			conn.commit();
			session.setAttribute("successMsg",
					"Đã duyệt yêu cầu điều chỉnh công cho " + correction.getEmployeeName() + ".");

		} catch (SQLException e) {
			rollback(conn);
			System.err.println("AttendanceCorrectionApproveServlet.doPost() ERROR: " + e.getMessage());
			session.setAttribute("errorMsg", "Lỗi hệ thống khi duyệt yêu cầu điều chỉnh.");
		} finally {
			close(conn);
		}

		response.sendRedirect(redirectUrl);
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void rollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				System.err.println("AttendanceCorrectionApproveServlet.rollback() ERROR: " + e.getMessage());
			}
		}
	}

	private void close(Connection conn) {
		if (conn != null) {
			try {
				conn.setAutoCommit(true);
				conn.close();
			} catch (SQLException e) {
				System.err.println("AttendanceCorrectionApproveServlet.close() ERROR: " + e.getMessage());
			}
		}
	}
}