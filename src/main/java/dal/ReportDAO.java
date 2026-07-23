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
import dto.AttendanceOtEmployeeRow;
import dto.AttendanceSummaryRow;
import dto.ContractExpiryReportRow;
import dto.ContractStatusRow;
import dto.HeadcountMovementRow;
import dto.HeadcountMovementStats;
import dto.HeadcountRow;
import dto.LeaveEmployeeReportRow;
import dto.LeaveTypeUsageRow;
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

	public List<Integer> getHeadcountYears() {
		return getDistinctYears("""
				SELECT DISTINCT year_value
				FROM (
				    SELECT YEAR(start_date) AS year_value FROM contracts
				    UNION
				    SELECT YEAR(terminated_at) AS year_value FROM contracts WHERE terminated_at IS NOT NULL
				    UNION
				    SELECT YEAR(created_at) AS year_value FROM users
				) years
				ORDER BY year_value DESC
				""");
	}

	public List<AttendanceOtEmployeeRow> getAttendanceOtEmployeeRows(LocalDate startDate, LocalDate endDate,
			int expectedWorkDays, Long departmentId) {
		List<AttendanceOtEmployeeRow> rows = new ArrayList<>();
		Map<Long, List<EmploymentPeriod>> employmentPeriodsByUser = getEmploymentPeriodsByUser(startDate, endDate,
				departmentId);
		Map<Long, BigDecimal> approvedLeaveDaysByUser = getApprovedLeaveDaysByUser(startDate, endDate, departmentId);

		StringBuilder sql = new StringBuilder("""
				SELECT u.id AS user_id, u.employee_code, u.full_name, d.name AS department_name, u.is_active,
				       COALESCE(att.actual_work_days, 0) AS actual_work_days,
				       COALESCE(att.late_count, 0) AS late_count,
				       COALESCE(ot.total_ot_hours, 0) AS total_ot_hours
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN (
				    SELECT user_id,
				           SUM(CASE WHEN status <> 'ABSENT' THEN 1 ELSE 0 END) AS actual_work_days,
				           SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) AS late_count
				    FROM attendance_records
				    WHERE date BETWEEN ? AND ?
				      AND DAYOFWEEK(date) NOT IN (1, 7)
				    GROUP BY user_id
				) att ON att.user_id = u.id
				LEFT JOIN (
				    SELECT user_id, SUM(COALESCE(approved_hours, 0)) AS total_ot_hours
				    FROM overtime_records
				    WHERE status = 'APPROVED'
				      AND date BETWEEN ? AND ?
				    GROUP BY user_id
				) ot ON ot.user_id = u.id
				LEFT JOIN (
				    SELECT DISTINCT c.user_id
				    FROM contracts c
				    WHERE c.start_date <= ?
				      AND (c.end_date IS NULL OR c.end_date >= ?)
				      AND (c.terminated_at IS NULL OR c.terminated_at >= ?)
				) cp ON cp.user_id = u.id
				WHERE (u.is_active = TRUE OR cp.user_id IS NOT NULL)
				  AND (u.department_id IS NULL OR d.is_active = TRUE)
				""");

		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(startDate));

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY d.name, u.full_name");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Long userId = rs.getLong("user_id");
					int actualWorkDays = rs.getInt("actual_work_days");
					int expectedWorkDaysForUser = countExpectedWorkDaysForUser(userId, employmentPeriodsByUser,
							startDate, endDate, expectedWorkDays, rs.getBoolean("is_active"));
					BigDecimal approvedLeaveDays = approvedLeaveDaysByUser.getOrDefault(userId, BigDecimal.ZERO);
					BigDecimal unauthorizedAbsenceDays = BigDecimal.valueOf(expectedWorkDaysForUser - actualWorkDays)
							.subtract(approvedLeaveDays);
					if (unauthorizedAbsenceDays.compareTo(BigDecimal.ZERO) < 0) {
						unauthorizedAbsenceDays = BigDecimal.ZERO;
					}

					AttendanceOtEmployeeRow row = new AttendanceOtEmployeeRow();
					row.setUserId(userId);
					row.setEmployeeCode(rs.getString("employee_code"));
					row.setFullName(rs.getString("full_name"));
					row.setDepartmentName(defaultDepartmentName(rs.getString("department_name")));
					row.setExpectedWorkDays(expectedWorkDaysForUser);
					row.setActualWorkDays(actualWorkDays);
					row.setApprovedLeaveDays(approvedLeaveDays);
					row.setUnauthorizedAbsenceDays(unauthorizedAbsenceDays);
					row.setLateCount(rs.getInt("late_count"));
					row.setTotalOtHours(safe(rs.getBigDecimal("total_ot_hours")));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getAttendanceOtEmployeeRows() ERROR: " + e.getMessage());
		}

		return rows;
	}

	private Map<Long, BigDecimal> getApprovedLeaveDaysByUser(LocalDate startDate, LocalDate endDate,
			Long departmentId) {
		Map<Long, BigDecimal> leaveDaysByUser = new LinkedHashMap<>();
		StringBuilder sql = new StringBuilder("""
				SELECT lr.user_id, lr.start_date, lr.end_date
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE lr.status = 'APPROVED'
				  AND lr.start_date <= ?
				  AND lr.end_date >= ?
				  AND (u.department_id IS NULL OR d.is_active = TRUE)
				""");

		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDate requestStart = rs.getDate("start_date").toLocalDate();
					LocalDate requestEnd = rs.getDate("end_date").toLocalDate();
					LocalDate countedStart = requestStart.isBefore(startDate) ? startDate : requestStart;
					LocalDate countedEnd = requestEnd.isAfter(endDate) ? endDate : requestEnd;
					BigDecimal countedDays = LeavePolicyUtil.calculateRequestDays(countedStart, countedEnd,
							"WORKING_DAY");
					leaveDaysByUser.merge(rs.getLong("user_id"), countedDays, BigDecimal::add);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getApprovedLeaveDaysByUser() ERROR: " + e.getMessage());
		}
		return leaveDaysByUser;
	}

	private Map<Long, List<EmploymentPeriod>> getEmploymentPeriodsByUser(LocalDate startDate, LocalDate endDate,
			Long departmentId) {
		Map<Long, List<EmploymentPeriod>> periodsByUser = new LinkedHashMap<>();
		StringBuilder sql = new StringBuilder("""
				SELECT c.user_id,
				       GREATEST(c.start_date, ?) AS period_start,
				       LEAST(COALESCE(c.end_date, ?), COALESCE(c.terminated_at, ?), ?) AS period_end
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE c.start_date <= ?
				  AND (c.end_date IS NULL OR c.end_date >= ?)
				  AND (c.terminated_at IS NULL OR c.terminated_at >= ?)
				  AND (u.department_id IS NULL OR d.is_active = TRUE)
				""");

		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(startDate));

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Date periodStart = rs.getDate("period_start");
					Date periodEnd = rs.getDate("period_end");
					if (periodStart == null || periodEnd == null || periodEnd.before(periodStart)) {
						continue;
					}
					periodsByUser.computeIfAbsent(rs.getLong("user_id"), id -> new ArrayList<>())
							.add(new EmploymentPeriod(periodStart.toLocalDate(), periodEnd.toLocalDate()));
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getEmploymentPeriodsByUser() ERROR: " + e.getMessage());
		}
		return periodsByUser;
	}

	private int countExpectedWorkDaysForUser(Long userId, Map<Long, List<EmploymentPeriod>> periodsByUser,
			LocalDate startDate, LocalDate endDate, int fullPeriodExpectedWorkDays, boolean isActive) {
		List<EmploymentPeriod> periods = periodsByUser.get(userId);
		if (periods == null || periods.isEmpty()) {
			return isActive ? fullPeriodExpectedWorkDays : 0;
		}
		Set<LocalDate> expectedDates = new HashSet<>();
		for (EmploymentPeriod period : periods) {
			LocalDate current = period.startDate.isBefore(startDate) ? startDate : period.startDate;
			LocalDate periodEnd = period.endDate.isAfter(endDate) ? endDate : period.endDate;
			while (!current.isAfter(periodEnd)) {
				switch (current.getDayOfWeek()) {
					case SATURDAY :
					case SUNDAY :
						break;
					default :
						expectedDates.add(current);
						break;
				}
				current = current.plusDays(1);
			}
		}
		return expectedDates.size();
	}

	public List<LeaveEmployeeReportRow> getLeaveEmployeeReportRows(int year, LocalDate startDate, LocalDate endDate,
			Long leaveTypeId) {
		List<LeaveEmployeeReportRow> rows = new ArrayList<>();
		Map<Long, LeaveEmployeeReportRow> rowsByUserId = new LinkedHashMap<>();
		String sql = """
				SELECT u.id AS user_id, u.employee_code, u.full_name, d.name AS department_name,
				       lb.id AS annual_leave_balance_id,
				       COALESCE(lb.total_days, 0) AS annual_leave_total_days,
				       COALESCE(lb.used_days, 0) AS annual_leave_used_days
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN leave_balances lb ON lb.user_id = u.id
				  AND lb.year = ?
				  AND lb.leave_type_id = (
				      SELECT lt.id
				      FROM leave_types lt
				      WHERE lt.is_annual_leave = TRUE
				        AND lt.requires_balance = TRUE
				        AND lt.is_active = TRUE
				      ORDER BY lt.id ASC
				      LIMIT 1
				  )
				WHERE (u.department_id IS NULL OR d.is_active = TRUE)
				  AND (
				      u.is_active = TRUE
				      OR EXISTS (
				          SELECT 1
				          FROM leave_requests lr2
				          WHERE lr2.user_id = u.id
				            AND lr2.status = 'APPROVED'
				            AND lr2.start_date <= ?
				            AND lr2.end_date >= ?
				      )
				      OR EXISTS (
				          SELECT 1
				          FROM contracts c2
				          WHERE c2.user_id = u.id
				            AND c2.start_date <= ?
				            AND (c2.end_date IS NULL OR c2.end_date >= ?)
				            AND (c2.terminated_at IS NULL OR c2.terminated_at >= ?)
				      )
				  )
				ORDER BY d.name, u.full_name
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setDate(2, Date.valueOf(endDate));
			ps.setDate(3, Date.valueOf(startDate));
			ps.setDate(4, Date.valueOf(endDate));
			ps.setDate(5, Date.valueOf(startDate));
			ps.setDate(6, Date.valueOf(startDate));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LeaveEmployeeReportRow row = new LeaveEmployeeReportRow();
					row.setEmployeeCode(rs.getString("employee_code"));
					row.setFullName(rs.getString("full_name"));
					row.setDepartmentName(defaultDepartmentName(rs.getString("department_name")));
					row.setHasAnnualLeaveBalance(rs.getObject("annual_leave_balance_id") != null);
					row.setAnnualLeaveTotalDays(safe(rs.getBigDecimal("annual_leave_total_days")));
					row.setAnnualLeaveUsedDays(safe(rs.getBigDecimal("annual_leave_used_days")));
					rows.add(row);
					rowsByUserId.put(rs.getLong("user_id"), row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getLeaveEmployeeReportRows() base ERROR: " + e.getMessage());
			return rows;
		}

		for (ApprovedLeaveMetric metric : getApprovedLeaveMetrics(startDate, endDate, leaveTypeId)) {
			LeaveEmployeeReportRow row = rowsByUserId.get(metric.userId);
			if (row == null) {
				continue;
			}
			if (isPaidLeaveMetric(metric.salaryPaidBy)) {
				row.addPaidLeaveDays(metric.days);
			} else if ("NONE".equals(metric.salaryPaidBy)) {
				row.addUnpaidLeaveDays(metric.days);
			}
		}

		return rows;
	}

	public List<LeaveTypeUsageRow> getLeaveTypeUsageRows(LocalDate startDate, LocalDate endDate, Long leaveTypeId) {
		Map<Long, LeaveTypeUsageRow> rowsByTypeId = new LinkedHashMap<>();
		BigDecimal totalDays = BigDecimal.ZERO;

		for (ApprovedLeaveMetric metric : getApprovedLeaveMetrics(startDate, endDate, leaveTypeId)) {
			LeaveTypeUsageRow row = rowsByTypeId.computeIfAbsent(metric.leaveTypeId, id -> {
				LeaveTypeUsageRow newRow = new LeaveTypeUsageRow();
				newRow.setLeaveTypeId(metric.leaveTypeId);
				newRow.setLeaveTypeCode(metric.leaveTypeCode);
				newRow.setLeaveTypeName(metric.leaveTypeName);
				return newRow;
			});
			row.addTotalDays(metric.days);
			totalDays = totalDays.add(metric.days);
		}

		List<LeaveTypeUsageRow> rows = new ArrayList<>(rowsByTypeId.values());
		for (LeaveTypeUsageRow row : rows) {
			row.calculatePercentage(totalDays);
		}
		rows.sort((first, second) -> {
			int daysCompare = second.getTotalDays().compareTo(first.getTotalDays());
			if (daysCompare != 0) {
				return daysCompare;
			}
			return String.CASE_INSENSITIVE_ORDER.compare(first.getLeaveTypeName(), second.getLeaveTypeName());
		});
		return rows;
	}

	private List<ApprovedLeaveMetric> getApprovedLeaveMetrics(LocalDate startDate, LocalDate endDate,
			Long leaveTypeId) {
		List<ApprovedLeaveMetric> metrics = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT lr.user_id, lr.leave_type_id, lt.code AS leave_type_code, lt.name AS leave_type_name,
				       lr.salary_paid_by_snapshot, lr.start_date, lr.end_date, lr.day_count_method_snapshot
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN leave_types lt ON lr.leave_type_id = lt.id
				WHERE lr.status = 'APPROVED'
				  AND lr.start_date <= ?
				  AND lr.end_date >= ?
				  AND (u.department_id IS NULL OR d.is_active = TRUE)
				""");

		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));

		if (leaveTypeId != null) {
			sql.append(" AND lr.leave_type_id = ?");
			params.add(leaveTypeId);
		}

		sql.append(" ORDER BY lt.name, lr.start_date, lr.id");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDate requestStart = rs.getDate("start_date").toLocalDate();
					LocalDate requestEnd = rs.getDate("end_date").toLocalDate();
					LocalDate countedStart = requestStart.isBefore(startDate) ? startDate : requestStart;
					LocalDate countedEnd = requestEnd.isAfter(endDate) ? endDate : requestEnd;
					BigDecimal countedDays = LeavePolicyUtil.calculateRequestDays(countedStart, countedEnd,
							rs.getString("day_count_method_snapshot"));

					ApprovedLeaveMetric metric = new ApprovedLeaveMetric();
					metric.userId = rs.getLong("user_id");
					metric.leaveTypeId = rs.getLong("leave_type_id");
					metric.leaveTypeCode = rs.getString("leave_type_code");
					metric.leaveTypeName = rs.getString("leave_type_name");
					metric.salaryPaidBy = rs.getString("salary_paid_by_snapshot");
					metric.days = countedDays;
					metrics.add(metric);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getApprovedLeaveMetrics() ERROR: " + e.getMessage());
		}

		return metrics;
	}

	private boolean isPaidLeaveMetric(String salaryPaidBy) {
		return "COMPANY".equals(salaryPaidBy) || "SOCIAL_INSURANCE".equals(salaryPaidBy);
	}

	public List<AttendanceSummaryRow> getAttendanceSummary(int year, Integer month, Long departmentId) {
		List<AttendanceSummaryRow> rows = new ArrayList<>();
		int expectedMonths = month != null ? 1 : 12;

		StringBuilder sql = new StringBuilder("""
				SELECT d.id AS department_id, d.name AS department_name,
				       COUNT(DISTINCT u.id) AS total_employees,
				       COUNT(DISTINCT CASE
				           WHEN ar.status <> 'ABSENT' THEN CONCAT(u.id, ':', ar.date)
				       END) AS actual_work_days,
				       COUNT(DISTINCT CASE
				           WHEN ar.status = 'ABSENT' THEN CONCAT(u.id, ':', ar.date)
				       END) AS absent_days,
				       SUM(CASE WHEN ar.status = 'LATE' THEN 1 ELSE 0 END) AS late_count
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN attendance_records ar ON ar.user_id = u.id
				  AND YEAR(ar.date) = ?
				""");

		List<Object> params = new ArrayList<>();
		params.add(year);

		if (month != null) {
			sql.append(" AND MONTH(ar.date) = ?");
			params.add(month);
		}

		sql.append(" WHERE u.is_active = TRUE AND (u.department_id IS NULL OR d.is_active = TRUE)");

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
					row.setDepartmentName(defaultDepartmentName(rs.getString("department_name")));
					row.setYear(year);
					row.setMonth(month != null ? month : 0);
					row.setTotalEmployees(rs.getInt("total_employees"));
					row.setExpectedWorkDays(row.getTotalEmployees() * STANDARD_WORK_DAYS * expectedMonths);
					row.setActualWorkDays(rs.getInt("actual_work_days"));
					row.setTotalWorkDays(row.getActualWorkDays());
					row.setAbsentDays(rs.getInt("absent_days"));
					row.setLateCount(rs.getInt("late_count"));
					row.setTotalDays(row.getExpectedWorkDays());
					row.setAttendanceRate(calculateAttendanceRate(row.getActualWorkDays(), row.getExpectedWorkDays()));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getAttendanceSummary() ERROR: " + e.getMessage());
		}

		return rows;
	}

	private BigDecimal calculateAttendanceRate(int actualWorkDays, int expectedWorkDays) {
		return calculatePercentage(actualWorkDays, expectedWorkDays);
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
				  AND u.is_active = TRUE
				  AND (u.department_id IS NULL OR d.is_active = TRUE)
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
						newRow.setDepartmentName(defaultDepartmentName(null));
						newRow.setYear(year);
						newRow.setTotalDays(BigDecimal.ZERO);
						return newRow;
					});
					row.setDepartmentName(defaultDepartmentName(rs.getString("department_name")));
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
					} else if ("CANCELLED".equals(status)) {
						row.setCancelledRequests(row.getCancelledRequests() + 1);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getLeaveUtilization() ERROR: " + e.getMessage());
		}

		List<LeaveSummaryRow> rows = new ArrayList<>(rowsByDepartment.values());
		for (LeaveSummaryRow row : rows) {
			if (row.getApprovedRequests() > 0) {
				row.setAverageApprovedDays(row.getApprovedDays().divide(BigDecimal.valueOf(row.getApprovedRequests()),
						2, RoundingMode.HALF_UP));
			}
		}
		return rows;
	}

	public List<HeadcountRow> getHeadcount(Long departmentId, Boolean isActive) {
		return getHeadcount(departmentId, isActive, null);
	}

	public List<HeadcountRow> getHeadcount(Long departmentId, Boolean isActive, String employeeType) {
		List<HeadcountRow> rows = new ArrayList<>();
		int companyTotalEmployees = countHeadcountEmployees(isActive, employeeType);

		StringBuilder sql = new StringBuilder("""
				SELECT d.id AS department_id, d.name AS department_name,
				       SUM(CASE WHEN u.employee_type = 'OFFICE' THEN 1 ELSE 0 END) AS office_employees,
				       SUM(CASE WHEN u.employee_type = 'WORKER' THEN 1 ELSE 0 END) AS worker_employees,
				       COUNT(u.id) AS active_employees
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE 1 = 1
				""");

		List<Object> params = new ArrayList<>();

		if (isActive != null) {
			sql.append(" AND u.is_active = ?");
			params.add(isActive);
		}

		sql.append(" AND (u.department_id IS NULL OR d.is_active = TRUE)");

		if (employeeType != null && !employeeType.isBlank()) {
			sql.append(" AND u.employee_type = ?");
			params.add(employeeType);
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
					HeadcountRow row = new HeadcountRow();
					row.setDepartmentId(rs.getObject("department_id") != null ? rs.getLong("department_id") : null);
					row.setDepartmentName(defaultDepartmentName(rs.getString("department_name")));
					row.setOfficeEmployees(rs.getInt("office_employees"));
					row.setWorkerEmployees(rs.getInt("worker_employees"));
					row.setActiveEmployees(rs.getInt("active_employees"));
					row.setTotalEmployees(row.getActiveEmployees());
					row.setCompanyPercentage(calculatePercentage(row.getActiveEmployees(), companyTotalEmployees));
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getHeadcount() ERROR: " + e.getMessage());
		}

		return rows;
	}

	private int countHeadcountEmployees(Boolean isActive) {
		return countHeadcountEmployees(isActive, null);
	}

	private int countHeadcountEmployees(Boolean isActive, String employeeType) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE (u.department_id IS NULL OR d.is_active = TRUE)
				""");
		List<Object> params = new ArrayList<>();

		if (isActive != null) {
			sql.append(" AND u.is_active = ?");
			params.add(isActive);
		}

		if (employeeType != null && !employeeType.isBlank()) {
			sql.append(" AND u.employee_type = ?");
			params.add(employeeType);
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.countHeadcountEmployees() ERROR: " + e.getMessage());
		}
		return 0;
	}

	private BigDecimal calculatePercentage(int value, int total) {
		if (value <= 0 || total <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2,
				RoundingMode.HALF_UP);
	}

	private String defaultDepartmentName(String departmentName) {
		if (departmentName == null || departmentName.isBlank()) {
			return "Chưa phân phòng ban";
		}
		return departmentName;
	}

	public HeadcountMovementStats getHeadcountMovementStats(LocalDate startDate, LocalDate endDate, Long departmentId,
			String employeeType) {
		HeadcountMovementStats stats = new HeadcountMovementStats();
		stats.setCurrentEmployees(countCurrentContractEmployees(endDate, departmentId, employeeType));
		stats.setNewEmployees(countNewContractEmployees(startDate, endDate, departmentId, employeeType));
		stats.setTerminatedEmployees(countTerminatedContractEmployees(startDate, endDate, departmentId, employeeType));
		if (stats.getCurrentEmployees() > 0) {
			stats.setTurnoverRate(BigDecimal.valueOf(stats.getTerminatedEmployees()).multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(stats.getCurrentEmployees()), 2, RoundingMode.HALF_UP));
		}
		return stats;
	}

	public List<HeadcountMovementRow> getHeadcountMovementRows(LocalDate startDate, LocalDate endDate,
			Long departmentId, String employeeType, String movementStatus) {
		List<HeadcountMovementRow> rows = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT u.employee_code, u.full_name, d.name AS department_name, u.employee_type,
				       fc.start_date AS hire_date,
				       tc.terminated_at, tc.terminate_reason,
				       cc.id AS current_contract_id,
				       fct.code AS first_contract_type_code,
				       cct.code AS current_contract_type_code,
				       COALESCE(cct.name, fct.name) AS contract_type_name
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN contracts cc ON cc.id = (
				    SELECT c2.id
				    FROM contracts c2
				    WHERE c2.user_id = u.id
				      AND c2.start_date <= ?
				      AND (c2.end_date IS NULL OR c2.end_date >= ?)
				      AND (c2.terminated_at IS NULL OR c2.terminated_at > ?)
				    ORDER BY c2.start_date DESC, c2.id DESC
				    LIMIT 1
				)
				LEFT JOIN contract_types cct ON cc.contract_type_id = cct.id
				LEFT JOIN contracts fc ON fc.id = (
				    SELECT c3.id
				    FROM contracts c3
				    WHERE c3.user_id = u.id
				    ORDER BY c3.start_date ASC, c3.id ASC
				    LIMIT 1
				)
				LEFT JOIN contract_types fct ON fc.contract_type_id = fct.id
				LEFT JOIN contracts tc ON tc.id = (
				    SELECT c4.id
				    FROM contracts c4
				    WHERE c4.user_id = u.id
				      AND c4.status = 'TERMINATED'
				      AND c4.terminated_at BETWEEN ? AND ?
				    ORDER BY c4.terminated_at DESC, c4.id DESC
				    LIMIT 1
				)
				WHERE (u.department_id IS NULL OR d.is_active = TRUE)
				  AND (
				      cc.id IS NOT NULL
				      OR fc.start_date BETWEEN ? AND ?
				      OR tc.id IS NOT NULL
				      OR (u.is_active = TRUE AND cc.id IS NULL)
				  )
				""");

		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));

		appendHeadcountDimensionFilters(sql, params, departmentId, employeeType);
		sql.append(" ORDER BY d.name ASC, u.full_name ASC");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Date hireDate = rs.getDate("hire_date");
					Date terminatedAt = rs.getDate("terminated_at");
					String status = resolveMovementStatus(hireDate, terminatedAt,
							rs.getObject("current_contract_id") != null, rs.getString("first_contract_type_code"),
							rs.getString("current_contract_type_code"), startDate, endDate);
					if (!matchesMovementStatus(status, movementStatus)) {
						continue;
					}
					HeadcountMovementRow row = new HeadcountMovementRow();
					row.setEmployeeCode(rs.getString("employee_code"));
					row.setFullName(rs.getString("full_name"));
					row.setDepartmentName(defaultDepartmentName(rs.getString("department_name")));
					row.setEmployeeType(rs.getString("employee_type"));
					row.setHireDate(hireDate);
					row.setTerminatedAt(terminatedAt);
					row.setTerminateReason(rs.getString("terminate_reason"));
					row.setContractTypeName(rs.getString("contract_type_name"));
					row.setMovementStatus(status);
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("ReportDAO.getHeadcountMovementRows() ERROR: " + e.getMessage());
		}

		return rows;
	}

	private int countCurrentContractEmployees(LocalDate endDate, Long departmentId, String employeeType) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(DISTINCT u.id)
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE c.start_date <= ?
				  AND (c.end_date IS NULL OR c.end_date >= ?)
				  AND (c.terminated_at IS NULL OR c.terminated_at > ?)
				""");
		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		params.add(Date.valueOf(endDate));
		appendHeadcountDimensionFilters(sql, params, departmentId, employeeType);
		return executeCount(sql.toString(), params, "ReportDAO.countCurrentContractEmployees()");
	}

	private int countNewContractEmployees(LocalDate startDate, LocalDate endDate, Long departmentId,
			String employeeType) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(DISTINCT u.id)
				FROM users u
				JOIN (
				    SELECT user_id, MIN(start_date) AS first_start_date
				    FROM contracts
				    GROUP BY user_id
				) first_contract ON first_contract.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE first_contract.first_start_date BETWEEN ? AND ?
				""");
		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));
		appendHeadcountDimensionFilters(sql, params, departmentId, employeeType);
		return executeCount(sql.toString(), params, "ReportDAO.countNewContractEmployees()");
	}

	private int countTerminatedContractEmployees(LocalDate startDate, LocalDate endDate, Long departmentId,
			String employeeType) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(DISTINCT u.id)
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				WHERE c.status = 'TERMINATED'
				  AND c.terminated_at BETWEEN ? AND ?
				""");
		List<Object> params = new ArrayList<>();
		params.add(Date.valueOf(startDate));
		params.add(Date.valueOf(endDate));
		appendHeadcountDimensionFilters(sql, params, departmentId, employeeType);
		return executeCount(sql.toString(), params, "ReportDAO.countTerminatedContractEmployees()");
	}

	private void appendHeadcountDimensionFilters(StringBuilder sql, List<Object> params, Long departmentId,
			String employeeType) {
		sql.append(" AND (u.department_id IS NULL OR d.is_active = TRUE)");
		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (employeeType != null && !employeeType.isBlank()) {
			sql.append(" AND u.employee_type = ?");
			params.add(employeeType);
		}
	}

	private int executeCount(String sql, List<Object> params, String logContext) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println(logContext + " ERROR: " + e.getMessage());
		}
		return 0;
	}

	private String resolveMovementStatus(Date hireDate, Date terminatedAt, boolean hasCurrentContract,
			String firstContractTypeCode, String currentContractTypeCode, LocalDate startDate, LocalDate endDate) {
		if (terminatedAt != null) {
			return "TERMINATED";
		}
		boolean isNew = isDateInRange(hireDate, startDate, endDate);
		if (isNew && "PROBATION".equals(firstContractTypeCode)) {
			return "NEW_PROBATION";
		}
		if (isNew) {
			return "NEW";
		}
		if (hasCurrentContract && "PROBATION".equals(currentContractTypeCode)) {
			return "PROBATION";
		}
		if (hasCurrentContract && "SEASONAL".equals(currentContractTypeCode)) {
			return "SEASONAL";
		}
		if (hasCurrentContract) {
			return "OFFICIAL";
		}
		return "NO_CONTRACT";
	}

	private boolean isDateInRange(Date date, LocalDate startDate, LocalDate endDate) {
		if (date == null) {
			return false;
		}
		LocalDate localDate = date.toLocalDate();
		return !localDate.isBefore(startDate) && !localDate.isAfter(endDate);
	}

	private boolean matchesMovementStatus(String status, String filter) {
		if (filter == null || filter.isBlank()) {
			return true;
		}
		if ("NEW".equals(filter)) {
			return "NEW".equals(status) || "NEW_PROBATION".equals(status);
		}
		if ("PROBATION".equals(filter)) {
			return "PROBATION".equals(status) || "NEW_PROBATION".equals(status);
		}
		return filter.equals(status);
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

	private static class EmploymentPeriod {
		private final LocalDate startDate;
		private final LocalDate endDate;

		private EmploymentPeriod(LocalDate startDate, LocalDate endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}

	private static class ApprovedLeaveMetric {
		private Long userId;
		private Long leaveTypeId;
		private String leaveTypeCode;
		private String leaveTypeName;
		private String salaryPaidBy;
		private BigDecimal days = BigDecimal.ZERO;
	}
}
