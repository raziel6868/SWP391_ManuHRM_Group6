package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.LeaveSummaryRow;
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

@WebServlet(name = "ReportLeaveServlet", urlPatterns = {"/report-leave"})
public class ReportLeaveServlet extends HttpServlet {

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

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_LEAVE")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);

		String yearParam = request.getParameter("year");
		String departmentIdParam = request.getParameter("departmentId");

		int year = currentYear;
		Long departmentId;
		String errorMsg = null;

		Integer parsedYear = parseInteger(yearParam);
		if (parsedYear != null && parsedYear >= MIN_REPORT_YEAR && parsedYear <= MAX_REPORT_YEAR) {
			year = parsedYear;
		} else if (yearParam != null && !yearParam.isBlank()) {
			errorMsg = appendError(errorMsg, "Năm không hợp lệ. Báo cáo đang dùng năm hiện tại.");
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

		List<LeaveSummaryRow> rows = reportDAO.getLeaveUtilization(year, departmentId);
		prepareApprovedDaysBars(rows);
		List<Department> departments = departmentDAO.getActiveDepartments();
		LeaveSummary summary = summarize(rows);

		request.setAttribute("rows", rows);
		request.setAttribute("departments", departments);
		request.setAttribute("yearOptions", YearOptionUtil.dataYearsWithCurrent(reportDAO.getLeaveYears()));
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("summaryTotalRequests", summary.totalRequests);
		request.setAttribute("summaryApprovedRequests", summary.approvedRequests);
		request.setAttribute("summaryPendingRequests", summary.pendingRequests);
		request.setAttribute("summaryRejectedRequests", summary.rejectedRequests);
		request.setAttribute("summaryCancelledRequests", summary.cancelledRequests);
		request.setAttribute("summaryApprovedDays", summary.approvedDays);
		request.setAttribute("summaryAverageApprovedDays", summary.averageApprovedDays);
		request.setAttribute("summaryApprovedPercentage",
				calculatePercentage(summary.approvedRequests, summary.totalRequests));
		request.setAttribute("summaryPendingPercentage",
				calculatePercentage(summary.pendingRequests, summary.totalRequests));
		request.setAttribute("summaryRejectedPercentage",
				calculatePercentage(summary.rejectedRequests, summary.totalRequests));
		request.setAttribute("summaryCancelledPercentage",
				calculatePercentage(summary.cancelledRequests, summary.totalRequests));
		request.setAttribute("hasCancelledRequests", summary.cancelledRequests > 0);
		request.setAttribute("errorMsg", errorMsg);

		request.getRequestDispatcher("/views/report/report-leave.jsp").forward(request, response);
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

	private void prepareApprovedDaysBars(List<LeaveSummaryRow> rows) {
		BigDecimal maxApprovedDays = BigDecimal.ZERO;
		for (LeaveSummaryRow row : rows) {
			if (row.getApprovedDays().compareTo(maxApprovedDays) > 0) {
				maxApprovedDays = row.getApprovedDays();
			}
		}
		for (LeaveSummaryRow row : rows) {
			if (maxApprovedDays.compareTo(BigDecimal.ZERO) > 0) {
				row.setApprovedDaysBarWidth(row.getApprovedDays().multiply(BigDecimal.valueOf(100))
						.divide(maxApprovedDays, 2, RoundingMode.HALF_UP));
			}
		}
	}

	private LeaveSummary summarize(List<LeaveSummaryRow> rows) {
		LeaveSummary summary = new LeaveSummary();
		for (LeaveSummaryRow row : rows) {
			summary.totalRequests += row.getTotalRequests();
			summary.approvedRequests += row.getApprovedRequests();
			summary.pendingRequests += row.getPendingRequests();
			summary.rejectedRequests += row.getRejectedRequests();
			summary.cancelledRequests += row.getCancelledRequests();
			summary.approvedDays = summary.approvedDays.add(row.getApprovedDays());
		}
		if (summary.approvedRequests > 0) {
			summary.averageApprovedDays = summary.approvedDays.divide(BigDecimal.valueOf(summary.approvedRequests), 2,
					RoundingMode.HALF_UP);
		}
		return summary;
	}

	private BigDecimal calculatePercentage(int value, int total) {
		if (value <= 0 || total <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2,
				RoundingMode.HALF_UP);
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

	private static class LeaveSummary {
		private int totalRequests;
		private int approvedRequests;
		private int pendingRequests;
		private int rejectedRequests;
		private int cancelledRequests;
		private BigDecimal approvedDays = BigDecimal.ZERO;
		private BigDecimal averageApprovedDays = BigDecimal.ZERO;
	}
}
