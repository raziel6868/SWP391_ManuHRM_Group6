package controller.auth;

import dal.TicketDAO;
import dal.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/auth/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private final TicketDAO ticketDAO = new TicketDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị trang nhập mã nhân viên để xin cấp lại mật khẩu
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String employeeCode = request.getParameter("employeeCode");

        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập Mã nhân viên!");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        // Gọi xuống DAO xử lý tạo Ticket kiểm tra ràng buộc nghiệp vụ
        String result = ticketDAO.sendPasswordResetTicket(employeeCode.trim());

        if ("SUCCESS".equals(result)) {
            request.setAttribute("success", "Gửi yêu cầu thành công! Vui lòng liên hệ Tổ trưởng/Quản đốc hoặc Admin để nhận lại mật khẩu mới mặc định (123456).");
        } else {
            // Trả về thông báo lỗi cụ thể (Không tồn tại code hoặc đang có ticket chờ xử lý)
            request.setAttribute("error", result);
        }

        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }
}