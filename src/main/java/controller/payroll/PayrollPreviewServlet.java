package controller.payroll;

import dal.MonthlySalaryDAO;
import dal.MonthlySheetDAO;
import dal.PayrollDAO;
import dto.PayrollPreviewRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.MonthlySalary;
import model.MonthlySheet;
import model.Permission;
import model.User;

@WebServlet(name = "PayrollPreviewServlet", urlPatterns = {"/payroll-preview"})
public class PayrollPreviewServlet extends HttpServlet {

	private final PayrollDAO payrollDAO = new PayrollDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final MonthlySalaryDAO monthlySalaryDAO = new MonthlySalaryDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PAYROLL_VIEW")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlash(session, request, "successMsg");
		moveFlash(session, request, "errorMsg");

		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);
		int currentMonth = now.get(Calendar.MONTH) + 1;

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");

		int year = currentYear;
		int month = currentMonth;

		if (yearParam != null && !yearParam.isEmpty()) {
			try {
				year = Integer.parseInt(yearParam);
			} catch (NumberFormatException e) {
				year = currentYear;
			}
		}
		if (monthParam != null && !monthParam.isEmpty()) {
			try {
				month = Integer.parseInt(monthParam);
			} catch (NumberFormatException e) {
				month = currentMonth;
			}
		}

		List<PayrollPreviewRow> previewRows = payrollDAO.buildPayrollPreview(year, month);
		MonthlySheet sheet = monthlySheetDAO.getByYearMonth(year, month);
		List<MonthlySalary> generatedSalaries = sheet != null ? monthlySalaryDAO.getBySheet(sheet.getId()) : null;

		Map<Long, String> generatedStatusByUserId = new HashMap<>();
		boolean hasGeneratedRows = generatedSalaries != null && !generatedSalaries.isEmpty();
		boolean hasDraftPayroll = false;
		boolean hasFinalOrPaidPayroll = false;

		if (generatedSalaries != null) {
			for (MonthlySalary salary : generatedSalaries) {
				generatedStatusByUserId.put(salary.getUserId(), salary.getStatus());
				if ("DRAFT".equals(salary.getStatus())) {
					hasDraftPayroll = true;
				}
				if ("FINAL".equals(salary.getStatus()) || "PAID".equals(salary.getStatus())) {
					hasFinalOrPaidPayroll = true;
				}
			}
		}

		boolean isMonthlySheetClosed = sheet != null && "CLOSED".equals(sheet.getStatus());
		boolean canGeneratePayroll = !previewRows.isEmpty() && sheet != null && isMonthlySheetClosed
				&& !hasFinalOrPaidPayroll;
		boolean canClosePayroll = hasPermission(permissions, "PAYROLL_CLOSE") && hasGeneratedRows && hasDraftPayroll;

		request.setAttribute("previewRows", previewRows);
		request.setAttribute("generatedSalaries", generatedSalaries);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);
		request.setAttribute("sheet", sheet);
		request.setAttribute("generatedSheetId", sheet != null ? sheet.getId() : null);
		request.setAttribute("generatedStatusByUserId", generatedStatusByUserId);
		request.setAttribute("hasGeneratedRows", hasGeneratedRows);
		request.setAttribute("hasDraftPayroll", hasDraftPayroll);
		request.setAttribute("hasFinalOrPaidPayroll", hasFinalOrPaidPayroll);
		request.setAttribute("canGeneratePayroll", canGeneratePayroll);
		request.setAttribute("canClosePayroll", canClosePayroll);
		request.setAttribute("isMonthlySheetClosed", isMonthlySheetClosed);

		request.getRequestDispatcher("/views/payroll/payroll-preview.jsp").forward(request, response);
	}

	private void moveFlash(HttpSession session, HttpServletRequest request, String key) {
		Object val = session.getAttribute(key);
		if (val != null) {
			request.setAttribute(key, val);
			session.removeAttribute(key);
		}
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (p.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}
}
