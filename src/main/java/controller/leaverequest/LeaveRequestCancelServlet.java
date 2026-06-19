package controller.leaverequest;

import dal.LeaveRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.LeaveRequest;
import model.User;

@WebServlet(name = "LeaveRequestCancelServlet", urlPatterns = {"/leave-request-cancel"})
public class LeaveRequestCancelServlet extends HttpServlet {

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
		if (leaveRequest == null) {
			session.setAttribute("errorMsg", "Khong tim thay don nghi phep.");
			response.sendRedirect(request.getContextPath() + "/leave-request-my");
			return;
		}
		if (authUser.getId() == null || !authUser.getId().equals(leaveRequest.getUserId())) {
			session.setAttribute("errorMsg", "Chi co the huy don nghi phep cua chinh minh.");
			response.sendRedirect(request.getContextPath() + "/leave-request-my");
			return;
		}
		if (!"PENDING".equals(leaveRequest.getStatus()) && !"APPROVED_LEVEL_1".equals(leaveRequest.getStatus())) {
			session.setAttribute("errorMsg", "Chi co the huy don dang cho duyet.");
			response.sendRedirect(request.getContextPath() + "/leave-request-my");
			return;
		}

		boolean success = leaveRequestDAO.cancel(id, authUser.getId());
		if (success) {
			session.setAttribute("successMsg", "Huy don nghi phep thanh cong.");
		} else {
			session.setAttribute("errorMsg", "Khong the huy don nghi phep. Vui long thu lai.");
		}
		response.sendRedirect(request.getContextPath() + "/leave-request-my");
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
