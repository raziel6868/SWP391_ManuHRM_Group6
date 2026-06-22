package controller.shiftcalendar;

import dal.ShiftAssignmentDAO;
import dal.DepartmentDAO;
import dal.ShiftDAO;
import model.ShiftAssignment;
import model.Department;
import model.Shift;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ShiftCalendarServlet", urlPatterns = {"/shift-calendar"})
public class ShiftCalendarServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");
		String departmentIdParam = request.getParameter("departmentId");

		int year = yearParam != null ? Integer.parseInt(yearParam) : LocalDate.now().getYear();
		int month = monthParam != null ? Integer.parseInt(monthParam) : LocalDate.now().getMonthValue();
		Long departmentId = departmentIdParam != null && !departmentIdParam.isBlank()
				? Long.parseLong(departmentIdParam)
				: null;

		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		List<ShiftAssignment> assignments = shiftAssignmentDAO.getByDepartmentAndDateRange(departmentId,
				Date.valueOf(startDate), Date.valueOf(endDate));

		Map<String, ShiftAssignment> assignmentMap = new HashMap<>();
		for (ShiftAssignment sa : assignments) {
			String key = sa.getUserId() + "_" + sa.getDate().toString();
			assignmentMap.put(key, sa);
		}

		List<Department> departments = departmentDAO.getActiveDepartments();
		List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);

		request.setAttribute("assignments", assignmentMap);
		request.setAttribute("departments", departments);
		request.setAttribute("shifts", shifts);
		request.setAttribute("currentYear", year);
		request.setAttribute("currentMonth", month);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("startDate", startDate);
		request.setAttribute("endDate", endDate);

		request.getRequestDispatcher("/views/shiftcalendar/shift-calendar.jsp").forward(request, response);
	}
}
