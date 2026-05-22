package controller.role;

import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;
import model.Role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet responsible for rendering the list of system roles. Includes support
 * for keyword searching and pagination.
 */
@WebServlet(name = "RoleListServlet", urlPatterns = {"/role-list"})
public class RoleListServlet extends HttpServlet {

	private final RoleDAO roleDAO = new RoleDAO();
	private final UserDAO userDAO = new UserDAO();

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
		request.setAttribute("hasRoleStatusPerm", hasRoleStatusPerm);

		List<Role> roles = roleDAO.searchRoles(keyword, offset, limit);
		int totalRoles = roleDAO.countRoles(keyword);
		int totalPages = (int) Math.ceil((double) totalRoles / limit);

		// Tính canDeactivate cho mỗi role: chỉ LINE_MANAGER/EMPLOYEE + không có user
		// active
		List<Boolean> canDeactivateList = new ArrayList<>();
		for (Role r : roles) {
			boolean canDeact = false;
			if (hasRoleStatusPerm) {
				// Rank <= 2: chỉ LINE_MANAGER (2) và EMPLOYEE (1) được deactive
				int roleRank = r.getRank() != null ? r.getRank() : 1;
				if (roleRank <= 2) {
					int activeCount = userDAO.countActiveUsersByRoleId(r.getId());
					canDeact = (activeCount == 0);
				}
			}
			canDeactivateList.add(canDeact);
		}
		request.setAttribute("canDeactivateList", canDeactivateList);

		request.setAttribute("roles", roles);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("keyword", keyword);

		request.getRequestDispatcher("/views/role/role-list.jsp").forward(request, response);
	}
}
