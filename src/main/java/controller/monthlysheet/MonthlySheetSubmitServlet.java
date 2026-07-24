package controller.monthlysheet;

import dal.DBContext;
import dal.MonthlySheetApprovalDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import model.MonthlySheet;
import model.Permission;
import model.User;

@WebServlet(name = "MonthlySheetSubmitServlet", urlPatterns = {"/monthly-sheet-submit"})
public class MonthlySheetSubmitServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final MonthlySheetApprovalDAO approvalDAO = new MonthlySheetApprovalDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "MONTHLY_SHEET_SUBMIT")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.isBlank()) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng công.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		Long sheetId;
		try {
			sheetId = Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		MonthlySheet sheet = monthlySheetDAO.getById(sheetId);
		if (sheet == null) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng công.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		if (!"OPEN".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg", "Bảng công phải ở trạng thái OPEN mới có thể gửi duyệt.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				List<String> departmentsWithoutHead = approvalDAO.findActiveDepartmentsWithoutHead(conn);
				if (!departmentsWithoutHead.isEmpty()) {
					conn.rollback();
					session.setAttribute("errorMsg",
							"Chưa xác định trưởng phòng cho: " + String.join(", ", departmentsWithoutHead)
									+ ". Vui lòng cập nhật cơ cấu quản lý trước khi gửi duyệt.");
					response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
					return;
				}

				boolean submitted = monthlySheetDAO.submit(conn, sheetId, authUser.getId());
				if (!submitted) {
					conn.rollback();
					session.setAttribute("errorMsg", "Không thể gửi duyệt. Vui lòng thử lại.");
					response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
					return;
				}

				int createdApprovals = approvalDAO.createForDepartmentHeads(conn, sheetId);
				if (createdApprovals <= 0) {
					conn.rollback();
					session.setAttribute("errorMsg",
							"Không tìm thấy trưởng phòng phù hợp. Vui lòng kiểm tra cơ cấu phòng ban và quản lý.");
					response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
					return;
				}

				conn.commit();
				session.setAttribute("successMsg", "Đã gửi bảng công tháng " + sheet.getMonth() + "/" + sheet.getYear()
						+ " đến các trưởng phòng để xác nhận.");
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Exception e) {
			System.err.println("MonthlySheetSubmitServlet ERROR: " + e.getMessage());
			session.setAttribute("errorMsg", "Lỗi hệ thống khi gửi duyệt.");
		}

		response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (code.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}
}
