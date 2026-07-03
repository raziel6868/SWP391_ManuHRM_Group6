package controller.home;

import dal.DashboardDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * Servlet responsible for rendering the Home/Dashboard view. Retrieves and
 * displays dashboard statistics and active announcements.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/dashboard"})
public class HomeServlet extends HttpServlet {

	private final DashboardDAO dashboardDAO = new DashboardDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User authUser = null;
		if (session != null) {
			authUser = (User) session.getAttribute("authUser");
		}

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		request.setAttribute("stats", dashboardDAO.getDashboardStats());
		request.setAttribute("currentDate", LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
		request.setAttribute("announcements", buildAnnouncements());

		request.getRequestDispatcher("/views/home/dashboard.jsp").forward(request, response);
	}

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}

	private List<String> buildAnnouncements() {
		List<String> announcements = new ArrayList<>();
		announcements.add("Phòng Hành chính Nhân sự nhắc các bộ phận rà soát hồ sơ nhân viên trong tháng này.");
		announcements.add("Cập nhật danh sách ca làm và thông tin liên hệ quản lý trực tiếp trước ngày chốt công.");
		announcements.add("Tài khoản nội bộ sẽ tự đăng xuất sau 30 phút không hoạt động để bảo vệ dữ liệu nhân sự.");
		return announcements;
	}
}
