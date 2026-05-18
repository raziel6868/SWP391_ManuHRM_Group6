package controller.auth;

import dal.UserDAO;
import model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/auth/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        /*if (session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.jsp");
            return;
        }*/
        // Forward sang trang giao diện đổi mật khẩu
        request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đảm bảo không lỗi tiếng Việt khi hiển thị thông báo alert
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        /*if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.jsp");
            return;
        }*/

        // Đọc dữ liệu thô (Plain text) từ Form gửi lên
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // RÀNG BUỘC 1: Kiểm tra độ dài mật khẩu mới (Tối thiểu 6 ký tự)
        if (newPassword == null || newPassword.trim().length() < 6) {
            request.setAttribute("error", "Mật khẩu mới phải có độ dài tối thiểu từ 6 ký tự trở lên!");
            request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
            return;
        }

        // RÀNG BUỘC 2: Kiểm tra mật khẩu nhập lại có trùng khớp không
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Xác nhận mật khẩu mới không trùng khớp! Vui lòng nhập lại.");
            request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
            return;
        }
        
        // RÀNG BUỘC 3: Mật khẩu mới không được trùng khớp hoàn toàn với mật khẩu cũ trước khi hash
        if (currentPassword.equals(newPassword)) {
            request.setAttribute("error", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
            return;
        }

        // Chuyển dữ liệu xuống tầng DAO để xử lý giải băm cũ và băm mật khẩu mới
        //boolean isUpdated = userDAO.changePassword(currentUser.getId(), currentPassword, newPassword);
        boolean isUpdated = userDAO.changePassword(Long.parseLong("1"), currentPassword, newPassword);

        if (isUpdated) {
            request.setAttribute("success", "Đổi mật khẩu thành công! Hãy dùng mật khẩu mới cho lần đăng nhập sau.");
            request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
        } else {
            // RÀNG BUỘC 4: Sai mật khẩu cũ (DAO trả về false khi checkpw thất bại)
            request.setAttribute("error", "Mật khẩu hiện tại không chính xác. Vui lòng kiểm tra lại.");
            request.getRequestDispatcher("/views/auth/change-password.jsp").forward(request, response);
        }
    }
}