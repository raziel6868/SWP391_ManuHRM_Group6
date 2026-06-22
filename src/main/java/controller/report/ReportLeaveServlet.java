package controller.report;

import java.io.IOException;
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

@WebServlet(name = "ReportLeaveServlet", urlPatterns = {"/report-leave"})
public class ReportLeaveServlet extends HttpServlet {

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

		java.util.Calendar now = java.util.Calendar.getInstance();
		int currentYear = now.get(java.util.Calendar.YEAR);

		String yearParam = request.getParameter("year");
		String departmentIdParam = request.getParameter("departmentId");

		int year = currentYear;
		Long departmentId = null;

		if (yearParam != null && !yearParam.isEmpty()) {
			try {
				year = Integer.parseInt(yearParam);
			} catch (NumberFormatException e) {
			}
		}
		if (departmentIdParam != null && !departmentIdParam.isEmpty()) {
			try {
				departmentId = Long.parseLong(departmentIdParam);
			} catch (NumberFormatException e) {
			}
		}

		List<LeaveSummaryRow> rows = reportDAO.getLeaveUtilization(year, departmentId);
		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("rows", rows);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedDepartmentId", departmentId);

		request.getRequestDispatcher("/views/report/report-leave.jsp").forward(request, response);
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
