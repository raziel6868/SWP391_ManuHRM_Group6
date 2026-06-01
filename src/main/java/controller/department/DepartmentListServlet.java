package controller.department;

import dal.DepartmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DepartmentListServlet", urlPatterns = {"/department-list"})
public class DepartmentListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);

		// ── Flash message: chuyển từ session sang request rồi xóa ──────────────
		String successMsg = (String) session.getAttribute("successMsg");
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}

		// ── Kiểm tra permission ────────────────────────────────────────────────
		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean canCreate = hasPermission(perms, "DEPARTMENT_CREATE");
		boolean canUpdate = hasPermission(perms, "DEPARTMENT_UPDATE");
		boolean canChangeStatus = hasPermission(perms, "DEPARTMENT_STATUS");

		request.setAttribute("canCreate", canCreate);
		request.setAttribute("canUpdate", canUpdate);
		request.setAttribute("canChangeStatus", canChangeStatus);

		// ── Đọc params ────────────────────────────────────────────────────────
		String keyword = request.getParameter("keyword");
		String departmentType = request.getParameter("departmentType");
		String pageParam = request.getParameter("page");

		// ── Tính page và offset ───────────────────────────────────────────────
		int page = 1;
		if (pageParam != null && pageParam.matches("\\d+")) {
			page = Integer.parseInt(pageParam);
			if (page < 1)
				page = 1;
		}
		int offset = (page - 1) * PAGE_SIZE;

		// ── Gọi DAO ───────────────────────────────────────────────────────────
		List<Department> departments = departmentDAO.searchDepartments(keyword, departmentType, offset, PAGE_SIZE);
		int totalRecords = departmentDAO.countDepartments(keyword, departmentType);
		int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);

		// ── Set attributes ────────────────────────────────────────────────────
		request.setAttribute("departments", departments);
		request.setAttribute("keyword", keyword);
		request.setAttribute("selectedType", departmentType);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalRecords);

		request.getRequestDispatcher("/views/department/department-list.jsp").forward(request, response);
	}

	// ── Helper: kiểm tra permission theo code ─────────────────────────────────
	private boolean hasPermission(List<Permission> perms, String code) {
		if (perms == null)
			return false;
		for (Permission p : perms) {
			if (code.equals(p.getCode()))
				return true;
		}
		return false;
	}
}