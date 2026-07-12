package dal;

import dto.PayrollPreviewRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.InsuranceRate;
import model.PayrollSetting;
import model.PersonalTaxBracket;
import model.PersonalTaxSetting;
import util.LeavePolicyUtil;

public class PayrollDAO {

	private static final BigDecimal DEFAULT_WORK_DAYS = new BigDecimal("22");
	private static final BigDecimal DEFAULT_HOURS_PER_DAY = new BigDecimal("8");
	private static final BigDecimal DEFAULT_OT_RATE = new BigDecimal("1.5");
	private static final BigDecimal MONEY_ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

	private final PayrollSettingDAO payrollSettingDAO = new PayrollSettingDAO();
	private final EmployeeAllowanceDAO employeeAllowanceDAO = new EmployeeAllowanceDAO();
	private final InsuranceRateDAO insuranceRateDAO = new InsuranceRateDAO();
	private final PersonalTaxSettingDAO personalTaxSettingDAO = new PersonalTaxSettingDAO();
	private final PersonalTaxBracketDAO personalTaxBracketDAO = new PersonalTaxBracketDAO();
	private final EmployeeDependentDAO employeeDependentDAO = new EmployeeDependentDAO();

	public List<String> validateRequiredConfiguration(int year, int month) {
		List<String> errors = new ArrayList<>();
		String period = month + "/" + year;

		if (payrollSettingDAO.getConfiguredForPeriod(year, month) == null) {
			errors.add("Chưa cấu hình tham số lương cho kỳ " + period + ".");
		}
		if (insuranceRateDAO.getActiveForPeriod(year, month) == null) {
			errors.add("Chưa cấu hình mức đóng bảo hiểm cho kỳ " + period + ".");
		}
		if (personalTaxSettingDAO.getActiveForPeriod(year, month) == null) {
			errors.add("Chưa cấu hình giảm trừ gia cảnh cho kỳ " + period + ".");
		}
		if (personalTaxBracketDAO.getActiveBracketsForPeriod(year, month).isEmpty()) {
			errors.add("Chưa cấu hình biểu thuế TNCN cho kỳ " + period + ".");
		}

		return errors;
	}

	public List<PayrollPreviewRow> buildPayrollPreview(int year, int month) {
		List<String> configErrors = validateRequiredConfiguration(year, month);
		if (!configErrors.isEmpty()) {
			throw new PayrollConfigurationException(configErrors);
		}

		List<PayrollPreviewRow> rows = new ArrayList<>();
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());
		PayrollSetting setting = payrollSettingDAO.getConfiguredForPeriod(year, month);
		InsuranceRate insuranceRate = insuranceRateDAO.getActiveForPeriod(year, month);
		PersonalTaxSetting personalTaxSetting = personalTaxSettingDAO.getActiveForPeriod(year, month);
		BigDecimal personalDeductionAmount = money(personalTaxSetting.getPersonalDeduction());
		BigDecimal dependentDeductionAmount = money(personalTaxSetting.getDependentDeduction());
		List<PersonalTaxBracket> taxBrackets = personalTaxBracketDAO.getActiveBracketsForPeriod(year, month);

		String usersSql = """
				SELECT u.id, u.full_name, u.employee_code, d.name AS department_name,
				       sb.base_salary, sb.insurance_salary
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN salary_bases sb ON sb.id = (
				    SELECT sb2.id
				    FROM salary_bases sb2
				    WHERE sb2.user_id = u.id
				      AND sb2.effective_from <= ?
				      AND (sb2.effective_to IS NULL OR sb2.effective_to >= ?)
				    ORDER BY sb2.effective_from DESC, sb2.id DESC
				    LIMIT 1
				)
				WHERE u.is_active = TRUE
				ORDER BY u.full_name
				""";

