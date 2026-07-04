package dto;

import java.math.BigDecimal;

public class PayrollPreviewRow {

	private Long userId;
	private String userFullName;
	private String employeeCode;
	private String departmentName;
	private BigDecimal baseSalary;
	private BigDecimal standardWorkDays;
	private BigDecimal standardWorkHoursPerDay;
	private BigDecimal actualWorkDays;
	private int absentDays;
	private BigDecimal paidLeaveDays;
	private BigDecimal proratedBaseSalary;
	private BigDecimal paidLeaveSalary;
	private BigDecimal approvedOtHours;
	private BigDecimal overtimePay;
	private BigDecimal totalAllowances;
	private BigDecimal grossIncome;
	private BigDecimal dailyRate;
	private BigDecimal hourlyRate;
	private BigDecimal insuranceSalary;
	private BigDecimal insuranceBasedAllowances;
	private BigDecimal socialInsuranceBase;
	private BigDecimal healthInsuranceBase;
	private BigDecimal unemploymentInsuranceBase;
	private BigDecimal socialInsurance;
	private BigDecimal healthInsurance;
	private BigDecimal unemploymentInsurance;
	private BigDecimal employeeInsurance;
	private BigDecimal personalDeduction;
	private Integer dependentCount;
	private BigDecimal dependentDeduction;
	private BigDecimal nonTaxableAllowances;
	private BigDecimal taxableIncome;
	private BigDecimal pitTax;
	private BigDecimal otHours;
	private BigDecimal grossSalary;
	private BigDecimal attendanceDeduction;
	private BigDecimal otBonus;
	private BigDecimal deductions;
	private BigDecimal netSalary;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public BigDecimal getBaseSalary() {
		return baseSalary;
	}

	public void setBaseSalary(BigDecimal baseSalary) {
		this.baseSalary = baseSalary;
	}

	public BigDecimal getStandardWorkDays() {
		return standardWorkDays;
	}

	public void setStandardWorkDays(BigDecimal standardWorkDays) {
		this.standardWorkDays = standardWorkDays;
	}

	public void setStandardWorkDays(int standardWorkDays) {
		this.standardWorkDays = BigDecimal.valueOf(standardWorkDays);
	}

	public BigDecimal getStandardWorkHoursPerDay() {
		return standardWorkHoursPerDay;
	}

	public void setStandardWorkHoursPerDay(BigDecimal standardWorkHoursPerDay) {
		this.standardWorkHoursPerDay = standardWorkHoursPerDay;
	}

	public BigDecimal getActualWorkDays() {
		return actualWorkDays;
	}

	public void setActualWorkDays(BigDecimal actualWorkDays) {
		this.actualWorkDays = actualWorkDays;
	}

	public void setActualWorkDays(int actualWorkDays) {
		this.actualWorkDays = BigDecimal.valueOf(actualWorkDays);
	}

	public int getAbsentDays() {
		return absentDays;
	}

	public void setAbsentDays(int absentDays) {
		this.absentDays = absentDays;
	}

	public BigDecimal getPaidLeaveDays() {
		return paidLeaveDays;
	}

	public void setPaidLeaveDays(BigDecimal paidLeaveDays) {
		this.paidLeaveDays = paidLeaveDays;
	}

	public BigDecimal getProratedBaseSalary() {
		return proratedBaseSalary;
	}

	public void setProratedBaseSalary(BigDecimal proratedBaseSalary) {
		this.proratedBaseSalary = proratedBaseSalary;
	}

	public BigDecimal getPaidLeaveSalary() {
		return paidLeaveSalary;
	}

	public void setPaidLeaveSalary(BigDecimal paidLeaveSalary) {
		this.paidLeaveSalary = paidLeaveSalary;
	}

	public BigDecimal getApprovedOtHours() {
		return approvedOtHours;
	}

	public void setApprovedOtHours(BigDecimal approvedOtHours) {
		this.approvedOtHours = approvedOtHours;
		this.otHours = approvedOtHours;
	}

	public BigDecimal getOvertimePay() {
		return overtimePay;
	}

	public void setOvertimePay(BigDecimal overtimePay) {
		this.overtimePay = overtimePay;
		this.otBonus = overtimePay;
	}

	public BigDecimal getTotalAllowances() {
		return totalAllowances;
	}

	public void setTotalAllowances(BigDecimal totalAllowances) {
		this.totalAllowances = totalAllowances;
	}

	public BigDecimal getGrossIncome() {
		return grossIncome;
	}

	public void setGrossIncome(BigDecimal grossIncome) {
		this.grossIncome = grossIncome;
		this.grossSalary = grossIncome;
	}

