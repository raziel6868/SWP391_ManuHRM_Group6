package controller.leavebalance;

import java.io.IOException;
import java.util.List;
import dal.DepartmentDAO;
import dal.LeaveBalanceDAO;
import dal.LeaveTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.LeaveBalance;
import model.LeaveType;
import model.Permission;
import model.User;

@WebServlet(name = "LeaveBalanceListServlet", urlPatterns = {"/leave-balance-list"})
public class LeaveBalanceListServlet extends HttpServlet {

	private final LeaveBalanceDAO balanceDAO = new LeaveBalanceDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "LEAVE_BALANCE_VIEW")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String yearStr = request.getParameter("year");
		Integer year = java.time.Year.now().getValue();
		if (yearStr != null && !yearStr.trim().isEmpty()) {
			try {
				year = Integer.parseInt(yearStr.trim());
			} catch (NumberFormatException e) {
			}
		}

		String departmentIdStr = request.getParameter("departmentId");
		Long departmentId = null;
		if (departmentIdStr != null && !departmentIdStr.trim().isEmpty()) {
			try {
				departmentId = Long.parseLong(departmentIdStr.trim());
			} catch (NumberFormatException e) {
			}
		}

		List<LeaveBalance> balances = balanceDAO.searchBalances(year, departmentId, 0, 1000);
		List<Department> departments = departmentDAO.getActiveDepartments();
		List<LeaveType> leaveTypes = leaveTypeDAO.searchLeaveTypes(null, null, true, 0, 100);

		request.setAttribute("balances", balances);
		request.setAttribute("departments", departments);
		request.setAttribute("leaveTypes", leaveTypes);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedDepartmentId", departmentId);

		request.getRequestDispatcher("/views/leavebalance/leave-balance-list.jsp").forward(request, response);
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
