package controller.role;

import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Role;

import java.io.IOException;
import java.util.List;

/**
 * Servlet responsible for rendering the list of system roles. Includes support
 * for keyword searching and pagination.
 */
@WebServlet(name = "RoleListServlet", urlPatterns = {"/role-list"})
public class RoleListServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		// Chuyển message từ session sang request rồi xóa session
		String successMsg = (String) session.getAttribute("successMsg");
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}

		String keyword = request.getParameter("keyword");
		String pageStr = request.getParameter("page");

		int page = 1;
		if (pageStr != null && !pageStr.isEmpty()) {
			try {
				page = Integer.parseInt(pageStr);
			} catch (NumberFormatException e) {
				page = 1;
			}
		}

		int limit = 10;
		int offset = (page - 1) * limit;

		RoleDAO roleDAO = new RoleDAO();
		List<Role> roles = roleDAO.searchRoles(keyword, offset, limit);
		int totalRoles = roleDAO.countRoles(keyword);
		int totalPages = (int) Math.ceil((double) totalRoles / limit);

		request.setAttribute("roles", roles);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("keyword", keyword);

		request.getRequestDispatcher("/views/role/role-list.jsp").forward(request, response);
	}
}
