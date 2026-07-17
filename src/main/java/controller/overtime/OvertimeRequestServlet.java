package controller.overtime;

import dal.AttendanceDAO;
import dal.LeaveRequestDAO;
import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Permission;
import model.User;
import util.OvertimeImportUtil;
import util.OvertimeImportUtil.OvertimeImportException;
import util.OvertimeImportUtil.OvertimeImportResult;
import util.OvertimeValidator;

/**
 * Xử lý 2 cách quản đốc tạo OT, cùng chung 1 URL/1 permission (OT_REQUEST):
 *
 * 1) Import Excel — quản đốc bấm "Tạo yêu cầu OT" trên grid, chọn file, form ẩn
 * tự submit POST kèm file (multipart) + year/month đang xem trên grid. 2) Tạo
 * OT hàng loạt — quản đốc mở modal ngay trên overtime-list, chọn nhiều ngày
 * (lịch) + tick nhiều nhân viên + 1 mức giờ/lý do chung, submit POST dạng form
 * thường (không phải file) kèm param "mode=bulk".
 *
 * Phân biệt 2 luồng dựa trên param "mode": có "bulk" thì chạy nhánh tạo hàng
 * loạt, ngược lại giữ nguyên hành vi import Excel như cũ. @MultipartConfig
 * không ảnh hưởng gì tới request dạng form thường (không phải multipart) nên
 * gộp 2 luồng vào cùng 1 servlet là an toàn.
 */
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "OvertimeRequestServlet", urlPatterns = {"/overtime-request"})
public class OvertimeRequestServlet extends HttpServlet {

	private static final int MAX_DATES_PER_BATCH = 62;

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final OvertimeImportUtil importUtil = new OvertimeImportUtil();
	private final UserDAO userDAO = new UserDAO();
	private final OvertimeDAO overtimeDAO = new OvertimeDAO();
	private final OvertimeValidator overtimeValidator = new OvertimeValidator(overtimeDAO, monthlySheetDAO,
			new AttendanceDAO(), new LeaveRequestDAO());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Không có trang GET riêng - cả 2 cách tạo OT đều là modal trên overtime-list.
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
		if (!hasPermission(session, "OT_REQUEST")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		LocalDate today = LocalDate.now();
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());

		if (year < 2000 || year > 2100 || month < 1 || month > 12) {
			session.setAttribute("errorMsg", "Tháng/năm không hợp lệ.");
			redirectBack(request, response, year, month);
			return;
		}

		if ("bulk".equals(request.getParameter("mode"))) {
			handleBulkCreate(request, response, session, authUser, year, month);
			return;
		}

