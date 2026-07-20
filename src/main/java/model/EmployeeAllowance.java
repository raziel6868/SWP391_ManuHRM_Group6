package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class EmployeeAllowance {

	private Long id;
	private Long userId;
	private Long allowanceTypeId;
	private BigDecimal amount;
	private Date effectiveFrom;
	private Date effectiveTo;
	private Boolean isActive;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private String employeeCode;
	private String employeeName;
	private String departmentName;
	private String allowanceCode;
	private String allowanceName;
	private Boolean allowanceTypeActive;
	private Boolean isTaxable;
	private Boolean isInsuranceBased;

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

	public Long getAllowanceTypeId() {
		return allowanceTypeId;
	}

	public void setAllowanceTypeId(Long allowanceTypeId) {
		this.allowanceTypeId = allowanceTypeId;
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

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
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

	public Boolean getAllowanceTypeActive() {
		return allowanceTypeActive;
	}

	public void setAllowanceTypeActive(Boolean allowanceTypeActive) {
		this.allowanceTypeActive = allowanceTypeActive;
	}

	public Boolean getIsTaxable() {
		return isTaxable;
	}

	public void setIsTaxable(Boolean isTaxable) {
		this.isTaxable = isTaxable;
	}

	public Boolean getIsInsuranceBased() {
		return isInsuranceBased;
	}

	public void setIsInsuranceBased(Boolean isInsuranceBased) {
		this.isInsuranceBased = isInsuranceBased;
	}
}
