package controller.report;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.AttendanceOtEmployeeRow;
import dto.AttendanceOtReportStats;
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

@WebServlet(name = "ReportAttendanceServlet", urlPatterns = {"/report-attendance"})
public class ReportAttendanceServlet extends HttpServlet {

	private static final int MIN_REPORT_YEAR = 2020;
	private static final int MAX_REPORT_YEAR = 2030;
	private static final String PERIOD_MONTH = "month";
	private static final String PERIOD_QUARTER = "quarter";
	private static final String PERIOD_YEAR = "year";

	private final ReportDAO reportDAO = new ReportDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_ATTENDANCE")) {
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

		if (departmentId != null) {
			Department department = departmentDAO.getById(departmentId);
			if (department == null || !Boolean.TRUE.equals(department.getIsActive())) {
				departmentId = null;
				validationErrors
						.add("Phòng ban không tồn tại hoặc đã bị vô hiệu hóa. Báo cáo đang hiển thị tất cả phòng ban.");
			}
		}

		ReportPeriod period = resolvePeriod(periodType, year, month, quarter);
		int expectedWorkDays = countWeekdays(period.startDate, period.endDate);
		List<AttendanceOtEmployeeRow> employeeRows = reportDAO.getAttendanceOtEmployeeRows(period.startDate,
				period.endDate, expectedWorkDays, departmentId);
		AttendanceOtReportStats stats = summarize(employeeRows, expectedWorkDays);

		request.setAttribute("employeeRows", employeeRows);
		request.setAttribute("topDiligentRows", topDiligentRows(employeeRows));
		request.setAttribute("warningRows", warningRows(employeeRows));
		request.setAttribute("stats", stats);
		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getAttendanceYears()));
		request.setAttribute("selectedPeriodType", periodType);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedQuarter", quarter);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("periodLabel", period.label);
		request.setAttribute("periodStart", period.startDate);
		request.setAttribute("periodEnd", period.endDate);
		request.setAttribute("errorMsg", String.join(" ", validationErrors));

		request.getRequestDispatcher("/views/report/report-attendance.jsp").forward(request, response);
	}

	private String normalizePeriodType(String rawPeriodType, List<String> errors) {
		if (rawPeriodType == null || rawPeriodType.isBlank()) {
			return PERIOD_MONTH;
		}
		String periodType = rawPeriodType.trim().toLowerCase();
		if (PERIOD_MONTH.equals(periodType) || PERIOD_QUARTER.equals(periodType) || PERIOD_YEAR.equals(periodType)) {
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

	private ReportPeriod resolvePeriod(String periodType, int year, int month, int quarter) {
		if (PERIOD_YEAR.equals(periodType)) {
			return new ReportPeriod(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), "Năm " + year);
		}
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

	private int countWeekdays(LocalDate startDate, LocalDate endDate) {
		int weekdays = 0;
		LocalDate current = startDate;
		while (!current.isAfter(endDate)) {
			DayOfWeek dayOfWeek = current.getDayOfWeek();
			if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
				weekdays++;
			}
			current = current.plusDays(1);
		}
		return weekdays;
	}

	private AttendanceOtReportStats summarize(List<AttendanceOtEmployeeRow> employeeRows, int expectedWorkDays) {
		AttendanceOtReportStats stats = new AttendanceOtReportStats();
		stats.setExpectedWorkDaysPerEmployee(expectedWorkDays);
		for (AttendanceOtEmployeeRow row : employeeRows) {
			stats.addRow(row);
		}
		return stats;
	}

	private List<AttendanceOtEmployeeRow> topDiligentRows(List<AttendanceOtEmployeeRow> employeeRows) {
		return employeeRows.stream().filter(AttendanceOtEmployeeRow::isDiligent)
				.sorted(Comparator.comparingInt(AttendanceOtEmployeeRow::getActualWorkDays).reversed()
						.thenComparing(AttendanceOtEmployeeRow::getFullName, String.CASE_INSENSITIVE_ORDER))
				.limit(5).toList();
	}

	private List<AttendanceOtEmployeeRow> warningRows(List<AttendanceOtEmployeeRow> employeeRows) {
		return employeeRows.stream().filter(AttendanceOtEmployeeRow::isWarning).sorted((first, second) -> {
			int absenceCompare = second.getUnauthorizedAbsenceDays().compareTo(first.getUnauthorizedAbsenceDays());
			if (absenceCompare != 0) {
				return absenceCompare;
			}
			int lateCompare = Integer.compare(second.getLateCount(), first.getLateCount());
			if (lateCompare != 0) {
				return lateCompare;
			}
			return String.CASE_INSENSITIVE_ORDER.compare(first.getFullName(), second.getFullName());
		}).limit(5).toList();
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
}
