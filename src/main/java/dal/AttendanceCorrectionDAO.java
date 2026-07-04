package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.AttendanceCorrection;

public class AttendanceCorrectionDAO {

	private static final String SELECT_BASE = """
			SELECT ac.id, ac.attendance_record_id, ac.requested_by,
			       ac.new_check_in, ac.new_check_out, ac.reason,
			       ac.supervisor_id, ac.supervisor_status, ac.supervisor_approved_at,
			       ac.supervisor_reject_reason,
			       ac.status, ac.approver_id, ac.hr_reject_reason,
			       ac.created_at, ac.updated_at,
			       ar.user_id AS attendance_user_id, ar.date AS attendance_date,
			       ar.check_in AS current_check_in, ar.check_out AS current_check_out,
			       u.employee_code, u.full_name AS employee_name,
			       requester.full_name AS requester_name,
			       supervisor.full_name AS supervisor_name,
			       approver.full_name AS approver_name
			FROM attendance_corrections ac
			JOIN attendance_records ar ON ac.attendance_record_id = ar.id
			JOIN users u ON ar.user_id = u.id
			JOIN users requester ON ac.requested_by = requester.id
			LEFT JOIN users supervisor ON ac.supervisor_id = supervisor.id
			LEFT JOIN users approver ON ac.approver_id = approver.id
			""";

	// ── Insert ──────────────────────────────────────────────────────────────────

