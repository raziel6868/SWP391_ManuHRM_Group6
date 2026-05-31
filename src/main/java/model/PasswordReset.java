package model;

import java.sql.Timestamp;

public class PasswordReset {

	public enum Status {
		PENDING, RESOLVED, REJECTED
	}

	private Long id;
	private Long userId;
	private Status status;
	private Timestamp createdAt;
	private Long resolvedBy;
	private Timestamp resolvedAt;
	private String newPassword;

	// Các trường bổ trợ (Không có trong table DB nhưng cần để hiển thị lên UI)
	private String employeeCode;
	private String fullName;

	// Constructor mặc định
	public PasswordReset() {
	}

	// Toàn bộ Getter và Setter
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Long getResolvedBy() {
		return resolvedBy;
	}

	public void setResolvedBy(Long resolvedBy) {
		this.resolvedBy = resolvedBy;
	}

	public Timestamp getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(Timestamp resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
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
}
