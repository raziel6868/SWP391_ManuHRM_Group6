package controller.employeeallowance;

import dal.AllowanceTypeDAO;
import dal.EmployeeAllowanceDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import model.AllowanceType;
import model.EmployeeAllowance;
import model.User;

@WebServlet(name = "EmployeeAllowanceSetupServlet", urlPatterns = {"/employee-allowance-setup"})
public class EmployeeAllowanceSetupServlet extends HttpServlet {

	private final EmployeeAllowanceDAO employeeAllowanceDAO = new EmployeeAllowanceDAO();
	private final AllowanceTypeDAO allowanceTypeDAO = new AllowanceTypeDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseLong(request.getParameter("id"));
		EmployeeAllowance employeeAllowance = null;
		if (id != null) {
			employeeAllowance = employeeAllowanceDAO.getById(id);
			if (employeeAllowance == null) {
				request.getSession().setAttribute("errorMsg", "Phụ cấp nhân viên không tồn tại hoặc đã bị xóa.");
				response.sendRedirect(request.getContextPath() + "/employee-allowance-list");
				return;
			}
		}

		if (employeeAllowance != null) {
			request.setAttribute("employeeAllowance", employeeAllowance);
		}
		setFormOptions(request);
		request.getRequestDispatcher("/views/employeeallowance/employee-allowance-setup.jsp").forward(request,
				response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long id = parseLong(request.getParameter("id"));
		Long userId = parseLong(request.getParameter("userId"));
		Long allowanceTypeId = parseLong(request.getParameter("allowanceTypeId"));
		BigDecimal amount = parseAmount(request.getParameter("amount"));
		Date effectiveFrom = parseDate(request.getParameter("effectiveFrom"));
		Date effectiveTo = parseDate(request.getParameter("effectiveTo"));

		EmployeeAllowance employeeAllowance = new EmployeeAllowance();
		employeeAllowance.setId(id);
		employeeAllowance.setUserId(userId);
		employeeAllowance.setAllowanceTypeId(allowanceTypeId);
		employeeAllowance.setAmount(amount);
		employeeAllowance.setEffectiveFrom(effectiveFrom);
		employeeAllowance.setEffectiveTo(effectiveTo);
		employeeAllowance.setIsActive(true);

		String validationError = validate(employeeAllowance);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("employeeAllowance", employeeAllowance);
			setFormOptions(request);
			request.getRequestDispatcher("/views/employeeallowance/employee-allowance-setup.jsp").forward(request,
					response);
			return;
		}

		boolean success;
		if (id == null) {
			success = employeeAllowanceDAO.insert(employeeAllowance);
		} else {
			EmployeeAllowance existing = employeeAllowanceDAO.getById(id);
			if (existing == null) {
				request.getSession().setAttribute("errorMsg", "Phụ cấp nhân viên không tồn tại hoặc đã bị xóa.");
				response.sendRedirect(request.getContextPath() + "/employee-allowance-list");
				return;
			}
			success = employeeAllowanceDAO.update(employeeAllowance);
		}

		if (success) {
			request.getSession().setAttribute("successMsg",
					id == null ? "Gán phụ cấp nhân viên thành công." : "Cập nhật phụ cấp nhân viên thành công.");
			response.sendRedirect(request.getContextPath() + "/employee-allowance-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể lưu phụ cấp nhân viên. Vui lòng thử lại.");
		request.setAttribute("employeeAllowance", employeeAllowance);
		setFormOptions(request);
		request.getRequestDispatcher("/views/employeeallowance/employee-allowance-setup.jsp").forward(request,
				response);
	}

	private void setFormOptions(HttpServletRequest request) {
		request.setAttribute("users", userDAO.getActiveUsersForDropdown());
		request.setAttribute("allowanceTypes", allowanceTypeDAO.getActiveTypes());
	}

	private String validate(EmployeeAllowance employeeAllowance) {
		if (employeeAllowance.getUserId() == null) {
			return "Vui lòng chọn nhân viên.";
		}
		User user = userDAO.getById(employeeAllowance.getUserId());
		if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
			return "Nhân viên không tồn tại hoặc đã bị vô hiệu hóa.";
		}
		if (employeeAllowance.getAllowanceTypeId() == null) {
			return "Vui lòng chọn loại phụ cấp.";
		}
		AllowanceType allowanceType = allowanceTypeDAO.getById(employeeAllowance.getAllowanceTypeId());
		if (allowanceType == null || !Boolean.TRUE.equals(allowanceType.getIsActive())) {
			return "Loại phụ cấp không tồn tại hoặc đã bị vô hiệu hóa.";
		}
		if (employeeAllowance.getAmount() == null || employeeAllowance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			return "Số tiền phụ cấp phải lớn hơn 0.";
		}
		if (employeeAllowance.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ không được để trống.";
		}
		if (employeeAllowance.getEffectiveTo() != null
				&& employeeAllowance.getEffectiveTo().before(employeeAllowance.getEffectiveFrom())) {
			return "Ngày hiệu lực đến không được trước ngày hiệu lực từ.";
		}
		return null;
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseAmount(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(value.replace(",", "").trim());
		} catch (NumberFormatException e) {
			return null;
		}
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
}
