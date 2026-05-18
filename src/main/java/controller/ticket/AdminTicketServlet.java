package controller.ticket;

import dal.TicketDAO;
import model.PasswordReset;
import model.User; 
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminTicketServlet", urlPatterns = {"/admin/tickets"})
public class AdminTicketServlet extends HttpServlet {

    private final TicketDAO ticketDAO = new TicketDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Tải danh sách bao gồm cả PENDING và REJECTED
        List<PasswordReset> ticketList = ticketDAO.getAllManageableTickets();
        
        request.setAttribute("ticketList", ticketList);
        request.getRequestDispatcher("/views/ticket/admin-tickets.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        long ticketId = Long.parseLong(request.getParameter("ticketId"));
        String action = request.getParameter("action"); // "APPROVE" hoặc "REJECT"

        HttpSession session = request.getSession();
        User currentAdmin = (User) session.getAttribute("user"); 
        
        long adminId = 1; 
        
        //fix cung admin = 1
        /*
        if (currentAdmin != null) {
            adminId = currentAdmin.getId(); 
        } else {
            response.sendRedirect(request.getContextPath() + "/auth/login.jsp");
            return;
        }*/

        // Thực thi cập nhật theo hành động tương ứng
        boolean success = ticketDAO.updateTicketStatus(ticketId, adminId, action);

        if (success) {
            if ("APPROVE".equals(action)) {
                request.setAttribute("success", "Đã phê duyệt và đặt lại mật khẩu thành viên về mặc định: 123456.");
            } else {
                request.setAttribute("success", "Đã từ chối yêu cầu khôi phục mật khẩu thành công.");
            }
        } else {
            request.setAttribute("error", "Hệ thống xử lý thất bại!");
        }
        
        // Tải lại danh sách mới
        List<PasswordReset> ticketList = ticketDAO.getAllManageableTickets();
        request.setAttribute("ticketList", ticketList);
        request.getRequestDispatcher("/views/ticket/admin-tickets.jsp").forward(request, response);
    }
}