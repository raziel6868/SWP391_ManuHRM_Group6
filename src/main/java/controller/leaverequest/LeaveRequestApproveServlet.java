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

@WebServlet(name = "LeaveRequestApproveServlet", urlPatterns = {"/leave-request-approve"})
public class LeaveRequestApproveServlet extends HttpServlet {

	private static final String ROLE_EMPLOYEE = "EMPLOYEE";

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

		String redirectUrl = resolveRedirectUrl(request);
		Long id = parseLong(request.getParameter("id"));
		LeaveRequest leaveRequest = leaveRequestDAO.getById(id);
		String validationError = validateFirstApproval(authUser, leaveRequest);
		if (validationError != null) {
			session.setAttribute("errorMsg", validationError);
			response.sendRedirect(redirectUrl);
			return;
		}

		boolean isEmployeeRequest = ROLE_EMPLOYEE.equals(leaveRequest.getRequesterRole());
		boolean success = isEmployeeRequest
				? leaveRequestDAO.approveLevel1(id, authUser.getId())
				: processDirectApproval(id, authUser.getId(), leaveRequest);

		if (success) {
			session.setAttribute("successMsg",
					isEmployeeRequest
							? "Duyệt cấp 1 đơn nghỉ phép thành công. Đơn sẽ được chuyển đến HR để duyệt cuối."
							: "Duyệt đơn nghỉ phép thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể duyệt đơn nghỉ phép. Vui lòng thử lại.");
		}
		response.sendRedirect(redirectUrl);
	}

	private String validateFirstApproval(User authUser, LeaveRequest leaveRequest) {
		if (leaveRequest == null) {
			return "Không tìm thấy đơn nghỉ phép.";
		}
		if (authUser.getId() != null && authUser.getId().equals(leaveRequest.getUserId())) {
			return "Không thể tự duyệt đơn nghỉ phép của chính mình.";
		}
		if (!"PENDING".equals(leaveRequest.getStatus())) {
			return "Chỉ có thể duyệt đơn đang chờ duyệt.";
		}
		if (!isDirectManager(authUser, leaveRequest)) {
			return "Chỉ manager trực tiếp mới có thể duyệt đơn nghỉ phép này.";
		}
		if (leaveRequest.getStartDate() == null || leaveRequest.getDays() == null) {
			return "Dữ liệu đơn nghỉ phép không hợp lệ.";
		}
		return null;
	}

	private boolean processDirectApproval(Long id, Long approverId, LeaveRequest leaveRequest) {
		int year = leaveRequest.getStartDate().toLocalDate().getYear();
		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				boolean requestUpdated = leaveRequestDAO.directApprove(conn, id, approverId);
				boolean balanceUpdated = false;
				if (requestUpdated) {
					balanceUpdated = leaveBalanceDAO.incrementUsedDays(conn, leaveRequest.getUserId(),
							leaveRequest.getLeaveTypeId(), year, leaveRequest.getDays());
				}

				if (requestUpdated && balanceUpdated) {
					conn.commit();
					return true;
				}
				conn.rollback();
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("LeaveRequestApproveServlet.processDirectApproval() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("LeaveRequestApproveServlet.getConnection() ERROR: " + e.getMessage());
		}
		return false;
	}

	private boolean isDirectManager(User authUser, LeaveRequest leaveRequest) {
		return authUser.getId() != null && authUser.getId().equals(leaveRequest.getRequesterManagerId());
	}

	private String resolveRedirectUrl(HttpServletRequest request) {
		String returnUrl = request.getParameter("returnUrl");
		String contextPath = request.getContextPath();
		if (returnUrl != null && returnUrl.startsWith(contextPath + "/") && !returnUrl.contains("://")) {
			return returnUrl;
		}
		return contextPath + "/leave-request-list";
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
