package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.HeadcountMovementRow;
import dto.HeadcountMovementStats;
import dto.HeadcountRow;
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

@WebServlet(name = "ReportHeadcountServlet", urlPatterns = {"/report-headcount"})
public class ReportHeadcountServlet extends HttpServlet {

	private static final int MIN_REPORT_YEAR = 2020;
	private static final int MAX_REPORT_YEAR = 2030;
	private static final String PERIOD_MONTH = "month";
	private static final String PERIOD_QUARTER = "quarter";

	private final ReportDAO reportDAO = new ReportDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_HEADCOUNT")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);
		int currentMonth = now.get(Calendar.MONTH) + 1;
		int currentQuarter = ((currentMonth - 1) / 3) + 1;

		List<String> validationErrors = new ArrayList<>();
		String periodType = normalizePeriodType(request.getParameter("periodType"), validationErrors);
		int year = parseYear(request.getParameter("year"), currentYear, validationErrors);
		int month = parseMonth(request.getParameter("month"), currentMonth, validationErrors);
		int quarter = parseQuarter(request.getParameter("quarter"), currentQuarter, validationErrors);
		Long departmentId = parseDepartmentId(request.getParameter("departmentId"), validationErrors);
		String employeeType = normalizeEmployeeType(request.getParameter("employeeType"), validationErrors);
		String movementStatus = normalizeMovementStatus(request.getParameter("movementStatus"), validationErrors);

		if (departmentId != null) {
			Department department = departmentDAO.getById(departmentId);
			if (department == null || !Boolean.TRUE.equals(department.getIsActive())) {
				departmentId = null;
				validationErrors
						.add("Phòng ban không tồn tại hoặc đã bị vô hiệu hóa. Báo cáo đang hiển thị tất cả phòng ban.");
			}
		}

		ReportPeriod period = resolvePeriod(periodType, year, month, quarter);
		List<HeadcountRow> rows = reportDAO.getHeadcount(departmentId, true, employeeType);
		HeadcountSummary summary = summarize(rows);
		HeadcountMovementStats movementStats = reportDAO.getHeadcountMovementStats(period.startDate, period.endDate,
				departmentId, employeeType);
		List<HeadcountMovementRow> movementRows = reportDAO.getHeadcountMovementRows(period.startDate, period.endDate,
				departmentId, employeeType, movementStatus);

