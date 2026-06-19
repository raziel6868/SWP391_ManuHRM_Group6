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

@WebServlet(name = "LeaveRequestRejectServlet", urlPatterns = {"/leave-request-reject"})
public class LeaveRequestRejectServlet extends HttpServlet {

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
			session.setAttribute("errorMsg", "Khong tim thay don nghi phep.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (!"PENDING".equals(leaveRequest.getStatus()) && !"APPROVED_LEVEL_1".equals(leaveRequest.getStatus())) {
			session.setAttribute("errorMsg", "Chi co the tu choi don dang cho duyet.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (shouldLimitToManagedEmployees(session)
				&& !leaveRequestDAO.isRequesterManagedBy(leaveRequest.getId(), authUser.getId())) {
			session.setAttribute("errorMsg", "Chi co the tu choi don cua nhan vien duoi quyen.");
			response.sendRedirect(redirectUrl);
			return;
		}

		boolean success = leaveRequestDAO.reject(id, authUser.getId());
		if (success) {
			session.setAttribute("successMsg", "Tu choi don nghi phep thanh cong.");
		} else {
			session.setAttribute("errorMsg", "Khong the tu choi don nghi phep. Vui long thu lai.");
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
