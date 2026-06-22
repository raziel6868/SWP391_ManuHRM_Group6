package controller.leaverequest;

import dal.DepartmentDAO;
import dal.LeaveRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Department;
import model.LeaveRequest;
import model.Permission;

@WebServlet(name = "LeaveRequestListServlet", urlPatterns = {"/leave-request-list"})
public class LeaveRequestListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("authUser") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String keyword = normalizeText(request.getParameter("keyword"));
		String selectedStatus = normalizeText(request.getParameter("status"));
		Long selectedDepartmentId = parseLong(normalizeText(request.getParameter("departmentId")));

		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<LeaveRequest> leaveRequests = leaveRequestDAO.searchRequests(keyword, selectedStatus, selectedDepartmentId,
				offset, PAGE_SIZE);
		int totalRecords = leaveRequestDAO.countRequests(keyword, selectedStatus, selectedDepartmentId);
		int totalPages = totalRecords / PAGE_SIZE;
		if (totalRecords % PAGE_SIZE != 0) {
			totalPages++;
		}
		if (totalPages == 0) {
			totalPages = 1;
		}

		if (currentPage > totalPages) {
			currentPage = totalPages;
			offset = (currentPage - 1) * PAGE_SIZE;
			leaveRequests = leaveRequestDAO.searchRequests(keyword, selectedStatus, selectedDepartmentId, offset,
					PAGE_SIZE);
		}

		List<Department> departments = departmentDAO.getActiveDepartments();
		request.setAttribute("leaveRequests", leaveRequests);
		request.setAttribute("departments", departments);
		request.setAttribute("keyword", keyword);
		request.setAttribute("selectedStatus", selectedStatus);
		request.setAttribute("selectedDepartmentId", selectedDepartmentId);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);
		request.setAttribute("canFinalApprove", hasPermission(session, "LEAVE_REQUEST_APPROVE_L2"));
		request.setAttribute("canReject", hasPermission(session, "LEAVE_REQUEST_REJECT"));

		request.getRequestDispatcher("/views/leaverequest/leave-request-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (permissionCode.equals(permission.getCode())) {
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

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Long parseLong(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private int parsePage(String pageParam) {
		if (pageParam == null || pageParam.isBlank()) {
			return 1;
		}
		try {
			return Math.max(1, Integer.parseInt(pageParam));
		} catch (NumberFormatException e) {
			return 1;
		}
	}
}
