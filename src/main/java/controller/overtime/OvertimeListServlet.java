package controller.overtime;

import dal.OvertimeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.OvertimeRecord;
import model.Permission;

@WebServlet(name = "OvertimeListServlet", urlPatterns = {"/overtime-list"})
public class OvertimeListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String status = normalizeStatus(request.getParameter("status"));
		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<OvertimeRecord> records = overtimeDAO.search(status, offset, PAGE_SIZE);
		int totalRecords = overtimeDAO.count(status);
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
			records = overtimeDAO.search(status, offset, PAGE_SIZE);
		}

		request.setAttribute("overtimeRecords", records);
		request.setAttribute("selectedStatus", status);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);

		// Pass permission flags to JSP for conditional button rendering
		request.setAttribute("canRequest", hasPermission(session, "OT_REQUEST"));
		request.setAttribute("canApprove", hasPermission(session, "OT_APPROVE"));
		request.setAttribute("canReject", hasPermission(session, "OT_REJECT"));

		request.getRequestDispatcher("/views/overtime/overtime-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (permissionCode.equals(p.getCode())) {
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