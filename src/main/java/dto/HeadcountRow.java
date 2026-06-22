package dto;

public class HeadcountRow {

	private Long departmentId;
	private String departmentName;
	private String employeeType;
	private int totalEmployees;
	private int activeEmployees;

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
}
