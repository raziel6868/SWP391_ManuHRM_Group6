package controller.attendancecorrection;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.AttendanceCorrection;
import model.AttendanceRecord;
import model.Permission;
import model.User;
import java.util.List;

@WebServlet(name = "AttendanceCorrectionRequestServlet", urlPatterns = {"/attendance-correction-request"})
public class AttendanceCorrectionRequestServlet extends HttpServlet {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null || !hasPermission(session, "ATTENDANCE_CORRECTION_REQUEST")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String recordIdStr = request.getParameter("attendanceRecordId");
		String newCheckInStr = request.getParameter("newCheckIn");
		String newCheckOutStr = request.getParameter("newCheckOut");
		String reason = request.getParameter("reason");

		if (recordIdStr == null || recordIdStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy bản ghi chấm công.");
			response.sendRedirect(request.getContextPath() + "/attendance-my");
			return;
		}

		Long recordId;
		try {
			recordId = Long.parseLong(recordIdStr.trim());
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID bản ghi không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/attendance-my");
			return;
		}

		AttendanceRecord record = attendanceDAO.getById(recordId);
		if (record == null) {
			session.setAttribute("errorMsg", "Bản ghi chấm công không tồn tại.");
			response.sendRedirect(request.getContextPath() + "/attendance-my");
			return;
		}

		if (!authUser.getId().equals(record.getUserId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền yêu cầu sửa bản ghi này.");
			response.sendRedirect(request.getContextPath() + "/attendance-my");
			return;
		}

		if (correctionDAO.hasPendingCorrection(recordId)) {
			session.setAttribute("errorMsg", "Đã có yêu cầu sửa đang chờ duyệt cho bản ghi này.");
			response.sendRedirect(request.getContextPath() + "/attendance-my");
			return;
		}

		Time newCheckIn = null;
		Time newCheckOut = null;

		if (newCheckInStr != null && !newCheckInStr.trim().isEmpty()) {
			try {
				LocalTime lt = LocalTime.parse(newCheckInStr.trim(), TIME_FORMAT);
				newCheckIn = Time.valueOf(lt);
			} catch (Exception e) {
				session.setAttribute("errorMsg", "Định dạng giờ vào không hợp lệ (HH:mm).");
				response.sendRedirect(request.getContextPath() + "/attendance-my");
				return;
			}
		}

		if (newCheckOutStr != null && !newCheckOutStr.trim().isEmpty()) {
			try {
				LocalTime lt = LocalTime.parse(newCheckOutStr.trim(), TIME_FORMAT);
				newCheckOut = Time.valueOf(lt);
			} catch (Exception e) {
				session.setAttribute("errorMsg", "Định dạng giờ ra không hợp lệ (HH:mm).");
				response.sendRedirect(request.getContextPath() + "/attendance-my");
				return;
			}
		}

		AttendanceCorrection correction = new AttendanceCorrection();
		correction.setAttendanceRecordId(recordId);
		correction.setRequestedBy(authUser.getId());
		correction.setNewCheckIn(newCheckIn);
		correction.setNewCheckOut(newCheckOut);
		correction.setReason(reason);

		boolean success = correctionDAO.insert(correction);
		if (success) {
			session.setAttribute("successMsg", "Đã gửi yêu cầu sửa chấm công thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể gửi yêu cầu. Vui lòng thử lại.");
		}

		response.sendRedirect(request.getContextPath() + "/attendance-my");
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
