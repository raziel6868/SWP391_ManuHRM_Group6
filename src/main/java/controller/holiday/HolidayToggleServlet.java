package controller.holiday;

import dal.HolidayDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "HolidayToggleServlet", urlPatterns = {"/holiday-toggle"})
public class HolidayToggleServlet extends HttpServlet {

	private final HolidayDAO holidayDAO = new HolidayDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		String action = request.getParameter("action");

		HttpSession session = request.getSession();

		if (idStr == null || idStr.isEmpty()) {
			session.setAttribute("errorMsg", "ID ngày lễ không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/holiday-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr);
			boolean activate = "activate".equals(action);
			boolean success = holidayDAO.toggleActive(id, activate);

			if (success) {
				session.setAttribute("successMsg", activate ? "Đã kích hoạt ngày lễ." : "Đã vô hiệu hóa ngày lễ.");
			} else {
				session.setAttribute("errorMsg", "Không thể cập nhật trạng thái ngày lễ.");
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID ngày lễ không hợp lệ.");
		}

		response.sendRedirect(request.getContextPath() + "/holiday-list");
	}
}
