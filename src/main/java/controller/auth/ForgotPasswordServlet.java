package controller.auth;

import dal.TicketDAO;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet responsible for handling forgotten password requests. Allows users to
 * submit their employee code to request a password reset ticket.
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password", "/auth/forgot-password"})
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
			forwardWithMessage(request, response, "error", "Vui lòng nhập Mã nhân viên!");
			return;
		}

		// Gọi xuống DAO xử lý tạo Ticket kiểm tra ràng buộc nghiệp vụ
		String result = ticketDAO.createResetTicket(employeeCode.trim());

		if ("SUCCESS".equals(result)) {
			forwardWithMessage(request, response, "success",
					"Gửi yêu cầu thành công! Vui lòng liên hệ Admin để nhận lại mật khẩu mới mặc định");
		} else {
			forwardWithMessage(request, response, "error", result);
		}
	}

	private void forwardWithMessage(HttpServletRequest request, HttpServletResponse response, String type,
			String message) throws ServletException, IOException {
		request.setAttribute(type, message);
		request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
	}
}