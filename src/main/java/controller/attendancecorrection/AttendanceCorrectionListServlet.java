package controller.attendancecorrection;

import java.io.IOException;
import java.util.List;
import dal.AttendanceCorrectionDAO;
import dal.DepartmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.AttendanceCorrection;
import model.Department;
import model.Permission;

@WebServlet(name = "AttendanceCorrectionListServlet", urlPatterns = {"/attendance-correction-list"})
public class AttendanceCorrectionListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 20;

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "ATTENDANCE_CORRECTION_VIEW")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String pageStr = request.getParameter("page");
		int currentPage = 1;
		try {
			currentPage = (pageStr != null && !pageStr.trim().isEmpty())
					? Math.max(1, Integer.parseInt(pageStr.trim()))
					: 1;
		} catch (NumberFormatException e) {
		}
		int offset = (currentPage - 1) * PAGE_SIZE;

		String status = request.getParameter("status");
		Long departmentId = parseId(request.getParameter("departmentId"));

		List<AttendanceCorrection> corrections = correctionDAO.searchCorrections(status, departmentId, offset,
				PAGE_SIZE);
		int totalCount = correctionDAO.countCorrections(status, departmentId);
		int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / PAGE_SIZE) : 1;
		if (totalPages == 0) {
			totalPages = 1;
		}

		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("corrections", corrections);
		request.setAttribute("departments", departments);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalCount", totalCount);
		request.setAttribute("filterStatus", status != null ? status : "");
		request.setAttribute("filterDepartmentId", request.getParameter("departmentId"));

		request.setAttribute("canApprove", hasPermission(session, "ATTENDANCE_CORRECTION_APPROVE"));
		request.setAttribute("canReject", hasPermission(session, "ATTENDANCE_CORRECTION_REJECT"));

		request.getRequestDispatcher("/views/attendancecorrection/attendance-correction-list.jsp").forward(request,
				response);
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

	private Long parseId(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
