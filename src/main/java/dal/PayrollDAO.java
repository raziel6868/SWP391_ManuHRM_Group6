package dal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dto.PayrollPreviewRow;

public class PayrollDAO {

	private static final int STANDARD_WORK_DAYS = 26;
	private static final BigDecimal OT_RATE_NORMAL = new BigDecimal("1.5");
	private static final BigDecimal OT_RATE_NIGHT = new BigDecimal("3.0");

	public List<PayrollPreviewRow> buildPayrollPreview(int year, int month) {
		List<PayrollPreviewRow> rows = new ArrayList<>();

		String usersSql = """
				SELECT u.id, u.full_name, u.employee_code, d.name AS department_name,
				       sb.base_salary
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN salary_bases sb ON u.id = sb.user_id
				        AND sb.effective_from <= CURRENT_DATE
				        AND (sb.effective_to IS NULL OR sb.effective_to >= CURRENT_DATE)
				WHERE u.is_active = TRUE
				ORDER BY u.full_name
				""";

		try (Connection conn = DBContext.getConnection()) {
			try (PreparedStatement ps = conn.prepareStatement(usersSql); ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					Long userId = rs.getLong("id");
					String fullName = rs.getString("full_name");
					String empCode = rs.getString("employee_code");
					String deptName = rs.getString("department_name");
					BigDecimal baseSalary = rs.getBigDecimal("base_salary");

					if (baseSalary == null) {
						continue;
					}

					int actualWorkDays = countActualWorkDays(conn, userId, year, month);
					BigDecimal otHours = sumApprovedOtHours(conn, userId, year, month);
					if (otHours == null) {
						otHours = BigDecimal.ZERO;
					}

					PayrollPreviewRow row = calculateRow(userId, fullName, empCode, deptName, baseSalary,
							actualWorkDays, otHours);
					rows.add(row);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error building payroll preview: " + e.getMessage());
		}

		return rows;
	}

	private int countActualWorkDays(Connection conn, Long userId, int year, int month) throws SQLException {
		String sql = """
				SELECT COUNT(DISTINCT DATE(date)) AS work_days
				FROM attendance_records
				WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ? AND status != 'ABSENT'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("work_days");
				}
			}
		}

		return 0;
	}

	private BigDecimal sumApprovedOtHours(Connection conn, Long userId, int year, int month) throws SQLException {
		String sql = """
				SELECT COALESCE(SUM(ot.approved_hours), 0) AS total_ot
				FROM overtime_records ot
				WHERE ot.user_id = ?
				  AND ot.status = 'APPROVED'
				  AND ot.approved_hours IS NOT NULL
				  AND YEAR(ot.date) = ?
				  AND MONTH(ot.date) = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal("total_ot");
				}
			}
		}

		return BigDecimal.ZERO;
	}

	private PayrollPreviewRow calculateRow(Long userId, String fullName, String empCode, String deptName,
			BigDecimal baseSalary, int actualWorkDays, BigDecimal otHours) {

		int absentDays = STANDARD_WORK_DAYS - actualWorkDays;
		if (absentDays < 0) {
			absentDays = 0;
		}

		BigDecimal dailyRate = baseSalary.divide(new BigDecimal(STANDARD_WORK_DAYS), 10, RoundingMode.HALF_UP);
		BigDecimal hourlyRate = dailyRate.divide(new BigDecimal(8), 10, RoundingMode.HALF_UP);

		BigDecimal attendanceDeduction = dailyRate.multiply(new BigDecimal(absentDays));
		BigDecimal otBonus = otHours.multiply(hourlyRate).multiply(OT_RATE_NORMAL);

		BigDecimal grossPay = baseSalary.subtract(attendanceDeduction).add(otBonus);
		BigDecimal deductions = BigDecimal.ZERO;
		BigDecimal netSalary = grossPay.subtract(deductions);

		PayrollPreviewRow row = new PayrollPreviewRow();
		row.setUserId(userId);
		row.setUserFullName(fullName);
		row.setEmployeeCode(empCode);
		row.setDepartmentName(deptName);
		row.setBaseSalary(baseSalary);
		row.setStandardWorkDays(STANDARD_WORK_DAYS);
		row.setActualWorkDays(actualWorkDays);
		row.setAbsentDays(absentDays);
		row.setOtHours(otHours);
		row.setGrossSalary(grossPay);
		row.setAttendanceDeduction(attendanceDeduction);
		row.setOtBonus(otBonus);
		row.setDeductions(deductions);
		row.setNetSalary(netSalary);

		return row;
	}
}
