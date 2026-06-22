package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.AttendanceRecord;

public class AttendanceDAO {

	public int batchUpsertByMonth(int year, int month, List<AttendanceRecord> records) {
		if (records == null || records.isEmpty()) {
			return 0;
		}

		String deleteSql = "DELETE FROM attendance_records WHERE YEAR(date) = ? AND MONTH(date) = ?";
		String insertSql = """
				INSERT INTO attendance_records
				(user_id, date, shift_id, check_in, check_out, working_hours, status, import_batch_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";

		int count = 0;
		Connection conn = null;

		try {
			conn = DBContext.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
				deletePs.setInt(1, year);
				deletePs.setInt(2, month);
				deletePs.executeUpdate();
			}

			try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
				for (AttendanceRecord record : records) {
					insertPs.setLong(1, record.getUserId());
					insertPs.setDate(2, record.getDate());
					if (record.getShiftId() != null) {
						insertPs.setLong(3, record.getShiftId());
					} else {
						insertPs.setNull(3, java.sql.Types.BIGINT);
					}
					insertPs.setTime(4, record.getCheckIn());
					insertPs.setTime(5, record.getCheckOut());
					insertPs.setBigDecimal(6, record.getWorkingHours());
					insertPs.setString(7, record.getStatus());
					insertPs.setString(8, record.getImportBatchId());
					insertPs.addBatch();
					count++;
				}
				insertPs.executeBatch();
			}

			conn.commit();
		} catch (SQLException e) {
			System.err.println("Error batch upserting attendance records: " + e.getMessage());
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ignored) {
				}
			}
			return 0;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException ignored) {
				}
			}
		}

		return count;
	}

	public List<AttendanceRecord> searchByMonth(int year, int month, Long departmentId, int offset, int limit) {
		List<AttendanceRecord> records = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT ar.id, ar.user_id, ar.date, ar.shift_id, ar.check_in, ar.check_out,
				       ar.working_hours, ar.status, ar.import_batch_id, ar.created_at, ar.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       s.name AS shift_name, s.start_time AS shift_start_time, s.end_time AS shift_end_time
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN shifts s ON ar.shift_id = s.id
				WHERE YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				""");
		List<Object> params = new ArrayList<>();
		params.add(year);
		params.add(month);

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY ar.date DESC, u.full_name LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					records.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error searching attendance records: " + e.getMessage());
		}

		return records;
	}

	public int countRecordsByMonth(int year, int month, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				WHERE YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				""");
		List<Object> params = new ArrayList<>();
		params.add(year);
		params.add(month);

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
			System.err.println("Error counting attendance records: " + e.getMessage());
		}

		return 0;
	}

	public List<AttendanceRecord> searchByUserAndMonth(Long userId, int year, int month) {
		List<AttendanceRecord> records = new ArrayList<>();
		String sql = """
				SELECT ar.id, ar.user_id, ar.date, ar.shift_id, ar.check_in, ar.check_out,
				       ar.working_hours, ar.status, ar.import_batch_id, ar.created_at, ar.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       s.name AS shift_name, s.start_time AS shift_start_time, s.end_time AS shift_end_time
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN shifts s ON ar.shift_id = s.id
				WHERE ar.user_id = ? AND YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				ORDER BY ar.date DESC
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					records.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error searching attendance by user: " + e.getMessage());
		}

		return records;
	}

	public AttendanceRecord getById(Long id) {
		String sql = """
				SELECT ar.id, ar.user_id, ar.date, ar.shift_id, ar.check_in, ar.check_out,
				       ar.working_hours, ar.status, ar.import_batch_id, ar.created_at, ar.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       s.name AS shift_name, s.start_time AS shift_start_time, s.end_time AS shift_end_time
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN shifts s ON ar.shift_id = s.id
				WHERE ar.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting attendance record: " + e.getMessage());
		}

		return null;
	}

	public AttendanceRecord getByUserAndDate(Long userId, java.sql.Date date) {
		String sql = """
				SELECT ar.id, ar.user_id, ar.date, ar.shift_id, ar.check_in, ar.check_out,
				       ar.working_hours, ar.status, ar.import_batch_id, ar.created_at, ar.updated_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       s.name AS shift_name, s.start_time AS shift_start_time, s.end_time AS shift_end_time
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN shifts s ON ar.shift_id = s.id
				WHERE ar.user_id = ? AND ar.date = ?
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
			System.err.println("Error getting attendance by user and date: " + e.getMessage());
		}

		return null;
	}

	public boolean updateAfterCorrection(Connection conn, Long recordId, Time newCheckIn, Time newCheckOut,
			BigDecimal workingHours) throws SQLException {
		String sql = """
				UPDATE attendance_records
				SET check_in = ?, check_out = ?, working_hours = ?, updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setTime(1, newCheckIn);
			ps.setTime(2, newCheckOut);
			ps.setBigDecimal(3, workingHours);
			ps.setLong(4, recordId);
			int affected = ps.executeUpdate();
			return affected > 0;
		}
	}

	public String getActiveBatchId() {
		String sql = "SELECT MAX(import_batch_id) FROM attendance_records";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			System.err.println("Error getting active batch id: " + e.getMessage());
		}

		return null;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
		AttendanceRecord ar = new AttendanceRecord();
		ar.setId(rs.getLong("id"));
		ar.setUserId(rs.getLong("user_id"));
		ar.setDate(rs.getDate("date"));
		ar.setShiftId(rs.getObject("shift_id") != null ? rs.getLong("shift_id") : null);
		ar.setCheckIn(rs.getTime("check_in"));
		ar.setCheckOut(rs.getTime("check_out"));
		ar.setWorkingHours(rs.getBigDecimal("working_hours"));
		ar.setStatus(rs.getString("status"));
		ar.setImportBatchId(rs.getString("import_batch_id"));
		ar.setCreatedAt(rs.getTimestamp("created_at"));
		ar.setUpdatedAt(rs.getTimestamp("updated_at"));
		ar.setUserFullName(rs.getString("user_full_name"));
		ar.setEmployeeCode(rs.getString("employee_code"));
		ar.setDepartmentName(rs.getString("department_name"));
		ar.setShiftName(rs.getString("shift_name"));

		Time startTime = rs.getTime("shift_start_time");
		ar.setShiftStartTime(startTime != null ? startTime.toString() : null);

		Time endTime = rs.getTime("shift_end_time");
		ar.setShiftEndTime(endTime != null ? endTime.toString() : null);

		return ar;
	}
}
