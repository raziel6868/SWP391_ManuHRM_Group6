package controller.auth;

import dal.UserDAO;
import model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet responsible for handling user password changes. Enforces security
 * constraints and updates the password in the database.
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/auth/change-password"})
public class ChangePasswordServlet extends HttpServlet {

	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		String currentPassword = request.getParameter("currentPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");

		if (newPassword == null || newPassword.trim().length() < 6) {
			forwardWithError(request, response, "Mật khẩu mới phải có độ dài tối thiểu từ 6 ký tự trở lên!");
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			forwardWithError(request, response, "Xác nhận mật khẩu mới không trùng khớp! Vui lòng nhập lại.");
			return;
		}

		if (currentPassword.equals(newPassword)) {
			forwardWithError(request, response, "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
			return;
		}

		boolean isUpdated = userDAO.changePassword(authUser.getId(), currentPassword, newPassword);

		if (isUpdated) {
			request.setAttribute("success", "Đổi mật khẩu thành công! Hãy dùng mật khẩu mới cho lần đăng nhập sau.");
			request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
		} else {
			forwardWithError(request, response, "Mật khẩu hiện tại không chính xác. Vui lòng kiểm tra lại.");
		}
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
			throws ServletException, IOException {
		request.setAttribute("error", message);
		request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
	}
}