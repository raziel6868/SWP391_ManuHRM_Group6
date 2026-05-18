package controller.user;

import dal.DepartmentDAO;
import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import model.User;

@WebServlet(
        name = "UserUpdateServlet",
        urlPatterns = {"/user-update"})
public class UserUpdateServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect("user-list");
            return;
        }

        try {
            Long id = Long.parseLong(idParam.trim());
            User user = userDAO.getById(id);

            if (user == null) {
                response.sendRedirect("user-list");
                return;
            }

            request.setAttribute("user", user);

        } catch (NumberFormatException e) {
            response.sendRedirect("user-list");
            return;
        }

        request.setAttribute("departments", departmentDAO.getActiveDepartments());
        request.setAttribute("roles", roleDAO.getActiveRoles());

        request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String dobParam = request.getParameter("dob");
        String jobTitle = request.getParameter("jobTitle");
        String deptParam = request.getParameter("departmentId");
        String empTypeParam = request.getParameter("employeeType");
        String roleParam = request.getParameter("roleId");
        String password = request.getParameter("password");

        try {
            Long id = Long.parseLong(idParam.trim());
            User user = userDAO.getById(id);

            if (user == null) {
                response.sendRedirect("user-list");
                return;
            }

            user.setFullName(fullName);
            user.setPhone(phone);
            user.setJobTitle(jobTitle);

            if (dobParam != null && !dobParam.isBlank()) {
                try {
                    Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobParam);
                    user.setDob(dob);
                } catch (ParseException e) {
                    user.setDob(null);
                }
            } else {
                user.setDob(null);
            }

            if (deptParam != null && !deptParam.isBlank()) {
                user.setDepartmentId(Long.parseLong(deptParam.trim()));
            } else {
                user.setDepartmentId(null);
            }

            if (empTypeParam != null) {
                user.setEmployeeType(User.EmployeeType.valueOf(empTypeParam));
            }

            if (roleParam != null && !roleParam.isBlank()) {
                user.setRoleId(Long.parseLong(roleParam.trim()));
            }

            boolean success = userDAO.updateProfile(user, password);

            if (success) {
                response.sendRedirect("user-list");
            } else {
                request.setAttribute("errorMsg", "Cập nhật dữ liệu thất bại! Vui lòng thử lại.");
                request.setAttribute("user", user);
                request.setAttribute("departments", departmentDAO.getActiveDepartments());
                request.setAttribute("roles", roleDAO.getActiveRoles());
                request.getRequestDispatcher("/views/user/user-form.jsp")
                        .forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Hệ thống gặp lỗi: " + e.getMessage());
            request.setAttribute("departments", departmentDAO.getActiveDepartments());
            request.setAttribute("roles", roleDAO.getActiveRoles());
            request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
        }
    }
}
