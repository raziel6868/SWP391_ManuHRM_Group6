package controller.monthlysheet;

import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.MonthlySheet;
import model.Permission;
import model.User;

@WebServlet(name = "MonthlySheetRejectServlet", urlPatterns = {"/monthly-sheet-reject"})
public class MonthlySheetRejectServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		// Chỉ HR thuần hoặc Giám đốc mới được reject
		if (authUser == null || !hasPermission(session, "MONTHLY_SHEET_REJECT")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long sheetId = parseLong(request.getParameter("id"));
		String departmentIdStr = request.getParameter("departmentId");
		Long departmentId = parseLong(departmentIdStr); // null = reset tất cả

		if (sheetId == null) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng công.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		MonthlySheet sheet = monthlySheetDAO.getById(sheetId);
		if (sheet == null) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng công.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		// Chỉ cho reject khi đang trong quy trình duyệt
		String status = sheet.getStatus();
		if ("OPEN".equals(status) || "CLOSED".equals(status)) {
			session.setAttribute("errorMsg", "Không thể từ chối bảng công ở trạng thái " + status + ".");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		boolean rejected = monthlySheetDAO.reject(sheetId, departmentId);
		if (rejected) {
			String scope = departmentId != null ? "phòng ban liên quan" : "tất cả quản đốc";
			session.setAttribute("successMsg", "Đã từ chối và reset bảng công tháng " + sheet.getMonth() + "/"
					+ sheet.getYear() + " về OPEN. Xác nhận của " + scope + " đã được xóa.");
		} else {
			session.setAttribute("errorMsg", "Không thể từ chối. Vui lòng thử lại.");
		}

		response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank())
			return null;
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null)
			return false;
		for (Permission p : permissions) {
			if (code.equals(p.getCode()))
				return true;
		}
		return false;
	}
}
