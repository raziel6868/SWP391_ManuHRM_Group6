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
import model.LeaveBalance;
import model.LeaveRequest;
import model.LeaveType;
import model.Permission;
import model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet(name = "LeaveRequestCreateServlet", urlPatterns = {"/leave-request-create"})
public class LeaveRequestCreateServlet extends HttpServlet {

	private final LeaveRequestDAO requestDAO = new LeaveRequestDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
	private final LeaveBalanceDAO balanceDAO = new LeaveBalanceDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "LEAVE_MY_CREATE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		List<LeaveType> leaveTypes = leaveTypeDAO.searchLeaveTypes(null, null, true, 0, 100);
		int currentYear = java.time.Year.now().getValue();
		List<LeaveBalance> balances = balanceDAO.getByUserAndYear(authUser.getId(), currentYear);

		request.setAttribute("leaveTypes", leaveTypes);
		request.setAttribute("balances", balances);
		request.setAttribute("currentYear", currentYear);

		request.getRequestDispatcher("/views/leaverequest/leave-request-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "LEAVE_MY_CREATE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String leaveTypeIdStr = request.getParameter("leaveTypeId");
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String reason = request.getParameter("reason");

		if (leaveTypeIdStr == null || leaveTypeIdStr.trim().isEmpty() || startDateStr == null
				|| startDateStr.trim().isEmpty() || endDateStr == null || endDateStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng điền đầy đủ thông tin bắt buộc.");
			response.sendRedirect(request.getContextPath() + "/leave-request-create");
			return;
		}

		try {
			Long leaveTypeId = Long.parseLong(leaveTypeIdStr.trim());
			Date startDate = Date.valueOf(startDateStr.trim());
			Date endDate = Date.valueOf(endDateStr.trim());

			if (endDate.before(startDate)) {
				session.setAttribute("errorMsg", "Ngày kết thúc phải sau ngày bắt đầu.");
				response.sendRedirect(request.getContextPath() + "/leave-request-create");
				return;
			}

			long daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
			BigDecimal requestedDays = BigDecimal.valueOf(daysBetween);

			int currentYear = java.time.Year.now().getValue();
			LeaveBalance balance = balanceDAO.getByUserAndTypeAndYear(authUser.getId(), leaveTypeId, currentYear);

			if (balance == null) {
				session.setAttribute("errorMsg", "Bạn chưa có ngày nghỉ phép cho loại nghỉ này. Vui lòng liên hệ HR.");
				response.sendRedirect(request.getContextPath() + "/leave-request-create");
				return;
			}

			BigDecimal remainingDays = balance.getRemainingDays();
			if (requestedDays.compareTo(remainingDays) > 0) {
				session.setAttribute("errorMsg", "Không đủ ngày nghỉ phép. Bạn chỉ còn " + remainingDays + " ngày.");
				response.sendRedirect(request.getContextPath() + "/leave-request-create");
				return;
			}

			LeaveRequest leaveRequest = new LeaveRequest();
			leaveRequest.setUserId(authUser.getId());
			leaveRequest.setLeaveTypeId(leaveTypeId);
			leaveRequest.setStartDate(startDate);
			leaveRequest.setEndDate(endDate);
			leaveRequest.setDays(requestedDays);
			leaveRequest.setReason(reason);
			leaveRequest.setStatus("PENDING");

			boolean success = requestDAO.insert(leaveRequest);
			if (success) {
				session.setAttribute("successMsg", "Đã gửi yêu cầu nghỉ phép thành công.");
				response.sendRedirect(request.getContextPath() + "/leave-request-my");
			} else {
				session.setAttribute("errorMsg", "Không thể tạo yêu cầu. Vui lòng thử lại.");
				response.sendRedirect(request.getContextPath() + "/leave-request-create");
			}

		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", "Định dạng ngày không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/leave-request-create");
		}
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