		request.setAttribute("rows", rows);
		request.setAttribute("movementRows", movementRows);
		request.setAttribute("movementStats", movementStats);
		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getHeadcountYears()));
		request.setAttribute("selectedPeriodType", periodType);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedQuarter", quarter);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("selectedEmployeeType", employeeType);
		request.setAttribute("selectedMovementStatus", movementStatus);
		request.setAttribute("periodLabel", period.label);
		request.setAttribute("periodStart", period.startDate);
		request.setAttribute("periodEnd", period.endDate);
		request.setAttribute("totalActiveEmployees", summary.totalActiveEmployees);
		request.setAttribute("totalOfficeEmployees", summary.totalOfficeEmployees);
		request.setAttribute("totalWorkerEmployees", summary.totalWorkerEmployees);
		request.setAttribute("activeDepartmentCount", summary.activeDepartmentCount);
		request.setAttribute("officePercentage",
				calculatePercentage(summary.totalOfficeEmployees, summary.totalActiveEmployees));
		request.setAttribute("workerPercentage",
				calculatePercentage(summary.totalWorkerEmployees, summary.totalActiveEmployees));
		request.setAttribute("missingContractRows", countRowsByStatus(movementRows, "NO_CONTRACT"));
		request.setAttribute("errorMsg", String.join(" ", validationErrors));

		request.getRequestDispatcher("/views/report/report-headcount.jsp").forward(request, response);
	}

	private String normalizePeriodType(String rawPeriodType, List<String> errors) {
		if (rawPeriodType == null || rawPeriodType.isBlank()) {
			return PERIOD_MONTH;
		}
		String periodType = rawPeriodType.trim().toLowerCase();
		if (PERIOD_MONTH.equals(periodType) || PERIOD_QUARTER.equals(periodType)) {
			return periodType;
		}
		errors.add("Kỳ báo cáo không hợp lệ. Báo cáo đang dùng kỳ theo tháng.");
		return PERIOD_MONTH;
	}

	private int parseYear(String rawYear, int fallback, List<String> errors) {
		if (rawYear == null || rawYear.isBlank()) {
			return fallback;
		}
		try {
			int year = Integer.parseInt(rawYear.trim());
			if (year >= MIN_REPORT_YEAR && year <= MAX_REPORT_YEAR) {
				return year;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Năm không hợp lệ. Báo cáo đang dùng năm hiện tại.");
		return fallback;
	}

	private int parseMonth(String rawMonth, int fallback, List<String> errors) {
		if (rawMonth == null || rawMonth.isBlank()) {
			return fallback;
		}
		try {
			int month = Integer.parseInt(rawMonth.trim());
			if (month >= 1 && month <= 12) {
				return month;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Tháng không hợp lệ. Báo cáo đang dùng tháng hiện tại.");
		return fallback;
	}

	private int parseQuarter(String rawQuarter, int fallback, List<String> errors) {
		if (rawQuarter == null || rawQuarter.isBlank()) {
			return fallback;
		}
		try {
			int quarter = Integer.parseInt(rawQuarter.trim());
			if (quarter >= 1 && quarter <= 4) {
				return quarter;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Quý không hợp lệ. Báo cáo đang dùng quý hiện tại.");
		return fallback;
	}

	private Long parseDepartmentId(String rawDepartmentId, List<String> errors) {
		if (rawDepartmentId == null || rawDepartmentId.isBlank()) {
			return null;
		}
		try {
			long departmentId = Long.parseLong(rawDepartmentId.trim());
			if (departmentId > 0) {
				return departmentId;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Phòng ban không hợp lệ. Báo cáo đang hiển thị tất cả phòng ban.");
		return null;
	}

	private String normalizeEmployeeType(String rawEmployeeType, List<String> errors) {
		if (rawEmployeeType == null || rawEmployeeType.isBlank()) {
			return null;
		}
		String employeeType = rawEmployeeType.trim().toUpperCase();
		if ("OFFICE".equals(employeeType) || "WORKER".equals(employeeType)) {
			return employeeType;
		}
		errors.add("Phân loại nhân sự không hợp lệ. Báo cáo đang hiển thị tất cả phân loại.");
		return null;
	}

	private String normalizeMovementStatus(String rawStatus, List<String> errors) {
		if (rawStatus == null || rawStatus.isBlank()) {
			return null;
		}
		String status = rawStatus.trim().toUpperCase();
		if ("NEW".equals(status) || "PROBATION".equals(status) || "OFFICIAL".equals(status) || "SEASONAL".equals(status)
				|| "TERMINATED".equals(status) || "NO_CONTRACT".equals(status)) {
			return status;
		}
		errors.add("Trạng thái biến động không hợp lệ. Báo cáo đang hiển thị tất cả trạng thái.");
		return null;
	}

	private ReportPeriod resolvePeriod(String periodType, int year, int month, int quarter) {
		if (PERIOD_QUARTER.equals(periodType)) {
			int startMonth = ((quarter - 1) * 3) + 1;
			LocalDate start = LocalDate.of(year, startMonth, 1);
			LocalDate end = YearMonth.of(year, startMonth + 2).atEndOfMonth();
			return new ReportPeriod(start, end, "Quý " + quarter + " / " + year);
		}
		YearMonth yearMonth = YearMonth.of(year, month);
		String label = String.format("Tháng %02d / %d", month, year);
		return new ReportPeriod(yearMonth.atDay(1), yearMonth.atEndOfMonth(), label);
	}

	private HeadcountSummary summarize(List<HeadcountRow> rows) {
		HeadcountSummary summary = new HeadcountSummary();
		for (HeadcountRow row : rows) {
			summary.totalActiveEmployees += row.getActiveEmployees();
			summary.totalOfficeEmployees += row.getOfficeEmployees();
			summary.totalWorkerEmployees += row.getWorkerEmployees();
			if (row.getDepartmentId() != null && row.getActiveEmployees() > 0) {
				summary.activeDepartmentCount++;
			}
		}
		return summary;
	}

	private int countRowsByStatus(List<HeadcountMovementRow> rows, String status) {
		int count = 0;
		for (HeadcountMovementRow row : rows) {
			if (status.equals(row.getMovementStatus())) {
				count++;
			}
		}
		return count;
	}

	private BigDecimal calculatePercentage(int value, int total) {
		if (value <= 0 || total <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2,
				RoundingMode.HALF_UP);
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

	private static class ReportPeriod {
		private final LocalDate startDate;
		private final LocalDate endDate;
		private final String label;

		private ReportPeriod(LocalDate startDate, LocalDate endDate, String label) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.label = label;
		}
	}

	private static class HeadcountSummary {
		private int totalActiveEmployees;
		private int totalOfficeEmployees;
		private int totalWorkerEmployees;
		private int activeDepartmentCount;
	}
}
