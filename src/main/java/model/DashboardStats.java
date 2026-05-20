package model;

public class DashboardStats {
	private int totalUsers;
	private int activeUsers;
	private int officeUsers;
	private int workerUsers;
	private int departments;

	public int getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}

	public int getActiveUsers() {
		return activeUsers;
	}

	public void setActiveUsers(int activeUsers) {
		this.activeUsers = activeUsers;
	}

	public int getOfficeUsers() {
		return officeUsers;
	}

	public void setOfficeUsers(int officeUsers) {
		this.officeUsers = officeUsers;
	}

	public int getWorkerUsers() {
		return workerUsers;
	}

	public void setWorkerUsers(int workerUsers) {
		this.workerUsers = workerUsers;
	}

	public int getDepartments() {
		return departments;
	}

	public void setDepartments(int departments) {
		this.departments = departments;
	}

	public int getActivePercentage() {
		return calculatePercentage(activeUsers, totalUsers);
	}

	public int getOfficePercentage() {
		return calculatePercentage(officeUsers, activeUsers);
	}

	public int getWorkerPercentage() {
		return calculatePercentage(workerUsers, activeUsers);
	}

	private int calculatePercentage(int value, int total) {
		if (total <= 0) {
			return 0;
		}
		return (int) Math.round(value * 100.0 / total);
	}
}
