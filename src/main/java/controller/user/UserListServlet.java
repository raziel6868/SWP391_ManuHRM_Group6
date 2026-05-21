package controller.user;

import dal.DepartmentDAO;
import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Department;
import model.Role;
import model.User;
import util.PermissionUtil;

@WebServlet(name = "UserListServlet", urlPatterns = {"/user-list"})
public class UserListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final RoleDAO roleDAO = new RoleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User authUser = (User) session.getAttribute("authUser");

		String keyword = request.getParameter("keyword");
		String deptParam = request.getParameter("departmentId");
		String roleParam = request.getParameter("roleId");
		String statusParam = request.getParameter("isActive");
		String employeeType = request.getParameter("employeeType");
		String pageParam = request.getParameter("page");

		Long departmentId = parseLong(deptParam);
		Long roleId = parseLong(roleParam);

		Boolean isActive = null;
		if ("1".equals(statusParam))
			isActive = true;
		else if ("0".equals(statusParam))
			isActive = false;

		int page = (pageParam != null && pageParam.matches("\\d+")) ? Integer.parseInt(pageParam) : 1;
		if (page < 1)
			page = 1;
		int offset = (page - 1) * PAGE_SIZE;

		// Line Manager chỉ thấy user dưới quyền mình
		Long managerId = null;
		if ("LINE_MANAGER".equals(authUser.getRoleName())) {
			managerId = authUser.getId();
		}

		List<User> users = userDAO.searchUsers(keyword, departmentId, roleId, isActive, employeeType, offset, PAGE_SIZE,
				managerId);
		int totalRecords = userDAO.countUsers(keyword, departmentId, roleId, isActive, employeeType, managerId);
		int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
		List<Department> departments = departmentDAO.getActiveDepartments();
		List<Role> roles = roleDAO.getActiveRoles();

		// Truyền thông tin RBAC xuống JSP
		int authUserRank = PermissionUtil.getRoleRank(authUser.getRoleName());
		request.setAttribute("authUserRank", authUserRank);
		request.setAttribute("authUserId", authUser.getId());

		request.setAttribute("users", users);
		request.setAttribute("departments", departments);
		request.setAttribute("roles", roles);
		request.setAttribute("keyword", keyword);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("selectedRoleId", roleId);
		request.setAttribute("selectedStatus", statusParam);
		request.setAttribute("selectedEmployeeType", employeeType);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);

		request.getRequestDispatcher("/views/user/user-list.jsp").forward(request, response);
	}

	private Long parseLong(String s) {
		if (s == null || s.isBlank())
			return null;
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
