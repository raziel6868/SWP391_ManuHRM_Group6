package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import dal.ReportDAO;
import dto.PayrollEmployeeReportRow;
import dto.PayrollSummaryRow;
import dto.PayrollTrendRow;
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

	private static final String PERIOD_MONTH = "month";
	private static final String PERIOD_QUARTER = "quarter";

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
		int currentQuarter = currentQuarter(currentMonth);

		List<String> validationErrors = new ArrayList<>();
		String periodType = parsePeriodType(request.getParameter("periodType"));
		int year = parseYear(request.getParameter("year"), currentYear, validationErrors);
		int quarter = parseQuarter(request.getParameter("quarter"), currentQuarter, validationErrors);
		int month = parseMonth(request.getParameter("month"), currentMonth, validationErrors);

		PeriodRange selectedPeriod = selectedPeriod(year, periodType, quarter, month);
		PeriodRange availablePeriod = limitToAvailableMonths(selectedPeriod, currentYear, currentMonth);
		PeriodRange trendPeriod = limitToAvailableMonths(
				trendPeriod(year, periodType, selectedPeriod.startMonth, selectedPeriod.endMonth, month), currentYear,
				currentMonth);

		List<PayrollSummaryRow> rows = availablePeriod.isEmpty()
				? Collections.emptyList()
				: reportDAO.getPayrollSummary(availablePeriod.year, availablePeriod.startMonth,
						availablePeriod.endMonth);
		List<PayrollEmployeeReportRow> employeeRows = availablePeriod.isEmpty()
				? Collections.emptyList()
				: reportDAO.getPayrollEmployeeDetails(availablePeriod.year, availablePeriod.startMonth,
						availablePeriod.endMonth);
		List<PayrollTrendRow> trendRows = trendPeriod.isEmpty()
				? Collections.emptyList()
				: reportDAO.getPayrollTrend(trendPeriod.year, trendPeriod.startMonth, trendPeriod.endMonth);
		PayrollSummaryRow totals = calculateTotals(rows);

		PeriodRange previousPeriod = previousPeriod(year, periodType, quarter, month);
		PayrollSummaryRow previousTotals = calculateTotals(
				reportDAO.getPayrollSummary(previousPeriod.year, previousPeriod.startMonth, previousPeriod.endMonth));

		request.setAttribute("rows", rows);
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getPayrollYears()));
		request.setAttribute("employeeRows", employeeRows);
		request.setAttribute("trendRows", trendRows);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedQuarter", quarter);
		request.setAttribute("selectedPeriodType", periodType);
		request.setAttribute("currentYear", currentYear);
		request.setAttribute("currentMonth", currentMonth);
		request.setAttribute("periodLabel", selectedPeriod.label);
		request.setAttribute("trendPeriodLabel",
				trendPeriod.isEmpty() ? "Không có dữ liệu trong kỳ đã chọn" : buildTrendPeriodLabel(trendPeriod));
		request.setAttribute("previousPeriodLabel", previousPeriod.label);
		request.setAttribute("validationErrors", validationErrors);
		request.setAttribute("totals", totals);
		request.setAttribute("maxTrendGrossIncome", findMaxTrendGrossIncome(trendRows));
		request.setAttribute("grossIncomeChangeLabel",
				buildChangeLabel(totals.getGrossIncome(), previousTotals.getGrossIncome()));
		request.setAttribute("grossIncomeChangeClass",
				buildChangeClass(totals.getGrossIncome(), previousTotals.getGrossIncome(), true));
		request.setAttribute("netSalaryChangeLabel",
				buildChangeLabel(totals.getNetSalary(), previousTotals.getNetSalary()));
		request.setAttribute("netSalaryChangeClass",
				buildChangeClass(totals.getNetSalary(), previousTotals.getNetSalary(), true));
		request.setAttribute("deductionsChangeLabel",
				buildChangeLabel(totals.getDeductions(), previousTotals.getDeductions()));
		request.setAttribute("deductionsChangeClass",
				buildChangeClass(totals.getDeductions(), previousTotals.getDeductions(), false));
		request.setAttribute("overtimeCostChangeLabel",
				buildChangeLabel(totals.getTotalOtCost(), previousTotals.getTotalOtCost()));
		request.setAttribute("overtimeCostChangeClass",
				buildChangeClass(totals.getTotalOtCost(), previousTotals.getTotalOtCost(), false));
		request.setAttribute("payrollSourceMessage", "Số liệu được tính theo logic bảng lương hiện tại của hệ thống.");

		request.getRequestDispatcher("/views/report/report-payroll.jsp").forward(request, response);
	}

	private String parsePeriodType(String value) {
		if (PERIOD_QUARTER.equals(value) || PERIOD_MONTH.equals(value)) {
			return value;
		}
		return PERIOD_MONTH;
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

	private int parseQuarter(String value, int fallback, List<String> errors) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		try {
			int quarter = Integer.parseInt(value);
			if (quarter >= 1 && quarter <= 4) {
				return quarter;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Quý không hợp lệ.");
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

	private int currentQuarter(int currentMonth) {
		return ((currentMonth - 1) / 3) + 1;
	}

	private PeriodRange selectedPeriod(int year, String periodType, int quarter, int month) {
		if (PERIOD_QUARTER.equals(periodType)) {
			int startMonth = (quarter - 1) * 3 + 1;
			return new PeriodRange(year, startMonth, startMonth + 2, "Quý " + quarter + "/" + year);
		}
		return new PeriodRange(year, month, month, "Tháng " + month + "/" + year);
	}

	private PeriodRange trendPeriod(int year, String periodType, int startMonth, int endMonth, int month) {
		if (PERIOD_MONTH.equals(periodType)) {
			return new PeriodRange(year, Math.max(1, month - 5), month, null);
		}
		return new PeriodRange(year, startMonth, endMonth, null);
	}

	private PeriodRange limitToAvailableMonths(PeriodRange period, int currentYear, int currentMonth) {
		if (period.year > currentYear) {
			return new PeriodRange(period.year, 1, 0, period.label);
		}
		if (period.year < currentYear) {
			return period;
		}
		if (period.startMonth > currentMonth) {
			return new PeriodRange(period.year, 1, 0, period.label);
		}
		return new PeriodRange(period.year, period.startMonth, Math.min(period.endMonth, currentMonth), period.label);
	}

	private PeriodRange previousPeriod(int year, String periodType, int quarter, int month) {
		if (PERIOD_QUARTER.equals(periodType)) {
			if (quarter == 1) {
				return new PeriodRange(year - 1, 10, 12, "quý trước");
			}
			int previousStart = (quarter - 2) * 3 + 1;
			return new PeriodRange(year, previousStart, previousStart + 2, "quý trước");
		}
		if (month == 1) {
			return new PeriodRange(year - 1, 12, 12, "tháng trước");
		}
		return new PeriodRange(year, month - 1, month - 1, "tháng trước");
	}

	private String buildTrendPeriodLabel(PeriodRange period) {
		if (period.startMonth == period.endMonth) {
			return "Tháng " + period.endMonth + "/" + period.year;
		}
		return "Tháng " + period.startMonth + " - " + period.endMonth + "/" + period.year;
	}

	private String buildChangeLabel(BigDecimal current, BigDecimal previous) {
		BigDecimal safeCurrent = safe(current);
		BigDecimal safePrevious = safe(previous);
		if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
			return safeCurrent.compareTo(BigDecimal.ZERO) == 0 ? "Không đổi" : "Kỳ trước chưa có dữ liệu";
		}
		BigDecimal change = safeCurrent.subtract(safePrevious).multiply(BigDecimal.valueOf(100)).divide(safePrevious, 1,
				RoundingMode.HALF_UP);
		String sign = change.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
		return sign + change + "%";
	}

	private String buildChangeClass(BigDecimal current, BigDecimal previous, boolean higherIsGood) {
		BigDecimal safeCurrent = safe(current);
		BigDecimal safePrevious = safe(previous);
		if (safePrevious.compareTo(BigDecimal.ZERO) == 0 || safeCurrent.compareTo(safePrevious) == 0) {
			return "text-on-surface-variant";
		}
		boolean increased = safeCurrent.compareTo(safePrevious) > 0;
		boolean positive = higherIsGood ? increased : !increased;
		return positive ? "text-success" : "text-danger";
	}

	private PayrollSummaryRow calculateTotals(List<PayrollSummaryRow> rows) {
		PayrollSummaryRow totals = new PayrollSummaryRow();
		totals.setTotalSalary(BigDecimal.ZERO);
		totals.setTotalAllowances(BigDecimal.ZERO);
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
			totals.setTotalOtCost(totals.getTotalOtCost().add(safe(row.getTotalOtCost())));
			totals.setGrossIncome(totals.getGrossIncome().add(safe(row.getGrossIncome())));
			totals.setEmployeeInsurance(totals.getEmployeeInsurance().add(safe(row.getEmployeeInsurance())));
			totals.setPitTax(totals.getPitTax().add(safe(row.getPitTax())));
			totals.setDeductions(totals.getDeductions().add(safe(row.getDeductions())));
			totals.setNetSalary(totals.getNetSalary().add(safe(row.getNetSalary())));
		}
		return totals;
	}

	private BigDecimal findMaxTrendGrossIncome(List<PayrollTrendRow> rows) {
		BigDecimal max = BigDecimal.ONE;
		for (PayrollTrendRow row : rows) {
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

	private static class PeriodRange {
		private final int year;
		private final int startMonth;
		private final int endMonth;
		private final String label;

		private PeriodRange(int year, int startMonth, int endMonth, String label) {
			this.year = year;
			this.startMonth = startMonth;
			this.endMonth = endMonth;
			this.label = label;
		}

		private boolean isEmpty() {
			return endMonth < startMonth;
		}
	}
}
