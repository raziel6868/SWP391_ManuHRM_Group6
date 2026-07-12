package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class LeaveType {

	private Long id;
	private String code;
	private String name;
	private String description;
	private Boolean isPaid;
	private String salaryPaidBy;
	private Boolean isAnnualLeave;
	private Boolean requiresBalance;
	private BigDecimal baseDays;
	private BigDecimal maxDays;
	private Boolean hasSeniorityBonus;
	private Integer seniorityIntervalYears;
	private BigDecimal seniorityBonusDays;
	private String dayCountMethod;
	private Boolean isActive;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}

	public String getSalaryPaidBy() {
		return salaryPaidBy;
	}

	public void setSalaryPaidBy(String salaryPaidBy) {
		this.salaryPaidBy = salaryPaidBy;
	}

	public Boolean getIsAnnualLeave() {
		return isAnnualLeave;
	}

	public void setIsAnnualLeave(Boolean isAnnualLeave) {
		this.isAnnualLeave = isAnnualLeave;
	}

	public Boolean getRequiresBalance() {
		return requiresBalance;
	}

	public void setRequiresBalance(Boolean requiresBalance) {
		this.requiresBalance = requiresBalance;
	}

	public BigDecimal getBaseDays() {
		return baseDays;
	}

	public void setBaseDays(BigDecimal baseDays) {
		this.baseDays = baseDays;
	}

	public BigDecimal getMaxDays() {
		return maxDays;
	}

	public void setMaxDays(BigDecimal maxDays) {
		this.maxDays = maxDays;
	}

	public Boolean getHasSeniorityBonus() {
		return hasSeniorityBonus;
	}

	public void setHasSeniorityBonus(Boolean hasSeniorityBonus) {
		this.hasSeniorityBonus = hasSeniorityBonus;
	}

	public Integer getSeniorityIntervalYears() {
		return seniorityIntervalYears;
	}

	public void setSeniorityIntervalYears(Integer seniorityIntervalYears) {
		this.seniorityIntervalYears = seniorityIntervalYears;
	}

	public BigDecimal getSeniorityBonusDays() {
		return seniorityBonusDays;
	}

	public void setSeniorityBonusDays(BigDecimal seniorityBonusDays) {
		this.seniorityBonusDays = seniorityBonusDays;
	}

	public String getDayCountMethod() {
		return dayCountMethod;
	}

	public void setDayCountMethod(String dayCountMethod) {
		this.dayCountMethod = dayCountMethod;
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
}
