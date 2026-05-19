package controller.role;

import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Role;

@WebServlet(name = "RoleUpdateServlet", urlPatterns = {"/role-update"})
public class RoleUpdateServlet extends HttpServlet {

	private final RoleDAO roleDAO = new RoleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		if (idStr != null && !idStr.isEmpty()) {
			try {
				Long id = Long.parseLong(idStr);
				Role role = roleDAO.getById(id);
				if (role != null) {
					request.setAttribute("role", role);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		request.getRequestDispatcher("/views/role/role-form.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String idStr = request.getParameter("id");
		String displayName = request.getParameter("displayName");
		String description = request.getParameter("description");

		if (idStr != null && !idStr.isEmpty()) {
			try {
				Long id = Long.parseLong(idStr);

				Role role = new Role();
				role.setId(id);
				role.setDisplayName(displayName);
				role.setDescription(description);

				boolean isSuccess = roleDAO.update(role);
				if (isSuccess) {
					response.sendRedirect(request.getContextPath() + "/role-list.jsp?msg=success");
					return;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		response.sendRedirect(request.getContextPath() + "/role-update?id=" + idStr + "&msg=failed");
	}
}
