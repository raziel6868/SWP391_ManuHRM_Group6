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

	/**
	 * Tìm bản ghi chấm công đã có của 1 nhân viên vào 1 ngày cụ thể (nếu có). Dùng
	 * để import theo dòng độc lập: phát hiện trùng (giống hệt dữ liệu cũ) hay
	 * conflict (khác dữ liệu cũ) trước khi quyết định insert hay bỏ qua.
	 */
	public AttendanceRecord findByUserAndDate(Long userId, Date date) {
		if (userId == null || date == null) {
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
				WHERE ar.user_id = ? AND ar.date = ?
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRecord(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.findByUserAndDate() ERROR: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Kiểm tra nhân viên đã có BẤT KỲ bản ghi chấm công nào trong khoảng ngày
	 * [startDate, endDate] chưa. Dùng để validate ngược khi duyệt đơn nghỉ phép:
	 * chặn duyệt nếu nhân viên đã có chấm công thật trong những ngày xin nghỉ
	 * (tránh trạng thái mâu thuẫn: vừa có chấm công vừa có nghỉ phép được duyệt
	 * cùng ngày).
	 */
	public boolean hasAnyAttendanceInRange(Long userId, Date startDate, Date endDate) {
		if (userId == null || startDate == null || endDate == null) {
			return false;
		}
		String sql = """
				SELECT COUNT(*) FROM attendance_records
				WHERE user_id = ? AND date BETWEEN ? AND ?
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, startDate);
			ps.setDate(3, endDate);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.hasAnyAttendanceInRange() ERROR: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Insert 1 bản ghi chấm công đơn lẻ. Dùng cho import theo dòng độc lập (mỗi
	 * dòng hợp lệ insert ngay, không xóa/ghi đè dữ liệu cũ của cả tháng như
	 * batchUpsertByMonth trước đây).
	 */
	public boolean insert(AttendanceRecord record) {
		if (record == null || record.getUserId() == null || record.getDate() == null) {
			return false;
		}
		String sql = """
				INSERT INTO attendance_records
				    (user_id, date, shift_id, check_in, check_out, working_hours, status, import_batch_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, record.getUserId());
			ps.setDate(2, record.getDate());
			if (record.getShiftId() != null) {
				ps.setLong(3, record.getShiftId());
			} else {
				ps.setNull(3, java.sql.Types.BIGINT);
			}
			ps.setTime(4, record.getCheckIn());
			ps.setTime(5, record.getCheckOut());
			ps.setBigDecimal(6, record.getWorkingHours());
			ps.setString(7, record.getStatus());
			ps.setString(8, record.getImportBatchId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.insert() ERROR: " + e.getMessage());
		}
		return false;
	}

	public List<AttendanceRecord> searchByUserIdsAndMonth(List<Long> userIds, int year, int month) {
		List<AttendanceRecord> records = new ArrayList<>();
		if (userIds == null || userIds.isEmpty()) {
			return records;
		}

		StringBuilder sql = new StringBuilder("""
				SELECT ar.id, ar.user_id, u.employee_code, u.full_name AS employee_name,
				       ar.date, ar.shift_id, s.name AS shift_name,
				       ar.check_in, ar.check_out, ar.working_hours, ar.status,
				       ar.import_batch_id, ar.created_at, ar.updated_at
				FROM attendance_records ar
				JOIN users u ON ar.user_id = u.id
				LEFT JOIN shifts s ON ar.shift_id = s.id
				WHERE YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				  AND ar.user_id IN (
				""");
		appendPlaceholders(sql, userIds.size());
		sql.append(") ORDER BY u.employee_code ASC, ar.date DESC");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			for (int i = 0; i < userIds.size(); i++) {
				ps.setLong(i + 3, userIds.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					records.add(mapRecord(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceDAO.searchByUserIdsAndMonth() ERROR: " + e.getMessage());
		}

		return records;
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

	private void appendPlaceholders(StringBuilder sql, int count) {
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append("?");
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

}