package dto;

import java.math.BigDecimal;

public class PayrollEmployeeReportRow {

	private String employeeCode;
	private String fullName;
	private String departmentName;
	private BigDecimal actualWorkDays;
	private BigDecimal paidLeaveDays;
	private BigDecimal approvedOtHours;
	private BigDecimal attendanceBonus;
	private BigDecimal grossIncome;
	private BigDecimal deductions;
	private BigDecimal netSalary;
	private String warningStatus;

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public BigDecimal getActualWorkDays() {
		return actualWorkDays;
	}

	public void setActualWorkDays(BigDecimal actualWorkDays) {
		this.actualWorkDays = actualWorkDays;
	}

	public BigDecimal getPaidLeaveDays() {
		return paidLeaveDays;
	}

	public void setPaidLeaveDays(BigDecimal paidLeaveDays) {
		this.paidLeaveDays = paidLeaveDays;
	}

	public BigDecimal getApprovedOtHours() {
		return approvedOtHours;
	}

	public void setApprovedOtHours(BigDecimal approvedOtHours) {
		this.approvedOtHours = approvedOtHours;
	}

	public BigDecimal getAttendanceBonus() {
		return attendanceBonus;
	}

	public void setAttendanceBonus(BigDecimal attendanceBonus) {
		this.attendanceBonus = attendanceBonus;
	}

	public BigDecimal getGrossIncome() {
		return grossIncome;
	}

	public void setGrossIncome(BigDecimal grossIncome) {
		this.grossIncome = grossIncome;
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

	public String getWarningStatus() {
		return warningStatus;
	}

	public void setWarningStatus(String warningStatus) {
		this.warningStatus = warningStatus;
	}
}
