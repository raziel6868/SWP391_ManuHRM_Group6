package dto;

import java.math.BigDecimal;

public class OvertimeEmployeeReportRow {

	private String employeeCode;
	private String fullName;
	private String departmentName;
	private BigDecimal totalApprovedHours;
	private BigDecimal estimatedOtCost;

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

	public BigDecimal getTotalApprovedHours() {
		return totalApprovedHours;
	}

	public void setTotalApprovedHours(BigDecimal totalApprovedHours) {
		this.totalApprovedHours = totalApprovedHours;
	}

	public BigDecimal getEstimatedOtCost() {
		return estimatedOtCost;
	}

	public void setEstimatedOtCost(BigDecimal estimatedOtCost) {
		this.estimatedOtCost = estimatedOtCost;
	}
}
