package controller.shift;

import dal.ShiftDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Permission;
import model.Shift;

@WebServlet(name = "ShiftListServlet", urlPatterns = {"/shift-list"})
public class ShiftListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		String keyword = normalizeText(request.getParameter("keyword"));
		String isNightShiftParam = normalizeText(request.getParameter("isNightShift"));
		Boolean selectedIsNightShift = parseBoolean(isNightShiftParam);
		String statusParam = normalizeText(request.getParameter("status"));
		Boolean selectedIsActive = parseBoolean(statusParam);

		int currentPage = parsePage(request.getParameter("page"));
		int offset = (currentPage - 1) * PAGE_SIZE;

		List<Shift> shifts = shiftDAO.searchShifts(keyword, selectedIsNightShift, selectedIsActive, offset, PAGE_SIZE);
		int totalRecords = shiftDAO.countShifts(keyword, selectedIsNightShift, selectedIsActive);
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
			shifts = shiftDAO.searchShifts(keyword, selectedIsNightShift, selectedIsActive, offset, PAGE_SIZE);
		}

		request.setAttribute("shifts", shifts);
		request.setAttribute("keyword", keyword);
		request.setAttribute("selectedIsNightShift", isNightShiftParam);
		request.setAttribute("selectedStatus", statusParam);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("canCreate", hasPermission(session, "SHIFT_CREATE"));
		request.setAttribute("canUpdate", hasPermission(session, "SHIFT_UPDATE"));
		request.setAttribute("canChangeStatus", hasPermission(session, "SHIFT_STATUS"));

		request.getRequestDispatcher("/views/shift/shift-list.jsp").forward(request, response);
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

	private Boolean parseBoolean(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		if ("true".equalsIgnoreCase(value)) {
			return true;
		}
		if ("false".equalsIgnoreCase(value)) {
			return false;
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
}
