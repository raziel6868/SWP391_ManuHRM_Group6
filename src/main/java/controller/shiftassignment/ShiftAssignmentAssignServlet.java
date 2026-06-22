package controller.shiftassignment;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import dal.DepartmentDAO;
import dal.ShiftAssignmentDAO;
import dal.ShiftDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import model.Shift;
import model.ShiftAssignment;
import model.User;

@WebServlet(name = "ShiftAssignmentAssignServlet", urlPatterns = {"/shift-assignment-assign"})
public class ShiftAssignmentAssignServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "SHIFT_ASSIGNMENT_ASSIGN")) {
			session.setAttribute("errorMsg", "Bạn không có quyền thực hiện thao tác này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String editId = request.getParameter("edit");
		ShiftAssignment assignment = null;

		if (editId != null && !editId.trim().isEmpty()) {
			try {
				Long id = Long.parseLong(editId.trim());
				assignment = shiftAssignmentDAO.getById(id);
			} catch (NumberFormatException e) {
			}
		}

		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, 100);
		List<Department> departments = departmentDAO.getActiveDepartments();
		List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);

		request.setAttribute("assignment", assignment);
		request.setAttribute("users", users);
		request.setAttribute("departments", departments);
		request.setAttribute("shifts", shifts);

		request.getRequestDispatcher("/views/shiftassignment/shift-assignment-assign.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (!hasPermission(session, "SHIFT_ASSIGNMENT_ASSIGN")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String userIdParam = request.getParameter("userId");
		String shiftIdParam = request.getParameter("shiftId");
		String dateParam = request.getParameter("date");

		if (userIdParam == null || userIdParam.trim().isEmpty() || shiftIdParam == null || shiftIdParam.trim().isEmpty()
				|| dateParam == null || dateParam.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng điền đầy đủ thông tin bắt buộc.");
			response.sendRedirect(request.getContextPath() + "/shift-assignment-assign");
			return;
		}

		try {
			Long userId = Long.parseLong(userIdParam.trim());
			Long shiftId = Long.parseLong(shiftIdParam.trim());
			Date date = Date.valueOf(dateParam.trim());

			boolean success = shiftAssignmentDAO.upsert(userId, shiftId, date);

			if (success) {
				session.setAttribute("successMsg", "Phân ca thành công.");
				response.sendRedirect(request.getContextPath() + "/shift-assignment-list");
			} else {
				session.setAttribute("errorMsg", "Không thể phân ca. Vui lòng thử lại.");
				response.sendRedirect(request.getContextPath() + "/shift-assignment-assign");
			}

		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", "Ngày không hợp lệ (định dạng: YYYY-MM-DD).");
			response.sendRedirect(request.getContextPath() + "/shift-assignment-assign");
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
