package controller.overtime;

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
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.OvertimeRecord;
import model.User;
import util.ValidationUtil;

@WebServlet(name = "OvertimeRequestServlet", urlPatterns = {"/overtime-request"})
public class OvertimeRequestServlet extends HttpServlet {

	private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final DateTimeFormatter STRICT_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
			.withResolverStyle(ResolverStyle.STRICT);

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		int authRank = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		Long managerId = authRank <= 2 ? authUser.getId() : null;
		List<User> subordinates = userDAO.searchUsers(null, null, null, true, null, 0, 1000, managerId);

		LocalDate today = LocalDate.now(VN_ZONE);
		LocalDate minOtDate = resolveMinOtDate(today);
		LocalDate maxOtDate = resolveMaxOtDate(today);

		request.setAttribute("subordinates", subordinates);
		request.setAttribute("minOtDate", minOtDate);
		request.setAttribute("maxOtDate", maxOtDate);
		request.getRequestDispatcher("/views/overtime/overtime-request.jsp").forward(request, response);
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

		Long userId = parseLong(request.getParameter("userId"));
		Date date = parseDate(request.getParameter("date"));
		BigDecimal requestedHours = parseBigDecimal(request.getParameter("requestedHours"));
		String reason = request.getParameter("reason");

		String redirectList = request.getContextPath() + "/overtime-list";

		if (userId == null) {
			session.setAttribute("errorMsg", "Vui lòng chọn nhân viên.");
			response.sendRedirect(redirectList);
			return;
		}

		User targetUser = userDAO.getById(userId);
		if (targetUser == null || !Boolean.TRUE.equals(targetUser.getIsActive())) {
			session.setAttribute("errorMsg", "Nhân viên không tồn tại hoặc đã bị vô hiệu hóa.");
			response.sendRedirect(redirectList);
			return;
		}
		int authRank = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		if (authRank <= 2) {
			if (targetUser.getManagerId() == null || !targetUser.getManagerId().equals(authUser.getId())) {
				session.setAttribute("errorMsg", "Bạn chỉ có thể tạo OT cho nhân viên dưới quyền của mình.");
				response.sendRedirect(redirectList);
				return;
			}
		}

		if (date == null) {
			session.setAttribute("errorMsg", "Ngày OT không hợp lệ. Vui lòng chọn ngày hợp lệ trên lịch.");
			response.sendRedirect(redirectList);
			return;
		}

		LocalDate otDate = date.toLocalDate();
		String dateError = validateOtDate(otDate);
		if (dateError != null) {
			session.setAttribute("errorMsg", dateError);
			response.sendRedirect(redirectList);
			return;
		}

		if (requestedHours == null || requestedHours.compareTo(BigDecimal.ZERO) <= 0
				|| requestedHours.compareTo(new BigDecimal("24")) > 0) {
			session.setAttribute("errorMsg", "Số giờ OT không hợp lệ (phải từ 0.5 đến 24).");
			response.sendRedirect(redirectList);
			return;
		}

		if (ValidationUtil.isBlank(reason)) {
			session.setAttribute("errorMsg", "Vui lòng nhập lý do tăng ca.");
			response.sendRedirect(redirectList);
			return;
		}

		if (overtimeDAO.hasPendingByUserAndDate(userId, date)) {
			session.setAttribute("errorMsg", "Nhân viên " + targetUser.getFullName()
					+ " đã có yêu cầu OT đang chờ duyệt cho ngày " + date + ".");
			response.sendRedirect(redirectList);
			return;
		}

		OvertimeRecord record = new OvertimeRecord();
		record.setUserId(userId);
		record.setDate(date);
		record.setRequestedHours(requestedHours);
		record.setReason(reason.trim());

		boolean success = overtimeDAO.insert(record);
		if (success) {
			session.setAttribute("successMsg", "Tạo yêu cầu OT cho " + targetUser.getFullName() + " thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể tạo yêu cầu OT. Vui lòng thử lại.");
		}

		response.sendRedirect(redirectList);
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

	private LocalDate resolveMinOtDate(LocalDate today) {
		LocalDate minDate = today.withDayOfMonth(1);
		LocalDate previousMonthStart = minDate.minusMonths(1);
		if (!monthlySheetDAO.isPeriodClosed(previousMonthStart.getYear(), previousMonthStart.getMonthValue())) {
			minDate = previousMonthStart;
		}
		return minDate;
	}

	private LocalDate resolveMaxOtDate(LocalDate today) {
		LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
		if (!monthlySheetDAO.isPeriodClosed(today.getYear(), today.getMonthValue())) {
			return monthEnd;
		}
		LocalDate nextMonth = today.plusMonths(1);
		return nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
	}

	private String validateOtDate(LocalDate otDate) {
		LocalDate today = LocalDate.now(VN_ZONE);
		LocalDate minDate = resolveMinOtDate(today);
		LocalDate maxDate = resolveMaxOtDate(today);

		if (otDate.isBefore(minDate)) {
			return "Ngày OT phải từ " + minDate + " trở đi (không chọn tháng đã chốt công).";
		}
		if (otDate.isAfter(maxDate)) {
			return "Ngày OT không được sau " + maxDate + ". Chỉ đăng ký trong kỳ công đang mở.";
		}
		if (monthlySheetDAO.isPeriodClosed(otDate.getYear(), otDate.getMonthValue())) {
			return "Tháng " + otDate.getMonthValue() + "/" + otDate.getYear()
					+ " đã chốt công, không thể tạo OT cho ngày này.";
		}
		return null;
	}

	private Date parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			LocalDate parsed = LocalDate.parse(value.trim(), STRICT_DATE);
			return Date.valueOf(parsed);
		} catch (DateTimeParseException | IllegalArgumentException e) {
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