	/**
	 * Employee tạo request — supervisor_id được tự động gán từ manager_id của
	 * employee.
	 */
	public boolean insert(AttendanceCorrection correction) {
		if (correction == null || correction.getAttendanceRecordId() == null || correction.getRequestedBy() == null) {
			return false;
		}
		String sql = """
				INSERT INTO attendance_corrections
				    (attendance_record_id, requested_by, new_check_in, new_check_out, reason,
				     supervisor_id, supervisor_status, status)
				VALUES (?, ?, ?, ?, ?, ?, 'PENDING', 'PENDING')
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, correction.getAttendanceRecordId());
			ps.setLong(2, correction.getRequestedBy());
			ps.setTime(3, correction.getNewCheckIn());
			ps.setTime(4, correction.getNewCheckOut());
			ps.setString(5, correction.getReason());
			if (correction.getSupervisorId() != null) {
				ps.setLong(6, correction.getSupervisorId());
			} else {
				ps.setNull(6, java.sql.Types.BIGINT);
			}
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.insert() ERROR: " + e.getMessage());
		}
		return false;
	}

	// ── Query ───────────────────────────────────────────────────────────────────

	public AttendanceCorrection getById(Long id) {
		if (id == null)
			return null;
		String sql = SELECT_BASE + " WHERE ac.id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean hasPendingByRecordId(Long attendanceRecordId) {
		if (attendanceRecordId == null)
			return false;
		String sql = """
				SELECT COUNT(*) FROM attendance_corrections
				WHERE attendance_record_id = ?
				  AND (
				      supervisor_status = 'PENDING'
				      OR (supervisor_status = 'APPROVED' AND status = 'PENDING')
				  )
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, attendanceRecordId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.hasPendingByRecordId() ERROR: " + e.getMessage());
		}
		return false;
	}

	/**
	 * HR xem tất cả — filter theo status HR (bước 2), chỉ hiện những request đã qua
	 * bước supervisor (supervisor_status = 'APPROVED').
	 */
	public List<AttendanceCorrection> search(String status, int offset, int limit) {
		List<AttendanceCorrection> list = new ArrayList<>();
		StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE ac.supervisor_status = 'APPROVED'");
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
				while (rs.next())
					list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.search() ERROR: " + e.getMessage());
		}
		return list;
	}

	public int count(String status) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM attendance_corrections
				WHERE supervisor_status = 'APPROVED'
				""");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isBlank()) {
			sql.append(" AND status = ?");
			params.add(status.trim().toUpperCase());
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.count() ERROR: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Quản đốc xem corrections của cấp dưới (theo manager_id). Chỉ hiện những
	 * request chưa qua bước supervisor (supervisor_status = 'PENDING').
	 */
	public List<AttendanceCorrection> searchBySupervisor(Long supervisorId, String supervisorStatus, int offset,
			int limit) {
		List<AttendanceCorrection> list = new ArrayList<>();
		StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE ac.supervisor_id = ?");
		List<Object> params = new ArrayList<>();
		params.add(supervisorId);

		if (supervisorStatus != null && !supervisorStatus.isBlank()) {
			sql.append(" AND ac.supervisor_status = ?");
			params.add(supervisorStatus.trim().toUpperCase());
		}

		sql.append(" ORDER BY ac.created_at DESC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.searchBySupervisor() ERROR: " + e.getMessage());
		}
		return list;
	}

	public int countBySupervisor(Long supervisorId, String supervisorStatus) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM attendance_corrections WHERE supervisor_id = ?
				""");
		List<Object> params = new ArrayList<>();
		params.add(supervisorId);

		if (supervisorStatus != null && !supervisorStatus.isBlank()) {
			sql.append(" AND supervisor_status = ?");
			params.add(supervisorStatus.trim().toUpperCase());
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.countBySupervisor() ERROR: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Employee xem request của chính mình.
	 */
	public List<AttendanceCorrection> searchByEmployee(Long userId, int offset, int limit) {
		List<AttendanceCorrection> list = new ArrayList<>();
		String sql = SELECT_BASE + """
				WHERE ac.requested_by = ?
				ORDER BY ac.created_at DESC LIMIT ? OFFSET ?
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, limit);
			ps.setInt(3, offset);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.searchByEmployee() ERROR: " + e.getMessage());
		}
		return list;
	}

	/**
	 * Kiểm tra còn correction PENDING của cấp dưới supervisor trong tháng không.
	 * Dùng để chặn quản đốc bấm chốt khi chưa duyệt hết.
	 */
	public boolean hasPendingSupervisorCorrectionInMonth(Long supervisorId, int year, int month) {
		String sql = """
				SELECT COUNT(*) FROM attendance_corrections ac
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				WHERE ac.supervisor_id = ?
				  AND ac.supervisor_status = 'PENDING'
				  AND YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, supervisorId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println(
					"AttendanceCorrectionDAO.hasPendingSupervisorCorrectionInMonth() ERROR: " + e.getMessage());
		}
		return false;
	}

	// ── Supervisor approve/reject (bước 1) ──────────────────────────────────────

	public boolean supervisorApprove(Long id, Long supervisorId) {
		String sql = """
				UPDATE attendance_corrections
				SET supervisor_status = 'APPROVED',
				    supervisor_id = ?,
				    supervisor_approved_at = NOW(),
				    updated_at = NOW()
				WHERE id = ? AND supervisor_status = 'PENDING'
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, supervisorId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.supervisorApprove() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean supervisorReject(Long id, Long supervisorId, String rejectReason) {
		String sql = """
				UPDATE attendance_corrections
				SET supervisor_status = 'REJECTED',
				    status = 'REJECTED',
				    supervisor_id = ?,
				    supervisor_reject_reason = ?,
				    updated_at = NOW()
				WHERE id = ? AND supervisor_status = 'PENDING'
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, supervisorId);
			ps.setString(2, rejectReason);
			ps.setLong(3, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.supervisorReject() ERROR: " + e.getMessage());
		}
		return false;
	}

	// ── HR approve/reject (bước 2) ───────────────────────────────────────────────

	public boolean approve(Connection conn, Long id, Long approverId) throws SQLException {
		String sql = """
				UPDATE attendance_corrections
				SET status = 'APPROVED',
				    approver_id = ?,
				    updated_at = NOW()
				WHERE id = ? AND supervisor_status = 'APPROVED' AND status = 'PENDING'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean reject(Long id, Long approverId, String rejectReason) {
		String sql = """
				UPDATE attendance_corrections
				SET status = 'REJECTED',
				    approver_id = ?,
				    hr_reject_reason = ?,
				    updated_at = NOW()
				WHERE id = ? AND supervisor_status = 'APPROVED' AND status = 'PENDING'
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setString(2, rejectReason);
			ps.setLong(3, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AttendanceCorrectionDAO.reject() ERROR: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Khi Giám đốc/HR đóng sổ: tự động reject tất cả corrections PENDING của tháng
	 * đó.
	 */
	public int rejectAllPendingInMonth(Connection conn, int year, int month, Long approverId, String reason)
			throws SQLException {
		String sql = """
				UPDATE attendance_corrections ac
				JOIN attendance_records ar ON ac.attendance_record_id = ar.id
				SET ac.supervisor_status = CASE
				        WHEN ac.supervisor_status = 'PENDING' THEN 'REJECTED'
				        ELSE ac.supervisor_status
				    END,
				    ac.status = 'REJECTED',
				    ac.approver_id = ?,
				    ac.hr_reject_reason = ?,
				    ac.updated_at = NOW()
				WHERE YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				  AND (ac.supervisor_status = 'PENDING' OR ac.status = 'PENDING')
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setString(2, reason);
			ps.setInt(3, year);
			ps.setInt(4, month);
			return ps.executeUpdate();
		}
	}

	// ── Helper ───────────────────────────────────────────────────────────────────

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private AttendanceCorrection mapRow(ResultSet rs) throws SQLException {
		AttendanceCorrection c = new AttendanceCorrection();
		c.setId(rs.getLong("id"));
		c.setAttendanceRecordId(rs.getLong("attendance_record_id"));
		c.setRequestedBy(rs.getLong("requested_by"));
		c.setNewCheckIn(rs.getTime("new_check_in"));
		c.setNewCheckOut(rs.getTime("new_check_out"));
		c.setReason(rs.getString("reason"));

		// Bước 1 — supervisor
		long supId = rs.getLong("supervisor_id");
		if (!rs.wasNull())
			c.setSupervisorId(supId);
		c.setSupervisorStatus(rs.getString("supervisor_status"));
		c.setSupervisorApprovedAt(rs.getTimestamp("supervisor_approved_at"));
		c.setSupervisorRejectReason(rs.getString("supervisor_reject_reason"));
		c.setSupervisorName(rs.getString("supervisor_name"));

		// Bước 2 — HR
		c.setStatus(rs.getString("status"));
		long appId = rs.getLong("approver_id");
		if (!rs.wasNull())
			c.setApproverId(appId);
		c.setHrRejectReason(rs.getString("hr_reject_reason"));
		c.setApproverName(rs.getString("approver_name"));

		c.setCreatedAt(rs.getTimestamp("created_at"));
		c.setUpdatedAt(rs.getTimestamp("updated_at"));

		// Join fields
		c.setAttendanceUserId(rs.getLong("attendance_user_id"));
		c.setAttendanceDate(rs.getDate("attendance_date"));
		c.setCurrentCheckIn(rs.getTime("current_check_in"));
		c.setCurrentCheckOut(rs.getTime("current_check_out"));
		c.setEmployeeCode(rs.getString("employee_code"));
		c.setEmployeeName(rs.getString("employee_name"));
		c.setRequesterName(rs.getString("requester_name"));
		return c;
	}
}
