package controller.overtime;

import dal.DepartmentDAO;
import dal.OvertimeDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Department;
import model.OvertimeRecord;
import model.Permission;
import model.User;

/**
 * Trang OT giờ hiển thị dạng LƯỚI: hàng = nhân viên, cột = ngày 1..31 của tháng
 * đang chọn, ô = số giờ OT ngày đó (rỗng nếu không có). Thay thế hoàn toàn kiểu
 * list cũ (không còn phân trang, không còn filter trạng thái vì mọi OT hiển thị
 * ở đây đều đang APPROVED).
 */
@WebServlet(name = "OvertimeListServlet", urlPatterns = {"/overtime-list"})
public class OvertimeListServlet extends HttpServlet {

	private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		if (!hasPermission(session, "OT_VIEW")) {
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
		// Rank cao hơn (HR_MANAGER/SYSADMIN): xem toàn bộ, chỉ để theo dõi (không có
		// nút thao tác).
		int authRank = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		Long managerId = authRank <= 2 ? authUser.getId() : null;

		boolean canRequest = hasPermission(session, "OT_REQUEST");
		boolean canUpdate = hasPermission(session, "OT_UPDATE");

		// Filter phòng ban chỉ có ý nghĩa khi xem toàn bộ (HR/Sysadmin); quản đốc
		// đã bị scope theo managerId nên bỏ qua departmentId nếu có truyền lên.
		boolean viewAll = managerId == null;
		Long effectiveDepartmentId = viewAll ? departmentId : null;
		List<Department> departments = viewAll ? departmentDAO.getActiveDepartments() : null;

		List<User> employees = userDAO.searchUsers(keyword, effectiveDepartmentId, null, true, null, 0, 1000,
				managerId);

		YearMonth yearMonth = YearMonth.of(year, month);
		int daysInMonth = yearMonth.lengthOfMonth();

		List<OvertimeRecord> records = overtimeDAO.getActiveByManagerAndMonth(managerId, year, month);

		// gridData.get(userId).get(day) = bản ghi OT ngày đó (null nếu không có)
		Map<Long, Map<Integer, OvertimeRecord>> gridData = new LinkedHashMap<>();
		Map<Long, BigDecimal> totals = new LinkedHashMap<>();
		for (User e : employees) {
			gridData.put(e.getId(), new LinkedHashMap<>());
			totals.put(e.getId(), BigDecimal.ZERO);
		}
		for (OvertimeRecord r : records) {
			Long uid = r.getUserId();
			Map<Integer, OvertimeRecord> row = gridData.get(uid);
			if (row == null) {
				// nhân viên có OT trong tháng nhưng bị loại khỏi danh sách hiện tại do search
				// keyword
				continue;
			}
			int day = r.getDate().toLocalDate().getDayOfMonth();
			row.put(day, r);
			totals.put(uid, totals.get(uid).add(r.getRequestedHours()));
		}

		request.setAttribute("employees", employees);
		request.setAttribute("gridData", gridData);
		request.setAttribute("totals", totals);
		request.setAttribute("daysInMonth", daysInMonth);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("keyword", keyword);
		request.setAttribute("canRequest", canRequest);
		request.setAttribute("canUpdate", canUpdate);
		request.setAttribute("viewAll", viewAll);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedDepartmentId", effectiveDepartmentId);

		// Thống kê nhanh + mũi tên tiến/lùi tháng (giống trang my-overtime)
		BigDecimal totalHoursAll = BigDecimal.ZERO;
		for (BigDecimal t : totals.values()) {
			totalHoursAll = totalHoursAll.add(t);
		}
		// "Số ngày có OT" = số ngày lịch riêng biệt có ít nhất 1 người OT,
		// không cộng dồn theo số lượt nhân viên OT trong cùng 1 ngày.
		java.util.Set<java.sql.Date> distinctOtDates = new java.util.HashSet<>();
		for (OvertimeRecord r : records) {
			distinctOtDates.add(r.getDate());
		}
		int prevMonth = month == 1 ? 12 : month - 1;
		int prevYear = month == 1 ? year - 1 : year;
		int nextMonth = month == 12 ? 1 : month + 1;
		int nextYear = month == 12 ? year + 1 : year;

		request.setAttribute("otDays", distinctOtDates.size());
		request.setAttribute("totalHoursAll", totalHoursAll);
		request.setAttribute("prevYear", prevYear);
		request.setAttribute("prevMonth", prevMonth);
		request.setAttribute("nextYear", nextYear);
		request.setAttribute("nextMonth", nextMonth);

		request.getRequestDispatcher("/views/overtime/overtime-list.jsp").forward(request, response);
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

	/** Nhận kết quả import từ OvertimeRequestServlet (flash qua session). */
	private void moveImportFlash(HttpSession session, HttpServletRequest request) {
		Object successCount = session.getAttribute("importSuccessCount");
		if (successCount == null) {
			return;
		}
		request.setAttribute("importSuccessCount", successCount);
		request.setAttribute("importDuplicateCount", session.getAttribute("importDuplicateCount"));
		request.setAttribute("importErrorCount", session.getAttribute("importErrorCount"));
		request.setAttribute("importErrorMessages", session.getAttribute("importErrorMessages"));
		request.setAttribute("importDuplicateMessages", session.getAttribute("importDuplicateMessages"));
		session.removeAttribute("importSuccessCount");
		session.removeAttribute("importDuplicateCount");
		session.removeAttribute("importErrorCount");
		session.removeAttribute("importErrorMessages");
		session.removeAttribute("importDuplicateMessages");
	}
}