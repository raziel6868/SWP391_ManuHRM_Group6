package controller.user;

import dal.DepartmentDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;

import dal.DepartmentDAO;
import dal.UserDAO;
import model.User;

@WebServlet(name = "UserUpdateServlet", urlPatterns = {"/user-update"})
public class UserUpdateServlet extends HttpServlet {

	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final dal.RoleDAO roleDAO = new dal.RoleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idParam = request.getParameter("id");
		if (idParam == null || idParam.isBlank()) {
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

		try {
			Long id = Long.parseLong(idParam.trim());
			User user = userDAO.getById(id);
			if (user == null) {
				response.sendRedirect(request.getContextPath() + "/user-list");
				return;
			}

			request.setAttribute("user", user);
			request.setAttribute("departments", departmentDAO.getActiveDepartments());
			request.setAttribute("roles", roleDAO.getActiveRoles());
			// Lấy danh sách manager tiềm năng (loại trừ chính user này để tránh circular)
			request.setAttribute("managers", userDAO.searchAndFilter("", null, null, true, null, 0, 1000));

			request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/user-list");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String idParam = request.getParameter("id");
		if (idParam == null || idParam.isBlank()) {
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

		try {
			Long id = Long.parseLong(idParam.trim());
			model.User user = new model.User();
			user.setId(id);

			// Fields that are not editable according to readonly attributes in JSP
			// user.setEmployeeCode(request.getParameter("employeeCode"));
			// user.setUsername(request.getParameter("username"));

			String rawPassword = request.getParameter("password");

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
				user.setRoleId(Long.parseLong(roleIdStr));
			}

			String managerIdStr = request.getParameter("managerId");
			if (managerIdStr != null && !managerIdStr.trim().isEmpty()) {
				user.setManagerId(Long.parseLong(managerIdStr));
			}

			String isActiveStr = request.getParameter("isActive");
			user.setIsActive("on".equals(isActiveStr));

			boolean success = userDAO.updateUserByAdmin(user, rawPassword);

			if (success) {
				request.getSession().setAttribute("successMsg", "Cập nhật nhân viên thành công!");
				response.sendRedirect(request.getContextPath() + "/user-list");
			} else {
				request.setAttribute("errorMsg", "Lỗi: Không thể cập nhật thông tin nhân viên.");
				// We need to fetch the full user again to keep readonly fields if returning to
				// form
				User existingUser = userDAO.getById(id);
				request.setAttribute("user", existingUser);
				doGet(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/user-list");
		}
	}
}
