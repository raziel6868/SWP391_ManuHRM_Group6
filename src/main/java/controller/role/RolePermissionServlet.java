package controller.role;

import dal.PermissionDAO;
import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Permission;
import model.Role;

import java.io.IOException;
import java.util.List;

/**
 * Servlet responsible for rendering the dynamic role-permission assignment
 * interface. Retrieves all permissions and marks those currently assigned to a
 * specific role.
 */
@WebServlet(name = "RolePermissionServlet", urlPatterns = {"/role-permission"})
public class RolePermissionServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		if (idStr == null || idStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/role-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr);
			RoleDAO roleDAO = new RoleDAO();
			Role role = roleDAO.getById(id);

			if (role == null) {
				response.sendRedirect(request.getContextPath() + "/role-list");
				return;
			}

			PermissionDAO permissionDAO = new PermissionDAO();
			List<Permission> allPermissions = permissionDAO.getAllPermissions();
			List<Long> assignedPermissionIds = permissionDAO.getPermissionIdsByRoleId(id);

			request.setAttribute("role", role);
			request.setAttribute("allPermissions", allPermissions);
			request.setAttribute("assignedPermissionIds", assignedPermissionIds);
			request.setAttribute("isSystemRole", role.getIsSystem() != null && role.getIsSystem());

			request.getRequestDispatcher("/views/role/role-permission.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/role-list");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String roleIdStr = request.getParameter("id");
		if (roleIdStr == null || roleIdStr.isBlank()) {
			response.sendRedirect(request.getContextPath() + "/role-list");
			return;
		}

		try {
			Long roleId = Long.parseLong(roleIdStr);
			RoleDAO roleDAO = new RoleDAO();
			Role role = roleDAO.getById(roleId);

			if (role == null || (role.getIsSystem() != null && role.getIsSystem())) {
				response.sendRedirect(request.getContextPath() + "/role-list");
				return;
			}

			String[] permissionIdStrs = request.getParameterValues("permissionIds");

			java.util.List<Long> permissionIds = new java.util.ArrayList<>();
			if (permissionIdStrs != null) {
				for (String pId : permissionIdStrs) {
					permissionIds.add(Long.parseLong(pId));
				}
			}

			PermissionDAO permissionDAO = new PermissionDAO();
			boolean success = permissionDAO.updateRolePermissions(roleId, permissionIds);

			if (success) {
				request.getSession().setAttribute("successMsg", "Cập nhật phân quyền thành công!");
			} else {
				request.getSession().setAttribute("errorMsg", "Lỗi: Không thể cập nhật quyền.");
			}

			response.sendRedirect(request.getContextPath() + "/role-permission?id=" + roleId);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/role-list");
		}
	}
}
