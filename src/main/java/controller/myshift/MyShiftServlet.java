package controller.myshift;

import dal.ShiftAssignmentDAO;
import model.ShiftAssignment;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@WebServlet(name = "MyShiftServlet", urlPatterns = {"/my-shift"})
public class MyShiftServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");

		int year = yearParam != null ? Integer.parseInt(yearParam) : LocalDate.now().getYear();
		int month = monthParam != null ? Integer.parseInt(monthParam) : LocalDate.now().getMonthValue();

		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		List<ShiftAssignment> myAssignments = shiftAssignmentDAO.getByUserIdAndDateRange(authUser.getId(),
				Date.valueOf(startDate), Date.valueOf(endDate));

		int prevMonth = month == 1 ? 12 : month - 1;
		int prevYear = month == 1 ? year - 1 : year;
		int nextMonth = month == 12 ? 1 : month + 1;
		int nextYear = month == 12 ? year + 1 : year;

		request.setAttribute("myAssignments", myAssignments);
		request.setAttribute("currentYear", year);
		request.setAttribute("currentMonth", month);
		request.setAttribute("prevYear", prevYear);
		request.setAttribute("prevMonth", prevMonth);
		request.setAttribute("nextYear", nextYear);
		request.setAttribute("nextMonth", nextMonth);
		request.setAttribute("startDate", startDate);
		request.setAttribute("endDate", endDate);

		request.getRequestDispatcher("/views/myshift/my-shift.jsp").forward(request, response);
	}
}
