package dto;

import java.math.BigDecimal;

public class PayrollAllowanceDetail {

	private Long userId;
	private Long allowanceTypeId;
	private String allowanceCode;
	private String allowanceName;
	private BigDecimal amount;
	private Boolean taxable;
	private Boolean insuranceBased;

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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Boolean getTaxable() {
		return taxable;
	}

	public void setTaxable(Boolean taxable) {
		this.taxable = taxable;
	}

	public Boolean getInsuranceBased() {
		return insuranceBased;
	}

	public void setInsuranceBased(Boolean insuranceBased) {
		this.insuranceBased = insuranceBased;
	}
}
