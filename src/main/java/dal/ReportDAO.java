package dal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dto.AttendanceSummaryRow;
import dto.ContractStatusRow;
import dto.HeadcountRow;
import dto.LeaveSummaryRow;
import dto.OvertimeSummaryRow;
import dto.PayrollSummaryRow;
import util.LeavePolicyUtil;

public class ReportDAO {

	private static final BigDecimal OT_RATE_NORMAL = new BigDecimal("1.5");
	private static final int STANDARD_WORK_DAYS = 26;
	private static final BigDecimal HOURS_PER_DAY = new BigDecimal("8");

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
				       SUM(CASE WHEN c.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_contracts,
				       SUM(CASE WHEN c.status = 'EXPIRED' THEN 1 ELSE 0 END) AS expired_contracts,
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

	public List<PayrollSummaryRow> getPayrollSummary(int year, int month) {
		List<PayrollSummaryRow> rows = new ArrayList<>();
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());

		String sql = """
				SELECT payroll.department_id,
				       payroll.department_name,
				       COUNT(*) AS employee_count,
				       SUM(payroll.base_salary) AS total_salary,
				       SUM(payroll.total_ot_hours) AS total_ot_hours
				FROM (
				    SELECT u.id AS user_id,
				           d.id AS department_id,
				           d.name AS department_name,
				           c.salary AS base_salary,
				           COALESCE((
				               SELECT SUM(ot.approved_hours)
				               FROM overtime_records ot
				               WHERE ot.user_id = u.id
				                 AND ot.status = 'APPROVED'
				                 AND YEAR(ot.date) = ?
				                 AND MONTH(ot.date) = ?
				           ), 0) AS total_ot_hours
				    FROM users u
				    LEFT JOIN departments d ON u.department_id = d.id
				    JOIN contracts c ON c.id = (
				        SELECT c2.id
				        FROM contracts c2
				        WHERE c2.user_id = u.id
				          AND c2.status = 'ACTIVE'
				          AND c2.start_date <= ?
				          AND (c2.end_date IS NULL OR c2.end_date >= ?)
				          AND c2.salary IS NOT NULL
				        ORDER BY c2.start_date DESC, c2.id DESC
				        LIMIT 1
				    )
				    WHERE u.is_active = TRUE
				) payroll
				GROUP BY payroll.department_id, payroll.department_name
				ORDER BY payroll.department_name
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.setDate(3, lastDay);
			ps.setDate(4, firstDay);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					PayrollSummaryRow row = new PayrollSummaryRow();
					row.setDepartmentId(rs.getObject("department_id") != null ? rs.getLong("department_id") : null);
					row.setDepartmentName(rs.getString("department_name"));
					row.setYear(year);
					row.setMonth(month);
					row.setEmployeeCount(rs.getInt("employee_count"));

					BigDecimal totalSalary = rs.getBigDecimal("total_salary");
					BigDecimal totalOtHours = rs.getBigDecimal("total_ot_hours");
					if (totalOtHours == null) {
						totalOtHours = BigDecimal.ZERO;
					}

					row.setTotalSalary(totalSalary != null ? totalSalary : BigDecimal.ZERO);
					row.setAverageSalary(totalSalary != null && row.getEmployeeCount() > 0
							? totalSalary.divide(BigDecimal.valueOf(row.getEmployeeCount()), 2, RoundingMode.HALF_UP)
							: BigDecimal.ZERO);

					BigDecimal otCost = calculateOtCost(totalOtHours, totalSalary, row.getEmployeeCount());
					row.setTotalOtCost(otCost);
					row.setTotalCost(row.getTotalSalary().add(otCost));

					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getPayrollSummary() ERROR: " + e.getMessage());
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
						       SUM(CASE WHEN ot.status = 'APPROVED' THEN COALESCE(ot.approved_hours, 0) ELSE 0 END) AS total_ot_hours
						FROM overtime_records ot
						JOIN users u ON ot.user_id = u.id
						LEFT JOIN departments d ON u.department_id = d.id
						WHERE YEAR(ot.date) = ?
						""");

		List<Object> params = new ArrayList<>();
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
					row.setTotalOtHours(rs.getBigDecimal("total_ot_hours"));
					row.setTotalOtCost(BigDecimal.ZERO);
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getOvertimeSummary() ERROR: " + e.getMessage());
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

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			Object param = params.get(i);
			if (param == null) {
				ps.setObject(i + 1, null);
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
