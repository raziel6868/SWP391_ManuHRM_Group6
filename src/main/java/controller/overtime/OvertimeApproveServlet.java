package controller.overtime;

import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import model.OvertimeRecord;
import model.User;

@WebServlet(name = "OvertimeApproveServlet", urlPatterns = {"/overtime-approve"})
public class OvertimeApproveServlet extends HttpServlet {

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

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

		Long id = parseLong(request.getParameter("id"));
		String redirectUrl = request.getContextPath() + "/overtime-list";

		if (id == null) {
			session.setAttribute("errorMsg", "Yêu cầu OT không hợp lệ.");
			response.sendRedirect(redirectUrl);
			return;
		}

		OvertimeRecord record = overtimeDAO.getById(id);
		if (record == null) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (!"PENDING".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý trước đó.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (authUser.getId().equals(record.getUserId())) {
			session.setAttribute("errorMsg", "Bạn không thể duyệt yêu cầu OT của chính mình.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (record.getDate() != null && monthlySheetDAO.isPeriodClosed(record.getDate().toLocalDate().getYear(),
				record.getDate().toLocalDate().getMonthValue())) {
			session.setAttribute("errorMsg", "Tháng của ngày OT đã chốt công, không thể duyệt.");
			response.sendRedirect(redirectUrl);
			return;
		}

		BigDecimal approvedHours = resolveApprovedHours(request.getParameter("approvedHours"),
				record.getRequestedHours());
		if (approvedHours == null) {
			session.setAttribute("errorMsg", "Số giờ duyệt không hợp lệ (phải từ 0.5 đến 24).");
			response.sendRedirect(redirectUrl);
			return;
		}

		boolean approved = overtimeDAO.approve(id, authUser.getId(), approvedHours);
		if (approved) {
			session.setAttribute("successMsg",
					"Đã duyệt " + approvedHours + " giờ OT cho " + record.getEmployeeName() + ".");
		} else {
			session.setAttribute("errorMsg", "Không thể duyệt yêu cầu (có thể đã được xử lý bởi người khác).");
		}

		response.sendRedirect(redirectUrl);
	}

	private BigDecimal resolveApprovedHours(String paramValue, BigDecimal requestedHours) {
		BigDecimal hours = parseBigDecimal(paramValue);
		if (hours == null) {
			hours = requestedHours;
		}
		if (hours == null || hours.compareTo(new BigDecimal("0.5")) < 0 || hours.compareTo(new BigDecimal("24")) > 0) {
			return null;
		}
		return hours;
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
