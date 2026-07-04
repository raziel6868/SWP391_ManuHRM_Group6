package controller.attendance;

import dal.AttendanceCorrectionDAO;
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
import model.User;

@WebServlet(name = "AttendanceMyServlet", urlPatterns = {"/attendance-my"})
public class AttendanceMyServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		LocalDate today = LocalDate.now();
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());

		List<AttendanceRecord> records = attendanceDAO.searchByUserAndMonth(authUser.getId(), year, month);
		System.out.println("[DEBUG] userId=" + authUser.getId() + " year=" + year + " month=" + month + " recordCount="
				+ records.size());

		request.setAttribute("records", records);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("myCorrections", correctionDAO.searchByEmployee(authUser.getId(), 0, 20));

		// Prev / next month for calendar navigation arrows
		java.time.YearMonth ym = java.time.YearMonth.of(year, month);
		java.time.YearMonth prev = ym.minusMonths(1);
		java.time.YearMonth next = ym.plusMonths(1);
		request.setAttribute("prevYear", prev.getYear());
		request.setAttribute("prevMonth", prev.getMonthValue());
		request.setAttribute("nextYear", next.getYear());
		request.setAttribute("nextMonth", next.getMonthValue());
		request.getRequestDispatcher("/views/attendance/attendance-my.jsp").forward(request, response);
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}