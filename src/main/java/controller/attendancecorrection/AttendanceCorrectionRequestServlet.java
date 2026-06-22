package controller.attendancecorrection;

import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
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
import model.AttendanceCorrection;
import model.AttendanceRecord;
import model.User;
import util.ValidationUtil;

@WebServlet(name = "AttendanceCorrectionRequestServlet", urlPatterns = {"/attendance-correction-request"})
public class AttendanceCorrectionRequestServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

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

		if (!"EMPLOYEE".equals(authUser.getRoleName())) {
			response.sendRedirect(request.getContextPath() + "/views/error/403.jsp");
			return;
		}

		Long attendanceRecordId = parseLong(request.getParameter("attendanceRecordId"));
		Time newCheckIn = parseTime(request.getParameter("newCheckIn"));
		Time newCheckOut = parseTime(request.getParameter("newCheckOut"));
		String reason = request.getParameter("reason");

		AttendanceRecord record = attendanceDAO.getById(attendanceRecordId);
		if (record == null || !authUser.getId().equals(record.getUserId())) {
			response.sendRedirect(request.getContextPath() + "/views/error/403.jsp");
			return;
		}

		String redirectUrl = request.getContextPath() + "/attendance-my?year="
				+ record.getDate().toLocalDate().getYear() + "&month=" + record.getDate().toLocalDate().getMonthValue();

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

		AttendanceCorrection correction = new AttendanceCorrection();
		correction.setAttendanceRecordId(attendanceRecordId);
		correction.setRequestedBy(authUser.getId());
		correction.setNewCheckIn(newCheckIn);
		correction.setNewCheckOut(newCheckOut);
		correction.setReason(reason.trim());

		boolean success = correctionDAO.insert(correction);
		if (success) {
			session.setAttribute("successMsg", "Gửi yêu cầu điều chỉnh công thành công.");
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
}
