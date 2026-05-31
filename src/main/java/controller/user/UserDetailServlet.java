package controller.user;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

@WebServlet(name = "UserDetailServlet", urlPatterns = {"/user-detail"})
public class UserDetailServlet extends HttpServlet {

	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idParam = request.getParameter("id");

		if (idParam == null || idParam.isBlank()) {
			response.sendRedirect("user-list");
			return;
		}

		Long id;
		try {
			id = Long.parseLong(idParam.trim());
		} catch (NumberFormatException e) {
			response.sendRedirect("user-list");
			return;
		}

		HttpSession session = request.getSession(false);
		User authUser = (User) session.getAttribute("authUser");
		User targetUser = userDAO.getById(id);

		if (targetUser == null) {
			response.sendRedirect("user-list");
			return;
		}

		// Mọi người đều xem được user detail
		// HierarchyLevel <= 2 (PRODUCTION_SUPERVISOR, EMPLOYEE): chỉ xem được
		// subordinate
		int authHierarchyLevel = authUser.getHierarchyLevel() != null ? authUser.getHierarchyLevel() : 1;
		if (authHierarchyLevel <= 2) {
			if (targetUser.getManagerId() == null || !targetUser.getManagerId().equals(authUser.getId())) {
				session.setAttribute("errorMsg", "Bạn không có quyền xem thông tin nhân viên này.");
				response.sendRedirect("user-list");
				return;
			}
		}

		// canEdit/canDeactivate: chỉ hierarchyLevel >= 3 (HR_MANAGER/SYSADMIN) được
		// sửa/khóa
		// PRODUCTION_SUPERVISOR (rank 2) và EMPLOYEE (rank 1) không có quyền này
		boolean canEdit = (authHierarchyLevel >= 3) && !authUser.getId().equals(targetUser.getId());
		boolean canDeactivate = canEdit;
		request.setAttribute("user", targetUser);
		request.setAttribute("canEdit", canEdit);
		request.setAttribute("canDeactivate", canDeactivate);
		request.getRequestDispatcher("/views/user/user-detail.jsp").forward(request, response);
	}
}
