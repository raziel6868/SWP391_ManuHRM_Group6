package controller.overtime;

import dal.LeaveRequestDAO;
import dal.AttendanceDAO;
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
import java.math.BigDecimal;
import java.util.List;
import model.OvertimeRecord;
import model.Permission;
import model.User;
import util.OvertimeValidator;

/**
 * Trang sửa 1 bản ghi OT đã tạo (đổi giờ / lý do). Chỉ quản đốc quản lý trực
 * tiếp nhân viên đó mới sửa được, và chỉ khi kỳ công đang mở cho quản đốc đó.
 * Toàn bộ rule nghiệp vụ dùng chung ở {@link OvertimeValidator} (trần
 * giờ/ngày-tháng-năm, trùng, conflict nghỉ phép/chấm công...).
 */
@WebServlet(name = "OvertimeEditServlet", urlPatterns = {"/overtime-edit"})
public class OvertimeEditServlet extends HttpServlet {

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final OvertimeValidator overtimeValidator = new OvertimeValidator(overtimeDAO, monthlySheetDAO,
			new AttendanceDAO(), new LeaveRequestDAO());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		if (!hasPermission(session, "OT_UPDATE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		OvertimeRecord record = id != null ? overtimeDAO.getById(id) : null;
		if (record == null || !"APPROVED".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT hoặc đã bị hủy.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		User targetUser = userDAO.getById(record.getUserId());
		if (targetUser == null || targetUser.getManagerId() == null
				|| !targetUser.getManagerId().equals(authUser.getId())) {
			session.setAttribute("errorMsg", "Bạn chỉ có thể sửa OT của nhân viên dưới quyền mình.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		int recYear = record.getDate().toLocalDate().getYear();
		int recMonth = record.getDate().toLocalDate().getMonthValue();
		if (!monthlySheetDAO.isEditablePeriodForSupervisor(recYear, recMonth, authUser.getId())) {
			session.setAttribute("errorMsg", "Tháng " + recMonth + "/" + recYear + " không còn mở cho bạn sửa OT.");
			response.sendRedirect(request.getContextPath() + "/overtime-list?year=" + recYear + "&month=" + recMonth);
			return;
		}

		request.setAttribute("record", record);
		request.setAttribute("recYear", recYear);
		request.setAttribute("recMonth", recMonth);
		request.getRequestDispatcher("/views/overtime/overtime-edit.jsp").forward(request, response);
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
		if (!hasPermission(session, "OT_UPDATE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		BigDecimal hours = parseBigDecimal(request.getParameter("requestedHours"));
		String reason = request.getParameter("reason");

		OvertimeRecord record = id != null ? overtimeDAO.getById(id) : null;
		if (record == null || !"APPROVED".equals(record.getStatus())) {
			session.setAttribute("errorMsg", "Không tìm thấy yêu cầu OT hoặc đã bị hủy.");
			response.sendRedirect(request.getContextPath() + "/overtime-list");
			return;
		}

		int recYear = record.getDate().toLocalDate().getYear();
		int recMonth = record.getDate().toLocalDate().getMonthValue();
		String redirectList = request.getContextPath() + "/overtime-list?year=" + recYear + "&month=" + recMonth;
		String redirectEdit = request.getContextPath() + "/overtime-edit?id=" + id;

		User targetUser = userDAO.getById(record.getUserId());
		if (targetUser == null || targetUser.getManagerId() == null
				|| !targetUser.getManagerId().equals(authUser.getId())) {
			session.setAttribute("errorMsg", "Bạn chỉ có thể sửa OT của nhân viên dưới quyền mình.");
			response.sendRedirect(redirectList);
			return;
		}

		if (!monthlySheetDAO.isEditablePeriodForSupervisor(recYear, recMonth, authUser.getId())) {
			session.setAttribute("errorMsg", "Tháng " + recMonth + "/" + recYear + " không còn mở cho bạn sửa OT.");
			response.sendRedirect(redirectList);
			return;
		}

		// ── Toàn bộ rule nghiệp vụ còn lại (giờ/ngày, trùng, nghỉ phép, chấm
		// công, trần tháng/năm) — dùng chung với Import Excel & Tạo OT hàng loạt.
		// excludeId = id: bỏ qua chính bản ghi đang sửa khi tính trùng/tổng giờ.
		OvertimeValidator.Outcome outcome = overtimeValidator.validate(targetUser, authUser.getId(),
				record.getDate().toLocalDate(), hours, reason, id);
		if (outcome.type != OvertimeValidator.OutcomeType.OK) {
			session.setAttribute("errorMsg", outcome.message);
			response.sendRedirect(redirectEdit);
			return;
		}

		boolean success = overtimeDAO.update(id, hours, reason.trim());
		if (success) {
			session.setAttribute("successMsg", "Cập nhật OT thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật OT. Vui lòng thử lại.");
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

	private BigDecimal parseBigDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
