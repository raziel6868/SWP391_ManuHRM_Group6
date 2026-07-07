package controller.attendancecorrection;

import dal.AttendanceCorrectionDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.AttendanceCorrection;
import model.Permission;
import model.User;

@WebServlet(name = "AttendanceCorrectionSupervisorApproveServlet", urlPatterns = {
		"/attendance-correction-supervisor-approve"})
public class AttendanceCorrectionSupervisorApproveServlet extends HttpServlet {

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "ATTENDANCE_CORRECTION_SUPERVISOR_APPROVE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String action = request.getParameter("action");
		Long id = parseLong(request.getParameter("id"));
		String rejectReason = request.getParameter("rejectReason");
		String redirectUrl = buildRedirectUrl(request);

		if (id == null) {
			session.setAttribute("errorMsg", "Yêu cầu điều chỉnh không hợp lệ.");
			response.sendRedirect(redirectUrl);
			return;
		}

		AttendanceCorrection correction = correctionDAO.getById(id);
		if (correction == null) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu điều chỉnh.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!authUser.getId().equals(correction.getSupervisorId())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		int year = correction.getAttendanceDate().toLocalDate().getYear();
		int month = correction.getAttendanceDate().toLocalDate().getMonthValue();

		if (!monthlySheetDAO.isSupervisorCorrectionWindow(year, month)) {
			session.setAttribute("errorMsg",
					"Quản đốc chỉ được xử lý điều chỉnh khi bảng công đang chờ quản đốc duyệt.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!"PENDING".equals(correction.getSupervisorStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý trước đó.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if ("approve".equals(action)) {
			boolean approved = correctionDAO.supervisorApprove(id, authUser.getId());
			if (approved) {
				session.setAttribute("successMsg", "Đã duyệt yêu cầu điều chỉnh của " + correction.getEmployeeName()
						+ ". Yêu cầu đã được chuyển lên HR xét duyệt.");
			} else {
				session.setAttribute("errorMsg", "Không thể duyệt. Vui lòng thử lại.");
			}
		} else if ("reject".equals(action)) {
			if (rejectReason == null || rejectReason.isBlank()) {
				session.setAttribute("errorMsg", "Vui lòng nhập lý do từ chối.");
				response.sendRedirect(redirectUrl);
				return;
			}
			boolean rejected = correctionDAO.supervisorReject(id, authUser.getId(), rejectReason.trim());
			if (rejected) {
				session.setAttribute("successMsg",
						"Đã từ chối yêu cầu điều chỉnh của " + correction.getEmployeeName() + ".");
			} else {
				session.setAttribute("errorMsg", "Không thể từ chối. Vui lòng thử lại.");
			}
		} else {
			session.setAttribute("errorMsg", "Hành động không hợp lệ.");
		}

		response.sendRedirect(redirectUrl);
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

	/**
	 * Luôn quay lại đúng /attendance-correction-list?tab=supervisor, giữ nguyên
	 * status/page hiện tại (nếu có) để không bị mất filter sau khi duyệt/từ chối.
	 */
	private String buildRedirectUrl(HttpServletRequest request) {
		StringBuilder url = new StringBuilder(request.getContextPath())
				.append("/attendance-correction-list?tab=supervisor");
		String status = request.getParameter("status");
		if (status != null && !status.isBlank()) {
			url.append("&status=").append(status.trim().toUpperCase());
		}
		String page = request.getParameter("page");
		if (page != null && !page.isBlank()) {
			url.append("&page=").append(page.trim());
		}
		return url.toString();
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