		try (Connection conn = DBContext.getConnection()) {
			try (PreparedStatement ps = conn.prepareStatement(usersSql)) {
				ps.setDate(1, lastDay);
				ps.setDate(2, firstDay);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						Long userId = rs.getLong("id");
						String fullName = rs.getString("full_name");
						String empCode = rs.getString("employee_code");
						String deptName = rs.getString("department_name");
						BigDecimal baseSalary = rs.getBigDecimal("base_salary");
						BigDecimal insuranceSalaryOverride = rs.getBigDecimal("insurance_salary");

						if (baseSalary == null) {
							continue;
						}

						BigDecimal actualWorkDays = countActualWorkDays(conn, userId, year, month);
						BigDecimal paidLeaveDays = countPaidLeaveDays(conn, userId, firstDay, lastDay);
						BigDecimal approvedOtHours = sumApprovedOtHours(conn, userId, year, month);
						BigDecimal totalAllowances = employeeAllowanceDAO.sumActiveAllowances(userId, year, month);
						BigDecimal insuranceBasedAllowances = employeeAllowanceDAO.sumInsuranceBasedAllowances(userId,
								year, month);
						BigDecimal nonTaxableAllowances = employeeAllowanceDAO.sumNonTaxableAllowances(userId, year,
								month);
						int dependentCount = employeeDependentDAO.countActiveDependents(userId, year, month);

						PayrollPreviewRow row = calculateRow(userId, fullName, empCode, deptName, baseSalary,
								insuranceSalaryOverride, actualWorkDays, paidLeaveDays, approvedOtHours,
								totalAllowances, insuranceBasedAllowances, nonTaxableAllowances, dependentCount,
								personalDeductionAmount, dependentDeductionAmount, setting, insuranceRate, taxBrackets);
						rows.add(row);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error building payroll preview: " + e.getMessage());
		}

		return rows;
	}

