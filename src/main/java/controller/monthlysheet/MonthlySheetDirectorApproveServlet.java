package controller.monthlysheet;

import dal.AttendanceCorrectionDAO;
import dal.DBContext;
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

@WebServlet(name = "MonthlySheetDirectorApproveServlet", urlPatterns = {"/monthly-sheet-director-approve"})
public class MonthlySheetDirectorApproveServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "MONTHLY_SHEET_DIRECTOR_APPROVE") || !isDirector(authUser)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long sheetId = parseLong(request.getParameter("id"));
		if (sheetId == null) {
			session.setAttribute("errorMsg", "Không tìm thấy bảng công.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		MonthlySheet sheet = monthlySheetDAO.getById(sheetId);
		if (sheet == null || !"PENDING_DIRECTOR".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg", "Bảng công không ở trạng thái chờ Giám đốc phê duyệt.");
			response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
			return;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				List<String> closeConflicts = monthlySheetDAO.findCloseConflicts(conn, sheet.getYear(),
						sheet.getMonth());
				if (!closeConflicts.isEmpty()) {
					conn.rollback();
					session.setAttribute("monthlySheetConflictTitle",
							"Không thể đóng sổ vì dữ liệu Leave/OT/Attendance còn conflict.");
					session.setAttribute("monthlySheetConflicts", closeConflicts);
					response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
					return;
				}

				boolean closed = monthlySheetDAO.directorClose(conn, sheetId, authUser.getId());
				if (!closed) {
					conn.rollback();
					session.setAttribute("errorMsg", "Không thể đóng sổ. Vui lòng thử lại.");
					response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
					return;
				}

				int rejected = correctionDAO.rejectAllPendingInMonth(conn, sheet.getYear(), sheet.getMonth(),
						authUser.getId(),
						"Bảng công tháng " + sheet.getMonth() + "/" + sheet.getYear() + " đã được đóng sổ.");

				conn.commit();

				String msg = "Đã đóng sổ bảng công tháng " + sheet.getMonth() + "/" + sheet.getYear() + ".";
				if (rejected > 0) {
					msg += " Đã tự động từ chối " + rejected + " yêu cầu điều chỉnh còn tồn đọng.";
				}
				session.setAttribute("successMsg", msg);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Exception e) {
			System.err.println("MonthlySheetDirectorApproveServlet ERROR: " + e.getMessage());
			session.setAttribute("errorMsg", "Lỗi hệ thống khi đóng sổ.");
		}

		response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
	}

	private boolean isDirector(User user) {
		return user.getJobTitleId() != null && user.getJobTitleId() == 1L;
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
