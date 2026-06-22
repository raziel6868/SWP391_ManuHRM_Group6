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
import model.User;
import java.util.List;

@WebServlet(name = "MonthlySheetCloseServlet", urlPatterns = {"/monthly-sheet-close"})
public class MonthlySheetCloseServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "MONTHLY_SHEET_CLOSE")) {
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
			boolean success = monthlySheetDAO.closeSheet(id, authUser.getId());

			if (success) {
				session.setAttribute("successMsg", "Đã đóng bảng công/tháng thành công.");
			} else {
				session.setAttribute("errorMsg", "Không thể đóng bảng. Bảng có thể đã được đóng hoặc không tồn tại.");
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
