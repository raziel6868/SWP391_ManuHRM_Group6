package controller.leavetype;

import dal.LeaveTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.LeaveType;

@WebServlet(name = "LeaveTypeStatusServlet", urlPatterns = {"/leave-type-status"})
public class LeaveTypeStatusServlet extends HttpServlet {

	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		String isActiveParam = request.getParameter("isActive");

		if (id == null || isActiveParam == null || isActiveParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		LeaveType leaveType = leaveTypeDAO.getById(id);
		if (leaveType == null) {
			request.getSession().setAttribute("errorMsg", "Loại nghỉ không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		boolean isActive = Boolean.parseBoolean(isActiveParam);
		boolean success = leaveTypeDAO.updateStatus(id, isActive);

		if (success) {
			request.getSession().setAttribute("successMsg",
					isActive ? "Kích hoạt loại nghỉ thành công." : "Vô hiệu hóa loại nghỉ thành công.");
		} else {
			request.getSession().setAttribute("errorMsg", "Không thể thay đổi trạng thái loại nghỉ.");
		}

		response.sendRedirect(request.getContextPath() + "/leave-type-list");
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
