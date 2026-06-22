package controller.leavebalance;

import dal.DepartmentDAO;
import dal.LeaveBalanceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Year;
import java.util.List;
import model.Department;
import model.LeaveBalance;
import model.Permission;

@WebServlet(name = "LeaveBalanceListServlet", urlPatterns = {"/leave-balance-list"})
public class LeaveBalanceListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		Integer selectedYear = parseInteger(normalizeText(request.getParameter("year")));
		Long selectedDepartmentId = parseLong(normalizeText(request.getParameter("departmentId")));

		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<LeaveBalance> balances = leaveBalanceDAO.searchBalances(selectedYear, selectedDepartmentId, offset,
				PAGE_SIZE);
		int totalRecords = leaveBalanceDAO.countBalances(selectedYear, selectedDepartmentId);
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
			balances = leaveBalanceDAO.searchBalances(selectedYear, selectedDepartmentId, offset, PAGE_SIZE);
		}

		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("balances", balances);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedYear", selectedYear);
		request.setAttribute("selectedDepartmentId", selectedDepartmentId);
		request.setAttribute("currentYear", Year.now().getValue());
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);
		request.setAttribute("canSetup", hasPermission(session, "LEAVE_BALANCE_SETUP"));

		request.getRequestDispatcher("/views/leavebalance/leave-balance-list.jsp").forward(request, response);
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

	private Integer parseInteger(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Long parseLong(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(value);
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
