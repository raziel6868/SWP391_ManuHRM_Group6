package dto;

import java.math.BigDecimal;

public class AttendanceSummaryRow {

	private Long departmentId;
	private String departmentName;
	private int year;
	private int month;
	private int totalDays;
	private int totalEmployees;
	private int totalWorkDays;
	private int expectedWorkDays;
	private int actualWorkDays;
	private int absentDays;
	private int lateCount;
	private BigDecimal attendanceRate = BigDecimal.ZERO;

	public AttendanceSummaryRow() {
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

	public int getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(int totalDays) {
		this.totalDays = totalDays;
	}

	public int getTotalEmployees() {
		return totalEmployees;
	}

	public void setTotalEmployees(int totalEmployees) {
		this.totalEmployees = totalEmployees;
	}

	public int getTotalWorkDays() {
		return totalWorkDays;
	}

	public void setTotalWorkDays(int totalWorkDays) {
		this.totalWorkDays = totalWorkDays;
	}

	public int getExpectedWorkDays() {
		return expectedWorkDays;
	}

	public void setExpectedWorkDays(int expectedWorkDays) {
		this.expectedWorkDays = expectedWorkDays;
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

	public int getLateCount() {
		return lateCount;
	}

	public void setLateCount(int lateCount) {
		this.lateCount = lateCount;
	}

	public BigDecimal getAttendanceRate() {
		return attendanceRate;
	}

	public void setAttendanceRate(BigDecimal attendanceRate) {
		this.attendanceRate = attendanceRate != null ? attendanceRate : BigDecimal.ZERO;
	}

	public BigDecimal getAttendanceRateBarWidth() {
		if (attendanceRate == null || attendanceRate.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		if (attendanceRate.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}
		return attendanceRate;
	}
}
