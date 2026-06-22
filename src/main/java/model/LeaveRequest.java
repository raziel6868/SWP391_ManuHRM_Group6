package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class LeaveRequest {

	private Long id;
	private Long userId;
	private Long leaveTypeId;
	private Date startDate;
	private Date endDate;
	private BigDecimal days;
	private String reason;
	private String status;
	private Long level1ApproverId;
	private Timestamp level1ApprovedAt;
	private Long approverId;
	private Timestamp approvedAt;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private String userFullName;
	private String employeeCode;
	private String leaveTypeName;
	private String level1ApproverName;
	private String approverName;

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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public BigDecimal getDays() {
		return days;
	}

	public void setDays(BigDecimal days) {
		this.days = days;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getLevel1ApproverId() {
		return level1ApproverId;
	}

	public void setLevel1ApproverId(Long level1ApproverId) {
		this.level1ApproverId = level1ApproverId;
	}

	public Timestamp getLevel1ApprovedAt() {
		return level1ApprovedAt;
	}

	public void setLevel1ApprovedAt(Timestamp level1ApprovedAt) {
		this.level1ApprovedAt = level1ApprovedAt;
	}

	public Long getApproverId() {
		return approverId;
	}

	public void setApproverId(Long approverId) {
		this.approverId = approverId;
	}

	public Timestamp getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Timestamp approvedAt) {
		this.approvedAt = approvedAt;
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

	public String getLevel1ApproverName() {
		return level1ApproverName;
	}

	public void setLevel1ApproverName(String level1ApproverName) {
		this.level1ApproverName = level1ApproverName;
	}

	public String getApproverName() {
		return approverName;
	}

	public void setApproverName(String approverName) {
		this.approverName = approverName;
	}
}
