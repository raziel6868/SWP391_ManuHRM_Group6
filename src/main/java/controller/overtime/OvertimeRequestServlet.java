package controller.overtime;

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
import java.util.List;
import model.Permission;
import model.User;
import util.OvertimeImportUtil;
import util.OvertimeImportUtil.OvertimeImportException;
import util.OvertimeImportUtil.OvertimeImportResult;

/**
 * Không còn là trang form tạo OT từng người nữa. Giờ đây quản đốc bấm "Tạo yêu
 * cầu OT" trên grid -> mở thẳng file picker -> chọn file -> form ẩn tự submit
 * POST thẳng vào đây, kèm year/month đang xem trên grid. Không có trang GET
 * riêng.
 */
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "OvertimeRequestServlet", urlPatterns = {"/overtime-request"})
public class OvertimeRequestServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final OvertimeImportUtil importUtil = new OvertimeImportUtil();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Không còn trang import riêng - quay lại grid.
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
			session.setAttribute("errorMsg", "Tháng/năm import không hợp lệ.");
			redirectBack(request, response, year, month);
			return;
		}
		if (monthlySheetDAO.isPeriodClosed(year, month)) {
			session.setAttribute("errorMsg", "Tháng " + month + "/" + year + " đã chốt công, không thể import OT.");
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
			session.setAttribute("importSuccessCount", result.getSuccessCount());
			session.setAttribute("importDuplicateCount", result.getDuplicateCount());
			session.setAttribute("importErrorCount", result.getErrorCount());
			session.setAttribute("importErrorMessages", result.getErrorMessages());
			session.setAttribute("importDuplicateMessages", result.getDuplicateMessages());
		} catch (OvertimeImportException e) {
			session.setAttribute("errorMsg", e.getMessage());
		}

		redirectBack(request, response, year, month);
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
}