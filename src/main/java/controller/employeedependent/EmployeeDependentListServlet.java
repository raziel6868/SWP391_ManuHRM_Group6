package controller.employeedependent;

import dal.DepartmentDAO;
import dal.EmployeeDependentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Department;
import model.EmployeeDependent;
import model.Permission;

@WebServlet(name = "EmployeeDependentListServlet", urlPatterns = {"/employee-dependent-list"})
public class EmployeeDependentListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final EmployeeDependentDAO employeeDependentDAO = new EmployeeDependentDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String keyword = normalizeText(request.getParameter("keyword"));
		Long departmentId = parseLong(request.getParameter("departmentId"));

		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<EmployeeDependent> employeeDependents = employeeDependentDAO.search(departmentId, keyword, offset,
				PAGE_SIZE);
		int totalRecords = employeeDependentDAO.count(departmentId, keyword);
		int totalPages = totalRecords / PAGE_SIZE;
		if (totalRecords % PAGE_SIZE != 0) {
			totalPages++;
		}
		if (totalPages == 0) {
			totalPages = 1;
		}
		if (currentPage > totalPages) {
			currentPage = totalPages;
			offset = (currentPage - 1) * PAGE_SIZE;
			employeeDependents = employeeDependentDAO.search(departmentId, keyword, offset, PAGE_SIZE);
		}

		List<Department> departments = departmentDAO.getActiveDepartments();
		request.setAttribute("employeeDependents", employeeDependents);
		request.setAttribute("departments", departments);
		request.setAttribute("keyword", keyword);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("canSetup", hasPermission(session, "EMPLOYEE_DEPENDENT_SETUP"));
		request.setAttribute("canChangeStatus", hasPermission(session, "EMPLOYEE_DEPENDENT_STATUS"));

		request.getRequestDispatcher("/views/employeedependent/employee-dependent-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (permissionCode.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private int parsePage(String pageParam) {
		if (pageParam == null || pageParam.isBlank()) {
			return 1;
		}
		try {
			return Math.max(1, Integer.parseInt(pageParam));
		} catch (NumberFormatException e) {
			return 1;
		}
	}
}
