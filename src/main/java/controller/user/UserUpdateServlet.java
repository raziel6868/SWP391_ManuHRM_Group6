package controller.user;

import dal.DepartmentDAO;
import dal.RoleDAO;
import dal.UserDAO;
import dal.JobTitleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import model.User;

@WebServlet(name = "UserUpdateServlet", urlPatterns = {"/user-update"})
public class UserUpdateServlet extends HttpServlet {

	private final UserDAO userDAO = new UserDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final RoleDAO roleDAO = new RoleDAO();
	private final JobTitleDAO jobTitleDAO = new JobTitleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User authUser = (User) session.getAttribute("authUser");

		// canUpdate: chỉ hierarchyLevel >= 3 (HR_MANAGER/SYSADMIN) được sửa
		int authHierarchyLevel = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		if (authHierarchyLevel < 3) {
			session.setAttribute("errorMsg", "Bạn không có quyền chỉnh sửa nhân viên.");
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

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

			// Không tự sửa mình
			if (authUser.getId().equals(user.getId())) {
				session.setAttribute("errorMsg", "Bạn không thể tự chỉnh sửa thông tin của mình.");
				response.sendRedirect(request.getContextPath() + "/user-list");
				return;
			}

			request.setAttribute("user", user);
			request.setAttribute("departments", departmentDAO.getActiveDepartments());
			request.setAttribute("roles", roleDAO.getActiveRoles());
			request.setAttribute("jobTitles", jobTitleDAO.getActiveJobTitles());
			request.setAttribute("managers", userDAO.searchUsers("", null, null, true, null, 0, 1000));

			request.getRequestDispatcher("/views/user/user-update.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/user-list");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User authUser = (User) session.getAttribute("authUser");

		// canUpdate: chỉ hierarchyLevel >= 3 (HR_MANAGER/SYSADMIN) được sửa
		int authHierarchyLevel = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		if (authHierarchyLevel < 3) {
			session.setAttribute("errorMsg", "Bạn không có quyền chỉnh sửa nhân viên.");
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

		request.setCharacterEncoding("UTF-8");

		String idParam = request.getParameter("id");
		if (idParam == null || idParam.isBlank()) {
			response.sendRedirect(request.getContextPath() + "/user-list");
			return;
		}

		try {
			Long id = Long.parseLong(idParam.trim());
			User targetUser = userDAO.getById(id);
			if (targetUser == null) {
				response.sendRedirect(request.getContextPath() + "/user-list");
				return;
			}

			// Không tự sửa mình
			if (authUser.getId().equals(targetUser.getId())) {
				session.setAttribute("errorMsg", "Bạn không thể tự chỉnh sửa thông tin của mình.");
				response.sendRedirect(request.getContextPath() + "/user-list");
				return;
			}

			User user = new User();
			user.setId(id);

			String rawPassword = request.getParameter("password");
			user.setFullName(request.getParameter("fullName"));
			user.setPhone(request.getParameter("phone"));

			String dobStr = request.getParameter("dob");
			if (dobStr != null && !dobStr.trim().isEmpty()) {
				user.setDob(Date.valueOf(dobStr));
			}

			user.setJobTitleId(paramLong(request, "jobTitleId"));

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
				user.setRoleId(Long.parseLong(roleIdStr));
			}

			String managerIdStr = request.getParameter("managerId");
			if (managerIdStr != null && !managerIdStr.trim().isEmpty()) {
				user.setManagerId(Long.parseLong(managerIdStr));
			}

			String isActiveStr = request.getParameter("isActive");
			user.setIsActive("on".equals(isActiveStr));

			boolean success = userDAO.updateByAdmin(user, rawPassword);

			if (success) {
				request.getSession().setAttribute("successMsg", "Cập nhật nhân viên thành công!");
				response.sendRedirect(request.getContextPath() + "/user-list");
			} else {
				request.setAttribute("errorMsg", "Lỗi: Không thể cập nhật thông tin nhân viên.");
				User existingUser = userDAO.getById(id);
				request.setAttribute("user", existingUser);
				doGet(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/user-list");
		}
	}

	private Long paramLong(HttpServletRequest request, String name) {
		String val = request.getParameter(name);
		if (val == null || val.trim().isEmpty())
			return null;
		try {
			return Long.parseLong(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
