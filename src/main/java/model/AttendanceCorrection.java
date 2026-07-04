package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class AttendanceCorrection {

	private Long id;
	private Long attendanceRecordId;
	private Long requestedBy;
	private Time newCheckIn;
	private Time newCheckOut;
	private String reason;

	// Bước 1: quản đốc duyệt
	private Long supervisorId;
	private String supervisorStatus; // PENDING / APPROVED / REJECTED
	private Timestamp supervisorApprovedAt;
	private String supervisorRejectReason;
	private String supervisorName;

	// Bước 2: HR duyệt
	private String status; // PENDING / APPROVED / REJECTED
	private Long approverId;
	private String hrRejectReason;
	private Timestamp approvedAt;
	private String approverName;

	private Timestamp createdAt;
	private Timestamp updatedAt;

	// Join fields
	private Long attendanceUserId;
	private String employeeCode;
	private String employeeName;
	private Date attendanceDate;
	private Time currentCheckIn;
	private Time currentCheckOut;
	private String requesterName;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getAttendanceRecordId() {
		return attendanceRecordId;
	}
	public void setAttendanceRecordId(Long attendanceRecordId) {
		this.attendanceRecordId = attendanceRecordId;
	}

	public Long getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(Long requestedBy) {
		this.requestedBy = requestedBy;
	}

	public Time getNewCheckIn() {
		return newCheckIn;
	}
	public void setNewCheckIn(Time newCheckIn) {
		this.newCheckIn = newCheckIn;
	}

	public Time getNewCheckOut() {
		return newCheckOut;
	}
	public void setNewCheckOut(Time newCheckOut) {
		this.newCheckOut = newCheckOut;
	}

	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}

	public Long getSupervisorId() {
		return supervisorId;
	}
	public void setSupervisorId(Long supervisorId) {
		this.supervisorId = supervisorId;
	}

	public String getSupervisorStatus() {
		return supervisorStatus;
	}
	public void setSupervisorStatus(String supervisorStatus) {
		this.supervisorStatus = supervisorStatus;
	}

	public Timestamp getSupervisorApprovedAt() {
		return supervisorApprovedAt;
	}
	public void setSupervisorApprovedAt(Timestamp supervisorApprovedAt) {
		this.supervisorApprovedAt = supervisorApprovedAt;
	}

	public String getSupervisorRejectReason() {
		return supervisorRejectReason;
	}
	public void setSupervisorRejectReason(String supervisorRejectReason) {
		this.supervisorRejectReason = supervisorRejectReason;
	}

	public String getSupervisorName() {
		return supervisorName;
	}
	public void setSupervisorName(String supervisorName) {
		this.supervisorName = supervisorName;
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

	public String getHrRejectReason() {
		return hrRejectReason;
	}
	public void setHrRejectReason(String hrRejectReason) {
		this.hrRejectReason = hrRejectReason;
	}

	public Timestamp getApprovedAt() {
		return approvedAt;
	}
	public void setApprovedAt(Timestamp approvedAt) {
		this.approvedAt = approvedAt;
	}

	public String getApproverName() {
		return approverName;
	}
	public void setApproverName(String approverName) {
		this.approverName = approverName;
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

	public Long getAttendanceUserId() {
		return attendanceUserId;
	}
	public void setAttendanceUserId(Long attendanceUserId) {
		this.attendanceUserId = attendanceUserId;
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

	public Date getAttendanceDate() {
		return attendanceDate;
	}
	public void setAttendanceDate(Date attendanceDate) {
		this.attendanceDate = attendanceDate;
	}

	public Time getCurrentCheckIn() {
		return currentCheckIn;
	}
	public void setCurrentCheckIn(Time currentCheckIn) {
		this.currentCheckIn = currentCheckIn;
	}

	public Time getCurrentCheckOut() {
		return currentCheckOut;
	}
	public void setCurrentCheckOut(Time currentCheckOut) {
		this.currentCheckOut = currentCheckOut;
	}

	public String getRequesterName() {
		return requesterName;
	}
	public void setRequesterName(String requesterName) {
		this.requesterName = requesterName;
	}
}