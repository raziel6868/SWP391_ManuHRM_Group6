package controller.salarybase;

import java.io.IOException;
import java.util.List;
import dal.DepartmentDAO;
import dal.SalaryBaseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import model.SalaryBase;
import model.User;

@WebServlet(name = "SalaryBaseListServlet", urlPatterns = {"/salary-base-list"})
public class SalaryBaseListServlet extends HttpServlet {

	private final SalaryBaseDAO salaryBaseDAO = new SalaryBaseDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "SALARY_BASE_VIEW")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String departmentIdParam = request.getParameter("departmentId");
		Long departmentId = null;

		if (departmentIdParam != null && !departmentIdParam.isEmpty()) {
			try {
				departmentId = Long.parseLong(departmentIdParam.trim());
			} catch (NumberFormatException e) {
			}
		}

		List<SalaryBase> salaryBases = salaryBaseDAO.searchSalaryBases(departmentId, 0, 100);
		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("hasSetupPermission", hasPermission(permissions, "SALARY_BASE_SETUP"));
		request.setAttribute("salaryBases", salaryBases);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedDepartmentId", departmentId);

		request.getRequestDispatcher("/views/salarybase/salary-base-list.jsp").forward(request, response);
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
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
}