		handleExcelImport(request, response, session, authUser, year, month);
	}

	/** Luồng cũ: import Excel, giữ nguyên hành vi như trước. */
	private void handleExcelImport(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			User authUser, int year, int month) throws IOException, ServletException {

		String sheetStatus = monthlySheetDAO.getStatusByYearMonth(year, month);
		if (!monthlySheetDAO.isEditablePeriodForSupervisor(year, month, authUser.getId())) {
			session.setAttribute("errorMsg", "Tháng " + month + "/" + year + " đang ở trạng thái " + sheetStatus
					+ ", không mở cho bạn import OT.");
			redirectBack(request, response, year, month);
			return;
		}

		Part filePart = request.getPart("excelFile");
		if (filePart == null || filePart.getSize() == 0) {
			session.setAttribute("errorMsg", "Vui lòng chọn file Excel để import.");
			redirectBack(request, response, year, month);
			return;
		}
		String submittedFileName = filePart.getSubmittedFileName();
		if (submittedFileName == null || !submittedFileName.toLowerCase().endsWith(".xlsx")) {
			session.setAttribute("errorMsg", "Chỉ hỗ trợ file Excel định dạng .xlsx.");
			redirectBack(request, response, year, month);
			return;
		}

		try (InputStream inputStream = filePart.getInputStream()) {
			OvertimeImportResult result = importUtil.importExcel(inputStream, year, month, authUser.getId());
			setImportFlash(session, result.getSuccessCount(), result.getDuplicateCount(), result.getErrorCount(),
					result.getErrorMessages(), result.getDuplicateMessages());
		} catch (OvertimeImportException e) {
			session.setAttribute("errorMsg", e.getMessage());
		}

		redirectBack(request, response, year, month);
	}

	/**
	 * Luồng mới: tạo OT hàng loạt từ modal (nhiều ngày x nhiều nhân viên, chung
	 * giờ/lý do).
	 */
	private void handleBulkCreate(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			User authUser, int year, int month) throws IOException {

		List<LocalDate> dates = parseDates(request.getParameter("dates"));
		BigDecimal hours = parseBigDecimal(request.getParameter("hours"));
		String reason = request.getParameter("reason");
		String[] rawUserIds = request.getParameterValues("userIds");

		if (dates.isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng chọn ít nhất 1 ngày.");
			redirectBack(request, response, year, month);
			return;
		}
		if (dates.size() > MAX_DATES_PER_BATCH) {
			session.setAttribute("errorMsg",
					"Chỉ được chọn tối đa " + MAX_DATES_PER_BATCH + " ngày trong 1 lần tạo OT hàng loạt.");
			redirectBack(request, response, year, month);
			return;
		}
		if (rawUserIds == null || rawUserIds.length == 0) {
			session.setAttribute("errorMsg", "Vui lòng chọn ít nhất 1 nhân viên.");
			redirectBack(request, response, year, month);
			return;
		}
		if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
			session.setAttribute("errorMsg", "Số giờ OT không hợp lệ (phải lớn hơn 0).");
			redirectBack(request, response, year, month);
			return;
		}
		if (reason == null || reason.isBlank()) {
			session.setAttribute("errorMsg", "Vui lòng nhập lý do tăng ca.");
			redirectBack(request, response, year, month);
			return;
		}

		List<Long> userIds = new ArrayList<>();
		for (String raw : rawUserIds) {
			Long uid = parseLong(raw);
			if (uid != null) {
				userIds.add(uid);
			}
		}

		int successCount = 0;
		List<String> errorMessages = new ArrayList<>();
		List<String> duplicateMessages = new ArrayList<>();

		for (LocalDate date : dates) {
			for (Long uid : userIds) {
				User targetUser = userDAO.getById(uid);
				OvertimeValidator.Outcome outcome = overtimeValidator.validate(targetUser, authUser.getId(), date,
						hours, reason, null);

				if (outcome.type == OvertimeValidator.OutcomeType.DUPLICATE) {
					duplicateMessages.add(outcome.message);
					continue;
				}
				if (outcome.type == OvertimeValidator.OutcomeType.ERROR) {
					errorMessages.add(outcome.message);
					continue;
				}

				boolean inserted = overtimeDAO.insertAutoApproved(uid, Date.valueOf(date), hours, reason.trim(),
						authUser.getId());
				if (inserted) {
					successCount++;
				} else {
					errorMessages.add("Không thể lưu OT cho nhân viên " + targetUser.getEmployeeCode() + " ngày " + date
							+ ". Vui lòng thử lại.");
				}
			}
		}

		setImportFlash(session, successCount, duplicateMessages.size(), errorMessages.size(), errorMessages,
				duplicateMessages);

		// Chuyển về đúng tháng của ngày sớm nhất vừa tạo, để thấy ngay kết quả trên
		// grid.
		LocalDate earliestDate = dates.get(0);
		redirectBack(request, response, earliestDate.getYear(), earliestDate.getMonthValue());
	}

	/**
	 * Parse chuỗi "yyyy-MM-dd,yyyy-MM-dd,..." từ hidden input do JS lịch trong
	 * modal sinh ra.
	 */
	private List<LocalDate> parseDates(String csv) {
		List<LocalDate> result = new ArrayList<>();
		if (csv == null || csv.isBlank()) {
			return result;
		}
		Set<LocalDate> seen = new LinkedHashSet<>();
		for (String part : csv.split(",")) {
			String trimmed = part.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			try {
				seen.add(LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE));
			} catch (DateTimeParseException ignore) {
				// bỏ qua giá trị ngày không hợp lệ (không nên xảy ra nếu JS lịch đúng)
			}
		}
		result.addAll(seen);
		result.sort(LocalDate::compareTo);
		return result;
	}

	/**
	 * Set session flash dùng chung tên attribute cho cả 2 luồng ->
	 * overtime-list.jsp tự hiển thị banner.
	 */
	private void setImportFlash(HttpSession session, int successCount, int duplicateCount, int errorCount,
			List<String> errorMessages, List<String> duplicateMessages) {
		session.setAttribute("importSuccessCount", successCount);
		session.setAttribute("importDuplicateCount", duplicateCount);
		session.setAttribute("importErrorCount", errorCount);
		session.setAttribute("importErrorMessages", errorMessages);
		session.setAttribute("importDuplicateMessages", duplicateMessages);
	}

	private void redirectBack(HttpServletRequest request, HttpServletResponse response, int year, int month)
			throws IOException {
		response.sendRedirect(
				request.getContextPath() + "/overtime-list?year=" + year + "&month=" + month + "&imported=true");
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

	private int parseInt(String value, int defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
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
