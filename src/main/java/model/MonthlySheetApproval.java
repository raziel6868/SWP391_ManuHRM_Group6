package model;

import java.sql.Timestamp;

public class MonthlySheetApproval {

	private Long id;
	private Long monthlySheetId;
	private Long supervisorId;
	private String supervisorName;
	private String supervisorEmployeeCode;
	private String departmentName;
	private String status; // PENDING / APPROVED
	private Timestamp approvedAt;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getMonthlySheetId() {
		return monthlySheetId;
	}
	public void setMonthlySheetId(Long monthlySheetId) {
		this.monthlySheetId = monthlySheetId;
	}

	public Long getSupervisorId() {
		return supervisorId;
	}
	public void setSupervisorId(Long supervisorId) {
		this.supervisorId = supervisorId;
	}

	public String getSupervisorName() {
		return supervisorName;
	}
	public void setSupervisorName(String supervisorName) {
		this.supervisorName = supervisorName;
	}

	public String getSupervisorEmployeeCode() {
		return supervisorEmployeeCode;
	}
	public void setSupervisorEmployeeCode(String supervisorEmployeeCode) {
		this.supervisorEmployeeCode = supervisorEmployeeCode;
	}

	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
}