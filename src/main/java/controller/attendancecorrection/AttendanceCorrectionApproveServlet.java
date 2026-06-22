package controller.attendancecorrection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import dal.AttendanceCorrectionDAO;
import dal.AttendanceDAO;
import dal.DBContext;
import dal.MonthlySheetDAO;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "AttendanceCorrectionApproveServlet", urlPatterns = {"/attendance-correction-approve"})
public class AttendanceCorrectionApproveServlet extends HttpServlet {

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "ATTENDANCE_CORRECTION_APPROVE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu sửa.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		Long correctionId;
		try {
			correctionId = Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		AttendanceCorrection correction = correctionDAO.getById(correctionId);
		if (correction == null) {
			session.setAttribute("errorMsg", "Yêu cầu sửa không tồn tại.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		if (!"PENDING".equals(correction.getStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		AttendanceRecord record = attendanceDAO.getById(correction.getAttendanceRecordId());
		if (record == null) {
			session.setAttribute("errorMsg", "Bản ghi chấm công không tồn tại.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(record.getDate());
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;

		if (monthlySheetDAO.isPeriodClosed(year, month)) {
			session.setAttribute("errorMsg",
					"Không thể duyệt sửa chấm công cho tháng đã đóng. Vui lòng mở lại bảng lương trước.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		Time newCheckIn = correction.getNewCheckIn() != null ? correction.getNewCheckIn() : record.getCheckIn();
		Time newCheckOut = correction.getNewCheckOut() != null ? correction.getNewCheckOut() : record.getCheckOut();
		BigDecimal workingHours = calculateWorkingHours(newCheckIn, newCheckOut);

		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			conn.setAutoCommit(false);

			boolean updated = correctionDAO.approve(conn, correctionId, authUser.getId());
			if (!updated) {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể duyệt yêu cầu. Trạng thái đã thay đổi.");
				response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
				return;
			}

			attendanceDAO.updateAfterCorrection(conn, record.getId(), newCheckIn, newCheckOut, workingHours);

			conn.commit();
			session.setAttribute("successMsg", "Duyệt sửa chấm công thành công.");

		} catch (Exception e) {
			System.err.println("Error approving correction: " + e.getMessage());
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ignored) {
				}
			}
			session.setAttribute("errorMsg", "Đã xảy ra lỗi khi duyệt.");
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException ignored) {
				}
			}
		}

		response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
	}

	private BigDecimal calculateWorkingHours(Time checkIn, Time checkOut) {
		if (checkIn == null || checkOut == null) {
			return BigDecimal.ZERO;
		}
		long totalMinutes = ChronoUnit.MINUTES.between(checkIn.toLocalTime(), checkOut.toLocalTime());
		if (totalMinutes < 0) {
			totalMinutes += 24 * 60;
		}
		if (totalMinutes < 0) {
			totalMinutes = 0;
		}
		return BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
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
