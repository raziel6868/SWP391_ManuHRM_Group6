package controller.attendancecorrection;

import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import dal.DBContext;
import dal.LeaveRequestDAO;
import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import model.AttendanceCorrection;
import model.OvertimeRecord;
import model.Permission;
import model.User;

@WebServlet(name = "AttendanceCorrectionApproveServlet", urlPatterns = {"/attendance-correction-approve"})
public class AttendanceCorrectionApproveServlet extends HttpServlet {

	private static final LocalTime STANDARD_SHIFT_END = LocalTime.of(17, 0);

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();
	private final OvertimeDAO overtimeDAO = new OvertimeDAO();

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
		if (!hasPermission(session, "ATTENDANCE_CORRECTION_APPROVE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		String redirectUrl = buildRedirectUrl(request);

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

		int year = correction.getAttendanceDate().toLocalDate().getYear();
		int month = correction.getAttendanceDate().toLocalDate().getMonthValue();
		if (!monthlySheetDAO.isHrCorrectionWindow(year, month)) {
			session.setAttribute("errorMsg", "HR chỉ được xử lý điều chỉnh khi bảng công đang chờ HR duyệt.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!"APPROVED".equals(correction.getSupervisorStatus())) {
			session.setAttribute("errorMsg",
					"Yêu cầu này chưa được quản đốc xác nhận. HR chỉ có thể duyệt sau khi quản đốc đã duyệt.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!"PENDING".equals(correction.getStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý trước đó.");
			response.sendRedirect(redirectUrl);
			return;
		}

		// Validate 2 chiều: giờ chấm công mới không được mâu thuẫn với nghỉ
		// phép/OT đã duyệt cùng ngày (giống hệt check khi import chấm công).
		Long targetUserId = correction.getAttendanceUserId();
		java.sql.Date attendanceDate = correction.getAttendanceDate();
		if (leaveRequestDAO.hasApprovedLeaveOnDate(targetUserId, attendanceDate)) {
			session.setAttribute("errorMsg", "Nhân viên đã có đơn nghỉ phép được duyệt ngày " + attendanceDate
					+ " — không thể duyệt điều chỉnh thành có chấm công. Vui lòng xử lý nghỉ phép trước.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (correction.getNewCheckIn() != null && correction.getNewCheckOut() != null) {
			OvertimeRecord approvedOT = overtimeDAO.findApprovedOTForUserAndDate(targetUserId, attendanceDate);
			if (approvedOT != null && approvedOT.getApprovedHours() != null) {
				long otMinutes = approvedOT.getApprovedHours().multiply(BigDecimal.valueOf(60)).longValue();
				LocalTime expectedCheckout = STANDARD_SHIFT_END.plusMinutes(otMinutes);
				if (correction.getNewCheckOut().toLocalTime().isBefore(expectedCheckout)) {
					session.setAttribute("errorMsg",
							"Nhân viên có OT " + approvedOT.getApprovedHours() + "h được duyệt ngày " + attendanceDate
									+ " nhưng giờ ra mới (" + correction.getNewCheckOut().toLocalTime()
									+ ") không đủ hỗ trợ (cần ra từ " + expectedCheckout + " trở đi).");
					response.sendRedirect(redirectUrl);
					return;
				}
			}
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

	/**
	 * Luôn quay lại đúng /attendance-correction-list?tab=hr, giữ nguyên status/page
	 * hiện tại (nếu có) để không bị mất filter sau khi duyệt.
	 */
	private String buildRedirectUrl(HttpServletRequest request) {
		StringBuilder url = new StringBuilder(request.getContextPath()).append("/attendance-correction-list?tab=hr");
		String status = request.getParameter("status");
		if (status != null && !status.isBlank()) {
			url.append("&status=").append(status.trim().toUpperCase());
		}
		String page = request.getParameter("page");
		if (page != null && !page.isBlank()) {
			url.append("&page=").append(page.trim());
		}
		return url.toString();
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (code.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
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