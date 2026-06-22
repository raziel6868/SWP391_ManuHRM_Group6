package controller.shiftassignment;

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
import java.util.List;

@WebServlet(name = "ShiftAssignmentListServlet", urlPatterns = {"/shift-assignment-list"})
public class ShiftAssignmentListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 20;
	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pageParam = request.getParameter("page");
		int page = pageParam != null ? Math.max(1, Integer.parseInt(pageParam)) : 1;
		int offset = (page - 1) * PAGE_SIZE;

		String departmentIdParam = request.getParameter("departmentId");
		String userIdParam = request.getParameter("userId");
		String shiftIdParam = request.getParameter("shiftId");
		String startDateParam = request.getParameter("startDate");
		String endDateParam = request.getParameter("endDate");

		Long departmentId = departmentIdParam != null && !departmentIdParam.isBlank()
				? Long.parseLong(departmentIdParam)
				: null;
		Long userId = userIdParam != null && !userIdParam.isBlank() ? Long.parseLong(userIdParam) : null;
		Long shiftId = shiftIdParam != null && !shiftIdParam.isBlank() ? Long.parseLong(shiftIdParam) : null;
		Date startDate = startDateParam != null && !startDateParam.isBlank()
				? Date.valueOf(LocalDate.parse(startDateParam))
				: null;
		Date endDate = endDateParam != null && !endDateParam.isBlank()
				? Date.valueOf(LocalDate.parse(endDateParam))
				: null;

		List<ShiftAssignment> assignments = shiftAssignmentDAO.searchAssignments(departmentId, userId, shiftId,
				startDate, endDate, offset, PAGE_SIZE);
		int total = shiftAssignmentDAO.countAssignments(departmentId, userId, shiftId, startDate, endDate);
		int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

		List<Department> departments = departmentDAO.getActiveDepartments();
		List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);

		request.setAttribute("assignments", assignments);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("total", total);
		request.setAttribute("departments", departments);
		request.setAttribute("shifts", shifts);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("selectedUserId", userId);
		request.setAttribute("selectedShiftId", shiftId);
		request.setAttribute("selectedStartDate", startDateParam);
		request.setAttribute("selectedEndDate", endDateParam);

		moveFlashMessage(request);

		request.getRequestDispatcher("/views/shiftassignment/shift-assignment-list.jsp").forward(request, response);
	}

	private void moveFlashMessage(HttpServletRequest request) {
		jakarta.servlet.http.HttpSession session = request.getSession();
		String successMsg = (String) session.getAttribute("successMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}
	}
}
