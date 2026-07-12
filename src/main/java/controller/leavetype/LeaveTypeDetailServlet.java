package controller.leavetype;

import dal.LeaveTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.LeaveType;

@WebServlet(name = "LeaveTypeDetailServlet", urlPatterns = {"/leave-type-detail"})
public class LeaveTypeDetailServlet extends HttpServlet {

	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		if (id == null) {
			request.getSession().setAttribute("errorMsg", "Không tìm thấy loại nghỉ cần xem chi tiết.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		LeaveType leaveType = leaveTypeDAO.getById(id);
		if (leaveType == null) {
			request.getSession().setAttribute("errorMsg", "Loại nghỉ không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		HttpSession session = request.getSession();
		request.setAttribute("leaveType", leaveType);
		request.setAttribute("canUpdate", hasPermission(session, "LEAVE_TYPE_UPDATE"));
		request.setAttribute("canChangeStatus", hasPermission(session, "LEAVE_TYPE_STATUS"));
		request.getRequestDispatcher("/views/leavetype/leave-type-detail.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<model.Permission> permissions = (List<model.Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}

		for (model.Permission permission : permissions) {
			if (permissionCode.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
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
