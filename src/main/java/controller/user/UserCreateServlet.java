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
import java.util.List;
import model.Department;
import model.Role;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet(
        name = "UserCreateServlet",
        urlPatterns = {"/user-create"})
public class UserCreateServlet extends HttpServlet {

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadDropdownData(request);
        // Lưu ý: Đổi tên file cho khớp với file jsp bạn đã tạo
        request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String employeeCode = request.getParameter("employeeCode");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String dobStr = request.getParameter("dob");
        String jobTitle = request.getParameter("jobTitle");
        String departmentIdStr = request.getParameter("departmentId");
        String employeeType = request.getParameter("employeeType");
        String roleIdStr = request.getParameter("roleId");

        // 1. Basic Server-side Validation
        if (employeeCode == null
                || employeeCode.trim().isEmpty()
                || username == null
                || username.trim().isEmpty()
                || password == null
                || password.trim().isEmpty()
                || fullName == null
                || fullName.trim().isEmpty()
                || roleIdStr == null
                || roleIdStr.trim().isEmpty()) {

            request.setAttribute("errorMsg", "Vui lòng nhập đầy đủ các trường bắt buộc (*).");
            loadDropdownData(request);
            request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
            return;
        }

        try {
            User user = new User();
            user.setEmployeeCode(employeeCode.trim());
            user.setUsername(username.trim());

            // Hash password
            String hashedPwd = BCrypt.hashpw(password, BCrypt.gensalt(12));
            user.setPasswordHash(hashedPwd);

            user.setFullName(fullName.trim());
            user.setPhone(phone != null ? phone.trim() : null);
            user.setJobTitle(jobTitle != null ? jobTitle.trim() : null);
            user.setEmployeeType(User.EmployeeType.valueOf(employeeType));
            user.setRoleId(Long.parseLong(roleIdStr));

            if (departmentIdStr != null && !departmentIdStr.trim().isEmpty()) {
                user.setDepartmentId(Long.parseLong(departmentIdStr));
            }

            if (dobStr != null && !dobStr.trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dob = sdf.parse(dobStr);
                user.setDob(dob);
            }

            user.setIsActive(true);

            // 2. Insert vào DB
            boolean isInserted = userDAO.insert(user);

            if (isInserted) {
                response.sendRedirect("user-list");
            } else {
                request.setAttribute(
                        "errorMsg",
                        "Có lỗi xảy ra khi lưu vào CSDL (Mã NV hoặc Username có thể đã tồn tại).");
                loadDropdownData(request);
                request.getRequestDispatcher("/views/user/user-form.jsp")
                        .forward(request, response);
            }

        } catch (ParseException | IllegalArgumentException e) {
            request.setAttribute(
                    "errorMsg",
                    "Dữ liệu đầu vào không hợp lệ (Ngày sinh, phòng ban hoặc vai trò).");
            loadDropdownData(request);
            request.getRequestDispatcher("/views/user/user-form.jsp").forward(request, response);
        }
    }

    private void loadDropdownData(HttpServletRequest request) {
        List<Department> departments =
                departmentDAO.getActiveDepartments(); // Giả sử bạn có hàm này
        List<Role> roles = roleDAO.getActiveRoles(); // Giả sử bạn có hàm này
        request.setAttribute("departments", departments);
        request.setAttribute("roles", roles);
    }
}
