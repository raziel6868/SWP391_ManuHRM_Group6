package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.OvertimeRecord;

public class OvertimeDAO {

	public List<OvertimeRecord> searchOvertime(String status, Long departmentId, int offset, int limit) {
		List<OvertimeRecord> records = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT ot.id, ot.user_id, ot.date, ot.requested_hours, ot.approved_hours, ot.reason,
				       ot.status, ot.approver_id, ot.approved_at, ot.created_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       u2.full_name AS approver_name
				FROM overtime_records ot
				JOIN users u ON ot.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN users u2 ON ot.approver_id = u2.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isEmpty()) {
			sql.append(" AND ot.status = ?");
			params.add(status);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY ot.created_at DESC LIMIT ? OFFSET ?");
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
			System.err.println("Error searching overtime records: " + e.getMessage());
		}

		return records;
	}

	public int countOvertime(String status, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM overtime_records ot
				JOIN users u ON ot.user_id = u.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isEmpty()) {
			sql.append(" AND ot.status = ?");
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
			System.err.println("Error counting overtime records: " + e.getMessage());
		}

		return 0;
	}

	public OvertimeRecord getById(Long id) {
		String sql = """
				SELECT ot.id, ot.user_id, ot.date, ot.requested_hours, ot.approved_hours, ot.reason,
				       ot.status, ot.approver_id, ot.approved_at, ot.created_at,
				       u.full_name AS user_full_name, u.employee_code, d.name AS department_name,
				       u2.full_name AS approver_name
				FROM overtime_records ot
				JOIN users u ON ot.user_id = u.id
				LEFT JOIN departments d ON u.department_id = d.id
				LEFT JOIN users u2 ON ot.approver_id = u2.id
				WHERE ot.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting overtime record: " + e.getMessage());
		}

		return null;
	}

	public boolean insert(OvertimeRecord record) {
		String sql = """
				INSERT INTO overtime_records (user_id, date, requested_hours, reason, status)
				VALUES (?, ?, ?, ?, 'PENDING')
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, record.getUserId());
			ps.setDate(2, record.getDate());
			ps.setBigDecimal(3, record.getRequestedHours());
			ps.setString(4, record.getReason());
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error inserting overtime record: " + e.getMessage());
			return false;
		}
	}

	public boolean approve(Long id, Long approverId, java.math.BigDecimal approvedHours) {
		String sql = """
				UPDATE overtime_records
				SET status = 'APPROVED', approver_id = ?, approved_hours = ?, approved_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setBigDecimal(2, approvedHours);
			ps.setLong(3, id);
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error approving overtime record: " + e.getMessage());
			return false;
		}
	}

	public boolean reject(Long id, Long approverId) {
		String sql = """
				UPDATE overtime_records
				SET status = 'REJECTED', approver_id = ?, approved_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error rejecting overtime record: " + e.getMessage());
			return false;
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private OvertimeRecord mapRow(ResultSet rs) throws SQLException {
		OvertimeRecord ot = new OvertimeRecord();
		ot.setId(rs.getLong("id"));
		ot.setUserId(rs.getLong("user_id"));
		ot.setDate(rs.getDate("date"));
		ot.setRequestedHours(rs.getBigDecimal("requested_hours"));
		ot.setApprovedHours(rs.getBigDecimal("approved_hours"));
		ot.setReason(rs.getString("reason"));
		ot.setStatus(rs.getString("status"));
		ot.setApproverId(rs.getObject("approver_id") != null ? rs.getLong("approver_id") : null);
		ot.setApprovedAt(rs.getTimestamp("approved_at"));
		ot.setCreatedAt(rs.getTimestamp("created_at"));
		ot.setUserFullName(rs.getString("user_full_name"));
		ot.setEmployeeCode(rs.getString("employee_code"));
		ot.setDepartmentName(rs.getString("department_name"));
		ot.setApproverName(rs.getString("approver_name"));
		return ot;
	}
}
