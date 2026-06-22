package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ShiftAssignment;

public class ShiftAssignmentDAO {

	public List<ShiftAssignment> searchAssignments(Long departmentId, Long userId, Long shiftId, Date startDate,
			Date endDate, int offset, int limit) {
		List<ShiftAssignment> assignments = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       d.name AS department_name,
				       s.name AS shift_name, s.code AS shift_code
				FROM shift_assignments sa
				INNER JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				INNER JOIN shifts s ON sa.shift_id = s.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (userId != null) {
			sql.append(" AND sa.user_id = ?");
			params.add(userId);
		}
		if (shiftId != null) {
			sql.append(" AND sa.shift_id = ?");
			params.add(shiftId);
		}
		if (startDate != null) {
			sql.append(" AND sa.date >= ?");
			params.add(startDate);
		}
		if (endDate != null) {
			sql.append(" AND sa.date <= ?");
			params.add(endDate);
		}

		sql.append(" ORDER BY sa.date DESC, u.full_name ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					assignments.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.searchAssignments() ERROR: " + e.getMessage());
		}
		return assignments;
	}

	public int countAssignments(Long departmentId, Long userId, Long shiftId, Date startDate, Date endDate) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM shift_assignments sa
				INNER JOIN users u ON sa.user_id = u.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (userId != null) {
			sql.append(" AND sa.user_id = ?");
			params.add(userId);
		}
		if (shiftId != null) {
			sql.append(" AND sa.shift_id = ?");
			params.add(shiftId);
		}
		if (startDate != null) {
			sql.append(" AND sa.date >= ?");
			params.add(startDate);
		}
		if (endDate != null) {
			sql.append(" AND sa.date <= ?");
			params.add(endDate);
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
			System.err.println("ShiftAssignmentDAO.countAssignments() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public ShiftAssignment getByUserAndDate(Long userId, Date date) {
		if (userId == null || date == null) {
			return null;
		}

		String sql = """
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       d.name AS department_name,
				       s.name AS shift_name, s.code AS shift_code
				FROM shift_assignments sa
				INNER JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				INNER JOIN shifts s ON sa.shift_id = s.id
				WHERE sa.user_id = ? AND sa.date = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.getByUserAndDate() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean upsert(Long userId, Long shiftId, Date date) {
		if (userId == null || shiftId == null || date == null) {
			return false;
		}

		String sql = """
				INSERT INTO shift_assignments (user_id, shift_id, date)
				VALUES (?, ?, ?)
				ON DUPLICATE KEY UPDATE shift_id = VALUES(shift_id), updated_at = CURRENT_TIMESTAMP
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setLong(2, shiftId);
			ps.setDate(3, date);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.upsert() ERROR: " + e.getMessage());
		}
		return false;
	}

	public int bulkUpsert(List<ShiftAssignment> assignments) {
		if (assignments == null || assignments.isEmpty()) {
			return 0;
		}

		int successCount = 0;
		for (ShiftAssignment assignment : assignments) {
			if (upsert(assignment.getUserId(), assignment.getShiftId(), assignment.getDate())) {
				successCount++;
			}
		}
		return successCount;
	}

	public List<ShiftAssignment> getByDepartmentAndDateRange(Long departmentId, Date startDate, Date endDate) {
		List<ShiftAssignment> assignments = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       d.name AS department_name,
				       s.name AS shift_name, s.code AS shift_code
				FROM shift_assignments sa
				INNER JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				INNER JOIN shifts s ON sa.shift_id = s.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}
		if (startDate != null) {
			sql.append(" AND sa.date >= ?");
			params.add(startDate);
		}
		if (endDate != null) {
			sql.append(" AND sa.date <= ?");
			params.add(endDate);
		}

		sql.append(" ORDER BY sa.date ASC, u.full_name ASC");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					assignments.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.getByDepartmentAndDateRange() ERROR: " + e.getMessage());
		}
		return assignments;
	}

	public List<ShiftAssignment> getByUserId(Long userId) {
		List<ShiftAssignment> assignments = new ArrayList<>();

		String sql = """
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       d.name AS department_name,
				       s.name AS shift_name, s.code AS shift_code
				FROM shift_assignments sa
				INNER JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				INNER JOIN shifts s ON sa.shift_id = s.id
				WHERE sa.user_id = ?
				ORDER BY sa.date DESC
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					assignments.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.getByUserId() ERROR: " + e.getMessage());
		}
		return assignments;
	}

	public List<ShiftAssignment> getByUserIdAndDateRange(Long userId, Date startDate, Date endDate) {
		List<ShiftAssignment> assignments = new ArrayList<>();

		StringBuilder sql = new StringBuilder("""
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       d.name AS department_name,
				       s.name AS shift_name, s.code AS shift_code
				FROM shift_assignments sa
				INNER JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				INNER JOIN shifts s ON sa.shift_id = s.id
				WHERE sa.user_id = ?
				""");
		List<Object> params = new ArrayList<>();
		params.add(userId);

		if (startDate != null) {
			sql.append(" AND sa.date >= ?");
			params.add(startDate);
		}
		if (endDate != null) {
			sql.append(" AND sa.date <= ?");
			params.add(endDate);
		}

		sql.append(" ORDER BY sa.date ASC");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					assignments.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.getByUserIdAndDateRange() ERROR: " + e.getMessage());
		}
		return assignments;
	}

	public boolean hasConflict(Long userId, Date date, Long excludeAssignmentId) {
		if (userId == null || date == null) {
			return false;
		}

		String sql;
		PreparedStatement ps;

		try (Connection conn = DBContext.getConnection()) {
			if (excludeAssignmentId != null) {
				sql = "SELECT COUNT(*) FROM shift_assignments WHERE user_id = ? AND date = ? AND id <> ?";
				ps = conn.prepareStatement(sql);
				ps.setLong(1, userId);
				ps.setDate(2, date);
				ps.setLong(3, excludeAssignmentId);
			} else {
				sql = "SELECT COUNT(*) FROM shift_assignments WHERE user_id = ? AND date = ?";
				ps = conn.prepareStatement(sql);
				ps.setLong(1, userId);
				ps.setDate(2, date);
			}

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.hasConflict() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean delete(Long id) {
		if (id == null) {
			return false;
		}

		String sql = "DELETE FROM shift_assignments WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ShiftAssignmentDAO.delete() ERROR: " + e.getMessage());
		}
		return false;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private ShiftAssignment mapRow(ResultSet rs) throws SQLException {
		ShiftAssignment sa = new ShiftAssignment();
		sa.setId(rs.getLong("id"));
		sa.setUserId(rs.getLong("user_id"));
		sa.setShiftId(rs.getLong("shift_id"));
		sa.setDate(rs.getDate("date"));
		sa.setCreatedAt(rs.getTimestamp("created_at"));
		sa.setUpdatedAt(rs.getTimestamp("updated_at"));
		sa.setUserFullName(rs.getString("user_full_name"));
		sa.setEmployeeCode(rs.getString("employee_code"));
		sa.setDepartmentName(rs.getString("department_name"));
		sa.setShiftName(rs.getString("shift_name"));
		sa.setShiftCode(rs.getString("shift_code"));
		return sa;
	}
}
