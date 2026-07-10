package controller.attendance;

import dal.AttendanceDAO;
import dal.DepartmentDAO;
import dal.LeaveRequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.AttendanceRecord;
import model.Department;
import model.LeaveRequest;
import model.Permission;
import model.User;

/**
 * Trang chấm công hiển thị dạng LƯỚI: hàng = nhân viên, cột = ngày 1..31 của
 * tháng đang chọn, mỗi ô là 1 trạng thái trong ngày: P (có mặt) / LATE (đi
 * muộn) / A (vắng) / L (nghỉ phép) / W (cuối tuần). Không hiển thị OT ở đây (OT
 * xem trong monthly-sheet). Cùng kiểu với overtime-list.
 */
@WebServlet(name = "AttendanceListServlet", urlPatterns = {"/attendance-list"})
public class AttendanceListServlet extends HttpServlet {

	private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		if (!hasPermission(session, "ATTENDANCE_VIEW")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");
		moveImportFlash(session, request);

		LocalDate today = LocalDate.now(VN_ZONE);
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());
		if (month < 1 || month > 12) {
			month = today.getMonthValue();
		}
		if (year < 2000 || year > 2100) {
			year = today.getYear();
		}

		String keyword = request.getParameter("keyword");
		if (keyword != null) {
			keyword = keyword.trim();
			if (keyword.isEmpty()) {
				keyword = null;
			}
		}

		Long departmentId = parseLong(request.getParameter("departmentId"));

		// authRank <= 2 (EMPLOYEE/PRODUCTION_SUPERVISOR): chỉ xem nhân viên dưới quyền
		// mình.
		// Rank cao hơn (HR_MANAGER/SYSADMIN): xem toàn bộ.
		int authRank = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		Long managerId = authRank <= 2 ? authUser.getId() : null;
		boolean viewAll = managerId == null;

		boolean canImport = hasPermission(session, "ATTENDANCE_IMPORT");

		// Filter phòng ban chỉ có ý nghĩa khi xem toàn bộ (HR/Sysadmin); quản đốc
		// đã bị scope theo managerId nên bỏ qua departmentId nếu có truyền lên.
		Long effectiveDepartmentId = viewAll ? departmentId : null;
		List<Department> departments = viewAll ? departmentDAO.getActiveDepartments() : null;

		List<User> employees = userDAO.searchUsers(keyword, effectiveDepartmentId, null, true, null, 0, 1000,
				managerId);

		YearMonth yearMonth = YearMonth.of(year, month);
		int daysInMonth = yearMonth.lengthOfMonth();

		List<Long> userIds = new ArrayList<>();
		for (User e : employees) {
			userIds.add(e.getId());
		}

		List<AttendanceRecord> records = attendanceDAO.searchByUserIdsAndMonth(userIds, year, month);
		List<LeaveRequest> approvedLeaves = leaveRequestDAO.getApprovedLeavesForUsersInMonth(userIds, year, month);

		// (userId, day) nào có nghỉ phép đã duyệt trong tháng này.
		Map<Long, Set<Integer>> leaveDaysByUser = new LinkedHashMap<>();
		for (LeaveRequest lr : approvedLeaves) {
			LocalDate rangeStart = lr.getStartDate().toLocalDate().isBefore(yearMonth.atDay(1))
					? yearMonth.atDay(1)
					: lr.getStartDate().toLocalDate();
			LocalDate rangeEnd = lr.getEndDate().toLocalDate().isAfter(yearMonth.atEndOfMonth())
					? yearMonth.atEndOfMonth()
					: lr.getEndDate().toLocalDate();
			Set<Integer> days = leaveDaysByUser.computeIfAbsent(lr.getUserId(), k -> new HashSet<>());
			for (LocalDate d = rangeStart; !d.isAfter(rangeEnd); d = d.plusDays(1)) {
				days.add(d.getDayOfMonth());
			}
		}

		// (userId, day) -> status chấm công gốc (NORMAL/LATE/ABSENT) trong tháng.
		Map<Long, Map<Integer, String>> attendanceByUserDay = new LinkedHashMap<>();
		for (AttendanceRecord r : records) {
			Map<Integer, String> byDay = attendanceByUserDay.computeIfAbsent(r.getUserId(), k -> new LinkedHashMap<>());
			byDay.put(r.getDate().toLocalDate().getDayOfMonth(), r.getStatus());
		}

