package controller.salarybase;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import dal.DepartmentDAO;
import dal.SalaryBaseDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;
import model.SalaryBase;
import model.User;

@WebServlet(name = "SalaryBaseSetupServlet", urlPatterns = {"/salary-base-setup"})
public class SalaryBaseSetupServlet extends HttpServlet {

	private final SalaryBaseDAO salaryBaseDAO = new SalaryBaseDAO();
	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "SALARY_BASE_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String userIdParam = request.getParameter("userId");
		SalaryBase currentBase = null;

		if (userIdParam != null && !userIdParam.isEmpty()) {
			try {
				Long userId = Long.parseLong(userIdParam);
				currentBase = salaryBaseDAO.getCurrentByUserId(userId);
			} catch (NumberFormatException e) {
			}
		}

		List<User> employees = userDAO.searchUsers(null, null, null, true, null, 0, 100);

		request.setAttribute("currentBase", currentBase);
		request.setAttribute("users", employees);
		request.getRequestDispatcher("/views/salarybase/salary-base-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "SALARY_BASE_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String userIdParam = request.getParameter("userId");
		String salaryParam = request.getParameter("baseSalary");
		String effectiveDateParam = request.getParameter("effectiveFrom");

		if (userIdParam == null || userIdParam.isEmpty() || salaryParam == null || salaryParam.isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng nhập đầy đủ thông tin.");
			response.sendRedirect(request.getContextPath() + "/salary-base-setup");
			return;
		}

		try {
			Long userId = Long.parseLong(userIdParam);
			BigDecimal baseSalary = new BigDecimal(salaryParam.trim());
			Date effectiveFrom = Date.valueOf(effectiveDateParam);

			if (baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
				session.setAttribute("errorMsg", "Lương cơ bản phải lớn hơn 0.");
				response.sendRedirect(request.getContextPath() + "/salary-base-setup?userId=" + userId);
				return;
			}

			boolean success = salaryBaseDAO.upsert(userId, baseSalary, effectiveFrom);

			if (success) {
				session.setAttribute("successMsg", "Lương cơ bản được cập nhật thành công.");
			} else {
				session.setAttribute("errorMsg", "Không thể cập nhật lương cơ bản.");
			}

		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Số tiền lương không hợp lệ.");
		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", "Ngày hiệu lực không hợp lệ.");
		}

		response.sendRedirect(request.getContextPath() + "/salary-base-setup");
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (p.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}
}
