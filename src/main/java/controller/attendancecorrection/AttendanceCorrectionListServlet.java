package controller.attendancecorrection;

import dal.AttendanceCorrectionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.AttendanceCorrection;
import model.Permission;

@WebServlet(name = "AttendanceCorrectionListServlet", urlPatterns = {"/attendance-correction-list"})
public class AttendanceCorrectionListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String status = normalizeStatus(request.getParameter("status"));
		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<AttendanceCorrection> corrections = correctionDAO.search(status, offset, PAGE_SIZE);
		int totalRecords = correctionDAO.count(status);
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
			corrections = correctionDAO.search(status, offset, PAGE_SIZE);
		}

		request.setAttribute("corrections", corrections);
		request.setAttribute("selectedStatus", status);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);
		request.setAttribute("canApprove", hasPermission(session, "ATTENDANCE_CORRECTION_APPROVE"));
		request.setAttribute("canReject", hasPermission(session, "ATTENDANCE_CORRECTION_REJECT"));
		request.getRequestDispatcher("/views/attendancecorrection/attendance-correction-list.jsp").forward(request,
				response);
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

	private String normalizeStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		String normalized = status.trim().toUpperCase();
		if ("PENDING".equals(normalized) || "APPROVED".equals(normalized) || "REJECTED".equals(normalized)) {
			return normalized;
		}
		return null;
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
