package controller.leavebalance;

import dal.LeaveBalanceDAO;
import dal.LeaveTypeDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import model.LeaveBalance;
import model.LeaveType;
import model.User;

@WebServlet(name = "LeaveBalanceSetupServlet", urlPatterns = {"/leave-balance-setup"})
public class LeaveBalanceSetupServlet extends HttpServlet {

	private static final int FORM_LIST_LIMIT = 1000;
	private static final BigDecimal MAX_TOTAL_DAYS = new BigDecimal("999.99");

	private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long selectedUserId = parseLong(normalizeText(request.getParameter("userId")));
		Long selectedLeaveTypeId = parseLong(normalizeText(request.getParameter("leaveTypeId")));
		Integer selectedYear = parseInteger(normalizeText(request.getParameter("year")));
		if (selectedYear == null) {
			selectedYear = Year.now().getValue();
		}

		LeaveBalance existingBalance = null;
		if (selectedUserId != null && selectedLeaveTypeId != null) {
			existingBalance = leaveBalanceDAO.getByUserAndTypeAndYear(selectedUserId, selectedLeaveTypeId,
					selectedYear);
		}

		request.setAttribute("selectedUserId", selectedUserId);
		request.setAttribute("selectedLeaveTypeId", selectedLeaveTypeId);
		request.setAttribute("selectedYear", selectedYear);
		request.setAttribute("existingBalance", existingBalance);
		populateFormData(request);
		request.getRequestDispatcher("/views/leavebalance/leave-balance-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long userId = parseLong(normalizeText(request.getParameter("userId")));
		Long leaveTypeId = parseLong(normalizeText(request.getParameter("leaveTypeId")));
		Integer year = parseInteger(normalizeText(request.getParameter("year")));
		String totalDaysText = normalizeText(request.getParameter("totalDays"));
		BigDecimal totalDays = parseBigDecimal(totalDaysText);

		request.setAttribute("selectedUserId", userId);
		request.setAttribute("selectedLeaveTypeId", leaveTypeId);
		request.setAttribute("selectedYear", year);
		request.setAttribute("totalDays", totalDaysText);

		String validationError = validate(userId, leaveTypeId, year, totalDays);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			populateFormData(request);
			request.getRequestDispatcher("/views/leavebalance/leave-balance-setup.jsp").forward(request, response);
			return;
		}

		boolean success = leaveBalanceDAO.upsert(userId, leaveTypeId, year, totalDays);
		if (success) {
			request.getSession().setAttribute("successMsg", "Thiet lap han muc nghi phep thanh cong.");
			response.sendRedirect(request.getContextPath() + "/leave-balance-list?year=" + year);
			return;
		}

		request.setAttribute("errorMsg", "Khong the thiet lap han muc nghi phep. Vui long thu lai.");
		populateFormData(request);
		request.getRequestDispatcher("/views/leavebalance/leave-balance-setup.jsp").forward(request, response);
	}

	private String validate(Long userId, Long leaveTypeId, Integer year, BigDecimal totalDays) {
		if (userId == null) {
			return "Vui long chon nhan vien.";
		}
		if (leaveTypeId == null) {
			return "Vui long chon loai nghi.";
		}
		if (year == null) {
			return "Nam ap dung khong duoc de trong.";
		}
		if (year < 2000 || year > 2100) {
			return "Nam ap dung khong hop le.";
		}
		if (totalDays == null) {
			return "Tong so ngay phep khong hop le.";
		}
		if (totalDays.compareTo(BigDecimal.ZERO) < 0) {
			return "Tong so ngay phep khong duoc nho hon 0.";
		}
		if (totalDays.compareTo(MAX_TOTAL_DAYS) > 0) {
			return "Tong so ngay phep khong duoc vuot qua 999.99.";
		}

		User user = userDAO.getById(userId);
		if (user == null || user.getIsActive() == null || !user.getIsActive()) {
			return "Nhan vien khong ton tai hoac da bi khoa.";
		}

		LeaveType leaveType = leaveTypeDAO.getById(leaveTypeId);
		if (leaveType == null || leaveType.getIsActive() == null || !leaveType.getIsActive()) {
			return "Loai nghi khong ton tai hoac da bi vo hieu hoa.";
		}

		LeaveBalance existingBalance = leaveBalanceDAO.getByUserAndTypeAndYear(userId, leaveTypeId, year);
		if (existingBalance != null && existingBalance.getUsedDays() != null
				&& totalDays.compareTo(existingBalance.getUsedDays()) < 0) {
			return "Tong so ngay phep khong duoc nho hon so ngay da su dung.";
		}

		return null;
	}

	private void populateFormData(HttpServletRequest request) {
		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, FORM_LIST_LIMIT);
		List<LeaveType> leaveTypes = leaveTypeDAO.searchLeaveTypes(null, null, true, 0, FORM_LIST_LIMIT);
		request.setAttribute("users", users);
		request.setAttribute("leaveTypes", leaveTypes);
		request.setAttribute("currentYear", Year.now().getValue());
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Integer parseInteger(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
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

	private BigDecimal parseBigDecimal(String value) {
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
