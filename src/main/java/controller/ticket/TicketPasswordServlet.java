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

@WebServlet(name = "TicketPasswordServlet", urlPatterns = {"/admin/tickets/set-password"})
public class TicketPasswordServlet extends HttpServlet {

	private final TicketDAO ticketDAO = new TicketDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String ticketIdStr = request.getParameter("id");
		if (ticketIdStr == null || ticketIdStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
			return;
		}

		try {
			long ticketId = Long.parseLong(ticketIdStr);
			List<PasswordReset> tickets = ticketDAO.getPendingTickets();
			PasswordReset ticket = tickets.stream().filter(t -> t.getId() == ticketId).findFirst().orElse(null);

			if (ticket == null) {
				response.sendRedirect(request.getContextPath() + "/admin/tickets");
				return;
			}

			request.setAttribute("pendingTicketId", ticketId);
			request.setAttribute("ticket", ticket);

			request.getRequestDispatcher("/views/ticket/ticket-password-form.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/admin/tickets");
		}
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

		if (newPassword == null || newPassword.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng nhập mật khẩu mới!");
			response.sendRedirect(request.getContextPath() + "/admin/tickets/set-password?id=" + ticketId);
			return;
		}

		dal.PasswordResetResult result = ticketDAO.processTicket(ticketId, admin.getId(), newPassword.trim());
		if (result.isSuccess()) {
			session.setAttribute("successMsg", "Duyệt thành công! Mật khẩu mới: " + result.getNewPassword());
		} else {
			session.setAttribute("errorMsg", result.getMessage());
		}

		response.sendRedirect(request.getContextPath() + "/admin/tickets");
	}
}
