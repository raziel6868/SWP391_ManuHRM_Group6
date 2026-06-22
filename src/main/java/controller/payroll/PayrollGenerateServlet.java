package controller.payroll;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
import model.MonthlySalary;
import model.MonthlySheet;
import model.Permission;
import model.User;

@WebServlet(name = "PayrollGenerateServlet", urlPatterns = {"/payroll-generate"})
public class PayrollGenerateServlet extends HttpServlet {

	private final PayrollDAO payrollDAO = new PayrollDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();
	private final MonthlySalaryDAO monthlySalaryDAO = new MonthlySalaryDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PAYROLL_GENERATE")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		String yearParam = request.getParameter("year");
		String monthParam = request.getParameter("month");

		if (yearParam == null || yearParam.isEmpty() || monthParam == null || monthParam.isEmpty()) {
			session.setAttribute("errorMsg", "Thông tin tháng/năm không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview");
			return;
		}

		int year, month;
		try {
			year = Integer.parseInt(yearParam);
			month = Integer.parseInt(monthParam);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Thông tin tháng/năm không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview");
			return;
		}

		MonthlySheet sheet = monthlySheetDAO.getByYearMonth(year, month);
		if (sheet == null) {
			sheet = monthlySheetDAO.getOrCreate(year, month);
		}

		if (sheet != null && "CLOSED".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg", "Cần mở lại bảng công trước khi tính lương.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview?year=" + year + "&month=" + month);
			return;
		}

		List<PayrollPreviewRow> previewRows = payrollDAO.buildPayrollPreview(year, month);
		List<MonthlySalary> salaries = new ArrayList<>();

		for (PayrollPreviewRow row : previewRows) {
			MonthlySalary ms = new MonthlySalary();
			ms.setUserId(row.getUserId());
			ms.setActualWorkDays(row.getActualWorkDays());
			ms.setOtHours(row.getOtHours() != null ? row.getOtHours() : BigDecimal.ZERO);
			ms.setGrossSalary(row.getGrossSalary());
			ms.setDeductions(row.getDeductions() != null ? row.getDeductions() : BigDecimal.ZERO);
			ms.setNetSalary(row.getNetSalary());
			ms.setStatus("GENERATED");
			salaries.add(ms);
		}

		if (!salaries.isEmpty()) {
			monthlySalaryDAO.batchUpsert(sheet.getId(), salaries);
		}

		session.setAttribute("successMsg", "Bảng lương tháng " + month + "/" + year + " đã được tạo thành công.");
		response.sendRedirect(request.getContextPath() + "/payroll-preview?year=" + year + "&month=" + month);
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
