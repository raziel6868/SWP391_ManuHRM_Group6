package dto;

import java.math.BigDecimal;

public class HeadcountMovementStats {

	private int currentEmployees;
	private int newEmployees;
	private int terminatedEmployees;
	private BigDecimal turnoverRate = BigDecimal.ZERO;

	public int getCurrentEmployees() {
		return currentEmployees;
	}

	public void setCurrentEmployees(int currentEmployees) {
		this.currentEmployees = Math.max(currentEmployees, 0);
	}

	public int getNewEmployees() {
		return newEmployees;
	}

	public void setNewEmployees(int newEmployees) {
		this.newEmployees = Math.max(newEmployees, 0);
	}

	public int getTerminatedEmployees() {
		return terminatedEmployees;
	}

	public void setTerminatedEmployees(int terminatedEmployees) {
		this.terminatedEmployees = Math.max(terminatedEmployees, 0);
	}

	public BigDecimal getTurnoverRate() {
		return turnoverRate;
	}

	public void setTurnoverRate(BigDecimal turnoverRate) {
		this.turnoverRate = turnoverRate != null ? turnoverRate : BigDecimal.ZERO;
	}
}
