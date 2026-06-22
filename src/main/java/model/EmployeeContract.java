package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class EmployeeContract {

	private Long id;
	private Long userId;
	private Long contractTypeId;
	private String contractTypeName;
	private String userFullName;
	private String employeeCode;
	private Date startDate;
	private Date endDate;
	private BigDecimal salary;
	private String filePath;
	private String status;
	private Date terminatedAt;
	private Long terminatedBy;
	private String terminatedByName;
	private String terminationReason;
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

	public String getContractTypeName() {
		return contractTypeName;
	}

	public void setContractTypeName(String contractTypeName) {
		this.contractTypeName = contractTypeName;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
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

	public String getTerminatedByName() {
		return terminatedByName;
	}

	public void setTerminatedByName(String terminatedByName) {
		this.terminatedByName = terminatedByName;
	}

	public String getTerminationReason() {
		return terminationReason;
	}

	public void setTerminationReason(String terminationReason) {
		this.terminationReason = terminationReason;
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

	public boolean isExpired() {
		if (endDate == null) {
			return false;
		}
		return endDate.before(new java.util.Date());
	}

	public boolean isPendingRenewal() {
		if (endDate == null || status == null) {
			return false;
		}
		if (!"ACTIVE".equals(status)) {
			return false;
		}
		java.util.Date now = new java.util.Date();
		long thirtyDays = 30L * 24 * 60 * 60 * 1000;
		return endDate.getTime() - now.getTime() <= thirtyDays && endDate.after(now);
	}
}
