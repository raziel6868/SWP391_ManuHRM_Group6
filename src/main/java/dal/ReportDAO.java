package dal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import dto.AttendanceSummaryRow;
import dto.ContractExpiryReportRow;
import dto.ContractStatusRow;
import dto.HeadcountRow;
import dto.LeaveSummaryRow;
import dto.OvertimeEmployeeReportRow;
import dto.OvertimeSummaryRow;
import dto.PayrollEmployeeReportRow;
import dto.PayrollPreviewRow;
import dto.PayrollSummaryRow;
import util.LeavePolicyUtil;

public class ReportDAO {

	private static final BigDecimal OT_RATE_NORMAL = new BigDecimal("1.5");
	private static final int STANDARD_WORK_DAYS = 26;
	private static final BigDecimal HOURS_PER_DAY = new BigDecimal("8");
	private static final BigDecimal MONEY_ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	private final PayrollDAO payrollDAO = new PayrollDAO();

	public List<Integer> getAttendanceYears() {
		return getDistinctYears(
				"SELECT DISTINCT YEAR(date) AS year_value FROM attendance_records ORDER BY year_value DESC");
	}

	public List<Integer> getLeaveYears() {
		return getDistinctYears("""
				SELECT DISTINCT year_value
				FROM (
				    SELECT YEAR(start_date) AS year_value FROM leave_requests
				    UNION
				    SELECT YEAR(end_date) AS year_value FROM leave_requests
				) years
				ORDER BY year_value DESC
				""");
	}

	public List<Integer> getOvertimeYears() {
		return getDistinctYears(
				"SELECT DISTINCT YEAR(date) AS year_value FROM overtime_records ORDER BY year_value DESC");
	}

	public List<Integer> getPayrollYears() {
		return getDistinctYears("SELECT DISTINCT year AS year_value FROM monthly_sheets ORDER BY year_value DESC");
	}

	public List<AttendanceSummaryRow> getAttendanceSummary(int year, Integer month, Long departmentId) {
		List<AttendanceSummaryRow> rows = new ArrayList<>();

		StringBuilder sql = new StringBuilder("""
				SELECT d.id AS department_id, d.name AS department_name,
				       COUNT(DISTINCT ar.user_id) AS total_employees,
				       COUNT(DISTINCT DATE(ar.date)) AS total_work_days
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE YEAR(ar.date) = ?
				""");

		List<Object> params = new ArrayList<>();
		params.add(year);

		if (month != null) {
			sql.append(" AND MONTH(ar.date) = ?");
			params.add(month);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" GROUP BY d.id, d.name ORDER BY d.name");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					AttendanceSummaryRow row = new AttendanceSummaryRow();
					row.setDepartmentId(rs.getObject("department_id") != null ? rs.getLong("department_id") : null);
					row.setDepartmentName(rs.getString("department_name"));
					row.setYear(year);
					row.setMonth(month != null ? month : 0);
					row.setTotalEmployees(rs.getInt("total_employees"));
					row.setTotalWorkDays(rs.getInt("total_work_days"));
					row.setTotalDays(STANDARD_WORK_DAYS);
					BigDecimal rate = BigDecimal.ZERO;
					if (row.getTotalEmployees() > 0) {
						BigDecimal expectedDays = BigDecimal.valueOf(row.getTotalEmployees())
								.multiply(BigDecimal.valueOf(row.getTotalDays()));
						if (expectedDays.compareTo(BigDecimal.ZERO) > 0) {
							rate = BigDecimal.valueOf(row.getTotalWorkDays())
									.divide(expectedDays, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
						}
					}
					row.setAttendanceRate(rate);
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getAttendanceSummary() ERROR: " + e.getMessage());
		}

		return rows;
	}

