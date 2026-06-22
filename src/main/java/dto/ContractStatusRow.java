package dto;

public class ContractStatusRow {

	private Long departmentId;
	private String departmentName;
	private int activeContracts;
	private int expiredContracts;
	private int pendingRenewal;
	private int terminatedContracts;
	private int totalContracts;

	public ContractStatusRow() {
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

	public int getActiveContracts() {
		return activeContracts;
	}

	public void setActiveContracts(int activeContracts) {
		this.activeContracts = activeContracts;
	}

	public int getExpiredContracts() {
		return expiredContracts;
	}

	public void setExpiredContracts(int expiredContracts) {
		this.expiredContracts = expiredContracts;
	}

	public int getPendingRenewal() {
		return pendingRenewal;
	}

	public void setPendingRenewal(int pendingRenewal) {
		this.pendingRenewal = pendingRenewal;
	}

	public int getTerminatedContracts() {
		return terminatedContracts;
	}

	public void setTerminatedContracts(int terminatedContracts) {
		this.terminatedContracts = terminatedContracts;
	}

	public int getTotalContracts() {
		return totalContracts;
	}

	public void setTotalContracts(int totalContracts) {
		this.totalContracts = totalContracts;
	}
}
