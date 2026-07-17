package controller.monthlysheet;

import dal.DepartmentDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import model.MonthlySheet;
import model.Permission;
import model.User;
import util.YearOptionUtil;

@WebServlet(name = "MonthlySheetListServlet", urlPatterns = {"/monthly-sheet-list"})
public class MonthlySheetListServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (!hasPermission(session, "MONTHLY_SHEET_VIEW")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");
		moveFlashMessage(session, request, "monthlySheetConflictTitle");
		moveFlashObject(session, request, "monthlySheetConflicts");

		// Filter params
		int currentYear = LocalDate.now().getYear();
		Integer selectedYear = parseOptionalInt(request.getParameter("year"), 2000, 2100);
		if (selectedYear == null) {
			selectedYear = currentYear;
		}
		Integer selectedMonth = parseOptionalInt(request.getParameter("month"), 1, 12);
		String selectedStatus = normalizeStatus(request.getParameter("status"));
		int currentPage = parsePage(request.getParameter("page"));

		List<Integer> yearOptions = YearOptionUtil.dataYearsWithCurrent(monthlySheetDAO.getAvailableYears());
		List<MonthlySheet> sheets = monthlySheetDAO.getAll();

		// Filter trong memory (getAll đã có, dataset nhỏ)
		Integer filterYear = selectedYear;
		sheets = sheets.stream().filter(s -> filterYear.equals(s.getYear()))
				.filter(s -> selectedMonth == null || selectedMonth.equals(s.getMonth()))
				.filter(s -> selectedStatus == null || selectedStatus.equals(s.getStatus())).toList();

		// Phân biệt HR thuần vs Giám đốc bằng job_title_id
		boolean isDirector = authUser != null && authUser.getJobTitleId() != null && authUser.getJobTitleId() == 1L;
		boolean isHR = hasPermission(session, "MONTHLY_SHEET_HR_APPROVE");
		boolean canSubmit = hasPermission(session, "MONTHLY_SHEET_SUBMIT") && !isDirector;
		boolean canHrApprove = hasPermission(session, "MONTHLY_SHEET_HR_APPROVE") && !isDirector;
		boolean canReject = hasPermission(session, "MONTHLY_SHEET_REJECT");
		boolean canReopen = hasPermission(session, "MONTHLY_SHEET_REOPEN");

		request.setAttribute("sheets", sheets);
		request.setAttribute("isHR", isHR);
		request.setAttribute("isDirector", isDirector);
		request.setAttribute("canSubmit", canSubmit);
		request.setAttribute("canHrApprove", canHrApprove);
		request.setAttribute("canReject", canReject);
		request.setAttribute("canReopen", canReopen);
		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("yearOptions", yearOptions);
		request.setAttribute("selectedYear", selectedYear);
		request.setAttribute("selectedMonth", selectedMonth);
		request.setAttribute("selectedStatus", selectedStatus);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("totalPages", 1); // dataset khi filter nhỏ, không cần page

		request.getRequestDispatcher("/views/monthlysheet/monthly-sheet-list.jsp").forward(request, response);
	}

	private String normalizeStatus(String status) {
		if (status == null || status.isBlank())
			return null;
		return switch (status.trim().toUpperCase()) {
			case "OPEN", "PENDING_SUPERVISOR", "PENDING_HR", "PENDING_DIRECTOR", "CLOSED" ->
				status.trim().toUpperCase();
			default -> null;
		};
	}

	private Integer parseOptionalInt(String value, int min, int max) {
		if (value == null || value.isBlank())
			return null;
		try {
			int v = Integer.parseInt(value.trim());
			return (v >= min && v <= max) ? v : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private int parsePage(String value) {
		if (value == null || value.isBlank())
			return 1;
		try {
			return Math.max(1, Integer.parseInt(value.trim()));
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

	private void moveFlashObject(HttpSession session, HttpServletRequest request, String key) {
		Object value = session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
