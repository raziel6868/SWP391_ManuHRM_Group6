package controller.attendance;

import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import dal.LeaveRequestDAO;
import dal.OvertimeDAO;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.AttendanceRecord;
import model.LeaveRequest;
import model.OvertimeRecord;
import model.User;

/**
 * Trang nhân viên tự xem lịch chấm công cá nhân dạng calendar theo tháng (giống
 * my-overtime), mỗi ô ngày là 1 trong 6 trạng thái: P (có mặt) / P đi muộn / A
 * (vắng) / L (nghỉ phép) / W (cuối tuần) / O (có OT được duyệt). Bấm vào ô có
 * dữ liệu chấm công thật để gửi yêu cầu điều chỉnh công.
 */
@WebServlet(name = "AttendanceMyServlet", urlPatterns = {"/attendance-my"})
public class AttendanceMyServlet extends HttpServlet {

	private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();
	private final OvertimeDAO overtimeDAO = new OvertimeDAO();

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

		LocalDate today = LocalDate.now(VN_ZONE);
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());
		if (month < 1 || month > 12) {
			month = today.getMonthValue();
		}
		if (year < 2000 || year > 2100) {
			year = today.getYear();
		}

		Long userId = authUser.getId();
		YearMonth yearMonth = YearMonth.of(year, month);
		int daysInMonth = yearMonth.lengthOfMonth();

		List<AttendanceRecord> records = attendanceDAO.searchByUserAndMonth(userId, year, month);
		List<LeaveRequest> approvedLeaves = leaveRequestDAO.getApprovedLeavesForUsersInMonth(List.of(userId), year,
				month);
		List<OvertimeRecord> approvedOTs = overtimeDAO.getActiveByUserAndMonth(userId, year, month);

		Map<Integer, AttendanceRecord> attByDay = new LinkedHashMap<>();
		for (AttendanceRecord r : records) {
			attByDay.put(r.getDate().toLocalDate().getDayOfMonth(), r);
		}

		Set<Integer> leaveDays = new HashSet<>();
		for (LeaveRequest lr : approvedLeaves) {
			LocalDate rangeStart = lr.getStartDate().toLocalDate().isBefore(yearMonth.atDay(1))
					? yearMonth.atDay(1)
					: lr.getStartDate().toLocalDate();
			LocalDate rangeEnd = lr.getEndDate().toLocalDate().isAfter(yearMonth.atEndOfMonth())
					? yearMonth.atEndOfMonth()
					: lr.getEndDate().toLocalDate();
			for (LocalDate d = rangeStart; !d.isAfter(rangeEnd); d = d.plusDays(1)) {
				if (!isWeekend(d)) {
					leaveDays.add(d.getDayOfMonth());
				}
			}
		}

		Set<Integer> otDays = new HashSet<>();
		BigDecimal totalOtHours = BigDecimal.ZERO;
		for (OvertimeRecord ot : approvedOTs) {
			int otDay = ot.getDate().toLocalDate().getDayOfMonth();
			// Chỉ tính OT của những ngày đã có chấm công thật (đã import),
			// tránh hiện OT của ngày tương lai chưa chấm công.
			if (attByDay.containsKey(otDay)) {
				otDays.add(otDay);
				totalOtHours = totalOtHours.add(ot.getRequestedHours());
			}
		}

		// dayStatus.get(day) = "P" | "LATE" | "A" | "L" | "W" | "O"
		// dayRecord.get(day) = AttendanceRecord thật (nếu có) để bấm vào gửi điều chỉnh
		Map<Integer, String> dayStatus = new LinkedHashMap<>();
		Map<Integer, AttendanceRecord> dayRecord = new LinkedHashMap<>();

		int countPresent = 0;
		int countLate = 0;
		int countAbsent = 0;
		int countLeave = 0;

		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate d = yearMonth.atDay(day);
			AttendanceRecord rec = attByDay.get(day);
			String status;

			if (isWeekend(d)) {
				status = "W";
			} else if (leaveDays.contains(day)) {
				status = "L";
				countLeave++;
			} else if (rec != null && "ABSENT".equals(rec.getStatus())) {
				status = "A";
				countAbsent++;
			} else if (rec != null && otDays.contains(day)) {
				// Chỉ tính là "có OT" khi ngày đó đã có chấm công thật (đã import).
				status = "O";
				countPresent++;
				if ("LATE".equals(rec.getStatus())) {
					countLate++;
				}
			} else if (rec != null && "LATE".equals(rec.getStatus())) {
				status = "P_LATE";
				countPresent++;
				countLate++;
			} else if (rec != null) {
				status = "P";
				countPresent++;
			} else {
				status = null;
			}

			if (status != null) {
				dayStatus.put(day, status);
			}
			if (rec != null && !"L".equals(status) && !"W".equals(status)) {
				dayRecord.put(day, rec);
			}
		}

		int prevMonth = month == 1 ? 12 : month - 1;
		int prevYear = month == 1 ? year - 1 : year;
		int nextMonth = month == 12 ? 1 : month + 1;
		int nextYear = month == 12 ? year + 1 : year;

		request.setAttribute("dayStatus", dayStatus);
		request.setAttribute("dayRecord", dayRecord);
		request.setAttribute("daysInMonth", daysInMonth);
		request.setAttribute("startDate", yearMonth.atDay(1));
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("myCorrections", correctionDAO.searchByEmployee(userId, 0, 20));

		request.setAttribute("countPresent", countPresent);
		request.setAttribute("countLate", countLate);
		request.setAttribute("countAbsent", countAbsent);
		request.setAttribute("countLeave", countLeave);
		request.setAttribute("totalOtHours", totalOtHours);

		request.setAttribute("prevYear", prevYear);
		request.setAttribute("prevMonth", prevMonth);
		request.setAttribute("nextYear", nextYear);
		request.setAttribute("nextMonth", nextMonth);

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

	private boolean isWeekend(LocalDate date) {
		return date.getDayOfWeek().getValue() >= 6;
	}
}
