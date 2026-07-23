package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import dal.ReportDAO;
import dto.PayrollEmployeeReportRow;
import dto.PayrollSummaryRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;
import model.User;
import util.YearOptionUtil;

@WebServlet(name = "ReportPayrollServlet", urlPatterns = {"/report-payroll"})
public class ReportPayrollServlet extends HttpServlet {

	private final ReportDAO reportDAO = new ReportDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_PAYROLL")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);
		int currentMonth = now.get(Calendar.MONTH) + 1;

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");
		List<String> validationErrors = new ArrayList<>();

		int year = parseYear(yearParam, currentYear, validationErrors);
		int month = parseMonth(monthParam, currentMonth, validationErrors);

		List<PayrollSummaryRow> rows = reportDAO.getPayrollSummary(year, month);
		List<PayrollEmployeeReportRow> employeeRows = reportDAO.getPayrollEmployeeDetails(year, month);
		PayrollSummaryRow totals = calculateTotals(rows);

		request.setAttribute("rows", rows);
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getPayrollYears()));
		request.setAttribute("employeeRows", employeeRows);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("validationErrors", validationErrors);
		request.setAttribute("totals", totals);
		request.setAttribute("maxGrossIncome", findMaxGrossIncome(rows));
		request.setAttribute("payrollSourceMessage",
				"Số liệu được tính theo logic payroll preview hiện tại của hệ thống.");

		request.getRequestDispatcher("/views/report/report-payroll.jsp").forward(request, response);
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

	private int parseMonth(String value, int fallback, List<String> errors) {
		if (value == null || value.isBlank()) {
			return fallback;
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
		return fallback;
	}

	private PayrollSummaryRow calculateTotals(List<PayrollSummaryRow> rows) {
		PayrollSummaryRow totals = new PayrollSummaryRow();
		totals.setTotalSalary(BigDecimal.ZERO);
		totals.setTotalAllowances(BigDecimal.ZERO);
		totals.setTotalAttendanceBonus(BigDecimal.ZERO);
		totals.setTotalOtCost(BigDecimal.ZERO);
		totals.setGrossIncome(BigDecimal.ZERO);
		totals.setEmployeeInsurance(BigDecimal.ZERO);
		totals.setPitTax(BigDecimal.ZERO);
		totals.setDeductions(BigDecimal.ZERO);
		totals.setNetSalary(BigDecimal.ZERO);
		for (PayrollSummaryRow row : rows) {
			totals.setEmployeeCount(totals.getEmployeeCount() + row.getEmployeeCount());
			totals.setTotalSalary(totals.getTotalSalary().add(safe(row.getTotalSalary())));
			totals.setTotalAllowances(totals.getTotalAllowances().add(safe(row.getTotalAllowances())));
			totals.setTotalAttendanceBonus(totals.getTotalAttendanceBonus().add(safe(row.getTotalAttendanceBonus())));
			totals.setTotalOtCost(totals.getTotalOtCost().add(safe(row.getTotalOtCost())));
			totals.setGrossIncome(totals.getGrossIncome().add(safe(row.getGrossIncome())));
			totals.setEmployeeInsurance(totals.getEmployeeInsurance().add(safe(row.getEmployeeInsurance())));
			totals.setPitTax(totals.getPitTax().add(safe(row.getPitTax())));
			totals.setDeductions(totals.getDeductions().add(safe(row.getDeductions())));
			totals.setNetSalary(totals.getNetSalary().add(safe(row.getNetSalary())));
		}
		return totals;
	}

	private BigDecimal findMaxGrossIncome(List<PayrollSummaryRow> rows) {
		BigDecimal max = BigDecimal.ONE;
		for (PayrollSummaryRow row : rows) {
			if (safe(row.getGrossIncome()).compareTo(max) > 0) {
				max = row.getGrossIncome();
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
