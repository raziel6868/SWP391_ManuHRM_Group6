package controller.attendancecorrection;

import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.AttendanceCorrection;
import model.AttendanceRecord;
import model.MonthlySheet;
import model.Permission;
import model.User;
import util.ValidationUtil;

@WebServlet(name = "AttendanceCorrectionRequestServlet", urlPatterns = {"/attendance-correction-request"})
public class AttendanceCorrectionRequestServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
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

		// Trước đây chỉ EMPLOYEE được gửi yêu cầu điều chỉnh công. Giờ mở rộng theo
		// permission ATTENDANCE_CORRECTION_REQUEST để quản đốc (PRODUCTION_SUPERVISOR)
		// và HR (HR_MANAGER) cũng có thể tự gửi yêu cầu điều chỉnh cho chính mình.
		if (!hasPermission(session, "ATTENDANCE_CORRECTION_REQUEST")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long attendanceRecordId = parseLong(request.getParameter("attendanceRecordId"));
		Time newCheckIn = parseTime(request.getParameter("newCheckIn"));
		Time newCheckOut = parseTime(request.getParameter("newCheckOut"));
		String reason = request.getParameter("reason");

		AttendanceRecord record = attendanceDAO.getById(attendanceRecordId);
		if (record == null || !authUser.getId().equals(record.getUserId())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		int year = record.getDate().toLocalDate().getYear();
		int month = record.getDate().toLocalDate().getMonthValue();
		String redirectUrl = request.getContextPath() + "/attendance-my?year=" + year + "&month=" + month;

		// Trước đây bắt buộc tháng phải đang ở trạng thái OPEN mới được gửi yêu cầu,
		// khiến việc gửi/duyệt điều chỉnh bị khoá cứng theo tiến độ workflow chốt
		// bảng công tháng (HR phải mở duyệt trước). Giờ chỉ chặn khi bảng công đã
		// CLOSED (đã chốt sổ hoàn toàn) — dữ liệu lịch sử không được đổi nữa.
		MonthlySheet sheet = monthlySheetDAO.getOrCreate(year, month);
		if (sheet != null && "CLOSED".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg", "Bảng công tháng " + month + "/" + year
					+ " đã được chốt sổ, không thể gửi yêu cầu điều chỉnh công.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (newCheckIn == null || newCheckOut == null) {
			session.setAttribute("errorMsg", "Giờ điều chỉnh không hợp lệ.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (!newCheckOut.toLocalTime().isAfter(newCheckIn.toLocalTime())) {
			session.setAttribute("errorMsg", "Giờ ra mới phải lớn hơn giờ vào mới.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (ValidationUtil.isBlank(reason)) {
			session.setAttribute("errorMsg", "Vui lòng nhập lý do điều chỉnh công.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (correctionDAO.hasPendingByRecordId(attendanceRecordId)) {
			session.setAttribute("errorMsg", "Bản ghi này đã có yêu cầu điều chỉnh đang chờ xử lý.");
			response.sendRedirect(redirectUrl);
			return;
		}

		// Chỉ EMPLOYEE (công nhân xưởng, IT staff, ...) mới cần bước 1 do quản lý
		// trực tiếp (manager_id) duyệt. Các role quản lý — PRODUCTION_SUPERVISOR,
		// HR_MANAGER, SYSADMIN — không có ý nghĩa nghiệp vụ khi phải chờ ai đó
		// "duyệt bước 1" hộ mình (quản đốc/HR manager tự quản lý chính mình, còn
		// SYSADMIN không thuộc chuỗi quản lý sản xuất/nhân sự), nên bỏ hẳn bước 1,
		// request đi thẳng vào hàng chờ HR duyệt bước 2.
		boolean skipSupervisorStep = !"EMPLOYEE".equals(authUser.getRoleName());

		Long supervisorId = null;
		if (!skipSupervisorStep) {
			supervisorId = authUser.getManagerId();
			if (supervisorId == null) {
				session.setAttribute("errorMsg", "Tài khoản của bạn chưa được gán quản đốc. Vui lòng liên hệ HR.");
				response.sendRedirect(redirectUrl);
				return;
			}
		}

		AttendanceCorrection correction = new AttendanceCorrection();
		correction.setAttendanceRecordId(attendanceRecordId);
		correction.setRequestedBy(authUser.getId());
		correction.setNewCheckIn(newCheckIn);
		correction.setNewCheckOut(newCheckOut);
		correction.setReason(reason.trim());
		correction.setSupervisorId(supervisorId);

		boolean success = correctionDAO.insert(correction, skipSupervisorStep);
		if (success) {
			if (skipSupervisorStep) {
				session.setAttribute("successMsg", "Gửi yêu cầu điều chỉnh công thành công. Đang chờ HR duyệt.");
			} else {
				session.setAttribute("successMsg",
						"Gửi yêu cầu điều chỉnh công thành công. Đang chờ quản đốc xác nhận.");
			}
		} else {
			session.setAttribute("errorMsg", "Không thể gửi yêu cầu điều chỉnh công. Vui lòng thử lại.");
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

	private Time parseTime(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Time.valueOf(LocalTime.parse(value.trim()));
		} catch (DateTimeParseException e) {
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