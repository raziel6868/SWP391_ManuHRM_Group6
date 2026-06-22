package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.AttendanceCorrection;

public class AttendanceCorrectionDAO {

	public boolean insert(AttendanceCorrection correction) {
		if (correction == null || correction.getAttendanceRecordId() == null || correction.getRequestedBy() == null
				|| correction.getNewCheckIn() == null || correction.getNewCheckOut() == null) {
			return false;
		}

		String sql = """
				INSERT INTO attendance_corrections
				    (attendance_record_id, requested_by, new_check_in, new_check_out, reason, status)
				VALUES (?, ?, ?, ?, ?, 'PENDING')
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, correction.getAttendanceRecordId());
			ps.setLong(2, correction.getRequestedBy());
			ps.setTime(3, correction.getNewCheckIn());
			ps.setTime(4, correction.getNewCheckOut());
			ps.setString(5, correction.getReason());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean hasPendingByRecordId(Long attendanceRecordId) {
		if (attendanceRecordId == null) {
			return false;
		}

		String sql = """
				SELECT COUNT(*)
				FROM attendance_corrections
				WHERE attendance_record_id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, attendanceRecordId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.hasPendingByRecordId() ERROR: " + e.getMessage());
		}

		return false;
	}

	public List<AttendanceCorrection> search(String status, int offset, int limit) {
		List<AttendanceCorrection> corrections = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT ac.id, ac.attendance_record_id, ac.requested_by, ac.new_check_in, ac.new_check_out,
				       ac.reason, ac.status, ac.approver_id, ac.created_at, ac.updated_at,
				       ar.user_id AS attendance_user_id, ar.date AS attendance_date,
				       ar.check_in AS current_check_in, ar.check_out AS current_check_out,
				       u.employee_code, u.full_name AS employee_name,
				       requester.full_name AS requester_name,
				       approver.full_name AS approver_name
				FROM attendance_corrections ac
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				JOIN users u ON ar.user_id = u.id
				JOIN users requester ON ac.requested_by = requester.id
				LEFT JOIN users approver ON ac.approver_id = approver.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isBlank()) {
			sql.append(" AND ac.status = ?");
			params.add(status.trim().toUpperCase());
		}

		sql.append(" ORDER BY ac.created_at DESC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					corrections.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.search() ERROR: " + e.getMessage());
		}

		return corrections;
	}

	public int count(String status) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM attendance_corrections WHERE 1 = 1");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isBlank()) {
			sql.append(" AND status = ?");
			params.add(status.trim().toUpperCase());
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
			System.err.println("AttendanceCorrectionDAO.count() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public AttendanceCorrection getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT ac.id, ac.attendance_record_id, ac.requested_by, ac.new_check_in, ac.new_check_out,
				       ac.reason, ac.status, ac.approver_id, ac.created_at, ac.updated_at,
				       ar.user_id AS attendance_user_id, ar.date AS attendance_date,
				       ar.check_in AS current_check_in, ar.check_out AS current_check_out,
				       u.employee_code, u.full_name AS employee_name,
				       requester.full_name AS requester_name,
				       approver.full_name AS approver_name
				FROM attendance_corrections ac
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				JOIN users u ON ar.user_id = u.id
				JOIN users requester ON ac.requested_by = requester.id
				LEFT JOIN users approver ON ac.approver_id = approver.id
				WHERE ac.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean approve(Connection conn, Long id, Long approverId) throws SQLException {
		if (conn == null || id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE attendance_corrections
				SET status = 'APPROVED',
				    approver_id = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean reject(Long id, Long approverId) {
		if (id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE attendance_corrections
				SET status = 'REJECTED',
				    approver_id = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.reject() ERROR: " + e.getMessage());
		}

		return false;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private AttendanceCorrection mapRow(ResultSet rs) throws SQLException {
		AttendanceCorrection correction = new AttendanceCorrection();
		correction.setId(rs.getLong("id"));
		correction.setAttendanceRecordId(rs.getLong("attendance_record_id"));
		correction.setRequestedBy(rs.getLong("requested_by"));
		correction.setNewCheckIn(rs.getTime("new_check_in"));
		correction.setNewCheckOut(rs.getTime("new_check_out"));
		correction.setReason(rs.getString("reason"));
		correction.setStatus(rs.getString("status"));
		long approverId = rs.getLong("approver_id");
		if (!rs.wasNull()) {
			correction.setApproverId(approverId);
		}
		correction.setCreatedAt(rs.getTimestamp("created_at"));
		correction.setUpdatedAt(rs.getTimestamp("updated_at"));
		correction.setAttendanceUserId(rs.getLong("attendance_user_id"));
		correction.setAttendanceDate(rs.getDate("attendance_date"));
		correction.setCurrentCheckIn(rs.getTime("current_check_in"));
		correction.setCurrentCheckOut(rs.getTime("current_check_out"));
		correction.setEmployeeCode(rs.getString("employee_code"));
		correction.setEmployeeName(rs.getString("employee_name"));
		correction.setRequesterName(rs.getString("requester_name"));
		correction.setApproverName(rs.getString("approver_name"));
		return correction;
	}
}
