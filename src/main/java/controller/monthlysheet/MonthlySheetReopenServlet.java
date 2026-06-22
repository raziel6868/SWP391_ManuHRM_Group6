package controller.monthlysheet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dal.MonthlySheetDAO;
import model.Permission;
import java.util.List;

@WebServlet(name = "MonthlySheetReopenServlet", urlPatterns = {"/monthly-sheet-reopen"})
public class MonthlySheetReopenServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (!hasPermission(permissions, "MONTHLY_SHEET_REOPEN")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng lương.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr.trim());
			boolean success = monthlySheetDAO.reopenSheet(id);

			if (success) {
				session.setAttribute("successMsg",
						"Đã mở lại bảng công/tháng thành công. Có thể nhập công và sửa chấm công.");
			} else {
				session.setAttribute("errorMsg",
						"Không thể mở lại bảng. Bảng có thể đang ở trạng thái đã mở hoặc không tồn tại.");
			}

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
		}

		response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
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
