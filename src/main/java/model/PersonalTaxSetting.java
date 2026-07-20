package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class PersonalTaxSetting {

	private Long id;
	private BigDecimal personalDeduction;
	private BigDecimal dependentDeduction;
	private Date effectiveFrom;
	private Date effectiveTo;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getPersonalDeduction() {
		return personalDeduction;
	}

	public void setPersonalDeduction(BigDecimal personalDeduction) {
		this.personalDeduction = personalDeduction;
	}

	public BigDecimal getDependentDeduction() {
		return dependentDeduction;
	}

	public void setDependentDeduction(BigDecimal dependentDeduction) {
		this.dependentDeduction = dependentDeduction;
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
