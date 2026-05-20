package controller.user;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;

/**
 * Servlet responsible for updating user account status (Activate/Deactivate).
 * Designed to be called via POST to prevent CSRF via link prefetching.
 */
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

		Long id;
		try {
			id = Long.parseLong(idParam.trim());
		} catch (NumberFormatException e) {
			response.sendRedirect("user-list");
			return;
		}

		HttpSession session = request.getSession(false);
		if (session != null) {
			User authUser = (User) session.getAttribute("authUser");
			if (authUser != null && authUser.getId() == id) {
				request.getSession().setAttribute("errorMessage",
						"Bạn không thể thay đổi trạng thái tài khoản của chính mình.");
				String referer = request.getParameter("referer");
				if ("detail".equals(referer)) {
					response.sendRedirect("user-detail?id=" + id);
				} else {
					response.sendRedirect("user-list");
				}
				return;
			}
		}

		boolean isActive = "true".equals(activeParam);

		boolean success = userDAO.updateStatus(id, isActive);

		String referer = request.getParameter("referer");
		if (success && "detail".equals(referer)) {
			response.sendRedirect("user-detail?id=" + id);
		} else {
			response.sendRedirect("user-list");
		}
	}
}
