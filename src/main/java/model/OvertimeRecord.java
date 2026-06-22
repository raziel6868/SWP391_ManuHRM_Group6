package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class OvertimeRecord {

	private Long id;
	private Long userId;
	private Date date;
	private BigDecimal requestedHours;
	private BigDecimal approvedHours;
	private String reason;
	private String status;
	private Long approverId;
	private Timestamp approvedAt;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	private String employeeCode;
	private String employeeName;
	private Long requesterId;
	private String approverName;

	public OvertimeRecord() {
	}

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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal getRequestedHours() {
		return requestedHours;
	}

	public void setRequestedHours(BigDecimal requestedHours) {
		this.requestedHours = requestedHours;
	}

	public BigDecimal getApprovedHours() {
		return approvedHours;
	}

	public void setApprovedHours(BigDecimal approvedHours) {
		this.approvedHours = approvedHours;
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

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public Long getRequesterId() {
		return requesterId;
	}

	public void setRequesterId(Long requesterId) {
		this.requesterId = requesterId;
	}

	public String getApproverName() {
		return approverName;
	}

	public void setApproverName(String approverName) {
		this.approverName = approverName;
	}
}