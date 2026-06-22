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
import model.User;

@WebServlet(name = "AttendanceMyServlet", urlPatterns = {"/attendance-my"})
public class AttendanceMyServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

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

		request.setAttribute("records", records);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
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
