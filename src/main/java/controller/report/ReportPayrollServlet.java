package controller.report;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import dal.ReportDAO;
import dto.PayrollSummaryRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;
import model.User;

@WebServlet(name = "ReportPayrollServlet", urlPatterns = {"/report-payroll"})
public class ReportPayrollServlet extends HttpServlet {

	private final ReportDAO reportDAO = new ReportDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "REPORT_PAYROLL")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

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
			}
		}
		if (monthParam != null && !monthParam.isEmpty()) {
			try {
				month = Integer.parseInt(monthParam);
			} catch (NumberFormatException e) {
			}
		}

		List<PayrollSummaryRow> rows = reportDAO.getPayrollSummary(year, month);

		request.setAttribute("rows", rows);
		request.setAttribute("selectedYear", year);
		request.setAttribute("selectedMonth", month);

		request.getRequestDispatcher("/views/report/report-payroll.jsp").forward(request, response);
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
