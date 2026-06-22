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
				       u.full_name AS user_full_name, u.employee_code,
				       lt.name AS leave_type_name
				FROM leave_balances lb
				JOIN users u ON lb.user_id = u.id
				JOIN leave_types lt ON lb.leave_type_id = lt.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (year != null) {
			sql.append(" AND lb.year = ?");
			params.add(year);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY u.full_name, lt.name LIMIT ? OFFSET ?");
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
				JOIN users u ON lb.user_id = u.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (year != null) {
			sql.append(" AND lb.year = ?");
			params.add(year);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
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
				       u.full_name AS user_full_name, u.employee_code,
				       lt.name AS leave_type_name
				FROM leave_balances lb
				JOIN users u ON lb.user_id = u.id
				JOIN leave_types lt ON lb.leave_type_id = lt.id
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

	public boolean upsert(Long userId, Long leaveTypeId, Integer year, BigDecimal totalDays) {
		if (userId == null || leaveTypeId == null || year == null || totalDays == null) {
			return false;
		}

		String sql = """
				INSERT INTO leave_balances (user_id, leave_type_id, year, total_days, used_days)
				VALUES (?, ?, ?, ?, 0)
				ON DUPLICATE KEY UPDATE total_days = VALUES(total_days)
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

	public boolean incrementUsedDays(Connection conn, Long userId, Long leaveTypeId, Integer year, BigDecimal days)
			throws SQLException {
		if (userId == null || leaveTypeId == null || year == null || days == null) {
			return false;
		}

		String sql = """
				UPDATE leave_balances
				SET used_days = used_days + ?
				WHERE user_id = ? AND leave_type_id = ? AND year = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBigDecimal(1, days);
			ps.setLong(2, userId);
			ps.setLong(3, leaveTypeId);
			ps.setInt(4, year);
			return ps.executeUpdate() > 0;
		}
	}

	public List<LeaveBalance> getByUserAndYear(Long userId, Integer year) {
		if (userId == null || year == null) {
			return new ArrayList<>();
		}

		List<LeaveBalance> balances = new ArrayList<>();
		String sql = """
				SELECT lb.id, lb.user_id, lb.leave_type_id, lb.year, lb.total_days, lb.used_days,
				       lb.created_at, lb.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       lt.name AS leave_type_name
				FROM leave_balances lb
				JOIN users u ON lb.user_id = u.id
				JOIN leave_types lt ON lb.leave_type_id = lt.id
				WHERE lb.user_id = ? AND lb.year = ?
				ORDER BY lt.name
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
			System.err.println("LeaveBalanceDAO.getByUserAndYear() ERROR: " + e.getMessage());
		}

		return balances;
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
		balance.setUserFullName(rs.getString("user_full_name"));
		balance.setEmployeeCode(rs.getString("employee_code"));
		balance.setLeaveTypeName(rs.getString("leave_type_name"));
		return balance;
	}
}
