package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MonthlySalary {

	private Long id;
	private Long monthlySheetId;
	private Long userId;
	private Integer actualWorkDays;
	private BigDecimal otHours;
	private BigDecimal grossSalary;
	private BigDecimal deductions;
	private BigDecimal netSalary;
	private String status;
	private Timestamp generatedAt;
	private Timestamp createdAt;
	private String userFullName;
	private String employeeCode;
	private String departmentName;
	private Integer year;
	private Integer month;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMonthlySheetId() {
		return monthlySheetId;
	}

	public void setMonthlySheetId(Long monthlySheetId) {
		this.monthlySheetId = monthlySheetId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getActualWorkDays() {
		return actualWorkDays;
	}

	public void setActualWorkDays(Integer actualWorkDays) {
		this.actualWorkDays = actualWorkDays;
	}

	public BigDecimal getOtHours() {
		return otHours;
	}

	public void setOtHours(BigDecimal otHours) {
		this.otHours = otHours;
	}

	public BigDecimal getGrossSalary() {
		return grossSalary;
	}

	public void setGrossSalary(BigDecimal grossSalary) {
		this.grossSalary = grossSalary;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(Timestamp generatedAt) {
		this.generatedAt = generatedAt;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
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

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}
}