	private BigDecimal countActualWorkDays(Connection conn, Long userId, int year, int month) throws SQLException {
		String sql = """
				SELECT COUNT(DISTINCT DATE(date)) AS work_days
				FROM attendance_records
				WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ? AND status != 'ABSENT'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return BigDecimal.valueOf(rs.getInt("work_days"));
				}
			}
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal countPaidLeaveDays(Connection conn, Long userId, Date firstDay, Date lastDay)
			throws SQLException {
		String sql = """
				SELECT lr.start_date, lr.end_date, lr.day_count_method_snapshot AS day_count_method
				FROM leave_requests lr
				WHERE lr.user_id = ?
				  AND lr.status = 'APPROVED'
				  AND lr.is_paid_snapshot = TRUE
				  AND lr.start_date <= ?
				  AND lr.end_date >= ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, lastDay);
			ps.setDate(3, firstDay);
			try (ResultSet rs = ps.executeQuery()) {
				BigDecimal totalDays = BigDecimal.ZERO;
				LocalDate periodStart = firstDay.toLocalDate();
				LocalDate periodEnd = lastDay.toLocalDate();
				while (rs.next()) {
					LocalDate requestStart = rs.getDate("start_date").toLocalDate();
					LocalDate requestEnd = rs.getDate("end_date").toLocalDate();
					LocalDate countedStart = requestStart.isBefore(periodStart) ? periodStart : requestStart;
					LocalDate countedEnd = requestEnd.isAfter(periodEnd) ? periodEnd : requestEnd;
					totalDays = totalDays.add(LeavePolicyUtil.calculateRequestDays(countedStart, countedEnd,
							rs.getString("day_count_method")));
				}
				return totalDays;
			}
		}
	}

	private BigDecimal sumApprovedOtHours(Connection conn, Long userId, int year, int month) throws SQLException {
		String sql = """
				SELECT COALESCE(SUM(ot.approved_hours), 0) AS total_ot
				FROM overtime_records ot
				WHERE ot.user_id = ?
				  AND ot.status = 'APPROVED'
				  AND ot.approved_hours IS NOT NULL
				  AND YEAR(ot.date) = ?
				  AND MONTH(ot.date) = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					BigDecimal approvedHours = rs.getBigDecimal("total_ot");
					return approvedHours != null ? approvedHours : BigDecimal.ZERO;
				}
			}
		}

		return BigDecimal.ZERO;
	}

	private PayrollPreviewRow calculateRow(Long userId, String fullName, String empCode, String deptName,
			BigDecimal baseSalary, BigDecimal insuranceSalaryOverride, BigDecimal actualWorkDays,
			BigDecimal paidLeaveDays, BigDecimal approvedOtHours, BigDecimal totalAllowances,
			BigDecimal insuranceBasedAllowances, BigDecimal nonTaxableAllowances, int dependentCount,
			BigDecimal personalDeductionAmount, BigDecimal dependentDeductionAmount, PayrollSetting setting,
			InsuranceRate insuranceRate, List<PersonalTaxBracket> taxBrackets) {

		BigDecimal standardWorkDays = positiveOrDefault(setting != null ? setting.getStandardWorkDays() : null,
				DEFAULT_WORK_DAYS);
		BigDecimal standardWorkHoursPerDay = positiveOrDefault(
				setting != null ? setting.getStandardWorkHoursPerDay() : null, DEFAULT_HOURS_PER_DAY);
		BigDecimal normalOvertimeRate = positiveOrDefault(setting != null ? setting.getNormalOvertimeRate() : null,
				DEFAULT_OT_RATE);

		actualWorkDays = safeNumber(actualWorkDays);
		paidLeaveDays = safeNumber(paidLeaveDays);
		approvedOtHours = safeNumber(approvedOtHours);
		totalAllowances = money(totalAllowances);
		insuranceBasedAllowances = money(insuranceBasedAllowances);
		nonTaxableAllowances = money(nonTaxableAllowances);
		baseSalary = money(baseSalary);
		personalDeductionAmount = money(personalDeductionAmount);
		dependentDeductionAmount = money(dependentDeductionAmount);

		BigDecimal dailyRate = baseSalary.divide(standardWorkDays, 10, RoundingMode.HALF_UP);
		BigDecimal hourlyRate = dailyRate.divide(standardWorkHoursPerDay, 10, RoundingMode.HALF_UP);

		BigDecimal proratedBaseSalary = money(actualWorkDays.multiply(dailyRate));
		BigDecimal paidLeaveSalary = money(paidLeaveDays.multiply(dailyRate));
		BigDecimal overtimePay = money(approvedOtHours.multiply(hourlyRate).multiply(normalOvertimeRate));
		BigDecimal grossIncome = money(proratedBaseSalary.add(paidLeaveSalary).add(overtimePay).add(totalAllowances));
		BigDecimal insuranceSalary = resolveInsuranceSalary(baseSalary, insuranceSalaryOverride,
				insuranceBasedAllowances);
		BigDecimal socialInsuranceBase = applyCap(insuranceSalary, insuranceRate.getSocialHealthInsuranceCap());
		BigDecimal healthInsuranceBase = applyCap(insuranceSalary, insuranceRate.getSocialHealthInsuranceCap());
		BigDecimal unemploymentInsuranceBase = applyCap(insuranceSalary, insuranceRate.getUnemploymentInsuranceCap());
		BigDecimal socialInsurance = money(
				socialInsuranceBase.multiply(safeNumber(insuranceRate.getSocialInsuranceEmployeeRate())));
		BigDecimal healthInsurance = money(
				healthInsuranceBase.multiply(safeNumber(insuranceRate.getHealthInsuranceEmployeeRate())));
		BigDecimal unemploymentInsurance = money(
				unemploymentInsuranceBase.multiply(safeNumber(insuranceRate.getUnemploymentInsuranceEmployeeRate())));
		BigDecimal employeeInsurance = money(socialInsurance.add(healthInsurance).add(unemploymentInsurance));
		BigDecimal dependentDeduction = money(
				BigDecimal.valueOf(Math.max(dependentCount, 0L)).multiply(dependentDeductionAmount));
		BigDecimal taxExemptIncome = money(nonTaxableAllowances.add(overtimePay));
		BigDecimal taxableIncome = money(grossIncome.subtract(employeeInsurance).subtract(personalDeductionAmount)
				.subtract(dependentDeduction).subtract(taxExemptIncome));
		if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
			taxableIncome = MONEY_ZERO;
		}
		BigDecimal pitTax = taxBrackets.isEmpty() ? MONEY_ZERO : calculateProgressivePit(taxableIncome, taxBrackets);
		BigDecimal deductions = money(employeeInsurance.add(pitTax));
		BigDecimal netSalary = money(grossIncome.subtract(deductions));
		int absentDays = standardWorkDays.subtract(actualWorkDays).subtract(paidLeaveDays).max(BigDecimal.ZERO)
				.setScale(0, RoundingMode.HALF_UP).intValue();

		PayrollPreviewRow row = new PayrollPreviewRow();
		row.setUserId(userId);
		row.setUserFullName(fullName);
		row.setEmployeeCode(empCode);
		row.setDepartmentName(deptName);
		row.setBaseSalary(baseSalary);
		row.setStandardWorkDays(scaleDays(standardWorkDays));
		row.setStandardWorkHoursPerDay(scaleDays(standardWorkHoursPerDay));
		row.setActualWorkDays(actualWorkDays);
		row.setAbsentDays(absentDays);
		row.setPaidLeaveDays(scaleDays(paidLeaveDays));
		row.setProratedBaseSalary(proratedBaseSalary);
		row.setPaidLeaveSalary(paidLeaveSalary);
		row.setApprovedOtHours(scaleDays(approvedOtHours));
		row.setOvertimePay(overtimePay);
		row.setTotalAllowances(totalAllowances);
		row.setGrossIncome(grossIncome);
		row.setDailyRate(money(dailyRate));
		row.setHourlyRate(hourlyRate.setScale(2, RoundingMode.HALF_UP));
		row.setInsuranceSalary(insuranceSalary);
		row.setInsuranceBasedAllowances(insuranceBasedAllowances);
		row.setSocialInsuranceBase(socialInsuranceBase);
		row.setHealthInsuranceBase(healthInsuranceBase);
		row.setUnemploymentInsuranceBase(unemploymentInsuranceBase);
		row.setSocialInsurance(socialInsurance);
		row.setHealthInsurance(healthInsurance);
		row.setUnemploymentInsurance(unemploymentInsurance);
		row.setEmployeeInsurance(employeeInsurance);
		row.setPersonalDeduction(personalDeductionAmount);
		row.setDependentCount(dependentCount);
		row.setDependentDeduction(dependentDeduction);
		row.setNonTaxableAllowances(nonTaxableAllowances);
		row.setTaxableIncome(taxableIncome);
		row.setPitTax(pitTax);
		row.setOtHours(scaleDays(approvedOtHours));
		row.setGrossSalary(grossIncome);
		row.setAttendanceDeduction(MONEY_ZERO);
		row.setOtBonus(overtimePay);
		row.setDeductions(deductions);
		row.setNetSalary(netSalary);

		return row;
	}

	private BigDecimal calculateProgressivePit(BigDecimal taxableIncome, List<PersonalTaxBracket> taxBrackets) {
		BigDecimal remainingIncome = money(taxableIncome);
		BigDecimal pitTax = MONEY_ZERO;

		for (PersonalTaxBracket bracket : taxBrackets) {
			if (remainingIncome.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}
			BigDecimal incomeFrom = safeNumber(bracket.getIncomeFrom());
			if (taxableIncome.compareTo(incomeFrom) <= 0) {
				continue;
			}

			BigDecimal taxablePart;
			if (bracket.getIncomeTo() == null) {
				taxablePart = remainingIncome;
			} else {
				BigDecimal bracketWidth = bracket.getIncomeTo().subtract(incomeFrom);
				if (bracketWidth.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				taxablePart = remainingIncome.min(bracketWidth);
			}

			if (taxablePart.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			pitTax = pitTax.add(taxablePart.multiply(safeNumber(bracket.getTaxRate())));
			remainingIncome = remainingIncome.subtract(taxablePart);
		}

		return money(pitTax);
	}

	private BigDecimal positiveOrDefault(BigDecimal value, BigDecimal fallback) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			return fallback;
		}
		return value;
	}

	private BigDecimal safeNumber(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	private BigDecimal resolveInsuranceSalary(BigDecimal baseSalary, BigDecimal insuranceSalaryOverride,
			BigDecimal insuranceBasedAllowances) {
		BigDecimal resolved = insuranceSalaryOverride != null
				? insuranceSalaryOverride
				: safeNumber(baseSalary).add(safeNumber(insuranceBasedAllowances));
		if (resolved.compareTo(BigDecimal.ZERO) < 0) {
			return MONEY_ZERO;
		}
		return money(resolved);
	}

	private BigDecimal applyCap(BigDecimal amount, BigDecimal cap) {
		BigDecimal normalizedAmount = money(amount);
		if (cap == null) {
			return normalizedAmount;
		}
		return money(normalizedAmount.min(money(cap)));
	}

	private BigDecimal money(BigDecimal value) {
		return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal scaleDays(BigDecimal value) {
		return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	public static class PayrollConfigurationException extends IllegalStateException {
		private final List<String> errors;

		public PayrollConfigurationException(List<String> errors) {
			super(String.join(" ", errors));
			this.errors = List.copyOf(errors);
		}

		public List<String> getErrors() {
			return errors;
		}
	}
}
