package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class InsuranceRate {

	private Long id;
	private BigDecimal socialInsuranceEmployeeRate;
	private BigDecimal healthInsuranceEmployeeRate;
	private BigDecimal unemploymentInsuranceEmployeeRate;
	private BigDecimal socialInsuranceEmployerRate;
	private BigDecimal healthInsuranceEmployerRate;
	private BigDecimal unemploymentInsuranceEmployerRate;
	private BigDecimal socialHealthInsuranceCap;
	private BigDecimal unemploymentInsuranceCap;
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

	public BigDecimal getSocialInsuranceEmployeeRate() {
		return socialInsuranceEmployeeRate;
	}

	public void setSocialInsuranceEmployeeRate(BigDecimal socialInsuranceEmployeeRate) {
		this.socialInsuranceEmployeeRate = socialInsuranceEmployeeRate;
	}

	public BigDecimal getHealthInsuranceEmployeeRate() {
		return healthInsuranceEmployeeRate;
	}

	public void setHealthInsuranceEmployeeRate(BigDecimal healthInsuranceEmployeeRate) {
		this.healthInsuranceEmployeeRate = healthInsuranceEmployeeRate;
	}

	public BigDecimal getUnemploymentInsuranceEmployeeRate() {
		return unemploymentInsuranceEmployeeRate;
	}

	public void setUnemploymentInsuranceEmployeeRate(BigDecimal unemploymentInsuranceEmployeeRate) {
		this.unemploymentInsuranceEmployeeRate = unemploymentInsuranceEmployeeRate;
	}

	public BigDecimal getSocialInsuranceEmployerRate() {
		return socialInsuranceEmployerRate;
	}

	public void setSocialInsuranceEmployerRate(BigDecimal socialInsuranceEmployerRate) {
		this.socialInsuranceEmployerRate = socialInsuranceEmployerRate;
	}

	public BigDecimal getHealthInsuranceEmployerRate() {
		return healthInsuranceEmployerRate;
	}

	public void setHealthInsuranceEmployerRate(BigDecimal healthInsuranceEmployerRate) {
		this.healthInsuranceEmployerRate = healthInsuranceEmployerRate;
	}

	public BigDecimal getUnemploymentInsuranceEmployerRate() {
		return unemploymentInsuranceEmployerRate;
	}

	public void setUnemploymentInsuranceEmployerRate(BigDecimal unemploymentInsuranceEmployerRate) {
		this.unemploymentInsuranceEmployerRate = unemploymentInsuranceEmployerRate;
	}

	public BigDecimal getSocialHealthInsuranceCap() {
		return socialHealthInsuranceCap;
	}

	public void setSocialHealthInsuranceCap(BigDecimal socialHealthInsuranceCap) {
		this.socialHealthInsuranceCap = socialHealthInsuranceCap;
	}

	public BigDecimal getUnemploymentInsuranceCap() {
		return unemploymentInsuranceCap;
	}

	public void setUnemploymentInsuranceCap(BigDecimal unemploymentInsuranceCap) {
		this.unemploymentInsuranceCap = unemploymentInsuranceCap;
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
