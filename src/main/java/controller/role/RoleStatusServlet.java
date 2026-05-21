package controller.role;

import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet responsible for toggling the active status of a role. Only allows
 * deactivating a role if there are no active users assigned to it. Only allows
 * activating a role if there are no users with this role.
 */
@WebServlet(name = "RoleStatusServlet", urlPatterns = {"/role-status"})
public class RoleStatusServlet extends HttpServlet {

	private final RoleDAO roleDAO = new RoleDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		String isActiveStr = request.getParameter("isActive");

		if (idStr == null || idStr.isEmpty() || isActiveStr == null || isActiveStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/role-list");
			return;
		}

		try {
			Long roleId = Long.parseLong(idStr);
			boolean newStatus = Boolean.parseBoolean(isActiveStr);

			if (!newStatus) { // Đang cố gắng VÔ HIỆU HÓA role
				int activeUserCount = userDAO.countActiveUsersByRoleId(roleId);
				if (activeUserCount > 0) {
					// Có user đang active dùng role này -> không cho khóa
					request.getSession().setAttribute("errorMsg", "Không thể vô hiệu hóa vai trò này. Hiện có "
							+ activeUserCount + " nhân viên đang hoạt động đang sử dụng vai trò này.");
					response.sendRedirect(request.getContextPath() + "/role-list");
					return;
				}
			}

			// Tiến hành cập nhật trạng thái
			boolean success = roleDAO.updateStatus(roleId, newStatus);
			if (success) {
				String message = newStatus ? "successMsg" : "successMsg";
				request.getSession().setAttribute(message,
						newStatus ? "Kích hoạt vai trò thành công!" : "Vô hiệu hóa vai trò thành công!");
			}

		} catch (NumberFormatException e) {
			// Invalid ID, ignore
		}

		response.sendRedirect(request.getContextPath() + "/role-list");
	}
}
