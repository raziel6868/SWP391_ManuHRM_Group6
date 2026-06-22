package controller.leaverequest;

import dal.LeaveRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.LeaveRequest;
import model.Permission;
import model.User;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LeaveRequestApproveServlet", urlPatterns = {"/leave-request-approve"})
public class LeaveRequestApproveServlet extends HttpServlet {

	private final LeaveRequestDAO requestDAO = new LeaveRequestDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "LEAVE_REQUEST_APPROVE_L1")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu.");
			response.sendRedirect(request.getContextPath() + "/leave-request-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr.trim());
			LeaveRequest leaveRequest = requestDAO.getById(id);

			if (leaveRequest == null) {
				session.setAttribute("errorMsg", "Không tìm thấy yêu cầu.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			if (!"PENDING".equals(leaveRequest.getStatus())) {
				session.setAttribute("errorMsg", "Yêu cầu không ở trạng thái chờ duyệt.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			if (authUser.getId().equals(leaveRequest.getUserId())) {
				session.setAttribute("errorMsg", "Bạn không thể tự duyệt yêu cầu nghỉ phép của mình.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			boolean success = requestDAO.approveLevel1(id, authUser.getId());
			if (success) {
				session.setAttribute("successMsg", "Đã duyệt yêu cầu nghỉ phép (Cấp 1).");
			} else {
				session.setAttribute("errorMsg", "Không thể duyệt yêu cầu. Trạng thái có thể đã thay đổi.");
			}

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID yêu cầu không hợp lệ.");
		}

		response.sendRedirect(request.getContextPath() + "/leave-request-list");
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (code.equals(p.getCode())) {
				return true;
			}
		}
		return false;
	}
}
