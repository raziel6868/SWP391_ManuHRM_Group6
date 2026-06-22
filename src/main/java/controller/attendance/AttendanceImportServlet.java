package controller.attendance;

import java.io.IOException;
import java.util.List;
import dal.AttendanceDAO;
import dal.DepartmentDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.AttendanceRecord;
import model.Department;
import model.Permission;
import util.AttendanceImportUtil;

@WebServlet(name = "AttendanceImportServlet", urlPatterns = {"/attendance-import"})
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class AttendanceImportServlet extends HttpServlet {

	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "ATTENDANCE_IMPORT")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		List<Department> departments = departmentDAO.getActiveDepartments();
		request.setAttribute("departments", departments);

		String lastBatchId = attendanceDAO.getActiveBatchId();
		request.setAttribute("lastBatchId", lastBatchId);

		request.getRequestDispatcher("/views/attendance/attendance-import.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (!hasPermission(session, "ATTENDANCE_IMPORT")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");

		if (yearParam == null || yearParam.trim().isEmpty() || monthParam == null || monthParam.trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng chọn năm và tháng.");
			response.sendRedirect(request.getContextPath() + "/attendance-import");
			return;
		}

		int year, month;
		try {
			year = Integer.parseInt(yearParam.trim());
			month = Integer.parseInt(monthParam.trim());
			if (month < 1 || month > 12) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Năm hoặc tháng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/attendance-import");
			return;
		}

		if (monthlySheetDAO.isPeriodClosed(year, month)) {
			session.setAttribute("errorMsg",
					"Không thể nhập công cho tháng đã đóng. Vui lòng mở lại bảng lương trước.");
			response.sendRedirect(request.getContextPath() + "/attendance-import");
			return;
		}

		Part filePart = null;
		try {
			filePart = request.getPart("file");
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Lỗi khi đọc file upload.");
			response.sendRedirect(request.getContextPath() + "/attendance-import");
			return;
		}

		if (filePart == null || filePart.getSubmittedFileName() == null
				|| filePart.getSubmittedFileName().trim().isEmpty()) {
			session.setAttribute("errorMsg", "Vui lòng chọn file Excel.");
			response.sendRedirect(request.getContextPath() + "/attendance-import");
			return;
		}

		String fileName = filePart.getSubmittedFileName().toLowerCase();
		if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
			session.setAttribute("errorMsg", "Chỉ chấp nhận file Excel (.xlsx, .xls).");
			response.sendRedirect(request.getContextPath() + "/attendance-import");
			return;
		}

		String batchId = "BATCH-" + year + "-" + String.format("%02d", month) + "-" + System.currentTimeMillis();

		try {
			AttendanceImportUtil importUtil = new AttendanceImportUtil();
			List<AttendanceRecord> records = importUtil.parseExcel(filePart.getInputStream(), batchId);

			if (records.isEmpty()) {
				session.setAttribute("errorMsg",
						"Không tìm thấy dữ liệu hợp lệ trong file. Kiểm tra định dạng cột (employee_code, date, check_in, check_out).");
				response.sendRedirect(request.getContextPath() + "/attendance-import");
				return;
			}

			int count = attendanceDAO.batchUpsertByMonth(year, month, records);
			session.setAttribute("successMsg", "Nhập công thành công. Đã nhập " + count + " bản ghi.");

		} catch (Exception e) {
			System.err.println("Error importing attendance: " + e.getMessage());
			session.setAttribute("errorMsg", "Lỗi khi xử lý file: " + e.getMessage());
		}

		response.sendRedirect(request.getContextPath() + "/attendance-list?year=" + year + "&month=" + month);
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

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
