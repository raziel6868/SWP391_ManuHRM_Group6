package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AllowanceType {

	private Long id;
	private String code;
	private String name;
	private String description;
	private Boolean isTaxable;
	private Boolean isInsuranceBased;
	private Boolean isActive;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private List<AllowanceRule> activeRules = new ArrayList<>();

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

	public List<AllowanceRule> getActiveRules() {
		return activeRules;
	}

	public void setActiveRules(List<AllowanceRule> activeRules) {
		this.activeRules = activeRules != null ? activeRules : new ArrayList<>();
	}
}
