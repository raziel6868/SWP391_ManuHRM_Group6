package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AttendanceOtReportStats {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private int totalEmployees;
	private int expectedWorkDaysPerEmployee;
	private int totalExpectedWorkDays;
	private int totalActualWorkDays;
	private BigDecimal totalApprovedLeaveDays = BigDecimal.ZERO;
	private BigDecimal totalUnauthorizedAbsenceDays = BigDecimal.ZERO;
	private BigDecimal totalOtHours = BigDecimal.ZERO;
	private int totalLateCount;
	private BigDecimal attendanceRate = BigDecimal.ZERO;

	public void addRow(AttendanceOtEmployeeRow row) {
		if (row == null) {
			return;
		}
		totalEmployees++;
		totalExpectedWorkDays += row.getExpectedWorkDays();
		totalActualWorkDays += row.getActualWorkDays();
		totalApprovedLeaveDays = totalApprovedLeaveDays.add(safe(row.getApprovedLeaveDays()));
		totalUnauthorizedAbsenceDays = totalUnauthorizedAbsenceDays.add(safe(row.getUnauthorizedAbsenceDays()));
		totalOtHours = totalOtHours.add(safe(row.getTotalOtHours()));
		totalLateCount += row.getLateCount();
		recalculateAttendanceRate();
	}

	public int getTotalEmployees() {
		return totalEmployees;
	}

	public int getExpectedWorkDaysPerEmployee() {
		return expectedWorkDaysPerEmployee;
	}

	public void setExpectedWorkDaysPerEmployee(int expectedWorkDaysPerEmployee) {
		this.expectedWorkDaysPerEmployee = Math.max(0, expectedWorkDaysPerEmployee);
	}

	public int getTotalExpectedWorkDays() {
		return totalExpectedWorkDays;
	}

	public int getTotalActualWorkDays() {
		return totalActualWorkDays;
	}

	public BigDecimal getTotalApprovedLeaveDays() {
		return totalApprovedLeaveDays;
	}

	public BigDecimal getTotalUnauthorizedAbsenceDays() {
		return totalUnauthorizedAbsenceDays;
	}

	public BigDecimal getTotalOtHours() {
		return totalOtHours;
	}

	public int getTotalLateCount() {
		return totalLateCount;
	}

	public BigDecimal getAttendanceRate() {
		return attendanceRate;
	}

	public BigDecimal getAttendanceRateBarWidth() {
		if (attendanceRate.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		if (attendanceRate.compareTo(ONE_HUNDRED) > 0) {
			return ONE_HUNDRED;
		}
		return attendanceRate;
	}

	private void recalculateAttendanceRate() {
		if (totalExpectedWorkDays <= 0 || totalActualWorkDays <= 0) {
			attendanceRate = BigDecimal.ZERO;
			return;
		}
		attendanceRate = BigDecimal.valueOf(totalActualWorkDays).multiply(ONE_HUNDRED)
				.divide(BigDecimal.valueOf(totalExpectedWorkDays), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}
}
