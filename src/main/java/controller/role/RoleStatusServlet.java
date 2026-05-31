package controller.role;

import dal.RoleDAO;
import dal.UserDAO;
import model.Permission;
import model.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "RoleStatusServlet", urlPatterns = {"/role-status"})
public class RoleStatusServlet extends HttpServlet {

	private final RoleDAO roleDAO = new RoleDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath() + "/role-list");
			return;
		}

		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean hasRoleStatusPerm = false;
		if (perms != null) {
			for (Permission p : perms) {
				if ("ROLE_STATUS".equals(p.getCode())) {
					hasRoleStatusPerm = true;
					break;
				}
			}
		}
		if (!hasRoleStatusPerm) {
			session.setAttribute("errorMsg", "Bạn không có quyền thay đổi trạng thái vai trò.");
			response.sendRedirect(request.getContextPath() + "/role-list");
			return;
		}

		String idStr = request.getParameter("id");
		String isActiveStr = request.getParameter("isActive");

		if (idStr == null || idStr.isEmpty() || isActiveStr == null || isActiveStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/role-list");
			return;
		}

		try {
			Long roleId = Long.parseLong(idStr);
			boolean newStatus = Boolean.parseBoolean(isActiveStr);

			Role role = roleDAO.getById(roleId);
			if (role == null) {
				response.sendRedirect(request.getContextPath() + "/role-list");
				return;
			}

			if (!newStatus) { // Vô hiệu hóa role: chỉ hierarchy_level <= 2 (PRODUCTION_SUPERVISOR, EMPLOYEE)
				int roleHierarchy = role.getHierarchyLevel() != null ? role.getHierarchyLevel() : 1;
				if (roleHierarchy > 2) {
					session.setAttribute("errorMsg",
							"Chỉ có thể vô hiệu hóa vai trò Production Supervisor và Employee.");
					response.sendRedirect(request.getContextPath() + "/role-list");
					return;
				}
				// Kiểm tra không có user active nào đang dùng role này
				int activeUserCount = userDAO.countActiveUsersByRoleId(roleId);
				if (activeUserCount > 0) {
					session.setAttribute("errorMsg", "Không thể vô hiệu hóa vai trò này. Hiện có " + activeUserCount
							+ " nhân viên đang hoạt động sử dụng vai trò này.");
					response.sendRedirect(request.getContextPath() + "/role-list");
					return;
				}
			}

			boolean success = roleDAO.updateStatus(roleId, newStatus);
			if (success) {
				session.setAttribute("successMsg",
						newStatus ? "Kích hoạt vai trò thành công!" : "Vô hiệu hóa vai trò thành công!");
			}

		} catch (NumberFormatException e) {
			// Invalid ID, ignore
		}

		response.sendRedirect(request.getContextPath() + "/role-list");
	}
}
