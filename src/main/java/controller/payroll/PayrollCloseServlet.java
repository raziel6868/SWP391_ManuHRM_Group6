package controller.payroll;

import dal.MonthlySalaryDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.MonthlySheet;
import model.Permission;
import model.User;

@WebServlet(name = "PayrollCloseServlet", urlPatterns = {"/payroll-close"})
public class PayrollCloseServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final MonthlySalaryDAO monthlySalaryDAO = new MonthlySalaryDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "PAYROLL_CLOSE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		MonthlySheet sheet = resolveSheet(request);
		if (sheet == null) {
			session.setAttribute("errorMsg", "Không tìm thấy kỳ lương cần chốt.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview");
			return;
		}

		if (!"CLOSED".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg", "Bảng công tháng " + sheet.getMonth() + "/" + sheet.getYear()
					+ " chưa đóng hoặc đã được mở lại, không thể chốt bảng lương.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview?year=" + sheet.getYear() + "&month="
					+ sheet.getMonth());
			return;
		}

		if (!monthlySalaryDAO.hasGeneratedRows(sheet.getId())) {
			session.setAttribute("errorMsg",
					"Chưa có dữ liệu bảng lương tháng " + sheet.getMonth() + "/" + sheet.getYear() + " để chốt.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview?year=" + sheet.getYear() + "&month="
					+ sheet.getMonth());
			return;
		}

		if (!monthlySalaryDAO.hasAnyDraft(sheet.getId())) {
			session.setAttribute("errorMsg",
					"Bảng lương tháng " + sheet.getMonth() + "/" + sheet.getYear() + " đã được chốt trước đó.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview?year=" + sheet.getYear() + "&month="
					+ sheet.getMonth());
			return;
		}

		int closedRows = monthlySalaryDAO.closePayroll(sheet.getId());
		if (closedRows > 0) {
			session.setAttribute("successMsg", "Đã chốt bảng lương tháng " + sheet.getMonth() + "/" + sheet.getYear()
					+ " cho " + closedRows + " nhân viên.");
		} else {
			session.setAttribute("errorMsg", "Không thể chốt bảng lương. Vui lòng thử lại.");
		}

		response.sendRedirect(
				request.getContextPath() + "/payroll-preview?year=" + sheet.getYear() + "&month=" + sheet.getMonth());
	}

	private MonthlySheet resolveSheet(HttpServletRequest request) {
		String sheetIdParam = request.getParameter("sheetId");
		if (sheetIdParam != null && !sheetIdParam.isBlank()) {
			try {
				return monthlySheetDAO.getById(Long.parseLong(sheetIdParam.trim()));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");
		if (yearParam == null || yearParam.isBlank() || monthParam == null || monthParam.isBlank()) {
			return null;
		}

		try {
			int year = Integer.parseInt(yearParam.trim());
			int month = Integer.parseInt(monthParam.trim());
			return monthlySheetDAO.getByYearMonth(year, month);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
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
}
