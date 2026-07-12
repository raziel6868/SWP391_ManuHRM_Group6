package controller.leaverequest;

import dal.DBContext;
import dal.AttendanceDAO;
import dal.LeaveBalanceDAO;
import dal.LeaveRequestDAO;
import dal.OvertimeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import model.LeaveRequest;
import model.User;

@WebServlet(name = "LeaveRequestFinalApproveServlet", urlPatterns = {"/leave-request-final-approve"})
public class LeaveRequestFinalApproveServlet extends HttpServlet {

	private static final String ROLE_EMPLOYEE = "EMPLOYEE";

	private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();
	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession(false);
		User authUser = session == null ? null : (User) session.getAttribute("authUser");
		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		LeaveRequest leaveRequest = leaveRequestDAO.getById(id);
		String validationError = validate(leaveRequest, authUser);
		if (validationError != null) {
			session.setAttribute("errorMsg", validationError);
			response.sendRedirect(request.getContextPath() + "/leave-request-list");
			return;
		}

		boolean success = finalApprove(leaveRequest, authUser.getId());
		if (success) {
			session.setAttribute("successMsg", "Duyệt cuối đơn nghỉ thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể duyệt cuối đơn nghỉ. Vui lòng kiểm tra hạn mức còn lại.");
		}
		response.sendRedirect(request.getContextPath() + "/leave-request-list");
	}

	private boolean finalApprove(LeaveRequest leaveRequest, Long approverId) throws ServletException {
		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			if (conn == null) {
				return false;
			}
			conn.setAutoCommit(false);

			boolean requestUpdated = leaveRequestDAO.finalApprove(conn, leaveRequest.getId(), approverId);
			boolean balanceUpdated = true;
			if (requestUpdated && requiresBalance(leaveRequest)) {
				int year = leaveRequest.getStartDate().toLocalDate().getYear();
				balanceUpdated = leaveBalanceDAO.incrementUsedDays(conn, leaveRequest.getUserId(),
						leaveRequest.getLeaveTypeId(), year, leaveRequest.getDays());
			}

			if (requestUpdated && balanceUpdated) {
				// Nhân viên đã được duyệt cuối nghỉ ngày này -> hủy hết OT đã duyệt
				// trước đó trùng ngày (tránh trạng thái mâu thuẫn vừa nghỉ vừa có OT).
				// Cùng transaction với việc duyệt leave để đảm bảo nhất quán.
				overtimeDAO.cancelApprovedInRange(conn, leaveRequest.getUserId(), leaveRequest.getStartDate(),
						leaveRequest.getEndDate(), approverId);
				conn.commit();
				return true;
			}
			conn.rollback();
		} catch (SQLException e) {
			rollback(conn);
			throw new ServletException(e);
		} finally {
			closeConnection(conn);
		}
		return false;
	}

	private String validate(LeaveRequest leaveRequest, User authUser) {
		if (leaveRequest == null) {
			return "Không tìm thấy đơn nghỉ.";
		}
		if (!ROLE_EMPLOYEE.equals(leaveRequest.getRequesterRole())) {
			return "Chỉ duyệt cuối cho đơn của nhân viên vai trò EMPLOYEE.";
		}
		if (authUser.getId() != null && authUser.getId().equals(leaveRequest.getUserId())) {
			return "Không thể tự duyệt đơn nghỉ của chính mình.";
		}
		if (!"APPROVED_LEVEL_1".equals(leaveRequest.getStatus())) {
			return "Chỉ có thể duyệt cuối đơn đã được duyệt cấp 1.";
		}
		if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null
				|| leaveRequest.getDays() == null) {
			return "Dữ liệu đơn nghỉ không hợp lệ.";
		}
		if (attendanceDAO.hasAnyAttendanceInRange(leaveRequest.getUserId(), leaveRequest.getStartDate(),
				leaveRequest.getEndDate())) {
			return "Nhân viên đã có dữ liệu chấm công trong khoảng ngày xin nghỉ này — không thể duyệt cuối."
					+ " Vui lòng kiểm tra lại chấm công trước khi duyệt đơn.";
		}
		if (leaveRequestDAO.hasOverlappingActiveRequest(leaveRequest.getUserId(), leaveRequest.getStartDate(),
				leaveRequest.getEndDate(), leaveRequest.getId())) {
			return "Nhân viên đã có đơn nghỉ khác trùng với khoảng thời gian này.";
		}
		return null;
	}

	private boolean requiresBalance(LeaveRequest leaveRequest) {
		return Boolean.TRUE.equals(leaveRequest.getLeaveTypeRequiresBalance())
				|| Boolean.TRUE.equals(leaveRequest.getLeaveTypeAnnualLeave());
	}

	private void rollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException ignored) {
			}
		}
	}

	private void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.setAutoCommit(true);
				conn.close();
			} catch (SQLException ignored) {
			}
		}
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
