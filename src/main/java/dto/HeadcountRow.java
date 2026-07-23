package dto;

import java.math.BigDecimal;

public class HeadcountRow {

	private Long departmentId;
	private String departmentName;
	private String employeeType;
	private int totalEmployees;
	private int activeEmployees;
	private int officeEmployees;
	private int workerEmployees;
	private BigDecimal companyPercentage = BigDecimal.ZERO;

	public HeadcountRow() {
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(String employeeType) {
		this.employeeType = employeeType;
	}

	public int getTotalEmployees() {
		return totalEmployees;
	}

	public void setTotalEmployees(int totalEmployees) {
		this.totalEmployees = totalEmployees;
	}

	public int getActiveEmployees() {
		return activeEmployees;
	}

	public void setActiveEmployees(int activeEmployees) {
		this.activeEmployees = activeEmployees;
	}

	public int getOfficeEmployees() {
		return officeEmployees;
	}

	public void setOfficeEmployees(int officeEmployees) {
		this.officeEmployees = officeEmployees;
	}

	public int getWorkerEmployees() {
		return workerEmployees;
	}

	public void setWorkerEmployees(int workerEmployees) {
		this.workerEmployees = workerEmployees;
	}

	public BigDecimal getCompanyPercentage() {
		return companyPercentage;
	}

	public void setCompanyPercentage(BigDecimal companyPercentage) {
		this.companyPercentage = companyPercentage != null ? companyPercentage : BigDecimal.ZERO;
	}
}
