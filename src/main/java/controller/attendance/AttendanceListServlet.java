package controller.attendance;

import dal.AttendanceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import model.AttendanceRecord;
import model.Permission;

@WebServlet(name = "AttendanceListServlet", urlPatterns = {"/attendance-list"})
public class AttendanceListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		LocalDate today = LocalDate.now();
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());
		Long departmentId = parseLong(request.getParameter("departmentId"));
		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<AttendanceRecord> records = attendanceDAO.searchByMonth(year, month, departmentId, offset, PAGE_SIZE);
		int totalRecords = attendanceDAO.countByMonth(year, month, departmentId);
		int totalPages = totalRecords / PAGE_SIZE;
		if (totalRecords % PAGE_SIZE != 0) {
			totalPages++;
		}
		if (totalPages == 0) {
			totalPages = 1;
		}

		if (currentPage > totalPages) {
			currentPage = totalPages;
			offset = (currentPage - 1) * PAGE_SIZE;
			records = attendanceDAO.searchByMonth(year, month, departmentId, offset, PAGE_SIZE);
		}

		request.setAttribute("records", records);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);
		request.setAttribute("canImport", hasPermission(session, "ATTENDANCE_IMPORT"));
		request.getRequestDispatcher("/views/attendance/attendance-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (permissionCode.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}

	private int parseInt(String value, int defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private int parsePage(String pageParam) {
		if (pageParam == null || pageParam.isBlank()) {
			return 1;
		}
		try {
			return Math.max(1, Integer.parseInt(pageParam));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
