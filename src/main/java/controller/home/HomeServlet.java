package controller.home;

import dal.DashboardDAO;
import dal.PermissionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import model.User;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/dashboard"})
public class HomeServlet extends HttpServlet {
	private final DashboardDAO dashboardDAO = new DashboardDAO();
	private final PermissionDAO permissionDAO = new PermissionDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User authUser = session == null ? null : (User) session.getAttribute("authUser");

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		request.setAttribute("stats", dashboardDAO.getDashboardStats());
		request.setAttribute("permissions", permissionDAO.getPermissionsByRoleId(authUser.getRoleId()));
		request.setAttribute("currentDate", LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
		request.setAttribute("announcements", buildAnnouncements());

		request.getRequestDispatcher("/views/home/dashboard.jsp").forward(request, response);
	}

	private List<String> buildAnnouncements() {
		return List.of("Phòng Hành chính Nhân sự nhắc các bộ phận rà soát hồ sơ nhân viên trong tháng này.",
				"Cập nhật danh sách ca làm và thông tin liên hệ quản lý trực tiếp trước ngày chốt công.",
				"Tài khoản nội bộ sẽ tự đăng xuất sau 30 phút không hoạt động để bảo vệ dữ liệu nhân sự.");
	}
}
