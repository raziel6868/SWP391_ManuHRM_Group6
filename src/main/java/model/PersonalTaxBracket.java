package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class PersonalTaxBracket {

	private Long id;
	private Integer bracketOrder;
	private BigDecimal incomeFrom;
	private BigDecimal incomeTo;
	private BigDecimal taxRate;
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

	public Integer getBracketOrder() {
		return bracketOrder;
	}

	public void setBracketOrder(Integer bracketOrder) {
		this.bracketOrder = bracketOrder;
	}

	public BigDecimal getIncomeFrom() {
		return incomeFrom;
	}

	public void setIncomeFrom(BigDecimal incomeFrom) {
		this.incomeFrom = incomeFrom;
	}

	public BigDecimal getIncomeTo() {
		return incomeTo;
	}

	public void setIncomeTo(BigDecimal incomeTo) {
		this.incomeTo = incomeTo;
	}

	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
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
