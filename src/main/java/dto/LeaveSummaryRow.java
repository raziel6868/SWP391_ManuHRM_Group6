package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LeaveSummaryRow {

	private Long departmentId;
	private String departmentName;
	private int year;
	private int totalRequests;
	private int approvedRequests;
	private int rejectedRequests;
	private int pendingRequests;
	private int cancelledRequests;
	private BigDecimal totalDays = BigDecimal.ZERO;
	private BigDecimal averageApprovedDays = BigDecimal.ZERO;
	private BigDecimal approvedDaysBarWidth = BigDecimal.ZERO;

	public LeaveSummaryRow() {
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

	public int getTotalRequests() {
		return totalRequests;
	}

	public void setTotalRequests(int totalRequests) {
		this.totalRequests = totalRequests;
	}

	public int getApprovedRequests() {
		return approvedRequests;
	}

	public void setApprovedRequests(int approvedRequests) {
		this.approvedRequests = approvedRequests;
	}

	public int getRejectedRequests() {
		return rejectedRequests;
	}

	public void setRejectedRequests(int rejectedRequests) {
		this.rejectedRequests = rejectedRequests;
	}

	public int getPendingRequests() {
		return pendingRequests;
	}

	public void setPendingRequests(int pendingRequests) {
		this.pendingRequests = pendingRequests;
	}

	public int getCancelledRequests() {
		return cancelledRequests;
	}

	public void setCancelledRequests(int cancelledRequests) {
		this.cancelledRequests = cancelledRequests;
	}

	public BigDecimal getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(BigDecimal totalDays) {
		this.totalDays = totalDays != null ? totalDays : BigDecimal.ZERO;
	}

	public BigDecimal getApprovedDays() {
		return totalDays;
	}

	public void setApprovedDays(BigDecimal approvedDays) {
		setTotalDays(approvedDays);
	}

	public BigDecimal getAverageApprovedDays() {
		return averageApprovedDays;
	}

	public void setAverageApprovedDays(BigDecimal averageApprovedDays) {
		this.averageApprovedDays = averageApprovedDays != null ? averageApprovedDays : BigDecimal.ZERO;
	}

	public BigDecimal getApprovedPercentage() {
		return calculatePercentage(approvedRequests, totalRequests);
	}

	public BigDecimal getPendingPercentage() {
		return calculatePercentage(pendingRequests, totalRequests);
	}

	public BigDecimal getRejectedPercentage() {
		return calculatePercentage(rejectedRequests, totalRequests);
	}

	public BigDecimal getCancelledPercentage() {
		return calculatePercentage(cancelledRequests, totalRequests);
	}

	public BigDecimal getApprovedDaysBarWidth() {
		return approvedDaysBarWidth;
	}

	public void setApprovedDaysBarWidth(BigDecimal approvedDaysBarWidth) {
		this.approvedDaysBarWidth = approvedDaysBarWidth != null ? approvedDaysBarWidth : BigDecimal.ZERO;
	}

	private BigDecimal calculatePercentage(int value, int total) {
		if (value <= 0 || total <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2,
				RoundingMode.HALF_UP);
	}
}
