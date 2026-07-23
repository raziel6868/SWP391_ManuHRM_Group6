package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.AttendanceSummaryRow;
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

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");
		String departmentIdParam = request.getParameter("departmentId");

		int year = currentYear;
		Integer month = null;
		Long departmentId;
		String errorMsg = null;

		Integer parsedYear = parseInteger(yearParam);
		if (parsedYear != null && parsedYear >= MIN_REPORT_YEAR && parsedYear <= MAX_REPORT_YEAR) {
			year = parsedYear;
		} else if (yearParam != null && !yearParam.isBlank()) {
			errorMsg = appendError(errorMsg, "Năm không hợp lệ. Báo cáo đang dùng năm hiện tại.");
		}

		Integer parsedMonth = parseInteger(monthParam);
		if (parsedMonth != null && parsedMonth >= 1 && parsedMonth <= 12) {
			month = parsedMonth;
		} else if (monthParam != null && !monthParam.isBlank()) {
			errorMsg = appendError(errorMsg, "Tháng không hợp lệ. Báo cáo đang hiển thị cả năm.");
		}

		departmentId = parseDepartmentId(departmentIdParam);
		if (departmentIdParam != null && !departmentIdParam.isBlank() && departmentId == null) {
			errorMsg = appendError(errorMsg, "Phòng ban không hợp lệ. Báo cáo đang hiển thị tất cả phòng ban.");
		}

		if (departmentId != null) {
			Department department = departmentDAO.getById(departmentId);
			if (department == null || !Boolean.TRUE.equals(department.getIsActive())) {
				departmentId = null;
				errorMsg = appendError(errorMsg,
						"Phòng ban không tồn tại hoặc đã bị vô hiệu hóa. Báo cáo đang hiển thị tất cả phòng ban.");
			}
		}

		List<AttendanceSummaryRow> rows = reportDAO.getAttendanceSummary(year, month, departmentId);
		List<Department> departments = departmentDAO.getActiveDepartments();
		AttendanceSummary summary = summarize(rows);

		request.setAttribute("rows", rows);
		request.setAttribute("departments", departments);
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getAttendanceYears()));
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("summaryAttendanceRate", summary.attendanceRate);
		request.setAttribute("summaryAttendanceRateBarWidth", clampPercentage(summary.attendanceRate));
		request.setAttribute("summaryExpectedWorkDays", summary.expectedWorkDays);
		request.setAttribute("summaryActualWorkDays", summary.actualWorkDays);
		request.setAttribute("summaryAbsentDays", summary.absentDays);
		request.setAttribute("summaryLateCount", summary.lateCount);
		request.setAttribute("yearlyExpectedDaysNote", month == null);
		request.setAttribute("hasAttendanceRecords", summary.actualWorkDays + summary.absentDays > 0);
		request.setAttribute("errorMsg", errorMsg);

		request.getRequestDispatcher("/views/report/report-attendance.jsp").forward(request, response);
	}

	private Integer parseInteger(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(rawValue.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Long parseDepartmentId(String rawDepartmentId) {
		if (rawDepartmentId == null || rawDepartmentId.isBlank()) {
			return null;
		}
		try {
			long departmentId = Long.parseLong(rawDepartmentId.trim());
			return departmentId > 0 ? departmentId : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private AttendanceSummary summarize(List<AttendanceSummaryRow> rows) {
		AttendanceSummary summary = new AttendanceSummary();
		for (AttendanceSummaryRow row : rows) {
			summary.expectedWorkDays += row.getExpectedWorkDays();
			summary.actualWorkDays += row.getActualWorkDays();
			summary.absentDays += row.getAbsentDays();
			summary.lateCount += row.getLateCount();
		}
		summary.attendanceRate = calculatePercentage(summary.actualWorkDays, summary.expectedWorkDays);
		return summary;
	}

	private BigDecimal calculatePercentage(int value, int total) {
		if (value <= 0 || total <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2,
				RoundingMode.HALF_UP);
	}

	private BigDecimal clampPercentage(BigDecimal percentage) {
		if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}
		return percentage;
	}

	private String appendError(String currentError, String newError) {
		if (currentError == null || currentError.isBlank()) {
			return newError;
		}
		return currentError + " " + newError;
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

	private static class AttendanceSummary {
		private int expectedWorkDays;
		private int actualWorkDays;
		private int absentDays;
		private int lateCount;
		private BigDecimal attendanceRate = BigDecimal.ZERO;
	}
}
