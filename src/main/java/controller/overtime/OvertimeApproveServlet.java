package controller.overtime;

import java.io.IOException;
import java.math.BigDecimal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dal.OvertimeDAO;
import model.OvertimeRecord;
import model.Permission;
import model.User;
import java.util.List;

@WebServlet(name = "OvertimeApproveServlet", urlPatterns = {"/overtime-approve"})
public class OvertimeApproveServlet extends HttpServlet {

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "OT_APPROVE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		String approvedHoursStr = request.getParameter("approvedHours");

		if (idStr == null || idStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy bản ghi tăng ca.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		BigDecimal approvedHours = null;
		if (approvedHoursStr != null && !approvedHoursStr.trim().isEmpty()) {
			try {
				approvedHours = new BigDecimal(approvedHoursStr.trim());
				if (approvedHours.compareTo(BigDecimal.ZERO) <= 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				session.setAttribute("errorMsg", "Số giờ phê duyệt không hợp lệ.");
				response.sendRedirect(request.getContextPath() + "/overtime-list");
				return;
			}
		}

		try {
			Long id = Long.parseLong(idStr.trim());
			OvertimeRecord record = overtimeDAO.getById(id);

			if (record == null) {
				session.setAttribute("errorMsg", "Bản ghi tăng ca không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/overtime-list");
				return;
			}

			if (!"PENDING".equals(record.getStatus())) {
				session.setAttribute("errorMsg", "Bản ghi không ở trạng thái chờ duyệt.");
				response.sendRedirect(request.getContextPath() + "/overtime-list");
				return;
			}

			if (authUser.getId().equals(record.getUserId())) {
				session.setAttribute("errorMsg", "Bạn không thể tự phê duyệt yêu cầu tăng ca của mình.");
				response.sendRedirect(request.getContextPath() + "/overtime-list");
				return;
			}

			if (approvedHours == null) {
				approvedHours = record.getRequestedHours();
			}

			boolean success = overtimeDAO.approve(id, authUser.getId(), approvedHours);
			if (success) {
				session.setAttribute("successMsg", "Đã phê duyệt yêu cầu tăng ca.");
			} else {
				session.setAttribute("errorMsg", "Không thể phê duyệt. Trạng thái có thể đã thay đổi.");
			}

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
		}

		response.sendRedirect(request.getContextPath() + "/overtime-list");
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
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
}