	public BigDecimal getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(BigDecimal dailyRate) {
		this.dailyRate = dailyRate;
	}

	public BigDecimal getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(BigDecimal hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public BigDecimal getInsuranceSalary() {
		return insuranceSalary;
	}

	public void setInsuranceSalary(BigDecimal insuranceSalary) {
		this.insuranceSalary = insuranceSalary;
	}

	public BigDecimal getInsuranceBasedAllowances() {
		return insuranceBasedAllowances;
	}

	public void setInsuranceBasedAllowances(BigDecimal insuranceBasedAllowances) {
		this.insuranceBasedAllowances = insuranceBasedAllowances;
	}

	public BigDecimal getSocialInsuranceBase() {
		return socialInsuranceBase;
	}

	public void setSocialInsuranceBase(BigDecimal socialInsuranceBase) {
		this.socialInsuranceBase = socialInsuranceBase;
	}

	public BigDecimal getHealthInsuranceBase() {
		return healthInsuranceBase;
	}

	public void setHealthInsuranceBase(BigDecimal healthInsuranceBase) {
		this.healthInsuranceBase = healthInsuranceBase;
	}

	public BigDecimal getUnemploymentInsuranceBase() {
		return unemploymentInsuranceBase;
	}

	public void setUnemploymentInsuranceBase(BigDecimal unemploymentInsuranceBase) {
		this.unemploymentInsuranceBase = unemploymentInsuranceBase;
	}

	public BigDecimal getSocialInsurance() {
		return socialInsurance;
	}

	public void setSocialInsurance(BigDecimal socialInsurance) {
		this.socialInsurance = socialInsurance;
	}

	public BigDecimal getHealthInsurance() {
		return healthInsurance;
	}

	public void setHealthInsurance(BigDecimal healthInsurance) {
		this.healthInsurance = healthInsurance;
	}

	public BigDecimal getUnemploymentInsurance() {
		return unemploymentInsurance;
	}

	public void setUnemploymentInsurance(BigDecimal unemploymentInsurance) {
		this.unemploymentInsurance = unemploymentInsurance;
	}

	public BigDecimal getEmployeeInsurance() {
		return employeeInsurance;
	}

	public void setEmployeeInsurance(BigDecimal employeeInsurance) {
		this.employeeInsurance = employeeInsurance;
	}

	public BigDecimal getPersonalDeduction() {
		return personalDeduction;
	}

	public void setPersonalDeduction(BigDecimal personalDeduction) {
		this.personalDeduction = personalDeduction;
	}

	public Integer getDependentCount() {
		return dependentCount;
	}

	public void setDependentCount(Integer dependentCount) {
		this.dependentCount = dependentCount;
	}

	public BigDecimal getDependentDeduction() {
		return dependentDeduction;
	}

	public void setDependentDeduction(BigDecimal dependentDeduction) {
		this.dependentDeduction = dependentDeduction;
	}

	public BigDecimal getNonTaxableAllowances() {
		return nonTaxableAllowances;
	}

	public void setNonTaxableAllowances(BigDecimal nonTaxableAllowances) {
		this.nonTaxableAllowances = nonTaxableAllowances;
	}

	public BigDecimal getTaxableIncome() {
		return taxableIncome;
	}

	public void setTaxableIncome(BigDecimal taxableIncome) {
		this.taxableIncome = taxableIncome;
	}

	public BigDecimal getPitTax() {
		return pitTax;
	}

	public void setPitTax(BigDecimal pitTax) {
		this.pitTax = pitTax;
	}

	public BigDecimal getOtHours() {
		return otHours;
	}

	public void setOtHours(BigDecimal otHours) {
		this.otHours = otHours;
		this.approvedOtHours = otHours;
	}

	public BigDecimal getGrossSalary() {
		return grossSalary;
	}

	public void setGrossSalary(BigDecimal grossSalary) {
		this.grossSalary = grossSalary;
		this.grossIncome = grossSalary;
	}

	public BigDecimal getAttendanceDeduction() {
		return attendanceDeduction;
	}

	public void setAttendanceDeduction(BigDecimal attendanceDeduction) {
		this.attendanceDeduction = attendanceDeduction;
	}

	public BigDecimal getOtBonus() {
		return otBonus;
	}

	public void setOtBonus(BigDecimal otBonus) {
		this.otBonus = otBonus;
		this.overtimePay = otBonus;
	}

	public BigDecimal getDeductions() {
		return deductions;
	}

	public void setDeductions(BigDecimal deductions) {
		this.deductions = deductions;
	}

	public BigDecimal getNetSalary() {
		return netSalary;
	}

	public void setNetSalary(BigDecimal netSalary) {
		this.netSalary = netSalary;
	}
}
