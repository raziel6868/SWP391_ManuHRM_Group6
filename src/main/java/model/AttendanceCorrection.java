package model;

import java.sql.Time;
import java.sql.Timestamp;

public class AttendanceCorrection {

	private Long id;
	private Long attendanceRecordId;
	private Long requestedBy;
	private Time newCheckIn;
	private Time newCheckOut;
	private String reason;
	private String status;
	private Long approverId;
	private Timestamp approvedAt;
	private Timestamp createdAt;
	private String requestedByName;
	private String approverName;
	private String attendanceUserName;
	private String attendanceDate;

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

	public String getRequestedByName() {
		return requestedByName;
	}

	public void setRequestedByName(String requestedByName) {
		this.requestedByName = requestedByName;
	}

	public String getApproverName() {
		return approverName;
	}

	public void setApproverName(String approverName) {
		this.approverName = approverName;
	}

	public String getAttendanceUserName() {
		return attendanceUserName;
	}

	public void setAttendanceUserName(String attendanceUserName) {
		this.attendanceUserName = attendanceUserName;
	}

	public String getAttendanceDate() {
		return attendanceDate;
	}

	public void setAttendanceDate(String attendanceDate) {
		this.attendanceDate = attendanceDate;
	}
}
