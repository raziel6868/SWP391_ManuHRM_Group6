package dto;

import java.math.BigDecimal;

public class PayrollPreviewRow {

	private Long userId;
	private String userFullName;
	private String employeeCode;
	private String departmentName;
	private BigDecimal baseSalary;
	private int standardWorkDays;
	private int actualWorkDays;
	private int absentDays;
	private BigDecimal otHours;
	private BigDecimal grossSalary;
	private BigDecimal attendanceDeduction;
	private BigDecimal otBonus;
	private BigDecimal deductions;
	private BigDecimal netSalary;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public BigDecimal getBaseSalary() {
		return baseSalary;
	}

	public void setBaseSalary(BigDecimal baseSalary) {
		this.baseSalary = baseSalary;
	}

	public int getStandardWorkDays() {
		return standardWorkDays;
	}

	public void setStandardWorkDays(int standardWorkDays) {
		this.standardWorkDays = standardWorkDays;
	}

	public int getActualWorkDays() {
		return actualWorkDays;
	}

	public void setActualWorkDays(int actualWorkDays) {
		this.actualWorkDays = actualWorkDays;
	}

	public int getAbsentDays() {
		return absentDays;
	}

	public void setAbsentDays(int absentDays) {
		this.absentDays = absentDays;
	}

	public BigDecimal getOtHours() {
		return otHours;
	}

	public void setOtHours(BigDecimal otHours) {
		this.otHours = otHours;
	}

	public BigDecimal getGrossSalary() {
		return grossSalary;
	}

	public void setGrossSalary(BigDecimal grossSalary) {
		this.grossSalary = grossSalary;
	}

	public BigDecimal getAttendanceDeduction() {
		return attendanceDeduction;
	}

	public void setAttendanceDeduction(BigDecimal attendanceDeduction) {
		this.attendanceDeduction = attendanceDeduction;
	}

	public BigDecimal getOtBonus() {
		return otBonus;
	}

	public void setOtBonus(BigDecimal otBonus) {
		this.otBonus = otBonus;
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
