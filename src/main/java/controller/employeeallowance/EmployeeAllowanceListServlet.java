package controller.employeeallowance;

import dal.DepartmentDAO;
import dal.EmployeeAllowanceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Department;
import model.EmployeeAllowance;
import model.Permission;

@WebServlet(name = "EmployeeAllowanceListServlet", urlPatterns = {"/employee-allowance-list"})
public class EmployeeAllowanceListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final EmployeeAllowanceDAO employeeAllowanceDAO = new EmployeeAllowanceDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String keyword = normalizeText(request.getParameter("keyword"));
		Long departmentId = parseLong(request.getParameter("departmentId"));
		String statusParam = normalizeText(request.getParameter("status"));
		Boolean selectedIsActive = parseBoolean(statusParam);

		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<EmployeeAllowance> employeeAllowances = employeeAllowanceDAO.search(keyword, departmentId,
				selectedIsActive, offset, PAGE_SIZE);
		int totalRecords = employeeAllowanceDAO.count(keyword, departmentId, selectedIsActive);
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
			employeeAllowances = employeeAllowanceDAO.search(keyword, departmentId, selectedIsActive, offset,
					PAGE_SIZE);
		}

		List<Department> departments = departmentDAO.getActiveDepartments();
		request.setAttribute("employeeAllowances", employeeAllowances);
		request.setAttribute("departments", departments);
		request.setAttribute("keyword", keyword);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("selectedStatus", statusParam);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("canSetup", hasPermission(session, "EMPLOYEE_ALLOWANCE_SETUP"));
		request.setAttribute("canChangeStatus", hasPermission(session, "EMPLOYEE_ALLOWANCE_STATUS"));

		request.getRequestDispatcher("/views/employeeallowance/employee-allowance-list.jsp").forward(request, response);
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

	private Boolean parseBoolean(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		if ("true".equalsIgnoreCase(value)) {
			return true;
		}
		if ("false".equalsIgnoreCase(value)) {
			return false;
		}
		return null;
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
