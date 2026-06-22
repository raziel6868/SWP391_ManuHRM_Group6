package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.LeaveBalance;

public class LeaveBalanceDAO {

	public List<LeaveBalance> searchBalances(Integer year, Long departmentId, int offset, int limit) {
		List<LeaveBalance> balances = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT lb.id, lb.user_id, lb.leave_type_id, lb.year, lb.total_days, lb.used_days,
				       lb.created_at, lb.updated_at,
				       u.employee_code, u.full_name AS employee_name,
				       d.name AS department_name,
				       lt.code AS leave_type_code, lt.name AS leave_type_name
				FROM leave_balances lb
				INNER JOIN users u ON u.id = lb.user_id
				LEFT JOIN departments d ON d.id = u.department_id
				INNER JOIN leave_types lt ON lt.id = lb.leave_type_id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();
		appendFilters(sql, params, year, departmentId);
		sql.append(" ORDER BY lb.year DESC, u.employee_code ASC, lt.code ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					balances.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveBalanceDAO.searchBalances() ERROR: " + e.getMessage());
		}
		return balances;
	}

	public int countBalances(Integer year, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM leave_balances lb
				INNER JOIN users u ON u.id = lb.user_id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();
		appendFilters(sql, params, year, departmentId);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveBalanceDAO.countBalances() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public LeaveBalance getByUserAndTypeAndYear(Long userId, Long leaveTypeId, Integer year) {
		if (userId == null || leaveTypeId == null || year == null) {
			return null;
		}

		String sql = """
				SELECT lb.id, lb.user_id, lb.leave_type_id, lb.year, lb.total_days, lb.used_days,
				       lb.created_at, lb.updated_at,
				       u.employee_code, u.full_name AS employee_name,
				       d.name AS department_name,
				       lt.code AS leave_type_code, lt.name AS leave_type_name
				FROM leave_balances lb
				INNER JOIN users u ON u.id = lb.user_id
				LEFT JOIN departments d ON d.id = u.department_id
				INNER JOIN leave_types lt ON lt.id = lb.leave_type_id
				WHERE lb.user_id = ? AND lb.leave_type_id = ? AND lb.year = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setLong(2, leaveTypeId);
			ps.setInt(3, year);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveBalanceDAO.getByUserAndTypeAndYear() ERROR: " + e.getMessage());
		}
		return null;
	}

	public List<LeaveBalance> searchByUserAndYear(Long userId, Integer year) {
		List<LeaveBalance> balances = new ArrayList<>();
		if (userId == null || year == null) {
			return balances;
		}

		String sql = """
				SELECT lb.id, lb.user_id, lb.leave_type_id, lb.year, lb.total_days, lb.used_days,
				       lb.created_at, lb.updated_at,
				       u.employee_code, u.full_name AS employee_name,
				       d.name AS department_name,
				       lt.code AS leave_type_code, lt.name AS leave_type_name
				FROM leave_balances lb
				INNER JOIN users u ON u.id = lb.user_id
				LEFT JOIN departments d ON d.id = u.department_id
				INNER JOIN leave_types lt ON lt.id = lb.leave_type_id
				WHERE lb.user_id = ? AND lb.year = ?
				ORDER BY lt.code ASC
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					balances.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveBalanceDAO.searchByUserAndYear() ERROR: " + e.getMessage());
		}
		return balances;
	}

	public boolean upsert(Long userId, Long leaveTypeId, Integer year, BigDecimal totalDays) {
		if (userId == null || leaveTypeId == null || year == null || totalDays == null) {
			return false;
		}

		String sql = """
				INSERT INTO leave_balances (user_id, leave_type_id, year, total_days, used_days)
				VALUES (?, ?, ?, ?, 0)
				ON DUPLICATE KEY UPDATE
				    total_days = VALUES(total_days),
				    updated_at = CURRENT_TIMESTAMP
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setLong(2, leaveTypeId);
			ps.setInt(3, year);
			ps.setBigDecimal(4, totalDays);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveBalanceDAO.upsert() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean incrementUsedDays(Long userId, Long leaveTypeId, Integer year, BigDecimal days) {
		try (Connection conn = DBContext.getConnection()) {
			return incrementUsedDays(conn, userId, leaveTypeId, year, days);
		} catch (SQLException e) {
			System.err.println("LeaveBalanceDAO.incrementUsedDays() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean incrementUsedDays(Connection conn, Long userId, Long leaveTypeId, Integer year, BigDecimal days)
			throws SQLException {
		if (conn == null || userId == null || leaveTypeId == null || year == null || days == null) {
			return false;
		}

		String sql = """
				UPDATE leave_balances
				SET used_days = used_days + ?
				WHERE user_id = ?
				  AND leave_type_id = ?
				  AND year = ?
				  AND used_days + ? <= total_days
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBigDecimal(1, days);
			ps.setLong(2, userId);
			ps.setLong(3, leaveTypeId);
			ps.setInt(4, year);
			ps.setBigDecimal(5, days);
			return ps.executeUpdate() > 0;
		}
	}

	private void appendFilters(StringBuilder sql, List<Object> params, Integer year, Long departmentId) {
		if (year != null) {
			sql.append(" AND lb.year = ?");
			params.add(year);
		}
		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private LeaveBalance mapRow(ResultSet rs) throws SQLException {
		LeaveBalance balance = new LeaveBalance();
		balance.setId(rs.getLong("id"));
		balance.setUserId(rs.getLong("user_id"));
		balance.setLeaveTypeId(rs.getLong("leave_type_id"));
		balance.setYear(rs.getInt("year"));
		balance.setTotalDays(rs.getBigDecimal("total_days"));
		balance.setUsedDays(rs.getBigDecimal("used_days"));
		balance.setCreatedAt(rs.getTimestamp("created_at"));
		balance.setUpdatedAt(rs.getTimestamp("updated_at"));
		balance.setEmployeeCode(rs.getString("employee_code"));
		balance.setEmployeeName(rs.getString("employee_name"));
		balance.setDepartmentName(rs.getString("department_name"));
		balance.setLeaveTypeCode(rs.getString("leave_type_code"));
		balance.setLeaveTypeName(rs.getString("leave_type_name"));
		return balance;
	}
}
