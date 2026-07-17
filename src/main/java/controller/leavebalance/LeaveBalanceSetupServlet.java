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
import java.time.Year;
import java.util.List;
import model.LeaveBalance;
import model.LeaveType;
import model.User;

@WebServlet(name = "LeaveBalanceSetupServlet", urlPatterns = {"/leave-balance-setup"})
public class LeaveBalanceSetupServlet extends HttpServlet {

	private static final int FORM_LIST_LIMIT = 1000;

	private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long selectedUserId = parseLong(normalizeText(request.getParameter("userId")));
		Integer selectedYear = parseInteger(normalizeText(request.getParameter("year")));
		if (selectedYear == null) {
			selectedYear = Year.now().getValue();
		}

		LeaveType annualLeaveType = leaveTypeDAO.getAnnualLeaveType();
		LeaveBalance existingBalance = null;
		if (selectedUserId != null && annualLeaveType != null) {
			existingBalance = leaveBalanceDAO.getByUserAndTypeAndYear(selectedUserId, annualLeaveType.getId(),
					selectedYear);
		}

		request.setAttribute("selectedUserId", selectedUserId);
		request.setAttribute("selectedYear", selectedYear);
		request.setAttribute("existingBalance", existingBalance);
		request.setAttribute("annualLeaveType", annualLeaveType);
		populateFormData(request);
		request.getRequestDispatcher("/views/leavebalance/leave-balance-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long userId = parseLong(normalizeText(request.getParameter("userId")));
		Integer year = parseInteger(normalizeText(request.getParameter("year")));

		String validationError = validate(userId, year);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("selectedUserId", userId);
			request.setAttribute("selectedYear", year);
			request.setAttribute("annualLeaveType", leaveTypeDAO.getAnnualLeaveType());
			populateFormData(request);
			request.getRequestDispatcher("/views/leavebalance/leave-balance-setup.jsp").forward(request, response);
			return;
		}

		if (userId == null) {
			int synced = leaveBalanceDAO.syncAnnualBalances(year);
			request.getSession().setAttribute("successMsg",
					"Đã đồng bộ hạn mức phép năm cho " + synced + " nhân viên.");
		} else if (leaveBalanceDAO.syncAnnualBalance(userId, year)) {
			request.getSession().setAttribute("successMsg", "Đồng bộ hạn mức phép năm thành công.");
		} else {
			request.getSession().setAttribute("errorMsg",
					"Không thể đồng bộ hạn mức. Hãy kiểm tra nhân viên đã có hợp đồng và loại ANNUAL_NORMAL đang hoạt động.");
		}

		response.sendRedirect(request.getContextPath() + "/leave-balance-list?year=" + year);
	}

	private String validate(Long userId, Integer year) {
		if (year == null) {
			return "Năm áp dụng không được để trống.";
		}
		if (year < 2000 || year > 2100) {
			return "Năm áp dụng không hợp lệ.";
		}
		if (leaveTypeDAO.getAnnualLeaveType() == null) {
			return "Chưa có loại nghỉ phép năm đang hoạt động.";
		}
		if (userId != null) {
			User user = userDAO.getById(userId);
			if (user == null || user.getIsActive() == null || !user.getIsActive()) {
				return "Nhân viên không tồn tại hoặc đã bị khóa.";
			}
		}
		return null;
	}

	private void populateFormData(HttpServletRequest request) {
		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, FORM_LIST_LIMIT);
		request.setAttribute("users", users);
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
}
