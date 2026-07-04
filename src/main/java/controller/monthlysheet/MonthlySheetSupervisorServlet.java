package controller.monthlysheet;

import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import dal.MonthlySheetApprovalDAO;
import dal.MonthlySheetDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.AttendanceCorrection;
import model.AttendanceRecord;
import model.MonthlySheet;
import model.MonthlySheetApproval;
import model.Permission;
import model.User;

@WebServlet(name = "MonthlySheetSupervisorServlet", urlPatterns = {"/monthly-sheet-supervisor"})
public class MonthlySheetSupervisorServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final MonthlySheetApprovalDAO approvalDAO = new MonthlySheetApprovalDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "MONTHLY_SHEET_SUPERVISOR_VIEW")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		LocalDate today = LocalDate.now();
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());

		Long filterUserId = parseLong(request.getParameter("userId"));
		List<User> subordinates = resolveSupervisorScope(authUser);
		MonthlySheet sheet = monthlySheetDAO.getByYearMonth(year, month);
		MonthlySheetApproval myApproval = null;
		List<MonthlySheetApproval> allApprovals = new ArrayList<>();
		List<Long> visibleUserIds = resolveVisibleUserIds(subordinates, filterUserId);
		List<AttendanceRecord> records = new ArrayList<>();

		if (sheet != null && !"OPEN".equals(sheet.getStatus())) {
			myApproval = approvalDAO.getBySupervisorAndSheet(sheet.getId(), authUser.getId());
			allApprovals = approvalDAO.getBySheetId(sheet.getId());
			records = attendanceDAO.searchByUserIdsAndMonth(visibleUserIds, year, month);
			if (subordinates.isEmpty() && !records.isEmpty()) {
				subordinates = buildUsersFromRecords(records);
			}
		}

		List<AttendanceCorrection> pendingCorrections = correctionDAO
				.searchBySupervisor(authUser.getId(), "PENDING", 0, Integer.MAX_VALUE).stream()
				.filter(c -> c.getAttendanceDate() != null && c.getAttendanceDate().toLocalDate().getYear() == year
						&& c.getAttendanceDate().toLocalDate().getMonthValue() == month)
				.toList();

		boolean canApprove = hasPermission(session, "MONTHLY_SHEET_SUPERVISOR_APPROVE") && sheet != null
				&& "PENDING_SUPERVISOR".equals(sheet.getStatus()) && myApproval != null
				&& "PENDING".equals(myApproval.getStatus());
		boolean canReviewCorrections = hasPermission(session, "ATTENDANCE_CORRECTION_SUPERVISOR_APPROVE");
		boolean hasPendingCorrections = correctionDAO.hasPendingSupervisorCorrectionInMonth(authUser.getId(), year,
				month);

		request.setAttribute("sheet", sheet);
		request.setAttribute("myApproval", myApproval);
		request.setAttribute("allApprovals", allApprovals);
		request.setAttribute("records", records);
		request.setAttribute("subordinates", subordinates);
		request.setAttribute("pendingCorrections", pendingCorrections);
		request.setAttribute("canApprove", canApprove);
		request.setAttribute("canReviewCorrections", canReviewCorrections);
		request.setAttribute("hasPendingCorrections", hasPendingCorrections);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("selectedUserId", filterUserId);

		request.getRequestDispatcher("/views/monthlysheet/monthly-sheet-supervisor.jsp").forward(request, response);
	}

	private List<User> resolveSupervisorScope(User authUser) {
		List<User> subordinates = userDAO.findByManagerId(authUser.getId());
		if (!subordinates.isEmpty()) {
			return subordinates;
		}

		List<User> departmentUsers = userDAO.getActiveUsersByDepartment(authUser.getDepartmentId());
		List<User> fallbackUsers = new ArrayList<>();
		for (User departmentUser : departmentUsers) {
			if (departmentUser.getId() == null || departmentUser.getId().equals(authUser.getId())) {
				continue;
			}
			fallbackUsers.add(departmentUser);
		}
		return fallbackUsers;
	}

	private List<Long> resolveVisibleUserIds(List<User> subordinates, Long filterUserId) {
		List<Long> userIds = new ArrayList<>();
		if (subordinates == null || subordinates.isEmpty()) {
			return userIds;
		}

		List<Long> allowedUserIds = new ArrayList<>();
		for (User subordinate : subordinates) {
			if (subordinate.getId() != null && !allowedUserIds.contains(subordinate.getId())) {
				allowedUserIds.add(subordinate.getId());
			}
		}

		if (filterUserId != null) {
			if (allowedUserIds.contains(filterUserId)) {
				userIds.add(filterUserId);
			}
			return userIds;
		}

		userIds.addAll(allowedUserIds);
		return userIds;
	}

	private List<User> buildUsersFromRecords(List<AttendanceRecord> records) {
		Map<Long, User> uniqueUsers = new LinkedHashMap<>();
		for (AttendanceRecord record : records) {
			if (record.getUserId() == null || uniqueUsers.containsKey(record.getUserId())) {
				continue;
			}
			User user = new User();
			user.setId(record.getUserId());
			user.setEmployeeCode(record.getEmployeeCode());
			user.setFullName(record.getEmployeeName());
			uniqueUsers.put(record.getUserId(), user);
		}
		return new ArrayList<>(uniqueUsers.values());
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

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
