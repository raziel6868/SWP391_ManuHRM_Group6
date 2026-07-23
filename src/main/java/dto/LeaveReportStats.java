package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LeaveReportStats {

	private BigDecimal totalLeaveDays = BigDecimal.ZERO;
	private BigDecimal paidLeaveDays = BigDecimal.ZERO;
	private BigDecimal unpaidLeaveDays = BigDecimal.ZERO;
	private BigDecimal averageAnnualLeaveRemainingDays = BigDecimal.ZERO;
	private int missingAnnualLeaveBalanceCount;

	public BigDecimal getTotalLeaveDays() {
		return totalLeaveDays;
	}

	public void addTotalLeaveDays(BigDecimal totalLeaveDays) {
		this.totalLeaveDays = this.totalLeaveDays.add(safe(totalLeaveDays));
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

	public BigDecimal getAverageAnnualLeaveRemainingDays() {
		return averageAnnualLeaveRemainingDays;
	}

	public void setAverageAnnualLeaveRemainingDays(BigDecimal averageAnnualLeaveRemainingDays) {
		this.averageAnnualLeaveRemainingDays = safe(averageAnnualLeaveRemainingDays).setScale(2, RoundingMode.HALF_UP);
	}

	public int getMissingAnnualLeaveBalanceCount() {
		return missingAnnualLeaveBalanceCount;
	}

	public void incrementMissingAnnualLeaveBalanceCount() {
		missingAnnualLeaveBalanceCount++;
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}
}
