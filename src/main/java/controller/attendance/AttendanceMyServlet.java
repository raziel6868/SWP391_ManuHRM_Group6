package controller.attendance;

import java.io.IOException;
import java.util.List;
import dal.AttendanceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.AttendanceRecord;
import model.Permission;
import model.User;

@WebServlet(name = "AttendanceMyServlet", urlPatterns = {"/attendance-my"})
public class AttendanceMyServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		if (!hasPermission(session, "ATTENDANCE_MY_VIEW")) {
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

		List<AttendanceRecord> records = attendanceDAO.searchByUserAndMonth(authUser.getId(), year, month);

		request.setAttribute("records", records);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);

		request.getRequestDispatcher("/views/attendance/attendance-my.jsp").forward(request, response);
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
}
