package controller.overtime;

import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.OvertimeRecord;
import model.Permission;
import model.User;

/**
 * Không còn là "từ chối duyệt" nữa (vì không còn bước chờ duyệt). Giờ đây là
 * HỦY 1 bản ghi OT đã tạo nhầm — chỉ quản đốc quản lý trực tiếp nhân viên đó
 * mới hủy được, và chỉ khi tháng chưa chốt công. Dùng quyền riêng OT_CANCEL
 * (khác với OT_REQUEST của tạo/import và OT_UPDATE của sửa) để khớp với
 * permission -> url_pattern mà AuthFilter dùng để chặn theo ALLOWED_URLS.
 */
@WebServlet(name = "OvertimeRejectServlet", urlPatterns = {"/overtime-reject"})
public class OvertimeRejectServlet extends HttpServlet {

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(request.getContextPath() + "/overtime-list");
	}

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
		if (!hasPermission(session, "OT_CANCEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		OvertimeRecord record = id != null ? overtimeDAO.getById(id) : null;
		if (record == null || !"APPROVED".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT hoặc đã bị hủy trước đó.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		int recYear = record.getDate().toLocalDate().getYear();
		int recMonth = record.getDate().toLocalDate().getMonthValue();
		String redirectList = request.getContextPath() + "/overtime-list?year=" + recYear + "&month=" + recMonth;

		User targetUser = userDAO.getById(record.getUserId());
		if (targetUser == null || targetUser.getManagerId() == null
				|| !targetUser.getManagerId().equals(authUser.getId())) {
			session.setAttribute("errorMsg", "Bạn chỉ có thể hủy OT của nhân viên dưới quyền mình.");
			response.sendRedirect(redirectList);
			return;
		}

		if (!monthlySheetDAO.isEditablePeriodForSupervisor(recYear, recMonth, authUser.getId())) {
			session.setAttribute("errorMsg", "Tháng " + recMonth + "/" + recYear + " không còn mở cho bạn hủy OT.");
			response.sendRedirect(redirectList);
			return;
		}

		boolean cancelled = overtimeDAO.cancel(id, authUser.getId());
		if (cancelled) {
			session.setAttribute("successMsg", "Đã hủy yêu cầu OT.");
		} else {
			session.setAttribute("errorMsg", "Không thể hủy yêu cầu OT. Vui lòng thử lại.");
		}
		response.sendRedirect(redirectList);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (code.equals(p.getCode())) {
				return true;
			}
		}
		return false;
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
