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
import model.User;

@WebServlet(name = "AttendanceCorrectionListServlet", urlPatterns = {"/attendance-correction-list"})
public class AttendanceCorrectionListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;
	private final AttendanceCorrectionDAO correctionDAO = new AttendanceCorrectionDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		boolean isSupervisor = hasPermission(session, "ATTENDANCE_CORRECTION_SUPERVISOR_APPROVE");
		boolean isHR = hasPermission(session, "ATTENDANCE_CORRECTION_APPROVE");

		// Tab đang xem: "supervisor" (quản đốc duyệt bước 1) hoặc "hr" (HR duyệt bước
		// 2)
		String tab = "supervisor".equals(request.getParameter("tab")) && isSupervisor ? "supervisor" : "hr";

		String status = normalizeStatus(request.getParameter("status"));
		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<AttendanceCorrection> corrections;
		int totalRecords;

		if ("supervisor".equals(tab) && isSupervisor) {
			// Quản đốc xem corrections của cấp dưới chờ bước 1
			String supervisorStatus = normalizeSupervisorStatus(request.getParameter("status"));
			corrections = correctionDAO.searchBySupervisor(authUser.getId(), supervisorStatus, offset, PAGE_SIZE);
			totalRecords = correctionDAO.countBySupervisor(authUser.getId(), supervisorStatus);
		} else {
			// HR xem corrections đã qua bước supervisor (bước 2)
			corrections = correctionDAO.search(status, offset, PAGE_SIZE);
			totalRecords = correctionDAO.count(status);
		}

		int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / PAGE_SIZE));

		if (currentPage > totalPages) {
			currentPage = totalPages;
			offset = (currentPage - 1) * PAGE_SIZE;
			if ("supervisor".equals(tab) && isSupervisor) {
				String supervisorStatus = normalizeSupervisorStatus(request.getParameter("status"));
				corrections = correctionDAO.searchBySupervisor(authUser.getId(), supervisorStatus, offset, PAGE_SIZE);
			} else {
				corrections = correctionDAO.search(status, offset, PAGE_SIZE);
			}
		}

		request.setAttribute("corrections", corrections);
		request.setAttribute("selectedStatus", status);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);
		request.setAttribute("tab", tab);
		request.setAttribute("isSupervisor", isSupervisor);
		request.setAttribute("isHR", isHR);
		request.setAttribute("canApprove", isHR);
		request.setAttribute("canReject", isHR);
		request.setAttribute("canSupervisorApprove", isSupervisor);

		request.getRequestDispatcher("/views/attendancecorrection/attendance-correction-list.jsp").forward(request,
				response);
	}

	private String normalizeSupervisorStatus(String status) {
		if (status == null || status.isBlank())
			return null;
		String s = status.trim().toUpperCase();
		return ("PENDING".equals(s) || "APPROVED".equals(s) || "REJECTED".equals(s)) ? s : null;
	}

	private String normalizeStatus(String status) {
		if (status == null || status.isBlank())
			return null;
		String s = status.trim().toUpperCase();
		return ("PENDING".equals(s) || "APPROVED".equals(s) || "REJECTED".equals(s)) ? s : null;
	}

	private int parsePage(String pageParam) {
		if (pageParam == null || pageParam.isBlank())
			return 1;
		try {
			return Math.max(1, Integer.parseInt(pageParam));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null)
			return false;
		for (Permission p : permissions) {
			if (code.equals(p.getCode()))
				return true;
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