package controller.employeecontract;

import dal.EmployeeContractDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Department;
import model.EmployeeContract;
import model.Permission;
import dal.DepartmentDAO;

@WebServlet(name = "EmployeeContractListServlet", urlPatterns = {"/contract-list"})
public class EmployeeContractListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String keyword = normalizeText(request.getParameter("keyword"));
		String status = normalizeText(request.getParameter("status"));
		String departmentIdParam = normalizeText(request.getParameter("departmentId"));
		Long departmentId = parseId(departmentIdParam);

		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<EmployeeContract> contracts = contractDAO.searchContracts(keyword, status, departmentId, offset,
				PAGE_SIZE);
		int totalRecords = contractDAO.countContracts(keyword, status, departmentId);
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
			contracts = contractDAO.searchContracts(keyword, status, departmentId, offset, PAGE_SIZE);
		}

		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("contracts", contracts);
		request.setAttribute("keyword", keyword != null ? keyword : "");
		request.setAttribute("selectedStatus", status != null ? status : "");
		request.setAttribute("selectedDepartmentId", departmentIdParam != null ? departmentIdParam : "");
		request.setAttribute("departments", departments);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);

		request.setAttribute("activeCount", contractDAO.countContracts(null, "ACTIVE", null));
		request.setAttribute("pendingRenewalCount", contractDAO.countContracts(null, "PENDING_RENEWAL", null));
		request.setAttribute("expiringSoonCount", contractDAO.countExpiringSoon(30));
		request.setAttribute("terminatedCount", contractDAO.countContracts(null, "TERMINATED", null));

		request.setAttribute("canCreate", hasPermission(session, "CONTRACT_CREATE"));
		request.setAttribute("canUpdate", hasPermission(session, "CONTRACT_UPDATE"));

		request.getRequestDispatcher("/views/employeecontract/employee-contract-list.jsp").forward(request, response);
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

	private Long parseId(String idParam) {
		if (idParam == null || idParam.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(idParam);
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
