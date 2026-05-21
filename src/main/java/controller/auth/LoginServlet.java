package controller.auth;

import dal.UserDAO;
import dal.PermissionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import model.User;
import util.ValidationUtil;

/**
 * Servlet responsible for handling user authentication. Manages login form
 * display and credential verification.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
	private final UserDAO userDAO = new UserDAO();
	private final PermissionDAO permissionDAO = new PermissionDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("authUser") != null) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String identifier = request.getParameter("identifier");
		String password = request.getParameter("password");

		if (ValidationUtil.isBlank(identifier) || ValidationUtil.isBlank(password)) {
			forwardWithError(request, response, "Vui lòng nhập tên đăng nhập/mã nhân viên và mật khẩu.");
			return;
		}

		try {
			User user = userDAO.findActiveUserByLogin(identifier.trim(), password);

			if (user == null) {
				forwardWithError(request, response, "Thông tin đăng nhập không chính xác hoặc tài khoản đã bị khóa.");
				return;
			}

			HttpSession oldSession = request.getSession(false);
			if (oldSession != null) {
				oldSession.invalidate();
			}

			HttpSession session = request.getSession(true);
			session.setAttribute("authUser", user);
			session.setAttribute("permissions", permissionDAO.getPermissionsByRoleId(user.getRoleId()));
			session.setMaxInactiveInterval(30 * 60);

			// Check if user must change password
			if (user.getMustChangePassword() != null && user.getMustChangePassword()) {
				response.sendRedirect(request.getContextPath() + "/change-password");
				return;
			}

			response.sendRedirect(request.getContextPath() + "/home");
		} catch (ServletException | IOException exception) {
			getServletContext().log("Login failed", exception);
			forwardWithError(request, response, "Hệ thống đang bảo trì hoặc gặp sự cố kết nối, vui lòng thử lại sau.");
		}
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
			throws ServletException, IOException {
		request.setAttribute("error", message);
		request.setAttribute("identifier", request.getParameter("identifier"));
		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}

}