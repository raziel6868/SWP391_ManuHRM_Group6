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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
		request.setCharacterEncoding("UTF-8");

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

		int year;
		int month;
		try {
			year = Integer.parseInt(yearParam);
			month = Integer.parseInt(monthParam);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Thông tin tháng/năm không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview");
			return;
		}

		String redirectUrl = payrollPreviewUrl(request, year, month);

		MonthlySheet sheet = monthlySheetDAO.getByYearMonth(year, month);
		if (sheet == null) {
			session.setAttribute("errorMsg", "Chưa có bảng công tháng " + month + "/" + year
					+ ". Vui lòng hoàn tất chốt bảng công trước khi tính lương.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (monthlySalaryDAO.hasAnyFinalOrPaid(sheet.getId())) {
			session.setAttribute("errorMsg", "Bảng lương tháng " + month + "/" + year + " đã chốt, không thể tạo lại.");
			response.sendRedirect(redirectUrl);
			return;
		}

		if (!"CLOSED".equals(sheet.getStatus())) {
			session.setAttribute("errorMsg",
					"Chỉ có thể tạo bảng lương sau khi bảng công tháng " + month + "/" + year + " đã được đóng sổ.");
			response.sendRedirect(redirectUrl);
			return;
		}

		List<String> configErrors = payrollDAO.validateRequiredConfiguration(year, month);
		if (!configErrors.isEmpty()) {
			session.setAttribute("errorMsg", String.join(" ", configErrors));
			response.sendRedirect(redirectUrl);
			return;
		}

		List<PayrollPreviewRow> previewRows = payrollDAO.buildPayrollPreview(year, month);
		if (previewRows == null || previewRows.isEmpty()) {
			session.setAttribute("errorMsg", "Không có dữ liệu bảng lương để tạo cho kỳ đã chọn.");
			response.sendRedirect(redirectUrl);
			return;
		}

		List<MonthlySalary> salaries = new ArrayList<>();
		for (PayrollPreviewRow row : previewRows) {
			MonthlySalary ms = new MonthlySalary();
			ms.setUserId(row.getUserId());
			ms.setBaseSalary(row.getBaseSalary());
			ms.setStandardWorkDays(row.getStandardWorkDays());
			ms.setStandardWorkHoursPerDay(row.getStandardWorkHoursPerDay());
			ms.setActualWorkDays(row.getActualWorkDays());
			ms.setPaidLeaveDays(row.getPaidLeaveDays());
			ms.setProratedBaseSalary(row.getProratedBaseSalary());
			ms.setPaidLeaveSalary(row.getPaidLeaveSalary());
			ms.setApprovedOtHours(row.getApprovedOtHours());
			ms.setOtHours(row.getApprovedOtHours() != null ? row.getApprovedOtHours() : BigDecimal.ZERO);
			ms.setOvertimePay(row.getOvertimePay());
			ms.setTotalAllowances(row.getTotalAllowances());
			ms.setGrossIncome(row.getGrossIncome());
			ms.setGrossSalary(row.getGrossIncome());
			ms.setInsuranceSalary(row.getInsuranceSalary());
			ms.setInsuranceBasedAllowances(row.getInsuranceBasedAllowances());
			ms.setSocialInsuranceBase(row.getSocialInsuranceBase());
			ms.setHealthInsuranceBase(row.getHealthInsuranceBase());
			ms.setUnemploymentInsuranceBase(row.getUnemploymentInsuranceBase());
			ms.setSocialInsurance(row.getSocialInsurance());
			ms.setHealthInsurance(row.getHealthInsurance());
			ms.setUnemploymentInsurance(row.getUnemploymentInsurance());
			ms.setEmployeeInsurance(row.getEmployeeInsurance());
			ms.setPersonalDeduction(row.getPersonalDeduction());
			ms.setDependentCount(row.getDependentCount());
			ms.setDependentDeduction(row.getDependentDeduction());
			ms.setNonTaxableAllowances(row.getNonTaxableAllowances());
			ms.setTaxableIncome(row.getTaxableIncome());
			ms.setPitTax(row.getPitTax());
			ms.setDeductions(row.getDeductions() != null ? row.getDeductions() : BigDecimal.ZERO);
			ms.setNetSalary(row.getNetSalary());
			ms.setStatus("DRAFT");
			salaries.add(ms);
		}

		boolean success = salaries.isEmpty() || monthlySalaryDAO.batchUpsert(sheet.getId(), salaries);
		if (success) {
			session.setAttribute("successMsg", "Bảng lương tháng " + month + "/" + year + " đã được tạo thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể tạo bảng lương. Vui lòng thử lại.");
		}
		response.sendRedirect(redirectUrl);
	}

	private String payrollPreviewUrl(HttpServletRequest request, int year, int month) {
		StringBuilder url = new StringBuilder(request.getContextPath()).append("/payroll-preview?year=").append(year)
				.append("&month=").append(month);
		String departmentId = request.getParameter("departmentId");
		if (departmentId != null && !departmentId.isBlank()) {
			url.append("&departmentId=").append(departmentId.trim());
		}
		return url.toString();
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
