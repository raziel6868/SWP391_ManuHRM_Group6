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
import java.time.temporal.ChronoUnit;
import java.util.List;
import model.LeaveBalance;
import model.LeaveRequest;
import model.LeaveType;
import model.User;

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

		String validationError = validate(authUser.getId(), leaveTypeId, startDate, endDate, reason);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			populateFormData(request, authUser.getId(), balanceYear);
			request.getRequestDispatcher("/views/leaverequest/leave-request-create.jsp").forward(request, response);
			return;
		}

		long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		LeaveRequest leaveRequest = new LeaveRequest();
		leaveRequest.setUserId(authUser.getId());
		leaveRequest.setLeaveTypeId(leaveTypeId);
		leaveRequest.setStartDate(Date.valueOf(startDate));
		leaveRequest.setEndDate(Date.valueOf(endDate));
		leaveRequest.setDays(BigDecimal.valueOf(requestedDays));
		leaveRequest.setReason(reason);

		boolean success = leaveRequestDAO.insert(leaveRequest);
		if (success) {
			session.setAttribute("successMsg", "Gui don nghi phep thanh cong.");
			response.sendRedirect(request.getContextPath() + "/leave-request-my");
			return;
		}

		request.setAttribute("errorMsg", "Khong the gui don nghi phep. Vui long thu lai.");
		populateFormData(request, authUser.getId(), balanceYear);
		request.getRequestDispatcher("/views/leaverequest/leave-request-create.jsp").forward(request, response);
	}

	private String validate(Long userId, Long leaveTypeId, LocalDate startDate, LocalDate endDate, String reason) {
		if (leaveTypeId == null) {
			return "Vui long chon loai nghi.";
		}
		if (startDate == null) {
			return "Ngay bat dau khong hop le.";
		}
		if (endDate == null) {
			return "Ngay ket thuc khong hop le.";
		}
		if (endDate.isBefore(startDate)) {
			return "Ngay ket thuc khong duoc truoc ngay bat dau.";
		}
		if (startDate.getYear() != endDate.getYear()) {
			return "Don nghi khong duoc vuot qua 2 nam khac nhau.";
		}
		if (reason != null && reason.length() > 1000) {
			return "Ly do nghi khong duoc vuot qua 1000 ky tu.";
		}

		LeaveType leaveType = leaveTypeDAO.getById(leaveTypeId);
		if (leaveType == null || leaveType.getIsActive() == null || !leaveType.getIsActive()) {
			return "Loai nghi khong ton tai hoac da bi vo hieu hoa.";
		}

		long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		LeaveBalance balance = leaveBalanceDAO.getByUserAndTypeAndYear(userId, leaveTypeId, startDate.getYear());
		if (balance == null) {
			return "Chua thiet lap han muc nghi cho loai nghi nay trong nam " + startDate.getYear() + ".";
		}

		BigDecimal totalDays = balance.getTotalDays() == null ? BigDecimal.ZERO : balance.getTotalDays();
		BigDecimal usedDays = balance.getUsedDays() == null ? BigDecimal.ZERO : balance.getUsedDays();
		BigDecimal remainingDays = totalDays.subtract(usedDays);
		if (remainingDays.compareTo(BigDecimal.valueOf(requestedDays)) < 0) {
			return "So ngay nghi vuot qua han muc con lai.";
		}

		return null;
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
