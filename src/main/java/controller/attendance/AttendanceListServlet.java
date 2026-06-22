package controller.attendance;

import java.io.IOException;
import java.util.List;
import dal.AttendanceDAO;
import dal.DepartmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.AttendanceRecord;
import model.Department;
import model.Permission;

@WebServlet(name = "AttendanceListServlet", urlPatterns = {"/attendance-list"})
public class AttendanceListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 20;

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "ATTENDANCE_VIEW")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		int currentYear = java.time.Year.now().getValue();
		int currentMonth = java.time.LocalDate.now().getMonthValue();

		String yearStr = request.getParameter("year");
		String monthStr = request.getParameter("month");
		String deptIdStr = request.getParameter("departmentId");
		String pageStr = request.getParameter("page");

		int year, month;
		try {
			year = (yearStr != null && !yearStr.trim().isEmpty()) ? Integer.parseInt(yearStr.trim()) : currentYear;
		} catch (NumberFormatException e) {
			year = currentYear;
		}
		try {
			month = (monthStr != null && !monthStr.trim().isEmpty()) ? Integer.parseInt(monthStr.trim()) : currentMonth;
		} catch (NumberFormatException e) {
			month = currentMonth;
		}

		int currentPage = 1;
		try {
			currentPage = (pageStr != null && !pageStr.trim().isEmpty())
					? Math.max(1, Integer.parseInt(pageStr.trim()))
					: 1;
		} catch (NumberFormatException e) {
		}
		int offset = (currentPage - 1) * PAGE_SIZE;

		Long departmentId = parseId(deptIdStr);

		List<AttendanceRecord> records = attendanceDAO.searchByMonth(year, month, departmentId, offset, PAGE_SIZE);
		int totalCount = attendanceDAO.countRecordsByMonth(year, month, departmentId);
		int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / PAGE_SIZE) : 1;

		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("records", records);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedDepartmentId", deptIdStr != null ? deptIdStr : "");
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalCount);

		request.getRequestDispatcher("/views/attendance/attendance-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}

	private Long parseId(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
