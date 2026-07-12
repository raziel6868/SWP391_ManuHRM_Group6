package controller.overtime;

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
import java.util.List;
import model.OvertimeRecord;
import model.Permission;
import model.User;

/**
 * Trang nhân viên tự xem lịch tăng ca (OT) của chính mình, dạng calendar theo
 * tháng (giống my-shift), mỗi ô ngày hiển thị số giờ OT ngày đó nếu có. Chỉ
 * xem, không tạo/sửa/hủy được ở đây.
 */
@WebServlet(name = "MyOvertimeServlet", urlPatterns = {"/my-overtime"})
public class MyOvertimeServlet extends HttpServlet {

	private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		if (!hasPermission(session, "OT_MY_VIEW")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
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

		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		List<OvertimeRecord> myOvertimes = overtimeDAO.getActiveByUserAndMonth(authUser.getId(), year, month);

		BigDecimal totalHours = BigDecimal.ZERO;
		for (OvertimeRecord r : myOvertimes) {
			totalHours = totalHours.add(r.getRequestedHours());
		}

		int prevMonth = month == 1 ? 12 : month - 1;
		int prevYear = month == 1 ? year - 1 : year;
		int nextMonth = month == 12 ? 1 : month + 1;
		int nextYear = month == 12 ? year + 1 : year;

		request.setAttribute("myOvertimes", myOvertimes);
		request.setAttribute("currentYear", year);
		request.setAttribute("currentMonth", month);
		request.setAttribute("prevYear", prevYear);
		request.setAttribute("prevMonth", prevMonth);
		request.setAttribute("nextYear", nextYear);
		request.setAttribute("nextMonth", nextMonth);
		request.setAttribute("startDate", startDate);
		request.setAttribute("endDate", endDate);
		request.setAttribute("otDays", myOvertimes.size());
		request.setAttribute("totalHours", totalHours);

		request.getRequestDispatcher("/views/overtime/my-overtime.jsp").forward(request, response);
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
}
