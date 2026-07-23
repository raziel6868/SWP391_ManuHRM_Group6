package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import dal.LeaveTypeDAO;
import dal.ReportDAO;
import dto.LeaveEmployeeReportRow;
import dto.LeaveReportStats;
import dto.LeaveTypeUsageRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.LeaveType;
import model.Permission;
import model.User;
import util.YearOptionUtil;

@WebServlet(name = "ReportLeaveServlet", urlPatterns = {"/report-leave"})
public class ReportLeaveServlet extends HttpServlet {

	private static final int MIN_REPORT_YEAR = 2020;
	private static final int MAX_REPORT_YEAR = 2030;
	private static final String PERIOD_MONTH = "month";
	private static final String PERIOD_QUARTER = "quarter";

	private final ReportDAO reportDAO = new ReportDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_LEAVE")) {
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
		Long leaveTypeId = parseLeaveTypeId(request.getParameter("leaveTypeId"), validationErrors);

		if (leaveTypeId != null) {
			LeaveType leaveType = leaveTypeDAO.getById(leaveTypeId);
			if (leaveType == null || !Boolean.TRUE.equals(leaveType.getIsActive())) {
				leaveTypeId = null;
				validationErrors
						.add("Loại phép không tồn tại hoặc đã bị vô hiệu hóa. Báo cáo đang hiển thị tất cả loại phép.");
			}
		}

		ReportPeriod period = resolvePeriod(periodType, year, month, quarter);
		List<LeaveEmployeeReportRow> employeeRows = reportDAO.getLeaveEmployeeReportRows(year, period.startDate,
				period.endDate, leaveTypeId);
		List<LeaveTypeUsageRow> leaveTypeUsageRows = reportDAO.getLeaveTypeUsageRows(period.startDate, period.endDate,
				leaveTypeId);
		LeaveReportStats stats = summarize(employeeRows);

		request.setAttribute("employeeRows", employeeRows);
		request.setAttribute("leaveTypeUsageRows", leaveTypeUsageRows);
		request.setAttribute("stats", stats);
		request.setAttribute("leaveTypes", leaveTypeDAO.searchLeaveTypes(null, null, true, 0, 1000));
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getLeaveYears()));
		request.setAttribute("selectedPeriodType", periodType);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedQuarter", quarter);
		request.setAttribute("selectedLeaveTypeId", leaveTypeId);
		request.setAttribute("periodLabel", period.label);
		request.setAttribute("periodStart", period.startDate);
		request.setAttribute("periodEnd", period.endDate);
		request.setAttribute("errorMsg", String.join(" ", validationErrors));

		request.getRequestDispatcher("/views/report/report-leave.jsp").forward(request, response);
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

	private Long parseLeaveTypeId(String rawLeaveTypeId, List<String> errors) {
		if (rawLeaveTypeId == null || rawLeaveTypeId.isBlank()) {
			return null;
		}
		try {
			long leaveTypeId = Long.parseLong(rawLeaveTypeId.trim());
			if (leaveTypeId > 0) {
				return leaveTypeId;
			}
		} catch (NumberFormatException e) {
			// handled below
		}
		errors.add("Loại phép không hợp lệ. Báo cáo đang hiển thị tất cả loại phép.");
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

	private LeaveReportStats summarize(List<LeaveEmployeeReportRow> rows) {
		LeaveReportStats stats = new LeaveReportStats();
		BigDecimal totalRemainingDays = BigDecimal.ZERO;
		int balanceRowCount = 0;
		for (LeaveEmployeeReportRow row : rows) {
			stats.addPaidLeaveDays(row.getPaidLeaveDays());
			stats.addUnpaidLeaveDays(row.getUnpaidLeaveDays());
			stats.addTotalLeaveDays(row.getPaidLeaveDays().add(row.getUnpaidLeaveDays()));
			if (row.isHasAnnualLeaveBalance()) {
				totalRemainingDays = totalRemainingDays.add(row.getAnnualLeaveRemainingDays());
				balanceRowCount++;
			} else {
				stats.incrementMissingAnnualLeaveBalanceCount();
			}
		}
		if (balanceRowCount > 0) {
			stats.setAverageAnnualLeaveRemainingDays(
					totalRemainingDays.divide(BigDecimal.valueOf(balanceRowCount), 2, RoundingMode.HALF_UP));
		}
		return stats;
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
