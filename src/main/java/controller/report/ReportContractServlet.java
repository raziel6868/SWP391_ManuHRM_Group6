package controller.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.ContractExpiryReportRow;
import dto.ContractStatusRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import model.User;

@WebServlet(name = "ReportContractServlet", urlPatterns = {"/report-contract"})
public class ReportContractServlet extends HttpServlet {

	private static final int EXPIRING_DAYS = 30;
	private final ReportDAO reportDAO = new ReportDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_CONTRACT")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		List<String> validationErrors = new ArrayList<>();
		Long departmentId = parsePositiveLong(request.getParameter("departmentId"), "Phòng ban", validationErrors);

		List<ContractStatusRow> rows = reportDAO.getContractStatus(departmentId);
		List<ContractExpiryReportRow> expiringContracts = reportDAO.getExpiringContracts(EXPIRING_DAYS, departmentId);
		List<Department> departments = departmentDAO.getActiveDepartments();
		ContractStatusRow totals = calculateTotals(rows);

		request.setAttribute("rows", rows);
		request.setAttribute("expiringContracts", expiringContracts);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("validationErrors", validationErrors);
		request.setAttribute("totals", totals);
		request.setAttribute("expiringDays", EXPIRING_DAYS);
		request.setAttribute("maxContracts", findMaxContracts(rows));

		request.getRequestDispatcher("/views/report/report-contract.jsp").forward(request, response);
	}

	private Long parsePositiveLong(String value, String label, List<String> errors) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			long parsed = Long.parseLong(value);
			if (parsed > 0) {
				return parsed;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add(label + " không hợp lệ.");
		return null;
	}

	private ContractStatusRow calculateTotals(List<ContractStatusRow> rows) {
		ContractStatusRow totals = new ContractStatusRow();
		totals.setDepartmentName("Tổng cộng");
		for (ContractStatusRow row : rows) {
			totals.setActiveContracts(totals.getActiveContracts() + row.getActiveContracts());
			totals.setExpiredContracts(totals.getExpiredContracts() + row.getExpiredContracts());
			totals.setExpiringSoonContracts(totals.getExpiringSoonContracts() + row.getExpiringSoonContracts());
			totals.setPendingRenewal(totals.getPendingRenewal() + row.getPendingRenewal());
			totals.setTerminatedContracts(totals.getTerminatedContracts() + row.getTerminatedContracts());
			totals.setTotalContracts(totals.getTotalContracts() + row.getTotalContracts());
		}
		return totals;
	}

	private int findMaxContracts(List<ContractStatusRow> rows) {
		int max = 1;
		for (ContractStatusRow row : rows) {
			max = Math.max(max, row.getTotalContracts());
		}
		return max;
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
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
}
