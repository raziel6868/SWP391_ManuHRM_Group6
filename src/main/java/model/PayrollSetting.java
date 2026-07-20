package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class PayrollSetting {

	private Long id;
	private BigDecimal standardWorkDays;
	private BigDecimal standardWorkHoursPerDay;
	private BigDecimal normalOvertimeRate;
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

	public BigDecimal getStandardWorkDays() {
		return standardWorkDays;
	}

	public void setStandardWorkDays(BigDecimal standardWorkDays) {
		this.standardWorkDays = standardWorkDays;
	}

	public BigDecimal getStandardWorkHoursPerDay() {
		return standardWorkHoursPerDay;
	}

	public void setStandardWorkHoursPerDay(BigDecimal standardWorkHoursPerDay) {
		this.standardWorkHoursPerDay = standardWorkHoursPerDay;
	}

	public BigDecimal getNormalOvertimeRate() {
		return normalOvertimeRate;
	}

	public void setNormalOvertimeRate(BigDecimal normalOvertimeRate) {
		this.normalOvertimeRate = normalOvertimeRate;
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
