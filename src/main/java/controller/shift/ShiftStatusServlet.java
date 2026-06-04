package controller.shift;

import dal.ShiftDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Shift;

@WebServlet(name = "ShiftStatusServlet", urlPatterns = {"/shift-status"})
public class ShiftStatusServlet extends HttpServlet {

	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		String isActiveParam = request.getParameter("isActive");

		if (id == null || isActiveParam == null || isActiveParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/shift-list");
			return;
		}

		Shift shift = shiftDAO.getById(id);
		if (shift == null) {
			request.getSession().setAttribute("errorMsg", "Ca làm việc không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/shift-list");
			return;
		}

		boolean isActive = Boolean.parseBoolean(isActiveParam);
		boolean success = shiftDAO.updateStatus(id, isActive);

		if (success) {
			request.getSession().setAttribute("successMsg",
					isActive ? "Kích hoạt ca làm việc thành công." : "Vô hiệu hóa ca làm việc thành công.");
		} else {
			request.getSession().setAttribute("errorMsg", "Không thể thay đổi trạng thái ca làm việc.");
		}

		response.sendRedirect(request.getContextPath() + "/shift-list");
	}

	private Long parseId(String idParam) {
		if (idParam == null || idParam.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(idParam);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
