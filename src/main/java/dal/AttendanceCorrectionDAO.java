package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.AttendanceCorrection;

public class AttendanceCorrectionDAO {

	public List<AttendanceCorrection> searchCorrections(String status, Long departmentId, int offset, int limit) {
		List<AttendanceCorrection> corrections = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT ac.id, ac.attendance_record_id, ac.requested_by, ac.new_check_in, ac.new_check_out,
				       ac.reason, ac.status, ac.approver_id, ac.approved_at, ac.created_at,
				       u1.full_name AS requested_by_name,
				       u2.full_name AS approver_name,
				       ar.date AS attendance_date,
				       u3.full_name AS attendance_user_name
				FROM attendance_corrections ac
				JOIN users u1 ON ac.requested_by = u1.id
				LEFT JOIN users u2 ON ac.approver_id = u2.id
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				JOIN users u3 ON ar.user_id = u3.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isEmpty()) {
			sql.append(" AND ac.status = ?");
			params.add(status);
		}

		if (departmentId != null) {
			sql.append(" AND u3.department_id = ?");
			params.add(departmentId);
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
			System.err.println("Error searching attendance corrections: " + e.getMessage());
		}

		return corrections;
	}

	public int countCorrections(String status, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM attendance_corrections ac
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				JOIN users u ON ar.user_id = u.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isEmpty()) {
			sql.append(" AND ac.status = ?");
			params.add(status);
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
			System.err.println("Error counting attendance corrections: " + e.getMessage());
		}

		return 0;
	}

	public AttendanceCorrection getById(Long id) {
		String sql = """
				SELECT ac.id, ac.attendance_record_id, ac.requested_by, ac.new_check_in, ac.new_check_out,
				       ac.reason, ac.status, ac.approver_id, ac.approved_at, ac.created_at,
				       u1.full_name AS requested_by_name,
				       u2.full_name AS approver_name,
				       ar.date AS attendance_date,
				       u3.full_name AS attendance_user_name
				FROM attendance_corrections ac
				JOIN users u1 ON ac.requested_by = u1.id
				LEFT JOIN users u2 ON ac.approver_id = u2.id
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				JOIN users u3 ON ar.user_id = u3.id
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
			System.err.println("Error getting attendance correction: " + e.getMessage());
		}

		return null;
	}

	public boolean insert(AttendanceCorrection correction) {
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
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error inserting attendance correction: " + e.getMessage());
			return false;
		}
	}

	public boolean hasPendingCorrection(Long attendanceRecordId) {
		String sql = "SELECT COUNT(*) FROM attendance_corrections WHERE attendance_record_id = ? AND status = 'PENDING'";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, attendanceRecordId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error checking pending correction: " + e.getMessage());
		}

		return false;
	}

	public boolean approve(Connection conn, Long id, Long approverId) throws SQLException {
		String sql = """
				UPDATE attendance_corrections
				SET status = 'APPROVED', approver_id = ?, approved_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			int affected = ps.executeUpdate();
			return affected > 0;
		}
	}

	public boolean reject(Connection conn, Long id, Long approverId) throws SQLException {
		String sql = """
				UPDATE attendance_corrections
				SET status = 'REJECTED', approver_id = ?, approved_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			int affected = ps.executeUpdate();
			return affected > 0;
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private AttendanceCorrection mapRow(ResultSet rs) throws SQLException {
		AttendanceCorrection ac = new AttendanceCorrection();
		ac.setId(rs.getLong("id"));
		ac.setAttendanceRecordId(rs.getLong("attendance_record_id"));
		ac.setRequestedBy(rs.getLong("requested_by"));
		ac.setNewCheckIn(rs.getTime("new_check_in"));
		ac.setNewCheckOut(rs.getTime("new_check_out"));
		ac.setReason(rs.getString("reason"));
		ac.setStatus(rs.getString("status"));
		ac.setApproverId(rs.getObject("approver_id") != null ? rs.getLong("approver_id") : null);
		ac.setApprovedAt(rs.getTimestamp("approved_at"));
		ac.setCreatedAt(rs.getTimestamp("created_at"));
		ac.setRequestedByName(rs.getString("requested_by_name"));
		ac.setApproverName(rs.getString("approver_name"));
		ac.setAttendanceUserName(rs.getString("attendance_user_name"));

		java.sql.Date attDate = rs.getDate("attendance_date");
		ac.setAttendanceDate(attDate != null ? attDate.toString() : null);

		return ac;
	}
}
