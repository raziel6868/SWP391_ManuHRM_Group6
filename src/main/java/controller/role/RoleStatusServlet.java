package controller.role;

import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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

			model.Role role = roleDAO.getById(roleId);
			if (role == null) {
				response.sendRedirect(request.getContextPath() + "/role-list");
				return;
			}

			if (!newStatus) { // Đang cố gắng VÔ HIỆU HÓA role
				// Chỉ cho phép deactive LINE_MANAGER và EMPLOYEE roles
				String roleName = role.getName();
				if (!"LINE_MANAGER".equals(roleName) && !"EMPLOYEE".equals(roleName)) {
					request.getSession().setAttribute("errorMsg",
							"Chỉ có thể vô hiệu hóa vai trò Line Manager và Employee.");
					response.sendRedirect(request.getContextPath() + "/role-list");
					return;
				}

				int activeUserCount = userDAO.countActiveUsersByRoleId(roleId);
				if (activeUserCount > 0) {
					request.getSession().setAttribute("errorMsg", "Không thể vô hiệu hóa vai trò này. Hiện có "
							+ activeUserCount + " nhân viên đang hoạt động đang sử dụng vai trò này.");
					response.sendRedirect(request.getContextPath() + "/role-list");
					return;
				}
			}

			boolean success = roleDAO.updateStatus(roleId, newStatus);
			if (success) {
				request.getSession().setAttribute("successMsg",
						newStatus ? "Kích hoạt vai trò thành công!" : "Vô hiệu hóa vai trò thành công!");
			}

		} catch (NumberFormatException e) {
			// Invalid ID, ignore
		}

		response.sendRedirect(request.getContextPath() + "/role-list");
	}
}
