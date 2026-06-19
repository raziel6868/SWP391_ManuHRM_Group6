package dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * DTO for contract detail view. Carries all fields plus display-friendly names
 * for joined entities and the full history fields needed on the detail page
 * (termination, renewal lineage).
 */
public class ContractDetail {

	private Long id;
	private Long userId;
	private String employeeCode;
	private String fullName;
	private String departmentName;
	private String jobTitleName;
	private String managerName;

	private Long contractTypeId;
	private String contractTypeCode;
	private String contractTypeName;

	private Date startDate;
	private Date endDate;
	private BigDecimal salary;
	private String filePath;
	private String status;

	private Date terminatedAt;
	private Long terminatedBy;
	private String terminatedByName;
	private String terminateReason;

	private Long renewalOfId;
	private String renewalOfCode;
	private Date renewalOfStartDate;
	private Date renewalOfEndDate;

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

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getJobTitleName() {
		return jobTitleName;
	}

	public void setJobTitleName(String jobTitleName) {
		this.jobTitleName = jobTitleName;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public Long getContractTypeId() {
		return contractTypeId;
	}

	public void setContractTypeId(Long contractTypeId) {
		this.contractTypeId = contractTypeId;
	}

	public String getContractTypeCode() {
		return contractTypeCode;
	}

	public void setContractTypeCode(String contractTypeCode) {
		this.contractTypeCode = contractTypeCode;
	}

	public String getContractTypeName() {
		return contractTypeName;
	}

	public void setContractTypeName(String contractTypeName) {
		this.contractTypeName = contractTypeName;
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

	public String getRenewalOfCode() {
		return renewalOfCode;
	}

	public void setRenewalOfCode(String renewalOfCode) {
		this.renewalOfCode = renewalOfCode;
	}

	public Date getRenewalOfStartDate() {
		return renewalOfStartDate;
	}

	public void setRenewalOfStartDate(Date renewalOfStartDate) {
		this.renewalOfStartDate = renewalOfStartDate;
	}

	public Date getRenewalOfEndDate() {
		return renewalOfEndDate;
	}

	public void setRenewalOfEndDate(Date renewalOfEndDate) {
		this.renewalOfEndDate = renewalOfEndDate;
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
