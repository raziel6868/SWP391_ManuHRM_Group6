package controller.leaverequest;

import dal.LeaveRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.LeaveRequest;
import model.Permission;
import model.User;

@WebServlet(name = "LeaveRequestApproveServlet", urlPatterns = {"/leave-request-approve"})
public class LeaveRequestApproveServlet extends HttpServlet {

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
		if (leaveRequest == null) {
			session.setAttribute("errorMsg", "Không tìm thấy đơn nghỉ phép.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (authUser.getId() != null && authUser.getId().equals(leaveRequest.getUserId())) {
			session.setAttribute("errorMsg", "Không thể tự duyệt đơn nghỉ phép của chính mình.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (!"PENDING".equals(leaveRequest.getStatus())) {
			session.setAttribute("errorMsg", "Chỉ có thể duyệt cấp 1 cho đơn đang chờ duyệt.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (shouldLimitToManagedEmployees(session)
				&& !leaveRequestDAO.isRequesterManagedBy(leaveRequest.getId(), authUser.getId())) {
			session.setAttribute("errorMsg", "Chỉ có thể duyệt đơn của nhân viên dưới quyền.");
			response.sendRedirect(redirectUrl);
			return;
		}

		boolean success = leaveRequestDAO.approveLevel1(id, authUser.getId());
		if (success) {
			session.setAttribute("successMsg", "Duyệt cấp 1 đơn nghỉ phép thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể duyệt đơn nghỉ phép. Vui lòng thử lại.");
		}
		response.sendRedirect(redirectUrl);
	}

	private boolean shouldLimitToManagedEmployees(HttpSession session) {
		return hasPermission(session, "LEAVE_REQUEST_APPROVE_L1") && !hasPermission(session, "LEAVE_REQUEST_VIEW");
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (permissionCode.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
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
