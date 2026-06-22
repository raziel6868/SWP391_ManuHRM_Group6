package dto;

import java.math.BigDecimal;

public class OvertimeSummaryRow {

	private Long departmentId;
	private String departmentName;
	private int year;
	private int month;
	private int totalRequests;
	private int approvedRequests;
	private int rejectedRequests;
	private int pendingRequests;
	private BigDecimal totalOtHours;
	private BigDecimal totalOtCost;

	public OvertimeSummaryRow() {
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

	public BigDecimal getTotalOtHours() {
		return totalOtHours;
	}

	public void setTotalOtHours(BigDecimal totalOtHours) {
		this.totalOtHours = totalOtHours;
	}

	public BigDecimal getTotalOtCost() {
		return totalOtCost;
	}

	public void setTotalOtCost(BigDecimal totalOtCost) {
		this.totalOtCost = totalOtCost;
	}
}
