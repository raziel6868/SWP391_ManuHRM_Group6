package controller.employeedependent;

import dal.DepartmentDAO;
import dal.EmployeeDependentDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import model.Department;
import model.EmployeeDependent;
import model.User;

@WebServlet(name = "EmployeeDependentSetupServlet", urlPatterns = {"/employee-dependent-setup"})
public class EmployeeDependentSetupServlet extends HttpServlet {

	private final EmployeeDependentDAO employeeDependentDAO = new EmployeeDependentDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseLong(request.getParameter("id"));
		boolean hasDepartmentParam = request.getParameterMap().containsKey("departmentId");
		Long selectedDepartmentId = parseLong(request.getParameter("departmentId"));
		EmployeeDependent employeeDependent = null;
		if (id != null) {
			employeeDependent = employeeDependentDAO.getById(id);
			if (employeeDependent == null) {
				request.getSession().setAttribute("errorMsg", "Người phụ thuộc không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/employee-dependent-list");
				return;
			}
		}

		if (employeeDependent != null) {
			request.setAttribute("employeeDependent", employeeDependent);
			if (selectedDepartmentId == null && !hasDepartmentParam) {
				selectedDepartmentId = findUserDepartmentId(employeeDependent.getUserId());
			}
		}
		setFormOptions(request, selectedDepartmentId);
		request.getRequestDispatcher("/views/employeedependent/employee-dependent-setup.jsp").forward(request,
				response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long id = parseLong(request.getParameter("id"));
		Long selectedDepartmentId = parseLong(request.getParameter("departmentId"));
		Long userId = parseLong(request.getParameter("userId"));
		String fullName = normalizeText(request.getParameter("fullName"));
		String relationship = normalizeText(request.getParameter("relationship"));
		String taxCode = normalizeText(request.getParameter("taxCode"));
		Date dateOfBirth = parseDate(request.getParameter("dateOfBirth"));
		Date effectiveFrom = parseDate(request.getParameter("effectiveFrom"));
		Date effectiveTo = parseDate(request.getParameter("effectiveTo"));

		EmployeeDependent employeeDependent = new EmployeeDependent();
		employeeDependent.setId(id);
		employeeDependent.setUserId(userId);
		employeeDependent.setFullName(fullName);
		employeeDependent.setRelationship(relationship);
		employeeDependent.setTaxCode(taxCode);
		employeeDependent.setDateOfBirth(dateOfBirth);
		employeeDependent.setEffectiveFrom(effectiveFrom);
		employeeDependent.setEffectiveTo(effectiveTo);
		employeeDependent.setIsActive(true);

		String validationError = validate(employeeDependent, selectedDepartmentId);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("employeeDependent", employeeDependent);
			setFormOptions(request, selectedDepartmentId);
			request.getRequestDispatcher("/views/employeedependent/employee-dependent-setup.jsp").forward(request,
					response);
			return;
		}

		boolean success;
		if (id == null) {
			success = employeeDependentDAO.create(employeeDependent);
		} else {
			EmployeeDependent existing = employeeDependentDAO.getById(id);
			if (existing == null) {
				request.getSession().setAttribute("errorMsg", "Người phụ thuộc không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/employee-dependent-list");
				return;
			}
			success = employeeDependentDAO.update(employeeDependent);
		}

		if (success) {
			request.getSession().setAttribute("successMsg",
					id == null ? "Đã thêm người phụ thuộc." : "Đã cập nhật người phụ thuộc.");
			response.sendRedirect(request.getContextPath() + "/employee-dependent-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể lưu người phụ thuộc. Vui lòng thử lại.");
		request.setAttribute("employeeDependent", employeeDependent);
		setFormOptions(request, selectedDepartmentId);
		request.getRequestDispatcher("/views/employeedependent/employee-dependent-setup.jsp").forward(request,
				response);
	}

	private void setFormOptions(HttpServletRequest request, Long selectedDepartmentId) {
		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("selectedDepartmentId", selectedDepartmentId);

		List<User> users = userDAO.getActiveUsersForDropdown();
		if (selectedDepartmentId != null && isActiveDepartment(selectedDepartmentId)) {
			users = userDAO.getActiveUsersByDepartment(selectedDepartmentId);
		}
		request.setAttribute("users", users);
	}

	private String validate(EmployeeDependent employeeDependent, Long selectedDepartmentId) {
		if (selectedDepartmentId != null && !isActiveDepartment(selectedDepartmentId)) {
			return "Phòng ban không tồn tại hoặc đã bị vô hiệu hóa.";
		}
		if (employeeDependent.getUserId() == null) {
			return "Vui lòng chọn nhân viên.";
		}
		User user = userDAO.getById(employeeDependent.getUserId());
		if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
			return "Nhân viên không tồn tại hoặc đã bị vô hiệu hóa.";
		}
		if (selectedDepartmentId != null && !selectedDepartmentId.equals(user.getDepartmentId())) {
			return "Nhân viên không thuộc phòng ban đã chọn.";
		}
		if (employeeDependent.getFullName() == null || employeeDependent.getFullName().isBlank()) {
			return "Tên người phụ thuộc là bắt buộc.";
		}
		if (employeeDependent.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ là bắt buộc.";
		}
		if (employeeDependent.getEffectiveTo() != null
				&& employeeDependent.getEffectiveTo().before(employeeDependent.getEffectiveFrom())) {
			return "Ngày hiệu lực đến không được trước ngày hiệu lực từ.";
		}
		return null;
	}

	private Long findUserDepartmentId(Long userId) {
		User user = userDAO.getById(userId);
		return user != null ? user.getDepartmentId() : null;
	}

	private boolean isActiveDepartment(Long departmentId) {
		Department department = departmentDAO.getById(departmentId);
		return department != null && Boolean.TRUE.equals(department.getIsActive());
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
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
