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

		// Mọi người đều có thể xem user detail
		// Nhưng Line Manager chỉ xem được user dưới quyền
		if ("LINE_MANAGER".equals(authUser.getRoleName())) {
			if (targetUser.getManagerId() == null || !targetUser.getManagerId().equals(authUser.getId())) {
				session.setAttribute("errorMsg", "Bạn không có quyền xem thông tin nhân viên này.");
				response.sendRedirect("user-list");
				return;
			}
		}

		request.setAttribute("user", targetUser);
		request.getRequestDispatcher("/views/user/user-detail.jsp").forward(request, response);
	}
}
