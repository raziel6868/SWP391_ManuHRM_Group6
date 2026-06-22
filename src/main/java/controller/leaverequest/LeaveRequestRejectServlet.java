package controller.leaverequest;

import dal.DBContext;
import dal.LeaveRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.LeaveRequest;
import model.Permission;
import model.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "LeaveRequestRejectServlet", urlPatterns = {"/leave-request-reject"})
public class LeaveRequestRejectServlet extends HttpServlet {

	private final LeaveRequestDAO requestDAO = new LeaveRequestDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "LEAVE_REQUEST_REJECT")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu.");
			response.sendRedirect(request.getContextPath() + "/leave-request-list");
			return;
		}

		Connection conn = null;
		try {
			Long id = Long.parseLong(idStr.trim());
			LeaveRequest leaveRequest = requestDAO.getById(id);

			if (leaveRequest == null) {
				session.setAttribute("errorMsg", "Không tìm thấy yêu cầu.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			String currentStatus = leaveRequest.getStatus();
			if (!"PENDING".equals(currentStatus) && !"APPROVED_LEVEL_1".equals(currentStatus)) {
				session.setAttribute("errorMsg", "Yêu cầu không ở trạng thái có thể từ chối.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			conn = DBContext.getConnection();
			conn.setAutoCommit(false);

			boolean success = requestDAO.reject(conn, id, authUser.getId());
			if (success) {
				conn.commit();
				session.setAttribute("successMsg", "Đã từ chối yêu cầu nghỉ phép.");
			} else {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể từ chối yêu cầu. Trạng thái có thể đã thay đổi.");
			}

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID yêu cầu không hợp lệ.");
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

		response.sendRedirect(request.getContextPath() + "/leave-request-list");
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
