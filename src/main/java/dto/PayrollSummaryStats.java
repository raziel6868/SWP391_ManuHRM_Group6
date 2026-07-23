package dto;

import java.math.BigDecimal;

public class PayrollSummaryStats {

	private int employeeCount;
	private BigDecimal totalGrossIncome = BigDecimal.ZERO;
	private BigDecimal totalNetSalary = BigDecimal.ZERO;
	private BigDecimal totalAllowances = BigDecimal.ZERO;
	private BigDecimal totalAttendanceBonus = BigDecimal.ZERO;
	private BigDecimal totalOvertimePay = BigDecimal.ZERO;
	private BigDecimal totalDeductions = BigDecimal.ZERO;
	private BigDecimal totalEmployeeInsurance = BigDecimal.ZERO;
	private BigDecimal totalPitTax = BigDecimal.ZERO;

	public int getEmployeeCount() {
		return employeeCount;
	}

	public void setEmployeeCount(int employeeCount) {
		this.employeeCount = employeeCount;
	}

	public BigDecimal getTotalGrossIncome() {
		return totalGrossIncome;
	}

	public void setTotalGrossIncome(BigDecimal totalGrossIncome) {
		this.totalGrossIncome = totalGrossIncome;
	}

	public BigDecimal getTotalNetSalary() {
		return totalNetSalary;
	}

	public void setTotalNetSalary(BigDecimal totalNetSalary) {
		this.totalNetSalary = totalNetSalary;
	}

	public BigDecimal getTotalAllowances() {
		return totalAllowances;
	}

	public void setTotalAllowances(BigDecimal totalAllowances) {
		this.totalAllowances = totalAllowances;
	}

	public BigDecimal getTotalAttendanceBonus() {
		return totalAttendanceBonus;
	}

	public void setTotalAttendanceBonus(BigDecimal totalAttendanceBonus) {
		this.totalAttendanceBonus = totalAttendanceBonus;
	}

	public BigDecimal getTotalOvertimePay() {
		return totalOvertimePay;
	}

	public void setTotalOvertimePay(BigDecimal totalOvertimePay) {
		this.totalOvertimePay = totalOvertimePay;
	}

	public BigDecimal getTotalDeductions() {
		return totalDeductions;
	}

	public void setTotalDeductions(BigDecimal totalDeductions) {
		this.totalDeductions = totalDeductions;
	}

	public BigDecimal getTotalEmployeeInsurance() {
		return totalEmployeeInsurance;
	}

	public void setTotalEmployeeInsurance(BigDecimal totalEmployeeInsurance) {
		this.totalEmployeeInsurance = totalEmployeeInsurance;
	}

	public BigDecimal getTotalPitTax() {
		return totalPitTax;
	}

	public void setTotalPitTax(BigDecimal totalPitTax) {
		this.totalPitTax = totalPitTax;
	}
}
