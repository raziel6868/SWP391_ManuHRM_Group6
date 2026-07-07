package controller.attendance;

import dal.AttendanceDAO;
import dal.MonthlySheetDAO;
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
import java.time.LocalDate;
import util.AttendanceImportUtil;
import util.AttendanceImportUtil.AttendanceImportException;
import util.AttendanceImportUtil.AttendanceImportResult;

/**
 * Không còn trang import riêng — HR/quản đốc bấm "Import Excel" trên
 * attendance-list -> mở modal ngay tại chỗ -> chọn file -> form submit thẳng
 * POST vào đây, kèm year/month đang xem trên lưới. Kết quả luôn redirect về lại
 * /attendance-list kèm flash session (giống hệt OvertimeRequestServlet).
 */
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "AttendanceImportServlet", urlPatterns = {"/attendance-import"})
public class AttendanceImportServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final AttendanceImportUtil importUtil = new AttendanceImportUtil(attendanceDAO);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Không còn trang import riêng - quay lại lưới chấm công.
		response.sendRedirect(request.getContextPath() + "/attendance-list");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		LocalDate today = LocalDate.now();
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());

		if (year < 2000 || year > 2100 || month < 1 || month > 12) {
			session.setAttribute("errorMsg", "Tháng/năm import không hợp lệ.");
			redirectBack(request, response, year, month);
			return;
		}
		LocalDate selected = LocalDate.of(year, month, 1);
		if (selected.isAfter(today.withDayOfMonth(1))) {
			session.setAttribute("errorMsg", "Không thể import chấm công cho tháng tương lai.");
			redirectBack(request, response, year, month);
			return;
		}

		String status = monthlySheetDAO.getStatusByYearMonth(year, month);
		if (status != null && !"OPEN".equals(status)) {
			session.setAttribute("errorMsg", "Không thể nhập công cho tháng đang ở trạng thái " + status + ".");
			redirectBack(request, response, year, month);
			return;
		}

		Part filePart = request.getPart("attendanceFile");
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
			AttendanceImportResult result = importUtil.importExcel(inputStream, year, month);
			if (result.getSuccessCount() > 0) {
				// Đảm bảo có monthly_sheets cho kỳ này (giữ nguyên hành vi cũ).
				monthlySheetDAO.getOrCreate(year, month);
			}
			session.setAttribute("importSuccessCount", result.getSuccessCount());
			session.setAttribute("importDuplicateCount", result.getDuplicateCount());
			session.setAttribute("importErrorCount", result.getErrorCount());
			session.setAttribute("importErrorMessages", result.getErrorMessages());
			session.setAttribute("importDuplicateMessages", result.getDuplicateMessages());
		} catch (AttendanceImportException e) {
			session.setAttribute("errorMsg", e.getMessage());
		}

		redirectBack(request, response, year, month);
	}

	private void redirectBack(HttpServletRequest request, HttpServletResponse response, int year, int month)
			throws IOException {
		response.sendRedirect(
				request.getContextPath() + "/attendance-list?year=" + year + "&month=" + month + "&imported=true");
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
}