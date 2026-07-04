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

@WebServlet(name = "AttendanceCorrectionRejectServlet", urlPatterns = {"/attendance-correction-reject"})
public class AttendanceCorrectionRejectServlet extends HttpServlet {

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
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
		if (!hasPermission(session, "ATTENDANCE_CORRECTION_REJECT")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		String rejectReason = request.getParameter("rejectReason");
		String redirectUrl = request.getContextPath() + "/attendance-correction-list";

		if (id == null) {
			session.setAttribute("errorMsg", "Yêu cầu điều chỉnh không hợp lệ.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (rejectReason == null || rejectReason.isBlank()) {
			session.setAttribute("errorMsg", "Vui lòng nhập lý do từ chối.");
			response.sendRedirect(redirectUrl);
			return;
		}

		AttendanceCorrection correction = correctionDAO.getById(id);
		if (correction == null) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu điều chỉnh.");
			response.sendRedirect(redirectUrl);
			return;
		}

		int year = correction.getAttendanceDate().toLocalDate().getYear();
		int month = correction.getAttendanceDate().toLocalDate().getMonthValue();
		if (!monthlySheetDAO.isHrCorrectionWindow(year, month)) {
			session.setAttribute("errorMsg", "HR chỉ được xử lý điều chỉnh khi bảng công đang chờ HR duyệt.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!"APPROVED".equals(correction.getSupervisorStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này chưa được quản đốc xác nhận.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!"PENDING".equals(correction.getStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý trước đó.");
			response.sendRedirect(redirectUrl);
			return;
		}

		boolean rejected = correctionDAO.reject(id, authUser.getId(), rejectReason.trim());
		if (rejected) {
			session.setAttribute("successMsg",
					"Đã từ chối yêu cầu điều chỉnh công của " + correction.getEmployeeName() + ".");
		} else {
			session.setAttribute("errorMsg", "Không thể từ chối yêu cầu (có thể đã được xử lý bởi người khác).");
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
