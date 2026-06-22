package controller.overtime;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import dal.OvertimeDAO;
import dal.DepartmentDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.OvertimeRecord;
import model.Permission;
import model.User;

@WebServlet(name = "OvertimeRequestServlet", urlPatterns = {"/overtime-request"})
public class OvertimeRequestServlet extends HttpServlet {

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "OT_REQUEST")) {
			session.setAttribute("errorMsg", "Bạn không có quyền thực hiện thao tác này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, 100);
		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("users", users);
		request.setAttribute("departments", departments);

		request.getRequestDispatcher("/views/overtime/overtime-request.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (!hasPermission(session, "OT_REQUEST")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String userIdStr = request.getParameter("userId");
		String dateStr = request.getParameter("date");
		String hoursStr = request.getParameter("requestedHours");
		String reason = request.getParameter("reason");

		if (userIdStr == null || userIdStr.trim().isEmpty() || dateStr == null || dateStr.trim().isEmpty()
				|| hoursStr == null || hoursStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng điền đầy đủ thông tin bắt buộc.");
			response.sendRedirect(request.getContextPath() + "/overtime-request");
			return;
		}

		try {
			Long userId = Long.parseLong(userIdStr.trim());
			Date date = Date.valueOf(dateStr.trim());
			BigDecimal requestedHours = new BigDecimal(hoursStr.trim());

			if (requestedHours.compareTo(BigDecimal.ZERO) <= 0 || requestedHours.compareTo(new BigDecimal("24")) > 0) {
				session.setAttribute("errorMsg", "Số giờ tăng ca phải lớn hơn 0 và nhỏ hơn hoặc bằng 24.");
				response.sendRedirect(request.getContextPath() + "/overtime-request");
				return;
			}

			OvertimeRecord record = new OvertimeRecord();
			record.setUserId(userId);
			record.setDate(date);
			record.setRequestedHours(requestedHours);
			record.setReason(reason);

			boolean success = overtimeDAO.insert(record);
			if (success) {
				session.setAttribute("successMsg", "Đã gửi yêu cầu tăng ca thành công.");
				response.sendRedirect(request.getContextPath() + "/overtime-list");
			} else {
				session.setAttribute("errorMsg", "Không thể gửi yêu cầu. Vui lòng thử lại.");
				response.sendRedirect(request.getContextPath() + "/overtime-request");
			}

		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", "Dữ liệu không hợp lệ. Kiểm tra định dạng ngày (YYYY-MM-DD) và số giờ.");
			response.sendRedirect(request.getContextPath() + "/overtime-request");
		}
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
