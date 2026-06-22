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
}
