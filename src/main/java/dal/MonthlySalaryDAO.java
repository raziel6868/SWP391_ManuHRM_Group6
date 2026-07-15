package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MonthlySalary;

public class MonthlySalaryDAO {

	private static final String SELECT_COLUMNS = """
			ms.id, ms.monthly_sheet_id, ms.user_id,
			ms.base_salary, ms.standard_work_days, ms.standard_work_hours_per_day,
			ms.actual_work_days, ms.paid_leave_days,
			ms.prorated_base_salary, ms.paid_leave_salary,
			ms.approved_ot_hours, ms.ot_hours, ms.overtime_pay,
			ms.total_allowances, ms.gross_income, ms.gross_salary,
			ms.insurance_salary, ms.insurance_based_allowances,
			ms.social_insurance_base, ms.health_insurance_base, ms.unemployment_insurance_base,
			ms.social_insurance, ms.health_insurance, ms.unemployment_insurance, ms.employee_insurance,
			ms.personal_deduction, ms.dependent_count, ms.dependent_deduction,
			ms.non_taxable_allowances, ms.taxable_income, ms.pit_tax,
			ms.deductions, ms.net_salary, ms.status,
			ms.updated_at AS generated_at, ms.created_at,
			u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
			msys.year, msys.month
			""";

	public List<MonthlySalary> getBySheet(Long sheetId) {
		List<MonthlySalary> salaries = new ArrayList<>();
		String sql = """
				SELECT %s
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.monthly_sheet_id = ?
				ORDER BY u.full_name
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					salaries.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting salaries by sheet: " + e.getMessage());
		}
		return salaries;
	}

	public MonthlySalary getBySheetAndUser(Long sheetId, Long userId) {
		String sql = """
				SELECT %s
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.monthly_sheet_id = ? AND ms.user_id = ?
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			ps.setLong(2, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("getBySheetAndUser error: " + e.getMessage());
		}
		return null;
	}

