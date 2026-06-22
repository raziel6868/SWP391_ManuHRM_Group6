package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Contract {

	public enum Status {
		ACTIVE, EXPIRED, PENDING_RENEWAL, TERMINATED
	}

	private Long id;
	private Long userId;
	private Long contractTypeId;
	private Date startDate;
	private Date endDate;
	private BigDecimal salary;
	private String filePath;
	private Status status;

	private Date terminatedAt;
	private Long terminatedBy;
	private String terminateReason;

	private Long renewalOfId;

	private Timestamp createdAt;
	private Timestamp updatedAt;

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

	public Long getContractTypeId() {
		return contractTypeId;
	}

	public void setContractTypeId(Long contractTypeId) {
		this.contractTypeId = contractTypeId;
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

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getTerminatedAt() {
		return terminatedAt;
	}

	public void setTerminatedAt(Date terminatedAt) {
		this.terminatedAt = terminatedAt;
	}

	public Long getTerminatedBy() {
		return terminatedBy;
	}

	public void setTerminatedBy(Long terminatedBy) {
		this.terminatedBy = terminatedBy;
	}

	public String getTerminateReason() {
		return terminateReason;
	}

	public void setTerminateReason(String terminateReason) {
		this.terminateReason = terminateReason;
	}

	public Long getRenewalOfId() {
		return renewalOfId;
	}

	public void setRenewalOfId(Long renewalOfId) {
		this.renewalOfId = renewalOfId;
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
