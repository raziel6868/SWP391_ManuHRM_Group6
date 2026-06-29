package controller.shiftcalendar;

import dal.ShiftAssignmentDAO;
import dal.DepartmentDAO;
import dal.ShiftDAO;
import dal.UserDAO;
import model.ShiftAssignment;
import model.Department;
import model.Permission;
import model.Shift;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ShiftCalendarServlet", urlPatterns = {"/shift-calendar"})
public class ShiftCalendarServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!hasPermission(request, "SHIFT_CALENDAR_VIEW")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

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
		List<User> users = userDAO.getActiveUsersForDropdown();

		request.setAttribute("assignments", assignmentMap);
		request.setAttribute("assignmentsList", assignments);
		request.setAttribute("departments", departments);
		request.setAttribute("shifts", shifts);
		request.setAttribute("users", users);
		request.setAttribute("currentYear", year);
		request.setAttribute("currentMonth", month);
		request.setAttribute("selectedDepartmentId", departmentId);
		request.setAttribute("startDate", startDate);
		request.setAttribute("endDate", endDate);

		// Import result messages
		moveFlashMessages(request);

		request.getRequestDispatcher("/views/shiftcalendar/shift-calendar.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");

		if ("assign".equals(action)) {
			if (!hasPermission(request, "SHIFT_ASSIGNMENT_ASSIGN")) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			handleAssign(request, response);
		} else if ("delete".equals(action)) {
			if (!hasPermission(request, "SHIFT_ASSIGNMENT_ASSIGN")) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			handleDelete(request, response);
		} else if ("deleteAll".equals(action)) {
			if (!hasPermission(request, "SHIFT_ASSIGNMENT_BULK")) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			handleDeleteAll(request, response);
		} else {
			response.sendRedirect(request.getContextPath() + "/shift-calendar");
		}
	}

	private void handleAssign(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String userIdParam = request.getParameter("userId");
		String shiftIdParam = request.getParameter("shiftId");
		String dateParam = request.getParameter("date");
		String departmentId = request.getParameter("departmentId");
		String year = request.getParameter("year");
		String month = request.getParameter("month");

		if (userIdParam == null || shiftIdParam == null || dateParam == null || userIdParam.isBlank()
				|| shiftIdParam.isBlank() || dateParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Vui lòng điền đầy đủ thông tin.");
			redirectBack(request, response, departmentId, year, month);
			return;
		}

		try {
			Long userId = Long.parseLong(userIdParam);
			Long shiftId = Long.parseLong(shiftIdParam);
			LocalDate date = LocalDate.parse(dateParam);

			boolean success = shiftAssignmentDAO.upsert(userId, shiftId, Date.valueOf(date));
			if (success) {
				request.getSession().setAttribute("successMsg", "Phân ca thành công!");
			} else {
				request.getSession().setAttribute("errorMsg", "Không thể phân ca. Vui lòng thử lại.");
			}
		} catch (Exception e) {
			request.getSession().setAttribute("errorMsg", "Định dạng ngày không hợp lệ.");
		}

		redirectBack(request, response, departmentId, year, month);
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String assignmentIdParam = request.getParameter("id");
		String departmentId = request.getParameter("departmentId");
		String year = request.getParameter("year");
		String month = request.getParameter("month");

		if (assignmentIdParam != null && !assignmentIdParam.isBlank()) {
			try {
				Long id = Long.parseLong(assignmentIdParam);
				boolean deleted = shiftAssignmentDAO.delete(id);
				if (deleted) {
					request.getSession().setAttribute("successMsg", "Xóa phân ca thành công!");
				}
			} catch (Exception e) {
				request.getSession().setAttribute("errorMsg", "Không thể xóa phân ca.");
			}
		}

		redirectBack(request, response, departmentId, year, month);
	}

	private void handleDeleteAll(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String departmentId = request.getParameter("departmentId");
		String year = request.getParameter("year");
		String month = request.getParameter("month");

		try {
			LocalDate today = LocalDate.now();
			int selectedYear = parseInt(year, today.getYear());
			int selectedMonth = parseInt(month, today.getMonthValue());
			YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
			Long selectedDepartmentId = parseLong(departmentId);

			int deleted = shiftAssignmentDAO.deleteByDateRangeAndDepartment(Date.valueOf(yearMonth.atDay(1)),
					Date.valueOf(yearMonth.atEndOfMonth()), selectedDepartmentId);
			request.getSession().setAttribute("successMsg", "Đã xóa " + deleted + " phân ca trong tháng đang xem!");
		} catch (Exception e) {
			request.getSession().setAttribute("errorMsg", "Không thể xóa phân ca.");
		}

		redirectBack(request, response, departmentId, year, month);
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

	private void redirectBack(HttpServletRequest request, HttpServletResponse response, String departmentId,
			String year, String month) throws IOException {
		StringBuilder url = new StringBuilder(request.getContextPath() + "/shift-calendar?");
		if (year != null)
			url.append("year=").append(year).append("&");
		if (month != null)
			url.append("month=").append(month).append("&");
		if (departmentId != null && !departmentId.isBlank())
			url.append("departmentId=").append(departmentId);
		response.sendRedirect(url.toString());
	}

	private void moveFlashMessages(HttpServletRequest request) {
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

		// Import results
		Integer importSuccess = (Integer) session.getAttribute("importSuccessCount");
		if (importSuccess != null) {
			request.setAttribute("importSuccessCount", importSuccess);
			request.setAttribute("importTotal", session.getAttribute("importTotal"));
			request.setAttribute("importErrorCount", session.getAttribute("importErrorCount"));
			request.setAttribute("importErrors", session.getAttribute("importErrors"));
			session.removeAttribute("importSuccessCount");
			session.removeAttribute("importTotal");
			session.removeAttribute("importErrorCount");
			session.removeAttribute("importErrors");
		}

		// Check URL parameter for redirect status
		String imported = request.getParameter("imported");
		if ("true".equals(imported)) {
			Integer successCount = (Integer) session.getAttribute("importSuccessCount");
			if (successCount != null) {
				request.setAttribute("importSuccessCount", successCount);
				session.removeAttribute("importSuccessCount");
			}
		}

		String error = request.getParameter("error");
		if (error != null) {
			String errorMsg2 = switch (error) {
				case "emptyFile" -> "Vui lòng chọn file Excel.";
				case "invalidFile" -> "File không hợp lệ. Vui lòng chọn file Excel (.xlsx).";
				case "noSheet" -> "File Excel không có dữ liệu.";
				case "parseError" -> "Không thể đọc file Excel. Vui lòng kiểm tra định dạng.";
				default -> "Đã xảy ra lỗi khi import.";
			};
			request.setAttribute("errorMsg", errorMsg2);
		}
	}

	private boolean hasPermission(HttpServletRequest request, String code) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}

		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}

		for (Permission permission : permissions) {
			if (code.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}
}