	public List<LeaveSummaryRow> getLeaveUtilization(int year, Long departmentId) {
		Map<Long, LeaveSummaryRow> rowsByDepartment = new LinkedHashMap<>();
		LocalDate yearStart = LocalDate.of(year, 1, 1);
		LocalDate yearEnd = LocalDate.of(year, 12, 31);

		StringBuilder sql = new StringBuilder("""
				SELECT d.id AS department_id, d.name AS department_name,
				       lr.status, lr.start_date, lr.end_date, lr.day_count_method_snapshot
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE lr.start_date <= ?
				  AND lr.end_date >= ?
				""");

		List<Object> params = new ArrayList<>();
		params.add(java.sql.Date.valueOf(yearEnd));
		params.add(java.sql.Date.valueOf(yearStart));

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY d.name, lr.start_date, lr.id");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Long rowDepartmentId = rs.getObject("department_id") != null ? rs.getLong("department_id") : null;
					LeaveSummaryRow row = rowsByDepartment.computeIfAbsent(rowDepartmentId, id -> {
						LeaveSummaryRow newRow = new LeaveSummaryRow();
						newRow.setDepartmentId(rowDepartmentId);
						newRow.setDepartmentName(null);
						newRow.setYear(year);
						newRow.setTotalDays(BigDecimal.ZERO);
						return newRow;
					});
					row.setDepartmentName(rs.getString("department_name"));
					row.setTotalRequests(row.getTotalRequests() + 1);

					String status = rs.getString("status");
					if ("APPROVED".equals(status)) {
						row.setApprovedRequests(row.getApprovedRequests() + 1);
						LocalDate requestStart = rs.getDate("start_date").toLocalDate();
						LocalDate requestEnd = rs.getDate("end_date").toLocalDate();
						LocalDate countedStart = requestStart.isBefore(yearStart) ? yearStart : requestStart;
						LocalDate countedEnd = requestEnd.isAfter(yearEnd) ? yearEnd : requestEnd;
						row.setTotalDays(row.getTotalDays().add(LeavePolicyUtil.calculateRequestDays(countedStart,
								countedEnd, rs.getString("day_count_method_snapshot"))));
					} else if ("REJECTED".equals(status)) {
						row.setRejectedRequests(row.getRejectedRequests() + 1);
					} else if ("PENDING".equals(status) || "APPROVED_LEVEL_1".equals(status)) {
						row.setPendingRequests(row.getPendingRequests() + 1);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getLeaveUtilization() ERROR: " + e.getMessage());
		}

		return new ArrayList<>(rowsByDepartment.values());
	}

