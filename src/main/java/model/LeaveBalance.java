package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class LeaveBalance {

	private Long id;
	private Long userId;
	private Long leaveTypeId;
	private Integer year;
	private BigDecimal totalDays;
	private BigDecimal usedDays;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private String userFullName;
	private String employeeCode;
	private String leaveTypeName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getLeaveTypeId() {
		return leaveTypeId;
	}

	public void setLeaveTypeId(Long leaveTypeId) {
		this.leaveTypeId = leaveTypeId;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public BigDecimal getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(BigDecimal totalDays) {
		this.totalDays = totalDays;
	}

	public BigDecimal getUsedDays() {
		return usedDays;
	}

	public void setUsedDays(BigDecimal usedDays) {
		this.usedDays = usedDays;
	}

	public BigDecimal getRemainingDays() {
		if (totalDays == null) {
			return BigDecimal.ZERO;
		}
		if (usedDays == null) {
			return totalDays;
		}
		return totalDays.subtract(usedDays);
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getLeaveTypeName() {
		return leaveTypeName;
	}

	public void setLeaveTypeName(String leaveTypeName) {
		this.leaveTypeName = leaveTypeName;
	}
}
