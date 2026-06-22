package controller.leaverequest;

import dal.DBContext;
import dal.LeaveBalanceDAO;
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

@WebServlet(name = "LeaveRequestFinalApproveServlet", urlPatterns = {"/leave-request-final-approve"})
public class LeaveRequestFinalApproveServlet extends HttpServlet {

	private final LeaveRequestDAO requestDAO = new LeaveRequestDAO();
	private final LeaveBalanceDAO balanceDAO = new LeaveBalanceDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "LEAVE_REQUEST_APPROVE_L2")) {
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

			if (!"APPROVED_LEVEL_1".equals(leaveRequest.getStatus())) {
				session.setAttribute("errorMsg", "Yêu cầu không ở trạng thái chờ duyệt cấp 2.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			conn = DBContext.getConnection();
			conn.setAutoCommit(false);

			boolean approveResult = requestDAO.finalApprove(conn, id, authUser.getId());
			if (!approveResult) {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể duyệt yêu cầu. Trạng thái có thể đã thay đổi.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			int currentYear = leaveRequest.getStartDate().toLocalDate().getYear();
			boolean balanceUpdated = balanceDAO.incrementUsedDays(conn, leaveRequest.getUserId(),
					leaveRequest.getLeaveTypeId(), currentYear, leaveRequest.getDays());

			if (!balanceUpdated) {
				conn.rollback();
				session.setAttribute("errorMsg", "Không thể cập nhật số ngày nghỉ phép.");
				response.sendRedirect(request.getContextPath() + "/leave-request-list");
				return;
			}

			conn.commit();
			session.setAttribute("successMsg", "Đã phê duyệt yêu cầu nghỉ phép thành công.");

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
