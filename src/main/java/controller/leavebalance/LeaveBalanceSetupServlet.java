package controller.leavebalance;

import dal.DepartmentDAO;
import dal.LeaveBalanceDAO;
import dal.LeaveTypeDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import model.Department;
import model.LeaveType;
import model.Permission;
import model.User;

@WebServlet(name = "LeaveBalanceSetupServlet", urlPatterns = {"/leave-balance-setup"})
public class LeaveBalanceSetupServlet extends HttpServlet {

	private final LeaveBalanceDAO balanceDAO = new LeaveBalanceDAO();
	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "LEAVE_BALANCE_SETUP")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
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

		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, 1000);
		List<Department> departments = departmentDAO.getActiveDepartments();
		List<LeaveType> leaveTypes = leaveTypeDAO.searchLeaveTypes(null, null, true, 0, 100);

		request.setAttribute("users", users);
		request.setAttribute("leaveTypes", leaveTypes);
		request.setAttribute("selectedYear", year);
		request.setAttribute("departments", departments);
		request.getRequestDispatcher("/views/leavebalance/leave-balance-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (!hasPermission(session, "LEAVE_BALANCE_SETUP")) {
			session.setAttribute("errorMsg", "Bạn không có quyền thực hiện thao tác này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String userIdStr = request.getParameter("userId");
		String leaveTypeIdStr = request.getParameter("leaveTypeId");
		String yearStr = request.getParameter("year");
		String totalDaysStr = request.getParameter("totalDays");

		if (userIdStr == null || userIdStr.trim().isEmpty() || leaveTypeIdStr == null || leaveTypeIdStr.trim().isEmpty()
				|| yearStr == null || yearStr.trim().isEmpty() || totalDaysStr == null
				|| totalDaysStr.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng điền đầy đủ thông tin.");
			response.sendRedirect(request.getContextPath() + "/leave-balance-setup");
			return;
		}

		try {
			Long userId = Long.parseLong(userIdStr.trim());
			Long leaveTypeId = Long.parseLong(leaveTypeIdStr.trim());
			Integer year = Integer.parseInt(yearStr.trim());
			BigDecimal totalDays = new BigDecimal(totalDaysStr.trim());

			if (totalDays.compareTo(BigDecimal.ZERO) < 0) {
				session.setAttribute("errorMsg", "Số ngày nghỉ không được âm.");
				response.sendRedirect(request.getContextPath() + "/leave-balance-setup");
				return;
			}

			balanceDAO.upsert(userId, leaveTypeId, year, totalDays);

			session.setAttribute("successMsg", "Đã cập nhật ngày nghỉ phép thành công.");
			response.sendRedirect(request.getContextPath() + "/leave-balance-setup?year=" + year);

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Dữ liệu không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/leave-balance-setup");
		}
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
}
