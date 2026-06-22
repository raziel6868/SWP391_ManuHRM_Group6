package controller.attendancecorrection;

import java.io.IOException;
import dal.AttendanceCorrectionDAO;
import dal.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.AttendanceCorrection;
import model.Permission;
import model.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "AttendanceCorrectionRejectServlet", urlPatterns = {"/attendance-correction-reject"})
public class AttendanceCorrectionRejectServlet extends HttpServlet {

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "ATTENDANCE_CORRECTION_REJECT")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu sửa.");
			response.sendRedirect(request.getContextPath() + "/attendance-correction-list");
			return;
		}

		Connection conn = null;
		try {
			Long id = Long.parseLong(idStr.trim());
			AttendanceCorrection correction = correctionDAO.getById(id);

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

			conn = DBContext.getConnection();
			conn.setAutoCommit(false);

			boolean success = correctionDAO.reject(conn, id, authUser.getId());
			if (success) {
				conn.commit();
				session.setAttribute("successMsg", "Đã từ chối yêu cầu sửa chấm công.");
			} else {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể từ chối yêu cầu. Trạng thái đã thay đổi.");
			}

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ignored) {
				}
			}
			session.setAttribute("errorMsg", "Lỗi cơ sở dữ liệu: " + e.getMessage());
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
