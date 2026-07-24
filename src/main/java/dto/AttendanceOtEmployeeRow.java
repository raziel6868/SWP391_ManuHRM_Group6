package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AttendanceOtEmployeeRow {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private Long userId;
	private String employeeCode;
	private String fullName;
	private String departmentName;
	private int expectedWorkDays;
	private int actualWorkDays;
	private BigDecimal approvedLeaveDays = BigDecimal.ZERO;
	private BigDecimal unauthorizedAbsenceDays = BigDecimal.ZERO;
	private BigDecimal totalOtHours = BigDecimal.ZERO;
	private int lateCount;
	private BigDecimal attendanceRate = BigDecimal.ZERO;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

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

	public int getExpectedWorkDays() {
		return expectedWorkDays;
	}

	public void setExpectedWorkDays(int expectedWorkDays) {
		this.expectedWorkDays = Math.max(0, expectedWorkDays);
		recalculateAttendanceRate();
	}

	public int getActualWorkDays() {
		return actualWorkDays;
	}

	public void setActualWorkDays(int actualWorkDays) {
		this.actualWorkDays = Math.max(0, actualWorkDays);
		recalculateAttendanceRate();
	}

	public BigDecimal getApprovedLeaveDays() {
		return approvedLeaveDays;
	}

	public void setApprovedLeaveDays(BigDecimal approvedLeaveDays) {
		this.approvedLeaveDays = safe(approvedLeaveDays);
	}

	public BigDecimal getUnauthorizedAbsenceDays() {
		return unauthorizedAbsenceDays;
	}

	public void setUnauthorizedAbsenceDays(BigDecimal unauthorizedAbsenceDays) {
		this.unauthorizedAbsenceDays = safe(unauthorizedAbsenceDays);
	}

	public BigDecimal getTotalOtHours() {
		return totalOtHours;
	}

	public void setTotalOtHours(BigDecimal totalOtHours) {
		this.totalOtHours = safe(totalOtHours);
	}

	public int getLateCount() {
		return lateCount;
	}

	public void setLateCount(int lateCount) {
		this.lateCount = Math.max(0, lateCount);
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

	public boolean isDiligent() {
		return expectedWorkDays > 0 && actualWorkDays >= expectedWorkDays && lateCount == 0
				&& unauthorizedAbsenceDays.compareTo(BigDecimal.ZERO) == 0;
	}

	public boolean isWarning() {
		return lateCount > 0 || unauthorizedAbsenceDays.compareTo(BigDecimal.ZERO) > 0;
	}

	private void recalculateAttendanceRate() {
		if (expectedWorkDays <= 0 || actualWorkDays <= 0) {
			attendanceRate = BigDecimal.ZERO;
			return;
		}
		attendanceRate = BigDecimal.valueOf(actualWorkDays).multiply(ONE_HUNDRED)
				.divide(BigDecimal.valueOf(expectedWorkDays), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}
}
