package controller.role;

import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet responsible for toggling the active status of a role. Designed to be
 * called via POST to prevent accidental state changes.
 */
@WebServlet(name = "RoleStatusServlet", urlPatterns = {"/role-status"})
public class RoleStatusServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		String isActiveStr = request.getParameter("isActive");

		if (idStr != null && !idStr.isEmpty() && isActiveStr != null && !isActiveStr.isEmpty()) {
			try {
				Long id = Long.parseLong(idStr);
				boolean isActive = Boolean.parseBoolean(isActiveStr);

				RoleDAO roleDAO = new RoleDAO();
				roleDAO.updateStatus(id, isActive);
			} catch (NumberFormatException e) {
				// Ignore
			}
		}
		response.sendRedirect(request.getContextPath() + "/role-list");
	}
}