		// gridData.get(userId).get(day) = "P" | "LATE" | "A" | "L" | "W" (null = không
		// có dữ liệu)
		Map<Long, Map<Integer, String>> gridData = new LinkedHashMap<>();
		Map<Long, int[]> rowSummary = new LinkedHashMap<>(); // [present(kể cả muộn), absent, leave]
		int countPresent = 0;
		int countLate = 0;
		int countAbsent = 0;
		int countLeaveDays = 0;

		for (User e : employees) {
			Map<Integer, String> row = new LinkedHashMap<>();
			int[] summary = new int[3];
			Map<Integer, String> attByDay = attendanceByUserDay.getOrDefault(e.getId(), Map.of());
			Set<Integer> leaveDays = leaveDaysByUser.getOrDefault(e.getId(), Set.of());

			for (int day = 1; day <= daysInMonth; day++) {
				LocalDate d = yearMonth.atDay(day);
				DayOfWeek dow = d.getDayOfWeek();
				String cell;
				if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
					cell = "W";
				} else if (leaveDays.contains(day)) {
					cell = "L";
					summary[2]++;
					countLeaveDays++;
				} else if (attByDay.containsKey(day)) {
					String status = attByDay.get(day);
					if ("ABSENT".equals(status)) {
						cell = "A";
						summary[1]++;
						countAbsent++;
					} else if ("LATE".equals(status)) {
						cell = "LATE";
						summary[0]++;
						countPresent++;
						countLate++;
					} else {
						cell = "P";
						summary[0]++;
						countPresent++;
					}
				} else {
					cell = null;
				}
				if (cell != null) {
					row.put(day, cell);
				}
			}
			gridData.put(e.getId(), row);
			rowSummary.put(e.getId(), summary);
		}

		int prevMonth = month == 1 ? 12 : month - 1;
		int prevYear = month == 1 ? year - 1 : year;
		int nextMonth = month == 12 ? 1 : month + 1;
		int nextYear = month == 12 ? year + 1 : year;

		request.setAttribute("employees", employees);
		request.setAttribute("gridData", gridData);
		request.setAttribute("rowSummary", rowSummary);
		request.setAttribute("daysInMonth", daysInMonth);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("keyword", keyword);
		request.setAttribute("canImport", canImport);
		request.setAttribute("viewAll", viewAll);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedDepartmentId", effectiveDepartmentId);

		request.setAttribute("countPresent", countPresent);
		request.setAttribute("countLate", countLate);
		request.setAttribute("countAbsent", countAbsent);
		request.setAttribute("countLeaveDays", countLeaveDays);

		request.setAttribute("prevYear", prevYear);
		request.setAttribute("prevMonth", prevMonth);
		request.setAttribute("nextYear", nextYear);
		request.setAttribute("nextMonth", nextMonth);

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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}

	/** Nhận kết quả import từ AttendanceImportServlet (flash qua session). */
	private void moveImportFlash(HttpSession session, HttpServletRequest request) {
		Object successCount = session.getAttribute("importSuccessCount");
		if (successCount != null) {
			request.setAttribute("importSuccessCount", successCount);
			request.setAttribute("importDuplicateCount", session.getAttribute("importDuplicateCount"));
			request.setAttribute("importDuplicateMessages", session.getAttribute("importDuplicateMessages"));
			session.removeAttribute("importSuccessCount");
			session.removeAttribute("importDuplicateCount");
			session.removeAttribute("importDuplicateMessages");
		}

		Object failed = session.getAttribute("importFailed");
		if (failed != null) {
			request.setAttribute("importFailed", failed);
			request.setAttribute("importTotalDataRows", session.getAttribute("importTotalDataRows"));
			request.setAttribute("importErrorMessages", session.getAttribute("importErrorMessages"));
			request.setAttribute("importDuplicateMessages", session.getAttribute("importDuplicateMessages"));
			session.removeAttribute("importFailed");
			session.removeAttribute("importTotalDataRows");
			session.removeAttribute("importErrorMessages");
			session.removeAttribute("importDuplicateMessages");
		}
	}
}