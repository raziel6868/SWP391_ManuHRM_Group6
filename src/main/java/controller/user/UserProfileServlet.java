package controller.user;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

@WebServlet(
        name = "UserProfileServlet",
        urlPatterns = {"/profile"})
public class UserProfileServlet extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Lấy session hiện tại
        HttpSession session = request.getSession();
        
        // 2. Lấy đối tượng user đã đăng nhập từ session (đặt tên thuộc tính tùy thuộc vào hệ thống của bạn, ví dụ: "currentUser")
        User currentUser = (User) session.getAttribute("currentUser");
        
        // Kiểm tra nếu chưa đăng nhập thì chuyển hướng về trang login
        /*if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }*/
        
        // 3. Gọi DAO lấy thông tin chi tiết, đầy đủ nhất từ DB bằng ID
        //User userDetail = userDAO.getById(currentUser.getId());
        User userDetail = userDAO.getById(Long.parseLong("1"));
        if (userDetail != null) {
            // 4. Đẩy dữ liệu sang request attribute để hiển thị bên JSP
            request.setAttribute("user", userDetail);
            request.getRequestDispatcher("/views/user/profile.jsp").forward(request, response);
        } else {
            // Xử lý lỗi nếu không tìm thấy user trong DB
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thông tin người dùng.");
        }
    }
}