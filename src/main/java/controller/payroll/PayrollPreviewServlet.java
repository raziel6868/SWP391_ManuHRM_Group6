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
import java.math.RoundingMode;
import java.util.ArrayList;
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
		boolean invalidPeriod = false;

		if (yearParam != null && !yearParam.isEmpty()) {
			try {
				year = Integer.parseInt(yearParam);
			} catch (NumberFormatException e) {
				invalidPeriod = true;
			}
		}
		if (monthParam != null && !monthParam.isEmpty()) {
			try {
				month = Integer.parseInt(monthParam);
			} catch (NumberFormatException e) {
				invalidPeriod = true;
			}
		}
		if (month < 1 || month > 12 || year < 1900 || year > 9999) {
			invalidPeriod = true;
		}
		if (invalidPeriod) {
			session.setAttribute("errorMsg", "Thông tin tháng/năm không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/payroll-preview");
			return;
		}

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

		List<String> configErrors = hasFinalOrPaidPayroll
				? List.of()
				: payrollDAO.validateRequiredConfiguration(year, month);
		List<PayrollPreviewRow> previewRows;
		if (hasGeneratedRows) {
			previewRows = toPreviewRows(generatedSalaries);
			if (!configErrors.isEmpty()) {
				request.setAttribute("errorMsg",
						String.join(" ", configErrors) + " Đang hiển thị dữ liệu bảng lương đã lưu.");
			}
		} else if (configErrors.isEmpty()) {
			previewRows = payrollDAO.buildPayrollPreview(year, month);
		} else {
			previewRows = List.of();
			request.setAttribute("errorMsg", String.join(" ", configErrors));
		}

		boolean isMonthlySheetClosed = sheet != null && "CLOSED".equals(sheet.getStatus());
		boolean canGeneratePayroll = configErrors.isEmpty() && !previewRows.isEmpty() && sheet != null
				&& isMonthlySheetClosed && !hasFinalOrPaidPayroll;
		boolean canClosePayroll = hasPermission(permissions, "PAYROLL_CLOSE") && isMonthlySheetClosed
				&& hasGeneratedRows && hasDraftPayroll;

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

	private List<PayrollPreviewRow> toPreviewRows(List<MonthlySalary> salaries) {
		List<PayrollPreviewRow> rows = new ArrayList<>();
		if (salaries == null) {
			return rows;
		}
		for (MonthlySalary salary : salaries) {
			PayrollPreviewRow row = new PayrollPreviewRow();
			row.setUserId(salary.getUserId());
			row.setUserFullName(salary.getUserFullName());
			row.setEmployeeCode(salary.getEmployeeCode());
			row.setDepartmentName(salary.getDepartmentName());
			row.setBaseSalary(salary.getBaseSalary());
			row.setStandardWorkDays(salary.getStandardWorkDays());
			row.setStandardWorkHoursPerDay(salary.getStandardWorkHoursPerDay());
			row.setActualWorkDays(salary.getActualWorkDays());
			row.setAbsentDays(calculateAbsentDays(salary));
			row.setPaidLeaveDays(salary.getPaidLeaveDays());
			row.setProratedBaseSalary(salary.getProratedBaseSalary());
			row.setPaidLeaveSalary(salary.getPaidLeaveSalary());
			row.setApprovedOtHours(firstNonNull(salary.getApprovedOtHours(), salary.getOtHours()));
			row.setOvertimePay(salary.getOvertimePay());
			row.setTotalAllowances(salary.getTotalAllowances());
			row.setGrossIncome(firstNonNull(salary.getGrossIncome(), salary.getGrossSalary()));
			row.setInsuranceSalary(salary.getInsuranceSalary());
			row.setInsuranceBasedAllowances(salary.getInsuranceBasedAllowances());
			row.setSocialInsuranceBase(salary.getSocialInsuranceBase());
			row.setHealthInsuranceBase(salary.getHealthInsuranceBase());
			row.setUnemploymentInsuranceBase(salary.getUnemploymentInsuranceBase());
			row.setSocialInsurance(salary.getSocialInsurance());
			row.setHealthInsurance(salary.getHealthInsurance());
			row.setUnemploymentInsurance(salary.getUnemploymentInsurance());
			row.setEmployeeInsurance(salary.getEmployeeInsurance());
			row.setPersonalDeduction(salary.getPersonalDeduction());
			row.setDependentCount(salary.getDependentCount());
			row.setDependentDeduction(salary.getDependentDeduction());
			row.setNonTaxableAllowances(salary.getNonTaxableAllowances());
			row.setTaxableIncome(salary.getTaxableIncome());
			row.setPitTax(salary.getPitTax());
			row.setOtHours(firstNonNull(salary.getOtHours(), salary.getApprovedOtHours()));
			row.setGrossSalary(firstNonNull(salary.getGrossSalary(), salary.getGrossIncome()));
			row.setAttendanceDeduction(BigDecimal.ZERO);
			row.setOtBonus(salary.getOvertimePay());
			row.setDeductions(salary.getDeductions());
			row.setNetSalary(salary.getNetSalary());
			rows.add(row);
		}
		return rows;
	}

	private int calculateAbsentDays(MonthlySalary salary) {
		BigDecimal absentDays = safeNumber(salary.getStandardWorkDays())
				.subtract(safeNumber(salary.getActualWorkDays())).subtract(safeNumber(salary.getPaidLeaveDays()));
		if (absentDays.compareTo(BigDecimal.ZERO) < 0) {
			return 0;
		}
		return absentDays.setScale(0, RoundingMode.HALF_UP).intValue();
	}

	private BigDecimal firstNonNull(BigDecimal first, BigDecimal second) {
		return first != null ? first : second;
	}

	private BigDecimal safeNumber(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
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
