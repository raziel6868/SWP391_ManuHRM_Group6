package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.AttendanceRecord;
import model.Shift;

public class AttendanceDAO {

	public boolean batchUpsertByMonth(int year, int month, List<AttendanceRecord> records) {
		if (records == null) {
			return false;
		}

		String deleteSql = """
				DELETE FROM attendance_records
				WHERE YEAR(date) = ? AND MONTH(date) = ?
				""";
		String insertSql = """
				INSERT INTO attendance_records
				    (user_id, date, shift_id, check_in, check_out, working_hours, status, import_batch_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";

		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			if (conn == null) {
				return false;
			}
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
				}
				insertPs.executeBatch();
			}

			conn.commit();
			return true;
		} catch (SQLException e) {
			rollback(conn);
			System.err.println("AttendanceDAO.batchUpsertByMonth() ERROR: " + e.getMessage());
		} finally {
			close(conn);
		}

		return false;
	}

	public List<AttendanceRecord> searchByMonth(int year, int month, Long departmentId, int offset, int limit) {
		List<AttendanceRecord> records = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT ar.id, ar.user_id, u.employee_code, u.full_name AS employee_name,
				       ar.date, ar.shift_id, s.name AS shift_name,
				       ar.check_in, ar.check_out, ar.working_hours, ar.status,
				       ar.import_batch_id, ar.created_at, ar.updated_at
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
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

		sql.append(" ORDER BY ar.date DESC, u.employee_code ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					records.add(mapRecord(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.searchByMonth() ERROR: " + e.getMessage());
		}

		return records;
	}

	public int countByMonth(int year, int month, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM attendance_records ar
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
			System.err.println("AttendanceDAO.countByMonth() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public List<AttendanceRecord> searchByUserAndMonth(Long userId, int year, int month) {
		List<AttendanceRecord> records = new ArrayList<>();
		if (userId == null) {
			return records;
		}

		String sql = """
				SELECT ar.id, ar.user_id, u.employee_code, u.full_name AS employee_name,
				       ar.date, ar.shift_id, s.name AS shift_name,
				       ar.check_in, ar.check_out, ar.working_hours, ar.status,
				       ar.import_batch_id, ar.created_at, ar.updated_at
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
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
					records.add(mapRecord(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.searchByUserAndMonth() ERROR: " + e.getMessage());
		}

		return records;
	}

	public AttendanceRecord getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT ar.id, ar.user_id, u.employee_code, u.full_name AS employee_name,
				       ar.date, ar.shift_id, s.name AS shift_name,
				       ar.check_in, ar.check_out, ar.working_hours, ar.status,
				       ar.import_batch_id, ar.created_at, ar.updated_at
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN shifts s ON ar.shift_id = s.id
				WHERE ar.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRecord(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean updateAfterCorrection(Connection conn, Long recordId, Time newCheckIn, Time newCheckOut)
			throws SQLException {
		if (conn == null || recordId == null || newCheckIn == null || newCheckOut == null) {
			return false;
		}

		String sql = """
				UPDATE attendance_records ar
				LEFT JOIN shifts s ON ar.shift_id = s.id
				SET ar.check_in = ?,
				    ar.check_out = ?,
				    ar.working_hours = ROUND(
				        GREATEST(
				            0,
				            (TIME_TO_SEC(TIMEDIFF(?, ?)) / 60) - COALESCE(s.break_minutes, 0)
				        ) / 60,
				        2
				    ),
				    ar.status = CASE
				        WHEN ? > ADDTIME(COALESCE(s.start_time, ?), '00:15:00') THEN 'LATE'
				        ELSE 'NORMAL'
				    END
				WHERE ar.id = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setTime(1, newCheckIn);
			ps.setTime(2, newCheckOut);
			ps.setTime(3, newCheckOut);
			ps.setTime(4, newCheckIn);
			ps.setTime(5, newCheckIn);
			ps.setTime(6, newCheckIn);
			ps.setLong(7, recordId);
			return ps.executeUpdate() > 0;
		}
	}

	public Long findActiveUserIdByEmployeeCode(String employeeCode) {
		if (employeeCode == null || employeeCode.isBlank()) {
			return null;
		}

		String sql = "SELECT id FROM users WHERE employee_code = ? AND is_active = TRUE LIMIT 1";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeCode.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong("id");
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.findActiveUserIdByEmployeeCode() ERROR: " + e.getMessage());
		}

		return null;
	}

	public Shift findShiftForUserAndDate(Long userId, Date date) {
		if (userId == null || date == null) {
			return null;
		}

		String sql = """
				SELECT s.id, s.code, s.name, s.start_time, s.end_time, s.break_minutes, s.is_night_shift,
				       s.is_active, s.created_at, s.updated_at
				FROM shift_assignments sa
				JOIN shifts s ON sa.shift_id = s.id
				WHERE sa.user_id = ? AND sa.date = ?
				LIMIT 1
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapShift(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.findShiftForUserAndDate() ERROR: " + e.getMessage());
		}

		return findDefaultShift();
	}

	public Shift findDefaultShift() {
		String sql = """
				SELECT id, code, name, start_time, end_time, break_minutes, is_night_shift,
				       is_active, created_at, updated_at
				FROM shifts
				WHERE is_active = TRUE
				ORDER BY is_night_shift ASC, id ASC
				LIMIT 1
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapShift(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.findDefaultShift() ERROR: " + e.getMessage());
		}

		return null;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private AttendanceRecord mapRecord(ResultSet rs) throws SQLException {
		AttendanceRecord record = new AttendanceRecord();
		record.setId(rs.getLong("id"));
		record.setUserId(rs.getLong("user_id"));
		record.setEmployeeCode(rs.getString("employee_code"));
		record.setEmployeeName(rs.getString("employee_name"));
		record.setDate(rs.getDate("date"));
		long shiftId = rs.getLong("shift_id");
		if (!rs.wasNull()) {
			record.setShiftId(shiftId);
		}
		record.setShiftName(rs.getString("shift_name"));
		record.setCheckIn(rs.getTime("check_in"));
		record.setCheckOut(rs.getTime("check_out"));
		record.setWorkingHours(rs.getBigDecimal("working_hours"));
		record.setStatus(rs.getString("status"));
		record.setImportBatchId(rs.getString("import_batch_id"));
		record.setCreatedAt(rs.getTimestamp("created_at"));
		record.setUpdatedAt(rs.getTimestamp("updated_at"));
		return record;
	}

	private Shift mapShift(ResultSet rs) throws SQLException {
		Shift shift = new Shift();
		shift.setId(rs.getLong("id"));
		shift.setCode(rs.getString("code"));
		shift.setName(rs.getString("name"));
		shift.setStartTime(rs.getTime("start_time"));
		shift.setEndTime(rs.getTime("end_time"));
		shift.setBreakMinutes(rs.getInt("break_minutes"));
		shift.setIsNightShift(rs.getBoolean("is_night_shift"));
		shift.setIsActive(rs.getBoolean("is_active"));
		shift.setCreatedAt(rs.getTimestamp("created_at"));
		shift.setUpdatedAt(rs.getTimestamp("updated_at"));
		return shift;
	}

	private void rollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				System.err.println("AttendanceDAO.rollback() ERROR: " + e.getMessage());
			}
		}
	}

	private void close(Connection conn) {
		if (conn != null) {
			try {
				conn.setAutoCommit(true);
				conn.close();
			} catch (SQLException e) {
				System.err.println("AttendanceDAO.close() ERROR: " + e.getMessage());
			}
		}
	}
}
