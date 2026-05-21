package controller.auth;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.PasswordUtil;
import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

		// Chỉ kiểm tra mật khẩu cũ nếu KHÔNG bắt buộc đổi mật khẩu (tức user tự đổi)
		if (!isRequired) {
			if (currentPassword == null || currentPassword.isBlank()) {
				forwardWithError(request, response, "Vui lòng nhập mật khẩu hiện tại.", isRequired);
				return;
			}

			String selectSql = "SELECT password_hash FROM users WHERE id = ?";
			try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(selectSql)) {
				ps.setLong(1, user.getId());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String storedHash = rs.getString("password_hash");
						if (!util.PasswordUtil.checkPassword(currentPassword, storedHash)) {
							forwardWithError(request, response, "Mật khẩu hiện tại không chính xác.", isRequired);
							return;
						}
					}
				}
			} catch (Exception e) {
				System.err.println("ChangePassword: " + e.getMessage());
			}
		}

		if (newPassword == null || newPassword.length() < 6) {
			forwardWithError(request, response, "Mật khẩu mới phải có ít nhất 6 ký tự.", isRequired);
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			forwardWithError(request, response, "Mật khẩu xác nhận không khớp.", isRequired);
			return;
		}

		String newHash = PasswordUtil.hashPassword(newPassword);
		if (userDAO.resetPassword(user.getId(), newHash, false)) {
			user.setMustChangePassword(false);
			user.setPasswordHash(newHash);
			session.setAttribute("authUser", user);
			session.setAttribute("successMsg", "Đổi mật khẩu thành công!");
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			forwardWithError(request, response, "Không thể đổi mật khẩu. Vui lòng thử lại.", isRequired);
		}
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message,
			boolean isRequired) throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("isRequired", isRequired);
		request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
	}
}
