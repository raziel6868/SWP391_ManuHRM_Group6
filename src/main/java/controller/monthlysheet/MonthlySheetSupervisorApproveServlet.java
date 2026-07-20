package controller.monthlysheet;

import dal.AttendanceCorrectionDAO;
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

@WebServlet(name = "MonthlySheetSupervisorApproveServlet", urlPatterns = {"/monthly-sheet-supervisor-approve"})
public class MonthlySheetSupervisorApproveServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final MonthlySheetApprovalDAO approvalDAO = new MonthlySheetApprovalDAO();
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "MONTHLY_SHEET_SUPERVISOR_APPROVE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.isBlank()) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng công.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor");
			return;
		}

		Long sheetId;
		try {
			sheetId = Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor");
			return;
		}

		MonthlySheet sheet = monthlySheetDAO.getById(sheetId);
		if (sheet == null || !"PENDING_SUPERVISOR".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg", "Bảng công không ở trạng thái chờ xác nhận.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor");
			return;
		}

		if (correctionDAO.hasPendingSupervisorCorrectionInMonth(authUser.getId(), sheet.getYear(), sheet.getMonth())) {
			session.setAttribute("errorMsg",
					"Vẫn còn yêu cầu điều chỉnh công chưa được duyệt. Vui lòng xử lý hết trước khi chốt.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor" + "?year=" + sheet.getYear()
					+ "&month=" + sheet.getMonth());
			return;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				boolean approved = approvalDAO.approve(conn, sheetId, authUser.getId());
				if (!approved) {
					conn.rollback();
					session.setAttribute("errorMsg", "Không thể chốt. Bạn có thể đã chốt trước đó.");
					response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor" + "?year="
							+ sheet.getYear() + "&month=" + sheet.getMonth());
					return;
				}

				boolean allApproved = monthlySheetDAO.allSupervisorsApproved(conn, sheetId);
				if (allApproved) {
					boolean advanced = monthlySheetDAO.advanceToHR(conn, sheetId);
					if (!advanced) {
						conn.rollback();
						session.setAttribute("errorMsg", "Không thể chuyển bảng công sang HR. Vui lòng thử lại.");
						response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor" + "?year="
								+ sheet.getYear() + "&month=" + sheet.getMonth());
						return;
					}
				}

				conn.commit();
				if (allApproved) {
					session.setAttribute("successMsg",
							"Đã chốt bảng công. Tất cả quản đốc đã xác nhận, bảng công chuyển sang HR duyệt.");
				} else {
					session.setAttribute("successMsg",
							"Đã chốt bảng công tháng " + sheet.getMonth() + "/" + sheet.getYear() + " thành công.");
				}
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Exception e) {
			System.err.println("MonthlySheetSupervisorApproveServlet ERROR: " + e.getMessage());
			session.setAttribute("errorMsg", "Lỗi hệ thống khi chốt bảng công.");
		}

		response.sendRedirect(request.getContextPath() + "/monthly-sheet-supervisor" + "?year=" + sheet.getYear()
				+ "&month=" + sheet.getMonth());
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
