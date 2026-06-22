package controller.leaverequest;

import dal.DBContext;
import dal.LeaveBalanceDAO;
import dal.LeaveRequestDAO;
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

	private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();

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
		String validationError = validate(leaveRequest);
		if (validationError != null) {
			session.setAttribute("errorMsg", validationError);
			response.sendRedirect(request.getContextPath() + "/leave-request-list");
			return;
		}

		boolean success = finalApprove(leaveRequest, authUser.getId());
		if (success) {
			session.setAttribute("successMsg", "Duyệt cuối đơn nghỉ phép thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể duyệt cuối đơn nghỉ phép. Vui lòng kiểm tra hạn mức còn lại.");
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
			boolean balanceUpdated = false;
			if (requestUpdated) {
				int year = leaveRequest.getStartDate().toLocalDate().getYear();
				balanceUpdated = leaveBalanceDAO.incrementUsedDays(conn, leaveRequest.getUserId(),
						leaveRequest.getLeaveTypeId(), year, leaveRequest.getDays());
			}

			if (requestUpdated && balanceUpdated) {
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

	private String validate(LeaveRequest leaveRequest) {
		if (leaveRequest == null) {
			return "Không tìm thấy đơn nghỉ phép.";
		}
		if (!"APPROVED_LEVEL_1".equals(leaveRequest.getStatus())) {
			return "Chỉ có thể duyệt cuối đơn đã được duyệt cấp 1.";
		}
		if (leaveRequest.getStartDate() == null || leaveRequest.getDays() == null) {
			return "Dữ liệu đơn nghỉ phép không hợp lệ.";
		}
		return null;
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
