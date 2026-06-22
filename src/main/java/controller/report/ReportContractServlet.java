package controller.report;

import java.io.IOException;
import java.util.List;
import dal.DepartmentDAO;
import dal.ReportDAO;
import dto.ContractStatusRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import model.User;

@WebServlet(name = "ReportContractServlet", urlPatterns = {"/report-contract"})
public class ReportContractServlet extends HttpServlet {

	private final ReportDAO reportDAO = new ReportDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_CONTRACT")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String departmentIdParam = request.getParameter("departmentId");

		Long departmentId = null;
		if (departmentIdParam != null && !departmentIdParam.isEmpty()) {
			try {
				departmentId = Long.parseLong(departmentIdParam);
			} catch (NumberFormatException e) {
			}
		}

		List<ContractStatusRow> rows = reportDAO.getContractStatus(departmentId);
		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("rows", rows);
		request.setAttribute("departments", departments);
		request.setAttribute("selectedDepartmentId", departmentId);

		request.getRequestDispatcher("/views/report/report-contract.jsp").forward(request, response);
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
