package controller.shiftassignment;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import dal.ShiftAssignmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;
import model.ShiftAssignment;

@WebServlet(name = "ShiftAssignmentBulkServlet", urlPatterns = {"/shift-assignment-bulk"})
public class ShiftAssignmentBulkServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (!hasPermission(session, "SHIFT_ASSIGNMENT_BULK")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String userIdsParam = request.getParameter("userIds");
		String shiftIdParam = request.getParameter("shiftId");
		String dateParam = request.getParameter("date");

		if (userIdsParam == null || userIdsParam.trim().isEmpty() || shiftIdParam == null
				|| shiftIdParam.trim().isEmpty() || dateParam == null || dateParam.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng điền đầy đủ thông tin bắt buộc.");
			response.sendRedirect(request.getContextPath() + "/shift-assignment-list");
			return;
		}

		try {
			Long shiftId = Long.parseLong(shiftIdParam.trim());
			Date date = Date.valueOf(dateParam.trim());

			String[] userIdParts = userIdsParam.split(",");
			List<ShiftAssignment> assignments = new ArrayList<>();

			for (String part : userIdParts) {
				String trimmed = part.trim();
				if (!trimmed.isEmpty()) {
					try {
						Long userId = Long.parseLong(trimmed);
						ShiftAssignment sa = new ShiftAssignment();
						sa.setUserId(userId);
						sa.setShiftId(shiftId);
						sa.setDate(date);
						assignments.add(sa);
					} catch (NumberFormatException e) {
					}
				}
			}

			if (assignments.isEmpty()) {
				session.setAttribute("errorMsg", "Không có nhân viên nào được chọn.");
				response.sendRedirect(request.getContextPath() + "/shift-assignment-list");
				return;
			}

			int count = shiftAssignmentDAO.bulkUpsert(assignments);

			if (count > 0) {
				session.setAttribute("successMsg", "Phân ca hàng loạt thành công cho " + count + " nhân viên.");
			} else {
				session.setAttribute("errorMsg", "Không thể phân ca hàng loạt. Vui lòng thử lại.");
			}

		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", "Ngày không hợp lệ (định dạng: YYYY-MM-DD).");
		} catch (Exception e) {
			System.err.println("Error bulk assigning shifts: " + e.getMessage());
			session.setAttribute("errorMsg", "Đã xảy ra lỗi khi phân ca hàng loạt.");
		}

		response.sendRedirect(request.getContextPath() + "/shift-assignment-list");
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
}
