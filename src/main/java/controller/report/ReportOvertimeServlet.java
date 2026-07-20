package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.OvertimeEmployeeReportRow;
import dto.OvertimeSummaryRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import model.User;
import util.YearOptionUtil;

@WebServlet(name = "ReportOvertimeServlet", urlPatterns = {"/report-overtime"})
public class ReportOvertimeServlet extends HttpServlet {

	private final ReportDAO reportDAO = new ReportDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_OT")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		java.util.Calendar now = java.util.Calendar.getInstance();
		int currentYear = now.get(java.util.Calendar.YEAR);

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");
		List<String> validationErrors = new ArrayList<>();

		int year = parseYear(yearParam, currentYear, validationErrors);
		Integer month = parseOptionalMonth(monthParam, validationErrors);
		Long departmentId = parsePositiveLong(request.getParameter("departmentId"), "Phòng ban", validationErrors);

		List<OvertimeSummaryRow> rows = reportDAO.getOvertimeSummary(year, month, departmentId);
		List<OvertimeEmployeeReportRow> topEmployees = reportDAO.getTopOvertimeEmployees(year, month, departmentId);
		List<Department> departments = departmentDAO.getActiveDepartments();
		OvertimeSummaryRow totals = calculateTotals(rows);

		request.setAttribute("rows", rows);
		request.setAttribute("topEmployees", topEmployees);
		request.setAttribute("departments", departments);
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getOvertimeYears()));
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("validationErrors", validationErrors);
		request.setAttribute("totals", totals);
		request.setAttribute("maxOtHours", findMaxOtHours(rows));
		request.setAttribute("defaultOtRate", "1.5");
		request.setAttribute("defaultWorkDays", "26");
		request.setAttribute("defaultHoursPerDay", "8");

		request.getRequestDispatcher("/views/report/report-overtime.jsp").forward(request, response);
	}

	private int parseYear(String value, int fallback, List<String> errors) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		try {
			int year = Integer.parseInt(value);
			if (year >= 2000 && year <= 2100) {
				return year;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Năm không hợp lệ.");
		return fallback;
	}

	private Integer parseOptionalMonth(String value, List<String> errors) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			int month = Integer.parseInt(value);
			if (month >= 1 && month <= 12) {
				return month;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Tháng không hợp lệ.");
		return null;
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

	private OvertimeSummaryRow calculateTotals(List<OvertimeSummaryRow> rows) {
		OvertimeSummaryRow totals = new OvertimeSummaryRow();
		totals.setTotalOtHours(BigDecimal.ZERO);
		totals.setTotalOtCost(BigDecimal.ZERO);
		for (OvertimeSummaryRow row : rows) {
			totals.setTotalRequests(totals.getTotalRequests() + row.getTotalRequests());
			totals.setApprovedRequests(totals.getApprovedRequests() + row.getApprovedRequests());
			totals.setRejectedRequests(totals.getRejectedRequests() + row.getRejectedRequests());
			totals.setPendingRequests(totals.getPendingRequests() + row.getPendingRequests());
			totals.setTotalOtHours(totals.getTotalOtHours().add(safe(row.getTotalOtHours())));
			totals.setTotalOtCost(totals.getTotalOtCost().add(safe(row.getTotalOtCost())));
		}
		return totals;
	}

	private BigDecimal findMaxOtHours(List<OvertimeSummaryRow> rows) {
		BigDecimal max = BigDecimal.ONE;
		for (OvertimeSummaryRow row : rows) {
			if (safe(row.getTotalOtHours()).compareTo(max) > 0) {
				max = row.getTotalOtHours();
			}
		}
		return max;
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
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
