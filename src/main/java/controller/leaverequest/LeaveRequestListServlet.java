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

	private static final int PAGE_SIZE = 20;

	private final LeaveRequestDAO requestDAO = new LeaveRequestDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		if (!hasPermission(session, "LEAVE_REQUEST_VIEW")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String keyword = request.getParameter("keyword");
		String status = request.getParameter("status");
		String departmentIdStr = request.getParameter("departmentId");
		String pageStr = request.getParameter("page");

		Long departmentId = parseId(departmentIdStr);

		int currentPage = parsePage(pageStr);
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<LeaveRequest> requests = requestDAO.searchRequests(keyword, status, departmentId, offset, PAGE_SIZE);
		int totalRecords = requestDAO.countRequests(keyword, status, departmentId);
		int totalPages = totalRecords / PAGE_SIZE;
		if (totalRecords % PAGE_SIZE != 0) {
			totalPages++;
		}
		if (totalPages == 0) {
			totalPages = 1;
		}

		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("requests", requests);
		request.setAttribute("keyword", keyword != null ? keyword : "");
		request.setAttribute("selectedStatus", status != null ? status : "");
		request.setAttribute("selectedDepartmentId", departmentIdStr != null ? departmentIdStr : "");
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);
		request.setAttribute("departments", departments);

		request.setAttribute("canApproveL1", hasPermission(session, "LEAVE_REQUEST_APPROVE_L1"));
		request.setAttribute("canApproveL2", hasPermission(session, "LEAVE_REQUEST_APPROVE_L2"));
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

	private int parsePage(String pageStr) {
		if (pageStr == null || pageStr.trim().isEmpty()) {
			return 1;
		}
		try {
			return Math.max(1, Integer.parseInt(pageStr.trim()));
		} catch (NumberFormatException e) {
			return 1;
		}
	}
}
