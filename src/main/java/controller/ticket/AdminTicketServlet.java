package controller.ticket;

import dal.UserDAO;
import model.PasswordTicket;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AdminTicketServlet", urlPatterns = {"/admin/tickets"})
public class AdminTicketServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy toàn bộ ticket ở trạng thái PENDING lên hiển thị
        List<PasswordTicket> ticketList = userDAO.getAllPendingTickets();
        request.setAttribute("ticketList", ticketList);
        request.getRequestDispatcher("/views/ticket/admin-tickets.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        int ticketId = Integer.parseInt(request.getParameter("ticketId"));
        String employeeCode = request.getParameter("employeeCode");

        // Thực thi cập nhật DB: Đổi pass thành 123456 và đổi status ticket thành RESOLVED
        boolean success = userDAO.resolvePasswordTicket(ticketId, employeeCode);

        if (success) {
            request.setAttribute("success", "Đã phê duyệt thành công! Mật khẩu của nhân viên " + employeeCode + " đã được đặt lại thành mặc định: 123456.");
        }
        
        // Load lại danh sách mới sau khi xử lý xong
        List<PasswordTicket> ticketList = userDAO.getAllPendingTickets();
        request.setAttribute("ticketList", ticketList);
        request.getRequestDispatcher("/views/ticket/admin-tickets.jsp").forward(request, response);
    }
}