	public List<HeadcountRow> getHeadcount(Long departmentId, Boolean isActive) {
		List<HeadcountRow> rows = new ArrayList<>();

		StringBuilder sql = new StringBuilder("""
				SELECT d.id AS department_id, d.name AS department_name,
				       u.employee_type,
				       COUNT(u.id) AS total_employees,
				       SUM(CASE WHEN u.is_active = TRUE THEN 1 ELSE 0 END) AS active_employees
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE u.is_active = TRUE
				""");

		List<Object> params = new ArrayList<>();

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" GROUP BY d.id, d.name, u.employee_type ORDER BY d.name, u.employee_type");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					HeadcountRow row = new HeadcountRow();
					row.setDepartmentId(rs.getObject("department_id") != null ? rs.getLong("department_id") : null);
					row.setDepartmentName(rs.getString("department_name"));
					row.setEmployeeType(rs.getString("employee_type"));
					row.setTotalEmployees(rs.getInt("total_employees"));
					row.setActiveEmployees(rs.getInt("active_employees"));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getHeadcount() ERROR: " + e.getMessage());
		}

		return rows;
	}

	public List<ContractStatusRow> getContractStatus(Long departmentId) {
		List<ContractStatusRow> rows = new ArrayList<>();

		StringBuilder sql = new StringBuilder("""
				SELECT d.id AS department_id, d.name AS department_name,
				       SUM(CASE
				               WHEN c.status = 'ACTIVE'
				                AND (c.end_date IS NULL OR c.end_date > DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY))
				               THEN 1 ELSE 0
				           END) AS active_contracts,
				       SUM(CASE WHEN c.status = 'EXPIRED' THEN 1 ELSE 0 END) AS expired_contracts,
				       SUM(CASE
				               WHEN c.status IN ('ACTIVE', 'EXPIRING_SOON')
				                AND c.end_date IS NOT NULL
				                AND c.end_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
				               THEN 1 ELSE 0
				           END) AS expiring_soon_contracts,
				       SUM(CASE WHEN c.status = 'PENDING_RENEWAL' THEN 1 ELSE 0 END) AS pending_renewal,
				       SUM(CASE WHEN c.status = 'TERMINATED' THEN 1 ELSE 0 END) AS terminated_contracts,
				       COUNT(c.id) AS total_contracts
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				""");

		List<Object> params = new ArrayList<>();

		if (departmentId != null) {
			sql.append(" WHERE u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" GROUP BY d.id, d.name ORDER BY d.name");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ContractStatusRow row = new ContractStatusRow();
					row.setDepartmentId(rs.getObject("department_id") != null ? rs.getLong("department_id") : null);
					row.setDepartmentName(rs.getString("department_name"));
					row.setActiveContracts(rs.getInt("active_contracts"));
					row.setExpiredContracts(rs.getInt("expired_contracts"));
					row.setExpiringSoonContracts(rs.getInt("expiring_soon_contracts"));
					row.setPendingRenewal(rs.getInt("pending_renewal"));
					row.setTerminatedContracts(rs.getInt("terminated_contracts"));
					row.setTotalContracts(rs.getInt("total_contracts"));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getContractStatus() ERROR: " + e.getMessage());
		}

		return rows;
	}

	public List<ContractExpiryReportRow> getExpiringContracts(int days, Long departmentId) {
		List<ContractExpiryReportRow> rows = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT u.employee_code, u.full_name, d.name AS department_name, ct.name AS contract_type_name,
				       c.start_date, c.end_date, DATEDIFF(c.end_date, CURRENT_DATE) AS days_remaining,
				       c.status
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN contract_types ct ON c.contract_type_id = ct.id
				WHERE c.status IN ('ACTIVE', 'EXPIRING_SOON')
				  AND c.end_date IS NOT NULL
				  AND c.end_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
				""");

		List<Object> params = new ArrayList<>();
		params.add(days);

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY c.end_date ASC, u.full_name ASC");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ContractExpiryReportRow row = new ContractExpiryReportRow();
					row.setEmployeeCode(rs.getString("employee_code"));
					row.setFullName(rs.getString("full_name"));
					row.setDepartmentName(rs.getString("department_name"));
					row.setContractTypeName(rs.getString("contract_type_name"));
					row.setStartDate(rs.getDate("start_date"));
					row.setEndDate(rs.getDate("end_date"));
					row.setDaysRemaining(rs.getInt("days_remaining"));
					row.setStatus(rs.getString("status"));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getExpiringContracts() ERROR: " + e.getMessage());
		}

		return rows;
	}

	public List<PayrollSummaryRow> getPayrollSummary(int year, int month) {
		Map<String, PayrollSummaryRow> byDepartment = new LinkedHashMap<>();
		for (PayrollPreviewRow preview : payrollDAO.buildPayrollPreview(year, month)) {
			String departmentName = normalizeDepartment(preview.getDepartmentName());
			PayrollSummaryRow row = byDepartment.computeIfAbsent(departmentName,
					key -> newPayrollSummaryRow(year, month, key));
			row.setEmployeeCount(row.getEmployeeCount() + 1);
			row.setTotalSalary(money(row.getTotalSalary().add(safe(preview.getBaseSalary()))));
			row.setTotalAllowances(money(row.getTotalAllowances().add(safe(preview.getTotalAllowances()))));
			row.setTotalAttendanceBonus(money(row.getTotalAttendanceBonus().add(safe(preview.getAttendanceBonus()))));
			row.setTotalOtCost(money(row.getTotalOtCost().add(safe(preview.getOvertimePay()))));
			row.setGrossIncome(money(row.getGrossIncome().add(safe(preview.getGrossIncome()))));
			row.setEmployeeInsurance(money(row.getEmployeeInsurance().add(safe(preview.getEmployeeInsurance()))));
			row.setPitTax(money(row.getPitTax().add(safe(preview.getPitTax()))));
			row.setDeductions(money(row.getDeductions().add(safe(preview.getDeductions()))));
			row.setNetSalary(money(row.getNetSalary().add(safe(preview.getNetSalary()))));
			row.setTotalCost(row.getGrossIncome());
		}

		List<PayrollSummaryRow> rows = new ArrayList<>(byDepartment.values());
		for (PayrollSummaryRow row : rows) {
			row.setAverageSalary(row.getEmployeeCount() > 0
					? row.getGrossIncome().divide(BigDecimal.valueOf(row.getEmployeeCount()), 2, RoundingMode.HALF_UP)
					: MONEY_ZERO);
		}
		return rows;
	}

	public List<PayrollEmployeeReportRow> getPayrollEmployeeDetails(int year, int month) {
		List<PayrollEmployeeReportRow> rows = new ArrayList<>();
		Set<Long> usersWithActiveContracts = getUsersWithActiveContracts(year, month);
		for (PayrollPreviewRow preview : payrollDAO.buildPayrollPreview(year, month)) {
			PayrollEmployeeReportRow row = new PayrollEmployeeReportRow();
			row.setEmployeeCode(preview.getEmployeeCode());
			row.setFullName(preview.getUserFullName());
			row.setDepartmentName(normalizeDepartment(preview.getDepartmentName()));
			row.setActualWorkDays(safe(preview.getActualWorkDays()));
			row.setPaidLeaveDays(safe(preview.getPaidLeaveDays()));
			row.setApprovedOtHours(safe(preview.getApprovedOtHours()));
			row.setAttendanceBonus(safe(preview.getAttendanceBonus()));
			row.setGrossIncome(safe(preview.getGrossIncome()));
			row.setDeductions(safe(preview.getDeductions()));
			row.setNetSalary(safe(preview.getNetSalary()));
			row.setWarningStatus(buildPayrollWarning(preview, usersWithActiveContracts.contains(preview.getUserId())));
			rows.add(row);
		}
		return rows;
	}

	public List<OvertimeSummaryRow> getOvertimeSummary(int year, Integer month, Long departmentId) {
		List<OvertimeSummaryRow> rows = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"""
						SELECT d.id AS department_id, d.name AS department_name,
						       COUNT(ot.id) AS total_requests,
						       SUM(CASE WHEN ot.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_requests,
						       SUM(CASE WHEN ot.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_requests,
						       SUM(CASE WHEN ot.status = 'PENDING' THEN 1 ELSE 0 END) AS pending_requests,
						       SUM(CASE WHEN ot.status = 'APPROVED' THEN COALESCE(ot.approved_hours, 0) ELSE 0 END) AS total_ot_hours,
						       SUM(CASE
						               WHEN ot.status = 'APPROVED'
						               THEN COALESCE(ot.approved_hours, 0)
						                    * COALESCE(c.salary, 0) / ? / ? * ?
						               ELSE 0
						           END) AS total_ot_cost
						FROM overtime_records ot
						JOIN users u ON ot.user_id = u.id
						LEFT JOIN departments d ON u.department_id = d.id
						LEFT JOIN contracts c ON c.id = (
						    SELECT c2.id
						    FROM contracts c2
						    WHERE c2.user_id = u.id
						      AND c2.status = 'ACTIVE'
						      AND c2.start_date <= ot.date
						      AND (c2.end_date IS NULL OR c2.end_date >= ot.date)
						      AND c2.salary IS NOT NULL
						    ORDER BY c2.start_date DESC, c2.id DESC
						    LIMIT 1
						)
						WHERE YEAR(ot.date) = ?
						""");

		List<Object> params = new ArrayList<>();
		params.add(STANDARD_WORK_DAYS);
		params.add(HOURS_PER_DAY);
		params.add(OT_RATE_NORMAL);
		params.add(year);

		if (month != null) {
			sql.append(" AND MONTH(ot.date) = ?");
			params.add(month);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" GROUP BY d.id, d.name ORDER BY d.name");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OvertimeSummaryRow row = new OvertimeSummaryRow();
					row.setDepartmentId(rs.getObject("department_id") != null ? rs.getLong("department_id") : null);
					row.setDepartmentName(rs.getString("department_name"));
					row.setYear(year);
					row.setMonth(month != null ? month : 0);
					row.setTotalRequests(rs.getInt("total_requests"));
					row.setApprovedRequests(rs.getInt("approved_requests"));
					row.setRejectedRequests(rs.getInt("rejected_requests"));
					row.setPendingRequests(rs.getInt("pending_requests"));
					row.setTotalOtHours(safe(rs.getBigDecimal("total_ot_hours")));
					row.setTotalOtCost(money(rs.getBigDecimal("total_ot_cost")));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getOvertimeSummary() ERROR: " + e.getMessage());
		}

		return rows;
	}

	public List<OvertimeEmployeeReportRow> getTopOvertimeEmployees(int year, Integer month, Long departmentId) {
		List<OvertimeEmployeeReportRow> rows = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT u.employee_code, u.full_name, d.name AS department_name,
				       SUM(COALESCE(ot.approved_hours, 0)) AS total_approved_hours,
				       SUM(COALESCE(ot.approved_hours, 0) * COALESCE(c.salary, 0) / ? / ? * ?) AS estimated_ot_cost
				FROM overtime_records ot
				JOIN users u ON ot.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN contracts c ON c.id = (
				    SELECT c2.id
				    FROM contracts c2
				    WHERE c2.user_id = u.id
				      AND c2.status = 'ACTIVE'
				      AND c2.start_date <= ot.date
				      AND (c2.end_date IS NULL OR c2.end_date >= ot.date)
				      AND c2.salary IS NOT NULL
				    ORDER BY c2.start_date DESC, c2.id DESC
				    LIMIT 1
				)
				WHERE ot.status = 'APPROVED'
				  AND ot.approved_hours IS NOT NULL
				  AND YEAR(ot.date) = ?
				""");

		List<Object> params = new ArrayList<>();
		params.add(STANDARD_WORK_DAYS);
		params.add(HOURS_PER_DAY);
		params.add(OT_RATE_NORMAL);
		params.add(year);

		if (month != null) {
			sql.append(" AND MONTH(ot.date) = ?");
			params.add(month);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append("""
				 GROUP BY u.id, u.employee_code, u.full_name, d.name
				 ORDER BY total_approved_hours DESC, u.full_name ASC
				 LIMIT 10
				""");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OvertimeEmployeeReportRow row = new OvertimeEmployeeReportRow();
					row.setEmployeeCode(rs.getString("employee_code"));
					row.setFullName(rs.getString("full_name"));
					row.setDepartmentName(normalizeDepartment(rs.getString("department_name")));
					row.setTotalApprovedHours(safe(rs.getBigDecimal("total_approved_hours")));
					row.setEstimatedOtCost(money(rs.getBigDecimal("estimated_ot_cost")));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getTopOvertimeEmployees() ERROR: " + e.getMessage());
		}
		return rows;
	}

	private BigDecimal calculateOtCost(BigDecimal otHours, BigDecimal totalSalary, int employeeCount) {
		if (otHours == null || otHours.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		if (totalSalary == null || employeeCount <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal perDaySalary = totalSalary.divide(BigDecimal.valueOf(employeeCount * STANDARD_WORK_DAYS), 10,
				RoundingMode.HALF_UP);
		BigDecimal perHourSalary = perDaySalary.divide(HOURS_PER_DAY, 10, RoundingMode.HALF_UP);
		return perHourSalary.multiply(otHours).multiply(OT_RATE_NORMAL).setScale(2, RoundingMode.HALF_UP);
	}

	private PayrollSummaryRow newPayrollSummaryRow(int year, int month, String departmentName) {
		PayrollSummaryRow row = new PayrollSummaryRow();
		row.setDepartmentName(departmentName);
		row.setYear(year);
		row.setMonth(month);
		row.setTotalSalary(MONEY_ZERO);
		row.setAverageSalary(MONEY_ZERO);
		row.setTotalOtCost(MONEY_ZERO);
		row.setTotalCost(MONEY_ZERO);
		row.setTotalAllowances(MONEY_ZERO);
		row.setTotalAttendanceBonus(MONEY_ZERO);
		row.setGrossIncome(MONEY_ZERO);
		row.setEmployeeInsurance(MONEY_ZERO);
		row.setPitTax(MONEY_ZERO);
		row.setDeductions(MONEY_ZERO);
		row.setNetSalary(MONEY_ZERO);
		return row;
	}

	private Set<Long> getUsersWithActiveContracts(int year, int month) {
		Set<Long> userIds = new HashSet<>();
		String sql = """
				SELECT DISTINCT user_id
				FROM contracts
				WHERE status IN ('ACTIVE', 'EXPIRING_SOON', 'PENDING_RENEWAL')
				  AND start_date <= LAST_DAY(STR_TO_DATE(CONCAT(?, '-', ?, '-01'), '%Y-%m-%d'))
				  AND (end_date IS NULL OR end_date >= STR_TO_DATE(CONCAT(?, '-', ?, '-01'), '%Y-%m-%d'))
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.setInt(3, year);
			ps.setInt(4, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					userIds.add(rs.getLong("user_id"));
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getUsersWithActiveContracts() ERROR: " + e.getMessage());
		}
		return userIds;
	}

	private String buildPayrollWarning(PayrollPreviewRow row, boolean hasActiveContract) {
		List<String> warnings = new ArrayList<>();
		if (!hasActiveContract) {
			warnings.add("Thiếu hợp đồng active");
		}
		if (safe(row.getActualWorkDays()).compareTo(BigDecimal.ZERO) == 0
				&& safe(row.getPaidLeaveDays()).compareTo(BigDecimal.ZERO) == 0) {
			warnings.add("Thiếu chấm công");
		}
		if (safe(row.getNetSalary()).compareTo(BigDecimal.ZERO) <= 0) {
			warnings.add("Net salary bằng 0");
		}
		return warnings.isEmpty() ? "OK" : String.join(", ", warnings);
	}

	private String normalizeDepartment(String departmentName) {
		return departmentName == null || departmentName.isBlank() ? "Chưa có phòng ban" : departmentName;
	}

	private BigDecimal safe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	private BigDecimal money(BigDecimal value) {
		return safe(value).setScale(2, RoundingMode.HALF_UP);
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			Object param = params.get(i);
			if (param == null) {
				ps.setObject(i + 1, null);
			} else if (param instanceof BigDecimal) {
				ps.setBigDecimal(i + 1, (BigDecimal) param);
			} else if (param instanceof Date) {
				ps.setDate(i + 1, (Date) param);
			} else if (param instanceof Long) {
				ps.setLong(i + 1, (Long) param);
			} else if (param instanceof Integer) {
				ps.setInt(i + 1, (Integer) param);
			} else {
				ps.setObject(i + 1, param);
			}
		}
	}

	private List<Integer> getDistinctYears(String sql) {
		List<Integer> years = new ArrayList<>();
		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				years.add(rs.getInt("year_value"));
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getDistinctYears() ERROR: " + e.getMessage());
		}
		return years;
	}
}
