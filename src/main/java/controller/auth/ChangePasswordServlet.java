package controller.auth;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password", "/auth/change-password"})
public class ChangePasswordServlet extends HttpServlet {

	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("authUser") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = (User) session.getAttribute("authUser");
		request.setAttribute("isRequired", user.getMustChangePassword() != null && user.getMustChangePassword());
		request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession(false);

		if (session == null || session.getAttribute("authUser") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = (User) session.getAttribute("authUser");
		boolean isRequired = user.getMustChangePassword() != null && user.getMustChangePassword();

		String currentPassword = request.getParameter("currentPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");

		if (currentPassword == null || currentPassword.isBlank()) {
			forwardWithError(request, response, "Vui lòng nhập mật khẩu hiện tại.", isRequired, user);
			return;
		}

		if (newPassword == null || newPassword.length() < 6) {
			forwardWithError(request, response, "Mật khẩu mới phải có ít nhất 6 ký tự.", isRequired, user);
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			forwardWithError(request, response, "Mật khẩu xác nhận không khớp.", isRequired, user);
			return;
		}

		String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
		if (userDAO.resetPassword(user.getId(), newHash, false)) {
			user.setMustChangePassword(false);
			user.setPasswordHash(newHash);
			session.setAttribute("authUser", user);
			session.setAttribute("successMsg", "Đổi mật khẩu thành công!");
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			forwardWithError(request, response, "Không thể đổi mật khẩu. Vui lòng thử lại.", isRequired, user);
		}
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message,
			boolean isRequired, User user) throws ServletException, IOException {
		request.setAttribute("error", message);
		request.setAttribute("isRequired", isRequired);
		request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
	}
}
