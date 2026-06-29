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
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.AttendanceRecord;
import util.AttendanceImportUtil;
import util.AttendanceImportUtil.AttendanceImportException;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "AttendanceImportServlet", urlPatterns = {"/attendance-import"})
public class AttendanceImportServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final AttendanceImportUtil importUtil = new AttendanceImportUtil(attendanceDAO);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		LocalDate today = LocalDate.now();
		request.setAttribute("selectedYear", today.getYear());
		request.setAttribute("selectedMonth", today.getMonthValue());
		request.setAttribute("availableMonths", buildAvailableMonths(today));
		request.getRequestDispatcher("/views/attendance/attendance-import.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		LocalDate today = LocalDate.now();
		int year = parseInt(request.getParameter("year"), today.getYear());
		int month = parseInt(request.getParameter("month"), today.getMonthValue());
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("availableMonths", buildAvailableMonths(today));

		if (year < 2000 || year > 2100 || month < 1 || month > 12) {
			forwardWithError(request, response, List.of("Tháng/năm import không hợp lệ."));
			return;
		}
		LocalDate selected = LocalDate.of(year, month, 1);
		if (selected.isAfter(today.withDayOfMonth(1))) {
			forwardWithError(request, response, List.of("Không thể import chấm công cho tháng tương lai."));
			return;
		}
		if (monthlySheetDAO.isPeriodClosed(year, month)) {
			forwardWithError(request, response, List.of("Không thể nhập công cho tháng đã đóng."));
			return;
		}

		Part filePart = request.getPart("attendanceFile");
		if (filePart == null || filePart.getSize() == 0) {
			forwardWithError(request, response, List.of("Vui lòng chọn file Excel để import."));
			return;
		}
		String submittedFileName = filePart.getSubmittedFileName();
		if (submittedFileName == null || !submittedFileName.toLowerCase().endsWith(".xlsx")) {
			forwardWithError(request, response, List.of("Chỉ hỗ trợ file Excel định dạng .xlsx."));
			return;
		}

		try (InputStream inputStream = filePart.getInputStream()) {
			List<AttendanceRecord> records = importUtil.parseExcel(inputStream, year, month);
			boolean success = attendanceDAO.batchUpsertByMonth(year, month, records);
			if (success) {
				request.getSession().setAttribute("successMsg",
						"Import chấm công thành công " + records.size() + " dòng.");
				response.sendRedirect(request.getContextPath() + "/attendance-list?year=" + year + "&month=" + month);
			} else {
				forwardWithError(request, response, List.of("Không thể lưu dữ liệu chấm công. Vui lòng thử lại."));
			}
		} catch (AttendanceImportException e) {
			forwardWithError(request, response, e.getErrors());
		}
	}

	/**
	 * Tạo danh sách các tháng có thể import: từ 12 tháng trước đến tháng hiện tại,
	 * bỏ qua tháng đã CLOSED. Key = "yyyy-M", Value = label hiển thị.
	 */
	private Map<String, String> buildAvailableMonths(LocalDate today) {
		Map<String, String> result = new LinkedHashMap<>();
		YearMonth current = YearMonth.from(today);
		for (int i = 11; i >= 0; i--) {
			YearMonth ym = current.minusMonths(i);
			if (!monthlySheetDAO.isPeriodClosed(ym.getYear(), ym.getMonthValue())) {
				String key = ym.getYear() + "-" + ym.getMonthValue();
				String label = "Tháng " + ym.getMonthValue() + "/" + ym.getYear();
				result.put(key, label);
			}
		}
		return result;
	}

	private boolean isSamePeriod(List<AttendanceRecord> records, int year, int month) {
		for (AttendanceRecord record : records) {
			LocalDate date = record.getDate().toLocalDate();
			if (date.getYear() != year || date.getMonthValue() != month) {
				return false;
			}
		}
		return true;
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, List<String> errors)
			throws ServletException, IOException {
		request.setAttribute("errorLogs", errors);
		request.getRequestDispatcher("/views/attendance/attendance-import.jsp").forward(request, response);
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}