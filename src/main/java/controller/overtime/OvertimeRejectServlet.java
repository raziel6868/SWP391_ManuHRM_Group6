package controller.overtime;

import dal.OvertimeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.OvertimeRecord;
import model.User;

@WebServlet(name = "OvertimeRejectServlet", urlPatterns = {"/overtime-reject"})
public class OvertimeRejectServlet extends HttpServlet {

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		String redirectUrl = request.getContextPath() + "/overtime-list";

		if (id == null) {
			session.setAttribute("errorMsg", "Yêu cầu OT không hợp lệ.");
			response.sendRedirect(redirectUrl);
			return;
		}

		OvertimeRecord record = overtimeDAO.getById(id);
		if (record == null) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (!"PENDING".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Yêu cầu này đã được xử lý trước đó.");
			response.sendRedirect(redirectUrl);
			return;
		}
		if (authUser.getId().equals(record.getUserId())) {
			session.setAttribute("errorMsg", "Bạn không thể từ chối yêu cầu OT của chính mình.");
			response.sendRedirect(redirectUrl);
			return;
		}

		boolean rejected = overtimeDAO.reject(id, authUser.getId());
		if (rejected) {
			session.setAttribute("successMsg", "Đã từ chối yêu cầu OT của " + record.getEmployeeName() + ".");
		} else {
			session.setAttribute("errorMsg", "Không thể từ chối yêu cầu (có thể đã được xử lý bởi người khác).");
		}

		response.sendRedirect(redirectUrl);
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
