package controller.user;

import dal.DepartmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import dal.DepartmentDAO;

@WebServlet(name = "UserCreateServlet", urlPatterns = {"/user-create"})
public class UserCreateServlet extends HttpServlet {

	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final dal.RoleDAO roleDAO = new dal.RoleDAO();
	private final dal.UserDAO userDAO = new dal.UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("roles", roleDAO.getActiveRoles());
		// Fetch all active users to be potential managers
		request.setAttribute("managers", userDAO.searchAndFilter("", null, null, true, null, 0, 1000));
		request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		try {
			model.User user = new model.User();
			user.setEmployeeCode(request.getParameter("employeeCode"));
			user.setUsername(request.getParameter("username"));

			String rawPassword = request.getParameter("password");
			if (rawPassword != null && !rawPassword.trim().isEmpty()) {
				user.setPasswordHash(util.PasswordUtil.hashPassword(rawPassword));
			} else {
				// Default password if not provided (though form says required)
				user.setPasswordHash(util.PasswordUtil.hashPassword("123456"));
			}

			user.setFullName(request.getParameter("fullName"));
			user.setPhone(request.getParameter("phone"));

			String dobStr = request.getParameter("dob");
			if (dobStr != null && !dobStr.trim().isEmpty()) {
				user.setDob(java.sql.Date.valueOf(dobStr));
			}

			user.setJobTitle(request.getParameter("jobTitle"));

			String employeeTypeStr = request.getParameter("employeeType");
			if (employeeTypeStr != null && !employeeTypeStr.trim().isEmpty()) {
				user.setEmployeeType(model.User.EmployeeType.valueOf(employeeTypeStr));
			}

			String deptIdStr = request.getParameter("departmentId");
			if (deptIdStr != null && !deptIdStr.trim().isEmpty()) {
				user.setDepartmentId(Long.parseLong(deptIdStr));
			}

			String roleIdStr = request.getParameter("roleId");
			if (roleIdStr != null && !roleIdStr.trim().isEmpty()) {
				user.setRoleId(Long.valueOf(roleIdStr));
			}

			String managerIdStr = request.getParameter("managerId");
			if (managerIdStr != null && !managerIdStr.trim().isEmpty()) {
				user.setManagerId(Long.valueOf(managerIdStr));
			}

			String isActiveStr = request.getParameter("isActive");
			user.setIsActive("on".equals(isActiveStr)); // Checkbox returns "on" if checked

			boolean success = userDAO.insertUser(user);

			if (success) {
				request.getSession().setAttribute("successMsg", "Thêm nhân viên thành công!");
				response.sendRedirect(request.getContextPath() + "/user-list");
			} else {
				request.setAttribute("errorMsg",
						"Lỗi: Không thể thêm nhân viên. Vui lòng kiểm tra trùng lặp Mã NV/Tên đăng nhập.");
				doGet(request, response); // re-populate form
			}
		} catch (ServletException | IOException | NumberFormatException e) {
			request.setAttribute("errorMsg", "Lỗi dữ liệu đầu vào: " + e.getMessage());
			doGet(request, response);
		}
	}
}
