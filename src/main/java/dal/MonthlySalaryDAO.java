package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MonthlySalary;

public class MonthlySalaryDAO {

	public List<MonthlySalary> getBySheet(Long sheetId) {
		List<MonthlySalary> salaries = new ArrayList<>();
		String sql = """
				SELECT ms.id, ms.monthly_sheet_id, ms.user_id, ms.actual_work_days, ms.ot_hours,
				       ms.gross_salary, ms.deductions, ms.net_salary, ms.status,
				       ms.updated_at AS generated_at, ms.created_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       msys.year, msys.month
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.monthly_sheet_id = ?
				ORDER BY u.full_name
				""";

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
				SELECT ms.id, ms.monthly_sheet_id, ms.user_id, ms.actual_work_days, ms.ot_hours,
				       ms.gross_salary, ms.deductions, ms.net_salary, ms.status,
				       ms.updated_at AS generated_at, ms.created_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       msys.year, msys.month
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.monthly_sheet_id = ? AND ms.user_id = ?
				""";

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
				SELECT ms.id, ms.monthly_sheet_id, ms.user_id, ms.actual_work_days, ms.ot_hours,
				       ms.gross_salary, ms.deductions, ms.net_salary, ms.status,
				       ms.updated_at AS generated_at, ms.created_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       msys.year, msys.month
				FROM monthly_salaries ms
				JOIN users u ON ms.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN monthly_sheets msys ON ms.monthly_sheet_id = msys.id
				WHERE ms.user_id = ?
				ORDER BY ms.updated_at DESC
				LIMIT 1
				""";

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

	public boolean batchUpsert(Long sheetId, List<MonthlySalary> salaries) {
		if (salaries == null || salaries.isEmpty()) {
			return true;
		}

		String sql = """
				INSERT INTO monthly_salaries
					(monthly_sheet_id, user_id, actual_work_days, ot_hours,
					 gross_salary, deductions, net_salary, status)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				ON DUPLICATE KEY UPDATE
					actual_work_days = VALUES(actual_work_days),
					ot_hours = VALUES(ot_hours),
					gross_salary = VALUES(gross_salary),
					deductions = VALUES(deductions),
					net_salary = VALUES(net_salary),
					status = VALUES(status),
					updated_at = CURRENT_TIMESTAMP
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (MonthlySalary ms : salaries) {
				ps.setLong(1, sheetId);
				ps.setLong(2, ms.getUserId());
				ps.setInt(3, ms.getActualWorkDays());
				ps.setBigDecimal(4, ms.getOtHours());
				ps.setBigDecimal(5, ms.getGrossSalary());
				ps.setBigDecimal(6, ms.getDeductions());
				ps.setBigDecimal(7, ms.getNetSalary());
				ps.setString(8, "GENERATED");
				ps.addBatch();
			}
			ps.executeBatch();
			return true;
		} catch (SQLException e) {
			System.err.println("Error batch upsert salaries: " + e.getMessage());
		}

		return false;
	}

	private MonthlySalary mapRow(ResultSet rs) throws SQLException {
		MonthlySalary ms = new MonthlySalary();
		ms.setId(rs.getLong("id"));
		ms.setMonthlySheetId(rs.getLong("monthly_sheet_id"));
		ms.setUserId(rs.getLong("user_id"));
		ms.setActualWorkDays(rs.getInt("actual_work_days"));
		ms.setOtHours(rs.getBigDecimal("ot_hours"));
		ms.setGrossSalary(rs.getBigDecimal("gross_salary"));
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
}
