package controller.overtime;

import dal.AttendanceDAO;
import dal.MonthlySheetDAO;
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
import java.time.LocalTime;
import java.util.List;
import model.AttendanceRecord;
import model.OvertimeRecord;
import model.Permission;
import model.User;
import util.WorkScheduleConfig;

/**
 * Trang sửa 1 bản ghi OT đã tạo (đổi giờ / lý do). Chỉ quản đốc quản lý trực
 * tiếp nhân viên đó mới sửa được, và chỉ khi tháng chưa chốt công. Validate lại
 * đầy đủ rule giống lúc tạo (trừ chính bản ghi đang sửa ra khỏi tổng giờ
 * tháng/năm). OT tối đa 2h/ngày theo quy định hiện hành.
 */
@WebServlet(name = "OvertimeEditServlet", urlPatterns = {"/overtime-edit"})
public class OvertimeEditServlet extends HttpServlet {

	private static final BigDecimal MAX_HOURS_PER_MONTH = new BigDecimal("40");
	private static final BigDecimal MAX_HOURS_PER_YEAR = new BigDecimal("200");

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		if (!hasPermission(session, "OT_UPDATE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		OvertimeRecord record = id != null ? overtimeDAO.getById(id) : null;
		if (record == null || !"APPROVED".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT hoặc đã bị hủy.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		User targetUser = userDAO.getById(record.getUserId());
		if (targetUser == null || targetUser.getManagerId() == null
				|| !targetUser.getManagerId().equals(authUser.getId())) {
			session.setAttribute("errorMsg", "Bạn chỉ có thể sửa OT của nhân viên dưới quyền mình.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		int recYear = record.getDate().toLocalDate().getYear();
		int recMonth = record.getDate().toLocalDate().getMonthValue();
		if (!monthlySheetDAO.isEditablePeriod(recYear, recMonth)) {
			session.setAttribute("errorMsg",
					"Tháng " + recMonth + "/" + recYear + " không còn ở trạng thái OPEN, không thể sửa OT.");
			response.sendRedirect(request.getContextPath() + "/overtime-list?year=" + recYear + "&month=" + recMonth);
			return;
		}

		request.setAttribute("record", record);
		request.setAttribute("recYear", recYear);
		request.setAttribute("recMonth", recMonth);
		request.getRequestDispatcher("/views/overtime/overtime-edit.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		if (!hasPermission(session, "OT_UPDATE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		BigDecimal hours = parseBigDecimal(request.getParameter("requestedHours"));
		String reason = request.getParameter("reason");

		OvertimeRecord record = id != null ? overtimeDAO.getById(id) : null;
		if (record == null || !"APPROVED".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT hoặc đã bị hủy.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		int recYear = record.getDate().toLocalDate().getYear();
		int recMonth = record.getDate().toLocalDate().getMonthValue();
		String redirectList = request.getContextPath() + "/overtime-list?year=" + recYear + "&month=" + recMonth;

		User targetUser = userDAO.getById(record.getUserId());
		if (targetUser == null || targetUser.getManagerId() == null
				|| !targetUser.getManagerId().equals(authUser.getId())) {
			session.setAttribute("errorMsg", "Bạn chỉ có thể sửa OT của nhân viên dưới quyền mình.");
			response.sendRedirect(redirectList);
			return;
		}

		if (!monthlySheetDAO.isEditablePeriod(recYear, recMonth)) {
			session.setAttribute("errorMsg",
					"Tháng " + recMonth + "/" + recYear + " không còn ở trạng thái OPEN, không thể sửa OT.");
			response.sendRedirect(redirectList);
			return;
		}

		if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
			session.setAttribute("errorMsg", "Số giờ OT không hợp lệ (phải lớn hơn 0).");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}
		if (reason == null || reason.isBlank()) {
			session.setAttribute("errorMsg", "Vui lòng nhập lý do tăng ca.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		if (hours.compareTo(WorkScheduleConfig.MAX_OT_HOURS_PER_DAY) > 0) {
			session.setAttribute("errorMsg", "Số giờ OT (" + hours + "h) vượt quá tối đa "
					+ WorkScheduleConfig.MAX_OT_HOURS_PER_DAY + "h/ngày.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		// Nếu ngày đó đã có chấm công thật, re-check giống lúc tạo (validate 2
		// chiều với AttendanceImportUtil để tránh trạng thái mâu thuẫn).
		AttendanceRecord existingAttendance = attendanceDAO.findByUserAndDate(record.getUserId(), record.getDate());
		if (existingAttendance != null && existingAttendance.getCheckIn() == null) {
			session.setAttribute("errorMsg",
					"Nhân viên được ghi nhận VẮNG MẶT ngày " + record.getDate() + ", không thể có OT ngày này.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}
		if (existingAttendance != null && existingAttendance.getCheckOut() != null) {
			long otMinutes = hours.multiply(BigDecimal.valueOf(60)).longValue();
			LocalTime expectedCheckout = WorkScheduleConfig.OVERTIME_START.plusMinutes(otMinutes);
			if (existingAttendance.getCheckOut().toLocalTime().isBefore(expectedCheckout)) {
				session.setAttribute("errorMsg",
						"Nhân viên đã chấm công ra lúc " + existingAttendance.getCheckOut().toLocalTime() + " ngày "
								+ record.getDate() + ", không đủ hỗ trợ " + hours + "h OT (cần ra từ "
								+ expectedCheckout + " trở đi).");
				response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
				return;
			}
		}

		BigDecimal monthTotal = overtimeDAO.sumHoursInMonth(record.getUserId(), recYear, recMonth, id);
		if (monthTotal.add(hours).compareTo(MAX_HOURS_PER_MONTH) > 0) {
			session.setAttribute("errorMsg", "Nhân viên đã có " + monthTotal + "h OT khác trong tháng " + recMonth + "/"
					+ recYear + ", cộng thêm " + hours + "h sẽ vượt trần 40h/tháng.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		BigDecimal yearTotal = overtimeDAO.sumHoursInYear(record.getUserId(), recYear, id);
		if (yearTotal.add(hours).compareTo(MAX_HOURS_PER_YEAR) > 0) {
			session.setAttribute("errorMsg", "Nhân viên đã có " + yearTotal + "h OT khác trong năm " + recYear
					+ ", cộng thêm " + hours + "h sẽ vượt trần 200h/năm.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		boolean success = overtimeDAO.update(id, hours, reason.trim());
		if (success) {
			session.setAttribute("successMsg", "Cập nhật OT thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật OT. Vui lòng thử lại.");
		}
		response.sendRedirect(redirectList);
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

	private BigDecimal parseBigDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
