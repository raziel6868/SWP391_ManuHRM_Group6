package controller.leaverequest;

import dal.LeaveBalanceDAO;
import dal.LeaveRequestDAO;
import dal.LeaveTypeDAO;
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
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.LeaveBalance;
import model.LeaveRequest;
import model.LeaveType;
import model.User;
import util.LeavePolicyUtil;

@WebServlet(name = "LeaveRequestCreateServlet", urlPatterns = {"/leave-request-create"})
public class LeaveRequestCreateServlet extends HttpServlet {

	private static final int FORM_LIST_LIMIT = 1000;

	private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User authUser = session == null ? null : (User) session.getAttribute("authUser");
		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		populateFormData(request, authUser.getId(), Year.now().getValue());
		request.getRequestDispatcher("/views/leaverequest/leave-request-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession(false);
		User authUser = session == null ? null : (User) session.getAttribute("authUser");
		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		Long leaveTypeId = parseLong(normalizeText(request.getParameter("leaveTypeId")));
		String startDateText = normalizeText(request.getParameter("startDate"));
		String endDateText = normalizeText(request.getParameter("endDate"));
		String reason = normalizeText(request.getParameter("reason"));

		request.setAttribute("selectedLeaveTypeId", leaveTypeId);
		request.setAttribute("startDate", startDateText);
		request.setAttribute("endDate", endDateText);
		request.setAttribute("reason", reason);

		LocalDate startDate = parseDate(startDateText);
		LocalDate endDate = parseDate(endDateText);
		Integer balanceYear = startDate == null ? Year.now().getValue() : startDate.getYear();
		LeaveType leaveType = leaveTypeDAO.getById(leaveTypeId);
		BigDecimal requestedDays = BigDecimal.ZERO;
		if (startDate != null && endDate != null && leaveType != null) {
			requestedDays = LeavePolicyUtil.calculateRequestDays(startDate, endDate, leaveType.getDayCountMethod());
		}

		String validationError = validate(authUser.getId(), leaveType, startDate, endDate, reason, requestedDays);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			populateFormData(request, authUser.getId(), balanceYear);
			request.getRequestDispatcher("/views/leaverequest/leave-request-create.jsp").forward(request, response);
			return;
		}

		LeaveRequest leaveRequest = new LeaveRequest();
		leaveRequest.setUserId(authUser.getId());
		leaveRequest.setLeaveTypeId(leaveTypeId);
		leaveRequest.setStartDate(Date.valueOf(startDate));
		leaveRequest.setEndDate(Date.valueOf(endDate));
		leaveRequest.setDays(requestedDays);
		leaveRequest.setReason(reason);

		boolean success = leaveRequestDAO.insert(leaveRequest);
		if (success) {
			session.setAttribute("successMsg", "Gửi đơn nghỉ thành công.");
			response.sendRedirect(request.getContextPath() + "/leave-request-my");
			return;
		}

		request.setAttribute("errorMsg", "Không thể gửi đơn nghỉ. Vui lòng thử lại.");
		populateFormData(request, authUser.getId(), balanceYear);
		request.getRequestDispatcher("/views/leaverequest/leave-request-create.jsp").forward(request, response);
	}

	private String validate(Long userId, LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason,
			BigDecimal requestedDays) {
		if (leaveType == null) {
			return "Vui lòng chọn loại nghỉ.";
		}
		if (startDate == null) {
			return "Ngày bắt đầu không hợp lệ.";
		}
		if (endDate == null) {
			return "Ngày kết thúc không hợp lệ.";
		}
		if (endDate.isBefore(startDate)) {
			return "Ngày kết thúc không được trước ngày bắt đầu.";
		}
		if (reason != null && reason.length() > 1000) {
			return "Lý do nghỉ không được vượt quá 1000 ký tự.";
		}
		if (leaveType.getIsActive() == null || !leaveType.getIsActive()) {
			return "Loại nghỉ không tồn tại hoặc đã bị vô hiệu hóa.";
		}

		boolean requiresBalance = requiresBalance(leaveType);
		if (requiresBalance && startDate.getYear() != endDate.getYear()) {
			return "Đơn nghỉ có trừ quỹ phép không được vượt qua 2 năm khác nhau.";
		}
		if (requestedDays == null || requestedDays.compareTo(BigDecimal.ZERO) <= 0) {
			return "Khoảng thời gian nghỉ không có ngày hợp lệ theo cách tính của loại nghỉ.";
		}
		if (leaveType.getMaxDays() != null && requestedDays.compareTo(leaveType.getMaxDays()) > 0) {
			return "Số ngày nghỉ vượt quá hạn mức tối đa của loại nghỉ này.";
		}
		if (leaveRequestDAO.hasOverlappingActiveRequest(userId, Date.valueOf(startDate), Date.valueOf(endDate), null)) {
			return "Bạn đã có đơn nghỉ đang chờ duyệt hoặc đã duyệt trùng với khoảng thời gian này.";
		}
		if (!requiresBalance) {
			return null;
		}

		LeaveBalance balance = leaveBalanceDAO.getByUserAndTypeAndYear(userId, leaveType.getId(), startDate.getYear());
		if (balance == null && Boolean.TRUE.equals(leaveType.getIsAnnualLeave())) {
			leaveBalanceDAO.syncAnnualBalance(userId, startDate.getYear());
			balance = leaveBalanceDAO.getByUserAndTypeAndYear(userId, leaveType.getId(), startDate.getYear());
		}
		if (balance == null) {
			return "Chưa thiết lập hạn mức nghỉ cho loại nghỉ này trong năm " + startDate.getYear() + ".";
		}

		BigDecimal totalDays = balance.getTotalDays() == null ? BigDecimal.ZERO : balance.getTotalDays();
		BigDecimal usedDays = balance.getUsedDays() == null ? BigDecimal.ZERO : balance.getUsedDays();
		BigDecimal remainingDays = totalDays.subtract(usedDays);
		if (remainingDays.compareTo(requestedDays) < 0) {
			return "Số ngày nghỉ vượt quá hạn mức còn lại.";
		}

		return null;
	}

	private boolean requiresBalance(LeaveType leaveType) {
		return Boolean.TRUE.equals(leaveType.getRequiresBalance()) || Boolean.TRUE.equals(leaveType.getIsAnnualLeave());
	}

	private void populateFormData(HttpServletRequest request, Long userId, Integer year) {
		List<LeaveType> leaveTypes = leaveTypeDAO.searchLeaveTypes(null, null, true, 0, FORM_LIST_LIMIT);
		List<LeaveBalance> balances = leaveBalanceDAO.searchByUserAndYear(userId, year);
		request.setAttribute("leaveTypes", leaveTypes);
		request.setAttribute("balances", balances);
		request.setAttribute("balanceYear", year);
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Long parseLong(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LocalDate parseDate(String value) {
		if (value == null) {
			return null;
		}
		try {
			return LocalDate.parse(value);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
