package controller.user;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.PermissionUtil;

import java.io.IOException;

@WebServlet(name = "UserStatusServlet", urlPatterns = {"/user-status"})
public class UserStatusServlet extends HttpServlet {
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idParam = request.getParameter("id");
		String activeParam = request.getParameter("isActive");

		if (idParam == null || activeParam == null) {
			response.sendRedirect("user-list");
			return;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect("user-list");
			return;
		}

		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null) {
			response.sendRedirect("user-list");
			return;
		}

		Long targetId;
		try {
			targetId = Long.parseLong(idParam.trim());
		} catch (NumberFormatException e) {
			response.sendRedirect("user-list");
			return;
		}

		// Lấy thông tin target user
		User targetUser = userDAO.getById(targetId);
		if (targetUser == null) {
			response.sendRedirect("user-list");
			return;
		}

		// Kiểm tra quyền theo RBAC
		if (!PermissionUtil.canManageUser(session, targetUser)) {
			session.setAttribute("errorMsg", "Bạn không có quyền thay đổi trạng thái nhân viên này.");
			response.sendRedirect("user-list");
			return;
		}

		boolean isActive = "true".equals(activeParam);
		boolean success = userDAO.updateStatus(targetId, isActive);

		String referer = request.getParameter("referer");
		if (success && "detail".equals(referer)) {
			response.sendRedirect("user-detail?id=" + targetId);
		} else {
			response.sendRedirect("user-list");
		}
	}
}
