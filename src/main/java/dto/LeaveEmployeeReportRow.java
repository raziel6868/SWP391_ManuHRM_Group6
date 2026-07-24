package dto;

import java.math.BigDecimal;

public class LeaveEmployeeReportRow {

	private String employeeCode;
	private String fullName;
	private String departmentName;
	private BigDecimal annualLeaveTotalDays = BigDecimal.ZERO;
	private BigDecimal annualLeaveUsedDays = BigDecimal.ZERO;
	private BigDecimal annualLeaveRemainingDays = BigDecimal.ZERO;
	private BigDecimal paidLeaveDays = BigDecimal.ZERO;
	private BigDecimal unpaidLeaveDays = BigDecimal.ZERO;
	private boolean hasAnnualLeaveBalance;

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

	public BigDecimal getAnnualLeaveTotalDays() {
		return annualLeaveTotalDays;
	}

	public void setAnnualLeaveTotalDays(BigDecimal annualLeaveTotalDays) {
		this.annualLeaveTotalDays = safe(annualLeaveTotalDays);
		recalculateAnnualLeaveRemainingDays();
	}

	public BigDecimal getAnnualLeaveUsedDays() {
		return annualLeaveUsedDays;
	}

	public void setAnnualLeaveUsedDays(BigDecimal annualLeaveUsedDays) {
		this.annualLeaveUsedDays = safe(annualLeaveUsedDays);
		recalculateAnnualLeaveRemainingDays();
	}

	public BigDecimal getAnnualLeaveRemainingDays() {
		return annualLeaveRemainingDays;
	}

	public boolean isHasAnnualLeaveBalance() {
		return hasAnnualLeaveBalance;
	}

	public void setHasAnnualLeaveBalance(boolean hasAnnualLeaveBalance) {
		this.hasAnnualLeaveBalance = hasAnnualLeaveBalance;
	}

	public BigDecimal getPaidLeaveDays() {
		return paidLeaveDays;
	}

	public void addPaidLeaveDays(BigDecimal paidLeaveDays) {
		this.paidLeaveDays = this.paidLeaveDays.add(safe(paidLeaveDays));
	}

	public BigDecimal getUnpaidLeaveDays() {
		return unpaidLeaveDays;
	}

	public void addUnpaidLeaveDays(BigDecimal unpaidLeaveDays) {
		this.unpaidLeaveDays = this.unpaidLeaveDays.add(safe(unpaidLeaveDays));
	}

	private void recalculateAnnualLeaveRemainingDays() {
		annualLeaveRemainingDays = annualLeaveTotalDays.subtract(annualLeaveUsedDays);
		if (annualLeaveRemainingDays.compareTo(BigDecimal.ZERO) < 0) {
			annualLeaveRemainingDays = BigDecimal.ZERO;
		}
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}
}
