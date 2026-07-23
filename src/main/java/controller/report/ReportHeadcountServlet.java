package controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
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

@WebServlet(name = "ReportHeadcountServlet", urlPatterns = {"/report-headcount"})
public class ReportHeadcountServlet extends HttpServlet {

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

		String errorMsg = null;
		Long departmentId = parseDepartmentId(request.getParameter("departmentId"));
		if (request.getParameter("departmentId") != null && !request.getParameter("departmentId").isBlank()
				&& departmentId == null) {
			errorMsg = "Phòng ban không hợp lệ. Báo cáo đang hiển thị tất cả phòng ban.";
		}

		if (departmentId != null) {
			Department department = departmentDAO.getById(departmentId);
			if (department == null || !Boolean.TRUE.equals(department.getIsActive())) {
				departmentId = null;
				errorMsg = "Phòng ban không tồn tại hoặc đã bị vô hiệu hóa. Báo cáo đang hiển thị tất cả phòng ban.";
			}
		}

		List<HeadcountRow> rows = reportDAO.getHeadcount(departmentId, true);
		List<Department> departments = departmentDAO.getActiveDepartments();
		HeadcountSummary summary = summarize(rows);

		request.setAttribute("rows", rows);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("totalActiveEmployees", summary.totalActiveEmployees);
		request.setAttribute("totalOfficeEmployees", summary.totalOfficeEmployees);
		request.setAttribute("totalWorkerEmployees", summary.totalWorkerEmployees);
		request.setAttribute("activeDepartmentCount", summary.activeDepartmentCount);
		request.setAttribute("officePercentage",
				calculatePercentage(summary.totalOfficeEmployees, summary.totalActiveEmployees));
		request.setAttribute("workerPercentage",
				calculatePercentage(summary.totalWorkerEmployees, summary.totalActiveEmployees));
		request.setAttribute("timelineDataLimited", true);
		request.setAttribute("errorMsg", errorMsg);

		request.getRequestDispatcher("/views/report/report-headcount.jsp").forward(request, response);
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

	private static class HeadcountSummary {
		private int totalActiveEmployees;
		private int totalOfficeEmployees;
		private int totalWorkerEmployees;
		private int activeDepartmentCount;
	}
}
