package controller.user;

import dal.DepartmentDAO;
import dal.UserDAO;
import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
import java.sql.Date;
import util.PasswordUtil;
import util.ValidationUtil;

@WebServlet(name = "UserCreateServlet", urlPatterns = {"/user-create"})
public class UserCreateServlet extends HttpServlet {

	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final RoleDAO roleDAO = new RoleDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		model.User authUser = (model.User) session.getAttribute("authUser");

		// Chỉ HR_Manager và Sysadmin mới được tạo user
		if ("EMPLOYEE".equals(authUser.getRoleName()) || "LINE_MANAGER".equals(authUser.getRoleName())) {
			session.setAttribute("errorMsg", "Bạn không có quyền thêm nhân viên mới.");
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("roles", roleDAO.getActiveRoles());
		request.setAttribute("managers", userDAO.searchUsers("", null, null, true, null, 0, 1000));
		request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		model.User authUser = (model.User) session.getAttribute("authUser");

		// Chỉ HR_Manager và Sysadmin mới được tạo user
		if ("EMPLOYEE".equals(authUser.getRoleName()) || "LINE_MANAGER".equals(authUser.getRoleName())) {
			session.setAttribute("errorMsg", "Bạn không có quyền thêm nhân viên mới.");
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

		request.setCharacterEncoding("UTF-8");

		String employeeCode = request.getParameter("employeeCode");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String fullName = request.getParameter("fullName");

		// Bước 1: Ép kiểu theo yêu cầu
		if (employeeCode != null) {
			employeeCode = employeeCode.trim().toUpperCase();
		}
		if (username != null) {
			username = username.trim().toLowerCase();
		}

		// Bước 2: Validate rỗng
		if (ValidationUtil.isBlank(employeeCode)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Mã nhân viên không được để trống.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}
		if (ValidationUtil.isBlank(username)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Tên đăng nhập không được để trống.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}
		if (ValidationUtil.isBlank(password)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Mật khẩu không được để trống.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}
		if (ValidationUtil.isBlank(fullName)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Họ và tên không được để trống.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}

		// Validate độ dài
		if (employeeCode.length() > 20) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Mã nhân viên không được vượt quá 20 ký tự.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}
		if (username.length() > 50) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Tên đăng nhập không được vượt quá 50 ký tự.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}
		if (!ValidationUtil.hasMinLength(password, 6)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Mật khẩu phải có ít nhất 6 ký tự.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}

		// Bước 3: Kiểm tra trùng lặp trong Database
		if (userDAO.existsByEmployeeCode(employeeCode)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Mã nhân viên '" + employeeCode + "' đã tồn tại trong hệ thống.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}
		if (userDAO.existsByUsername(username)) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Tên đăng nhập '" + username + "' đã tồn tại trong hệ thống.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}

		// Bước 4: Validate username format (chỉ chấp nhận alphanumeric và underscore)
		if (!ValidationUtil.matchRegex(username, "^[a-zA-Z0-9_]+$")) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới.");
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			return;
		}

		try {
			User user = new User();
			user.setEmployeeCode(employeeCode);
			user.setUsername(username);
			user.setPasswordHash(PasswordUtil.hashPassword(password));
			user.setFullName(fullName.trim());
			user.setPhone(request.getParameter("phone"));

			String dobStr = request.getParameter("dob");
			if (dobStr != null && !dobStr.trim().isEmpty()) {
				user.setDob(Date.valueOf(dobStr));
			}

			user.setJobTitle(request.getParameter("jobTitle"));

			String employeeTypeStr = request.getParameter("employeeType");
			if (employeeTypeStr != null && !employeeTypeStr.trim().isEmpty()) {
				user.setEmployeeType(User.EmployeeType.valueOf(employeeTypeStr));
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

			user.setIsActive(true);

			boolean success = userDAO.insert(user);

			if (success) {
				request.getSession().setAttribute("successMsg", "Thêm nhân viên '" + fullName + "' thành công!");
				response.sendRedirect(request.getContextPath() + "/user-list");
			} else {
				setFormAttributes(request);
				request.setAttribute("errorMsg", "Lỗi: Không thể thêm nhân viên. Vui lòng thử lại.");
				request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
			}
		} catch (Exception e) {
			setFormAttributes(request);
			request.setAttribute("errorMsg", "Lỗi dữ liệu đầu vào: " + e.getMessage());
			request.getRequestDispatcher("/views/user/user-create.jsp").forward(request, response);
		}
	}

	private void setFormAttributes(HttpServletRequest request) {
		request.setAttribute("departments", departmentDAO.getActiveDepartments());
		request.setAttribute("roles", roleDAO.getActiveRoles());
		request.setAttribute("managers", userDAO.searchUsers("", null, null, true, null, 0, 1000));
	}
}
