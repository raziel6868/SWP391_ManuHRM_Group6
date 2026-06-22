package controller.shiftassignment;

import java.io.IOException;
import java.util.List;
import java.sql.Date;
import dal.DepartmentDAO;
import dal.ShiftAssignmentDAO;
import dal.ShiftDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import model.Shift;
import model.ShiftAssignment;
import model.User;

@WebServlet(name = "ShiftAssignmentListServlet", urlPatterns = {"/shift-assignment-list"})
public class ShiftAssignmentListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 20;

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "SHIFT_ASSIGNMENT_VIEW")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String pageStr = request.getParameter("page");
		int currentPage = 1;
		if (pageStr != null && !pageStr.trim().isEmpty()) {
			try {
				currentPage = Math.max(1, Integer.parseInt(pageStr.trim()));
			} catch (NumberFormatException e) {
			}
		}
		int offset = (currentPage - 1) * PAGE_SIZE;

		Long departmentId = parseId(request.getParameter("departmentId"));
		Long shiftId = parseId(request.getParameter("shiftId"));
		Date startDate = parseDate(request.getParameter("startDate"));
		Date endDate = parseDate(request.getParameter("endDate"));

		List<ShiftAssignment> assignments = shiftAssignmentDAO.searchAssignments(departmentId, null, shiftId, startDate,
				endDate, offset, PAGE_SIZE);
		int totalCount = shiftAssignmentDAO.countAssignments(departmentId, null, shiftId, startDate, endDate);
		int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / PAGE_SIZE) : 1;

		List<Department> departments = departmentDAO.getActiveDepartments();
		List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);
		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, 100);

		request.setAttribute("assignments", assignments);
		request.setAttribute("departments", departments);
		request.setAttribute("shifts", shifts);
		request.setAttribute("users", users);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalCount", totalCount);
		request.setAttribute("filterDepartmentId", departmentId);
		request.setAttribute("filterShiftId", shiftId);
		request.setAttribute("filterStartDate", request.getParameter("startDate"));
		request.setAttribute("filterEndDate", request.getParameter("endDate"));

		request.setAttribute("canAssign", hasPermission(session, "SHIFT_ASSIGNMENT_ASSIGN"));
		request.setAttribute("canBulkAssign", hasPermission(session, "SHIFT_ASSIGNMENT_BULK"));

		request.getRequestDispatcher("/views/shiftassignment/shift-assignment-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (code.equals(p.getCode())) {
				return true;
			}
		}
		return false;
	}

	private Long parseId(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Date parseDate(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Date.valueOf(value.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
