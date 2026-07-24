package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class AllowanceRule {

	private Long id;
	private Long allowanceTypeId;
	private String applyScope;
	private String employeeType;
	private String departmentType;
	private Long departmentId;
	private Long jobTitleId;
	private BigDecimal amount;
	private Date effectiveFrom;
	private Date effectiveTo;
	private Boolean isActive;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	private String allowanceCode;
	private String allowanceName;
	private String departmentName;
	private String jobTitleName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAllowanceTypeId() {
		return allowanceTypeId;
	}

	public void setAllowanceTypeId(Long allowanceTypeId) {
		this.allowanceTypeId = allowanceTypeId;
	}

	public String getApplyScope() {
		return applyScope;
	}

	public void setApplyScope(String applyScope) {
		this.applyScope = applyScope;
	}

	public String getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(String employeeType) {
		this.employeeType = employeeType;
	}

	public String getDepartmentType() {
		return departmentType;
	}

	public void setDepartmentType(String departmentType) {
		this.departmentType = departmentType;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Long getJobTitleId() {
		return jobTitleId;
	}

	public void setJobTitleId(Long jobTitleId) {
		this.jobTitleId = jobTitleId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Date getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(Date effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public Date getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(Date effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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

	public String getAllowanceCode() {
		return allowanceCode;
	}

	public void setAllowanceCode(String allowanceCode) {
		this.allowanceCode = allowanceCode;
	}

	public String getAllowanceName() {
		return allowanceName;
	}

	public void setAllowanceName(String allowanceName) {
		this.allowanceName = allowanceName;
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
}
