package controller.ticket;

import dal.TicketDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.PasswordReset;
import model.User;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminTicketServlet", urlPatterns = {"/admin/tickets"})
public class AdminTicketServlet extends HttpServlet {

	private final TicketDAO ticketDAO = new TicketDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String successMsg = (String) session.getAttribute("successMsg");
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}

		List<PasswordReset> tickets = ticketDAO.getPendingTickets();
		request.setAttribute("tickets", tickets);

		request.getRequestDispatcher("/views/ticket/admin-ticket.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		User admin = (User) session.getAttribute("authUser");
		if (admin == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		String action = request.getParameter("action");
		String ticketIdStr = request.getParameter("ticketId");
		String newPassword = request.getParameter("newPassword");

		if (ticketIdStr == null || ticketIdStr.isEmpty()) {
			session.setAttribute("errorMsg", "Ticket không hợp lệ!");
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
			return;
		}

		long ticketId;
		try {
			ticketId = Long.parseLong(ticketIdStr);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Ticket ID không hợp lệ!");
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
			return;
		}

		if ("approve".equals(action)) {
			if (newPassword == null || newPassword.trim().isEmpty()) {
				session.setAttribute("errorMsg", "Vui lòng nhập mật khẩu mới!");
				response.sendRedirect(request.getContextPath() + "/admin/tickets");
				return;
			}

			dal.PasswordResetResult result = ticketDAO.processTicket(ticketId, admin.getId(), newPassword.trim());
			if (result.isSuccess()) {
				session.setAttribute("successMsg", "Duyệt thành công! Mật khẩu mới: " + result.getNewPassword());
			} else {
				session.setAttribute("errorMsg", result.getMessage());
			}
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
		} else if ("reject".equals(action)) {
			if (ticketDAO.rejectTicket(ticketId, admin.getId())) {
				session.setAttribute("successMsg", "Đã từ chối yêu cầu!");
			} else {
				session.setAttribute("errorMsg", "Không thể từ chối yêu cầu!");
			}
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
		} else {
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
		}
	}
}
