package dto;

import java.math.BigDecimal;

public class PayrollSummaryRow {

	private Long departmentId;
	private String departmentName;
	private int year;
	private int month;
	private int employeeCount;
	private BigDecimal totalSalary;
	private BigDecimal averageSalary;
	private BigDecimal totalOtCost;
	private BigDecimal totalCost;
	private BigDecimal totalAllowances;
	private BigDecimal grossIncome;
	private BigDecimal employeeInsurance;
	private BigDecimal pitTax;
	private BigDecimal deductions;
	private BigDecimal netSalary;

	public PayrollSummaryRow() {
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getEmployeeCount() {
		return employeeCount;
	}

	public void setEmployeeCount(int employeeCount) {
		this.employeeCount = employeeCount;
	}

	public BigDecimal getTotalSalary() {
		return totalSalary;
	}

	public void setTotalSalary(BigDecimal totalSalary) {
		this.totalSalary = totalSalary;
	}

	public BigDecimal getAverageSalary() {
		return averageSalary;
	}

	public void setAverageSalary(BigDecimal averageSalary) {
		this.averageSalary = averageSalary;
	}

	public BigDecimal getTotalOtCost() {
		return totalOtCost;
	}

	public void setTotalOtCost(BigDecimal totalOtCost) {
		this.totalOtCost = totalOtCost;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	public BigDecimal getTotalAllowances() {
		return totalAllowances;
	}

	public void setTotalAllowances(BigDecimal totalAllowances) {
		this.totalAllowances = totalAllowances;
	}

	public BigDecimal getGrossIncome() {
		return grossIncome;
	}

	public void setGrossIncome(BigDecimal grossIncome) {
		this.grossIncome = grossIncome;
	}

	public BigDecimal getEmployeeInsurance() {
		return employeeInsurance;
	}

	public void setEmployeeInsurance(BigDecimal employeeInsurance) {
		this.employeeInsurance = employeeInsurance;
	}

	public BigDecimal getPitTax() {
		return pitTax;
	}

	public void setPitTax(BigDecimal pitTax) {
		this.pitTax = pitTax;
	}

	public BigDecimal getDeductions() {
		return deductions;
	}

	public void setDeductions(BigDecimal deductions) {
		this.deductions = deductions;
	}

	public BigDecimal getNetSalary() {
		return netSalary;
	}

	public void setNetSalary(BigDecimal netSalary) {
		this.netSalary = netSalary;
	}
}
