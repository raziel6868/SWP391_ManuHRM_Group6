package dto;

import java.sql.Date;

public class HeadcountMovementRow {

	private String employeeCode;
	private String fullName;
	private String departmentName;
	private String employeeType;
	private Date hireDate;
	private Date terminatedAt;
	private String terminateReason;
	private String contractTypeCode;
	private String contractTypeName;
	private String movementStatus;

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public Date getTerminatedAt() {
		return terminatedAt;
	}

	public void setTerminatedAt(Date terminatedAt) {
		this.terminatedAt = terminatedAt;
	}

	public String getTerminateReason() {
		return terminateReason;
	}

	public void setTerminateReason(String terminateReason) {
		this.terminateReason = terminateReason;
	}

	public String getContractTypeCode() {
		return contractTypeCode;
	}

	public void setContractTypeCode(String contractTypeCode) {
		this.contractTypeCode = contractTypeCode;
	}

	public String getContractTypeName() {
		return contractTypeName;
	}

	public void setContractTypeName(String contractTypeName) {
		this.contractTypeName = contractTypeName;
	}

	public String getMovementStatus() {
		return movementStatus;
	}

	public void setMovementStatus(String movementStatus) {
		this.movementStatus = movementStatus;
	}

	public String getEmployeeTypeLabel() {
		if ("WORKER".equals(employeeType)) {
			return "Công nhân";
		}
		return "Văn phòng";
	}

	public String getStatusLabel() {
		if ("TERMINATED".equals(movementStatus)) {
			return "Đã thôi việc";
		}
		if ("NEW_PROBATION".equals(movementStatus)) {
			return "Mới (Đang thử việc)";
		}
		if ("NEW".equals(movementStatus)) {
			return "Mới";
		}
		if ("PROBATION".equals(movementStatus)) {
			return "Đang thử việc";
		}
		if ("SEASONAL".equals(movementStatus)) {
			return "Thời vụ";
		}
		if ("NO_CONTRACT".equals(movementStatus)) {
			return "Chưa có hợp đồng hiệu lực";
		}
		return "Chính thức";
	}

	public String getMovementBadgeClass() {
		if ("TERMINATED".equals(movementStatus)) {
			return "text-bg-danger";
		}
		if ("NEW_PROBATION".equals(movementStatus) || "NEW".equals(movementStatus)
				|| "PROBATION".equals(movementStatus)) {
			return "text-bg-info";
		}
		if ("SEASONAL".equals(movementStatus)) {
			return "text-bg-warning";
		}
		if ("NO_CONTRACT".equals(movementStatus)) {
			return "text-bg-secondary";
		}
		return "text-bg-success";
	}
}