	public MonthlySalary getLatestByUser(Long userId) {
		String sql = """
				SELECT %s
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.user_id = ?
				ORDER BY msys.year DESC, msys.month DESC, ms.updated_at DESC
				LIMIT 1
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("getLatestByUser error: " + e.getMessage());
		}
		return null;
	}

	public MonthlySalary getLatestFinalizedByUser(Long userId) {
		String sql = """
				SELECT %s
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.user_id = ? AND ms.status IN ('FINAL', 'PAID')
				ORDER BY msys.year DESC, msys.month DESC, ms.updated_at DESC
				LIMIT 1
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("getLatestFinalizedByUser error: " + e.getMessage());
		}
		return null;
	}

	public List<MonthlySalary> getByUser(Long userId) {
		return getByUser(userId, false);
	}

	public List<MonthlySalary> getFinalizedByUser(Long userId) {
		return getByUser(userId, true);
	}

	private List<MonthlySalary> getByUser(Long userId, boolean finalizedOnly) {
		List<MonthlySalary> salaries = new ArrayList<>();
		if (userId == null) {
			return salaries;
		}

		String sql = """
				SELECT %s
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.user_id = ?
				%s
				ORDER BY msys.year DESC, msys.month DESC, ms.updated_at DESC
				""".formatted(SELECT_COLUMNS, finalizedOnly ? "AND ms.status IN ('FINAL', 'PAID')" : "");

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					salaries.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("getByUser error: " + e.getMessage());
		}
		return salaries;
	}

	public boolean hasGeneratedRows(Long sheetId) {
		return countByStatuses(sheetId) > 0;
	}

	public boolean hasAnyDraft(Long sheetId) {
		return countByStatuses(sheetId, "DRAFT") > 0;
	}

	public boolean hasAnyFinalOrPaid(Long sheetId) {
		return countByStatuses(sheetId, "FINAL", "PAID") > 0;
	}

	public int closePayroll(Long sheetId) {
		String sql = """
				UPDATE monthly_salaries
				SET status = 'FINAL',
				    updated_at = CURRENT_TIMESTAMP
				WHERE monthly_sheet_id = ?
				  AND status = 'DRAFT'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			return ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("closePayroll error: " + e.getMessage());
		}
		return 0;
	}

	public boolean batchUpsert(Long sheetId, List<MonthlySalary> salaries) {
		if (salaries == null || salaries.isEmpty()) {
			return true;
		}

		String sql = """
				INSERT INTO monthly_salaries
					(monthly_sheet_id, user_id,
					 base_salary, standard_work_days, standard_work_hours_per_day,
					 actual_work_days, paid_leave_days,
					 prorated_base_salary, paid_leave_salary,
					 approved_ot_hours, ot_hours, overtime_pay,
					 total_allowances, gross_income, gross_salary,
					 insurance_salary, insurance_based_allowances,
					 social_insurance_base, health_insurance_base, unemployment_insurance_base,
					 social_insurance, health_insurance, unemployment_insurance, employee_insurance,
					 personal_deduction, dependent_count, dependent_deduction,
					 non_taxable_allowances, taxable_income, pit_tax,
					 deductions, net_salary, status)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				ON DUPLICATE KEY UPDATE
					base_salary = VALUES(base_salary),
					standard_work_days = VALUES(standard_work_days),
					standard_work_hours_per_day = VALUES(standard_work_hours_per_day),
					actual_work_days = VALUES(actual_work_days),
					paid_leave_days = VALUES(paid_leave_days),
					prorated_base_salary = VALUES(prorated_base_salary),
					paid_leave_salary = VALUES(paid_leave_salary),
					approved_ot_hours = VALUES(approved_ot_hours),
					ot_hours = VALUES(ot_hours),
					overtime_pay = VALUES(overtime_pay),
					total_allowances = VALUES(total_allowances),
					gross_income = VALUES(gross_income),
					gross_salary = VALUES(gross_salary),
					insurance_salary = VALUES(insurance_salary),
					insurance_based_allowances = VALUES(insurance_based_allowances),
					social_insurance_base = VALUES(social_insurance_base),
					health_insurance_base = VALUES(health_insurance_base),
					unemployment_insurance_base = VALUES(unemployment_insurance_base),
					social_insurance = VALUES(social_insurance),
					health_insurance = VALUES(health_insurance),
					unemployment_insurance = VALUES(unemployment_insurance),
					employee_insurance = VALUES(employee_insurance),
					personal_deduction = VALUES(personal_deduction),
					dependent_count = VALUES(dependent_count),
					dependent_deduction = VALUES(dependent_deduction),
					non_taxable_allowances = VALUES(non_taxable_allowances),
					taxable_income = VALUES(taxable_income),
					pit_tax = VALUES(pit_tax),
					deductions = VALUES(deductions),
					net_salary = VALUES(net_salary),
					status = VALUES(status),
					updated_at = CURRENT_TIMESTAMP
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (MonthlySalary ms : salaries) {
				int i = 1;
				ps.setLong(i++, sheetId);
				ps.setLong(i++, ms.getUserId());
				ps.setBigDecimal(i++, zeroIfNull(ms.getBaseSalary()));
				ps.setBigDecimal(i++, defaultIfNull(ms.getStandardWorkDays(), new BigDecimal("26")));
				ps.setBigDecimal(i++, defaultIfNull(ms.getStandardWorkHoursPerDay(), new BigDecimal("8")));
				ps.setBigDecimal(i++, zeroIfNull(ms.getActualWorkDays()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getPaidLeaveDays()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getProratedBaseSalary()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getPaidLeaveSalary()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getApprovedOtHours()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getOtHours()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getOvertimePay()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getTotalAllowances()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getGrossIncome()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getGrossSalary()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getInsuranceSalary()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getInsuranceBasedAllowances()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getSocialInsuranceBase()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getHealthInsuranceBase()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getUnemploymentInsuranceBase()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getSocialInsurance()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getHealthInsurance()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getUnemploymentInsurance()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getEmployeeInsurance()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getPersonalDeduction()));
				ps.setInt(i++, defaultIntIfNull(ms.getDependentCount()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getDependentDeduction()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getNonTaxableAllowances()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getTaxableIncome()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getPitTax()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getDeductions()));
				ps.setBigDecimal(i++, zeroIfNull(ms.getNetSalary()));
				ps.setString(i++, normalizeStatus(ms.getStatus()));
				ps.addBatch();
			}
			ps.executeBatch();
			return true;
		} catch (SQLException e) {
			System.err.println("Error batch upsert salaries: " + e.getMessage());
		}
		return false;
	}

	private int countByStatuses(Long sheetId, String... statuses) {
		if (sheetId == null) {
			return 0;
		}

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM monthly_salaries WHERE monthly_sheet_id = ?");
		if (statuses != null && statuses.length > 0) {
			sql.append(" AND status IN (");
			for (int i = 0; i < statuses.length; i++) {
				if (i > 0) {
					sql.append(", ");
				}
				sql.append("?");
			}
			sql.append(")");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setLong(1, sheetId);
			if (statuses != null) {
				for (int i = 0; i < statuses.length; i++) {
					ps.setString(i + 2, statuses[i]);
				}
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("countByStatuses error: " + e.getMessage());
		}
		return 0;
	}

	private MonthlySalary mapRow(ResultSet rs) throws SQLException {
		MonthlySalary ms = new MonthlySalary();
		ms.setId(rs.getLong("id"));
		ms.setMonthlySheetId(rs.getLong("monthly_sheet_id"));
		ms.setUserId(rs.getLong("user_id"));
		ms.setBaseSalary(rs.getBigDecimal("base_salary"));
		ms.setStandardWorkDays(rs.getBigDecimal("standard_work_days"));
		ms.setStandardWorkHoursPerDay(rs.getBigDecimal("standard_work_hours_per_day"));
		ms.setActualWorkDays(rs.getBigDecimal("actual_work_days"));
		ms.setPaidLeaveDays(rs.getBigDecimal("paid_leave_days"));
		ms.setProratedBaseSalary(rs.getBigDecimal("prorated_base_salary"));
		ms.setPaidLeaveSalary(rs.getBigDecimal("paid_leave_salary"));
		ms.setApprovedOtHours(rs.getBigDecimal("approved_ot_hours"));
		ms.setOtHours(rs.getBigDecimal("ot_hours"));
		ms.setOvertimePay(rs.getBigDecimal("overtime_pay"));
		ms.setTotalAllowances(rs.getBigDecimal("total_allowances"));
		ms.setGrossIncome(rs.getBigDecimal("gross_income"));
		ms.setGrossSalary(rs.getBigDecimal("gross_salary"));
		ms.setInsuranceSalary(rs.getBigDecimal("insurance_salary"));
		ms.setInsuranceBasedAllowances(rs.getBigDecimal("insurance_based_allowances"));
		ms.setSocialInsuranceBase(rs.getBigDecimal("social_insurance_base"));
		ms.setHealthInsuranceBase(rs.getBigDecimal("health_insurance_base"));
		ms.setUnemploymentInsuranceBase(rs.getBigDecimal("unemployment_insurance_base"));
		ms.setSocialInsurance(rs.getBigDecimal("social_insurance"));
		ms.setHealthInsurance(rs.getBigDecimal("health_insurance"));
		ms.setUnemploymentInsurance(rs.getBigDecimal("unemployment_insurance"));
		ms.setEmployeeInsurance(rs.getBigDecimal("employee_insurance"));
		ms.setPersonalDeduction(rs.getBigDecimal("personal_deduction"));
		ms.setDependentCount(rs.getInt("dependent_count"));
		ms.setDependentDeduction(rs.getBigDecimal("dependent_deduction"));
		ms.setNonTaxableAllowances(rs.getBigDecimal("non_taxable_allowances"));
		ms.setTaxableIncome(rs.getBigDecimal("taxable_income"));
		ms.setPitTax(rs.getBigDecimal("pit_tax"));
		ms.setDeductions(rs.getBigDecimal("deductions"));
		ms.setNetSalary(rs.getBigDecimal("net_salary"));
		ms.setStatus(rs.getString("status"));
		ms.setGeneratedAt(rs.getTimestamp("generated_at"));
		ms.setCreatedAt(rs.getTimestamp("created_at"));
		ms.setUserFullName(rs.getString("user_full_name"));
		ms.setEmployeeCode(rs.getString("employee_code"));
		ms.setDepartmentName(rs.getString("department_name"));
		ms.setYear(rs.getInt("year"));
		ms.setMonth(rs.getInt("month"));
		return ms;
	}

	private BigDecimal zeroIfNull(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	private BigDecimal defaultIfNull(BigDecimal value, BigDecimal defaultValue) {
		return value != null ? value : defaultValue;
	}

	private int defaultIntIfNull(Integer value) {
		return value != null ? value : 0;
	}

	private String normalizeStatus(String status) {
		if ("FINAL".equals(status) || "PAID".equals(status)) {
			return status;
		}
		return "DRAFT";
	}
}
