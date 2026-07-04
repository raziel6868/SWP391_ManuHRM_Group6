package controller.salarybase;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
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

		Long selectedUserId = parseLong(request.getParameter("userId"));
		SalaryBase currentBase = null;
		if (selectedUserId != null) {
			currentBase = salaryBaseDAO.getCurrentByUserId(selectedUserId);
		}

		List<User> employees = userDAO.searchUsers(null, null, null, true, null, 0, 100);
		request.setAttribute("currentBase", currentBase);
		request.setAttribute("selectedUserId", selectedUserId);
		request.setAttribute("users", employees);
		request.getRequestDispatcher("/views/salarybase/salary-base-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "SALARY_BASE_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Long userId = parseLong(request.getParameter("userId"));
		BigDecimal baseSalary = parseDecimal(request.getParameter("baseSalary"));
		BigDecimal insuranceSalary = parseOptionalDecimal(request.getParameter("insuranceSalary"));
		Date effectiveFrom = parseDate(request.getParameter("effectiveFrom"));

		if (userId == null || baseSalary == null || effectiveFrom == null) {
			session.setAttribute("errorMsg", "Vui lòng nhập đầy đủ thông tin.");
			response.sendRedirect(request.getContextPath() + "/salary-base-setup");
			return;
		}

		if (baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
			session.setAttribute("errorMsg", "Lương cơ bản phải lớn hơn 0.");
			response.sendRedirect(request.getContextPath() + "/salary-base-setup?userId=" + userId);
			return;
		}

		if (insuranceSalary != null && insuranceSalary.compareTo(BigDecimal.ZERO) < 0) {
			session.setAttribute("errorMsg", "Lương đóng bảo hiểm không được nhỏ hơn 0.");
			response.sendRedirect(request.getContextPath() + "/salary-base-setup?userId=" + userId);
			return;
		}

		boolean success = salaryBaseDAO.upsert(userId, baseSalary, insuranceSalary, effectiveFrom);
		if (success) {
			session.setAttribute("successMsg", "Lương cơ bản đã được cập nhật thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật lương cơ bản.");
		}

		response.sendRedirect(request.getContextPath() + "/salary-base-setup?userId=" + userId);
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(value.replace(",", "").trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseOptionalDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return parseDecimal(value);
	}

	private Date parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Date.valueOf(value.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
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
