package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.ShiftAssignment;

public class ShiftAssignmentDAO {

	public List<ShiftAssignment> searchAssignments(Long departmentId, Long userId, Long shiftId, Date startDate,
			Date endDate, int offset, int limit) {
		List<ShiftAssignment> assignments = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       s.name AS shift_name, s.start_time AS shift_start_time, s.end_time AS shift_end_time
				FROM shift_assignments sa
				JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN shifts s ON sa.shift_id = s.id
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

		sql.append(" ORDER BY sa.date DESC, u.full_name LIMIT ? OFFSET ?");
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
			System.err.println("Error searching shift assignments: " + e.getMessage());
		}

		return assignments;
	}

	public int countAssignments(Long departmentId, Long userId, Long shiftId, Date startDate, Date endDate) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM shift_assignments sa
				JOIN users u ON sa.user_id = u.id
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
			System.err.println("Error counting shift assignments: " + e.getMessage());
		}

		return 0;
	}

	public ShiftAssignment getById(Long id) {
		String sql = """
				SELECT sa.id, sa.user_id, sa.shift_id, sa.date, sa.created_at, sa.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       s.name AS shift_name, s.start_time AS shift_start_time, s.end_time AS shift_end_time
				FROM shift_assignments sa
				JOIN users u ON sa.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN shifts s ON sa.shift_id = s.id
				WHERE sa.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting shift assignment by id: " + e.getMessage());
		}

		return null;
	}

	public boolean upsert(Long userId, Long shiftId, Date date) {
		String sql = """
				INSERT INTO shift_assignments (user_id, shift_id, date)
				VALUES (?, ?, ?)
				ON DUPLICATE KEY UPDATE shift_id = VALUES(shift_id), updated_at = CURRENT_TIMESTAMP
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setLong(2, shiftId);
			ps.setDate(3, date);
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error upserting shift assignment: " + e.getMessage());
			return false;
		}
	}

	public int bulkUpsert(List<ShiftAssignment> assignments) {
		if (assignments == null || assignments.isEmpty()) {
			return 0;
		}

		String sql = """
				INSERT INTO shift_assignments (user_id, shift_id, date)
				VALUES (?, ?, ?)
				ON DUPLICATE KEY UPDATE shift_id = VALUES(shift_id), updated_at = CURRENT_TIMESTAMP
				""";

		int count = 0;
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (ShiftAssignment sa : assignments) {
				ps.setLong(1, sa.getUserId());
				ps.setLong(2, sa.getShiftId());
				ps.setDate(3, sa.getDate());
				ps.addBatch();
				count++;
			}
			ps.executeBatch();
		} catch (SQLException e) {
			System.err.println("Error bulk upserting shift assignments: " + e.getMessage());
			return 0;
		}

		return count;
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

		Time startTime = rs.getTime("shift_start_time");
		sa.setShiftStartTime(startTime != null ? startTime.toString() : null);

		Time endTime = rs.getTime("shift_end_time");
		sa.setShiftEndTime(endTime != null ? endTime.toString() : null);

		return sa;
	}
}
