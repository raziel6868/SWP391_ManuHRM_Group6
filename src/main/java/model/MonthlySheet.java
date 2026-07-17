package model;

import java.sql.Timestamp;

public class MonthlySheet {

	private Long id;
	private Integer year;
	private Integer month;
	private String status; // OPEN / PENDING_SUPERVISOR / PENDING_HR / CLOSED

	// HR gửi duyệt
	private Long submittedBy;
	private String submittedByName;
	private Timestamp submittedAt;

	// HR chốt
	private Long hrApprovedBy;
	private String hrApprovedByName;
	private Timestamp hrApprovedAt;

	// Người đóng sổ
	private Long closedBy;
	private String closedByName;
	private Timestamp closedAt;

	private Timestamp createdAt;

	// Thống kê tổng số quản đốc / đã chốt (join từ monthly_sheet_approvals)
	private int totalSupervisors;
	private int approvedSupervisors;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}
	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public Long getSubmittedBy() {
		return submittedBy;
	}
	public void setSubmittedBy(Long submittedBy) {
		this.submittedBy = submittedBy;
	}

	public String getSubmittedByName() {
		return submittedByName;
	}
	public void setSubmittedByName(String submittedByName) {
		this.submittedByName = submittedByName;
	}

	public Timestamp getSubmittedAt() {
		return submittedAt;
	}
	public void setSubmittedAt(Timestamp submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Long getHrApprovedBy() {
		return hrApprovedBy;
	}
	public void setHrApprovedBy(Long hrApprovedBy) {
		this.hrApprovedBy = hrApprovedBy;
	}

	public String getHrApprovedByName() {
		return hrApprovedByName;
	}
	public void setHrApprovedByName(String hrApprovedByName) {
		this.hrApprovedByName = hrApprovedByName;
	}

	public Timestamp getHrApprovedAt() {
		return hrApprovedAt;
	}
	public void setHrApprovedAt(Timestamp hrApprovedAt) {
		this.hrApprovedAt = hrApprovedAt;
	}

	public Long getClosedBy() {
		return closedBy;
	}
	public void setClosedBy(Long closedBy) {
		this.closedBy = closedBy;
	}

	public String getClosedByName() {
		return closedByName;
	}
	public void setClosedByName(String closedByName) {
		this.closedByName = closedByName;
	}

	public Timestamp getClosedAt() {
		return closedAt;
	}
	public void setClosedAt(Timestamp closedAt) {
		this.closedAt = closedAt;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public int getTotalSupervisors() {
		return totalSupervisors;
	}
	public void setTotalSupervisors(int totalSupervisors) {
		this.totalSupervisors = totalSupervisors;
	}

	public int getApprovedSupervisors() {
		return approvedSupervisors;
	}
	public void setApprovedSupervisors(int approvedSupervisors) {
		this.approvedSupervisors = approvedSupervisors;
	}
}
