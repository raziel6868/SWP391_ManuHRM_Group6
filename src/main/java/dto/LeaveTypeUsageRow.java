package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LeaveTypeUsageRow {

	private Long leaveTypeId;
	private String leaveTypeCode;
	private String leaveTypeName;
	private BigDecimal totalDays = BigDecimal.ZERO;
	private BigDecimal percentage = BigDecimal.ZERO;

	public Long getLeaveTypeId() {
		return leaveTypeId;
	}

	public void setLeaveTypeId(Long leaveTypeId) {
		this.leaveTypeId = leaveTypeId;
	}

	public String getLeaveTypeCode() {
		return leaveTypeCode;
	}

	public void setLeaveTypeCode(String leaveTypeCode) {
		this.leaveTypeCode = leaveTypeCode;
	}

	public String getLeaveTypeName() {
		return leaveTypeName;
	}

	public void setLeaveTypeName(String leaveTypeName) {
		this.leaveTypeName = leaveTypeName;
	}

	public BigDecimal getTotalDays() {
		return totalDays;
	}

	public void addTotalDays(BigDecimal totalDays) {
		this.totalDays = this.totalDays.add(safe(totalDays));
	}

	public BigDecimal getPercentage() {
		return percentage;
	}

	public void calculatePercentage(BigDecimal grandTotalDays) {
		if (grandTotalDays == null || grandTotalDays.compareTo(BigDecimal.ZERO) <= 0) {
			percentage = BigDecimal.ZERO;
			return;
		}
		percentage = totalDays.multiply(BigDecimal.valueOf(100)).divide(grandTotalDays, 2, RoundingMode.HALF_UP);
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}
}
