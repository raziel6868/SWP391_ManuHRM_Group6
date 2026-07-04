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
import java.math.BigDecimal;
import java.util.List;
import model.OvertimeRecord;
import model.Permission;
import model.User;

/**
 * Trang sửa 1 bản ghi OT đã tạo (đổi giờ / lý do). Chỉ quản đốc quản lý trực
 * tiếp nhân viên đó mới sửa được, và chỉ khi tháng chưa chốt công. Validate lại
 * đầy đủ rule giống lúc tạo (trừ chính bản ghi đang sửa ra khỏi tổng giờ
 * tháng/năm). Không còn phụ thuộc phân ca — rule "không quá 20:00" giờ là hằng
 * số MAX_HOURS_PER_DAY = 3 (ca hành chính chung 7:00-17:00).
 */
@WebServlet(name = "OvertimeEditServlet", urlPatterns = {"/overtime-edit"})
public class OvertimeEditServlet extends HttpServlet {

	// Từ khi bỏ phân ca: toàn bộ nhân viên làm ca hành chính T2-T6, 7:00-17:00.
	// OT tối đa trong ngày để đảm bảo nghỉ trước 20:00 = 20:00 - 17:00 = 3 giờ.
	private static final BigDecimal MAX_HOURS_PER_DAY = new BigDecimal("3");
	private static final BigDecimal MAX_HOURS_PER_MONTH = new BigDecimal("40");
	private static final BigDecimal MAX_HOURS_PER_YEAR = new BigDecimal("200");

	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final UserDAO userDAO = new UserDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

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
		if (monthlySheetDAO.isPeriodClosed(recYear, recMonth)) {
			session.setAttribute("errorMsg", "Tháng " + recMonth + "/" + recYear + " đã chốt công, không thể sửa OT.");
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

		User targetUser = userDAO.getById(record.getUserId());
		if (targetUser == null || targetUser.getManagerId() == null
				|| !targetUser.getManagerId().equals(authUser.getId())) {
			session.setAttribute("errorMsg", "Bạn chỉ có thể sửa OT của nhân viên dưới quyền mình.");
			response.sendRedirect(redirectList);
			return;
		}

		if (monthlySheetDAO.isPeriodClosed(recYear, recMonth)) {
			session.setAttribute("errorMsg", "Tháng " + recMonth + "/" + recYear + " đã chốt công, không thể sửa OT.");
			response.sendRedirect(redirectList);
			return;
		}

		if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
			session.setAttribute("errorMsg", "Số giờ OT không hợp lệ (phải lớn hơn 0).");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}
		if (reason == null || reason.isBlank()) {
			session.setAttribute("errorMsg", "Vui lòng nhập lý do tăng ca.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		if (hours.compareTo(MAX_HOURS_PER_DAY) > 0) {
			session.setAttribute("errorMsg", "Số giờ OT (" + hours + "h) vượt quá tối đa " + MAX_HOURS_PER_DAY
					+ "h/ngày (ca hành chính 7:00-17:00, OT không được quá 20:00).");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		BigDecimal monthTotal = overtimeDAO.sumHoursInMonth(record.getUserId(), recYear, recMonth, id);
		if (monthTotal.add(hours).compareTo(MAX_HOURS_PER_MONTH) > 0) {
			session.setAttribute("errorMsg", "Nhân viên đã có " + monthTotal + "h OT khác trong tháng " + recMonth + "/"
					+ recYear + ", cộng thêm " + hours + "h sẽ vượt trần 40h/tháng.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
			return;
		}

		BigDecimal yearTotal = overtimeDAO.sumHoursInYear(record.getUserId(), recYear, id);
		if (yearTotal.add(hours).compareTo(MAX_HOURS_PER_YEAR) > 0) {
			session.setAttribute("errorMsg", "Nhân viên đã có " + yearTotal + "h OT khác trong năm " + recYear
					+ ", cộng thêm " + hours + "h sẽ vượt trần 200h/năm.");
			response.sendRedirect(request.getContextPath() + "/overtime-edit?id=" + id);
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
