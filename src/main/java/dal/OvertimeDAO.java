package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.OvertimeRecord;

public class OvertimeDAO {

	private static final String SELECT_BASE = """
			SELECT ot.id, ot.user_id, ot.date, ot.requested_hours, ot.approved_hours,
			       ot.reason, ot.status, ot.approver_id, ot.approved_at,
			       ot.created_at, ot.updated_at,
			       u.employee_code, u.full_name AS employee_name,
			       approver.full_name AS approver_name
			FROM overtime_records ot
			JOIN users u ON ot.user_id = u.id
			LEFT JOIN users approver ON ot.approver_id = approver.id
			""";

	public boolean insert(OvertimeRecord record) {
		if (record == null || record.getUserId() == null || record.getDate() == null
				|| record.getRequestedHours() == null) {
			return false;
		}

		String sql = """
				INSERT INTO overtime_records
				    (user_id, date, requested_hours, reason, status)
				VALUES (?, ?, ?, ?, 'PENDING')
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, record.getUserId());
			ps.setDate(2, record.getDate());
			ps.setBigDecimal(3, record.getRequestedHours());
			ps.setString(4, record.getReason());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public List<OvertimeRecord> search(String status, int offset, int limit) {
		List<OvertimeRecord> list = new ArrayList<>();
		StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
		List<Object> params = new ArrayList<>();

		if (status != null && !status.isBlank()) {
			sql.append(" AND ot.status = ?");
			params.add(status.trim().toUpperCase());
		}

		sql.append(" ORDER BY ot.created_at DESC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.search() ERROR: " + e.getMessage());
		}

		return list;
	}

	public int count(String status) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM overtime_records WHERE 1=1");
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
			System.err.println("OvertimeDAO.count() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public OvertimeRecord getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = SELECT_BASE + " WHERE ot.id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean hasPendingByUserAndDate(Long userId, java.sql.Date date) {
		if (userId == null || date == null) {
			return false;
		}

		String sql = """
				SELECT COUNT(*) FROM overtime_records
				WHERE user_id = ? AND date = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.hasPendingByUserAndDate() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean approve(Long id, Long approverId, BigDecimal approvedHours) {
		if (id == null || approverId == null || approvedHours == null) {
			return false;
		}

		String sql = """
				UPDATE overtime_records
				SET status       = 'APPROVED',
				    approved_hours = ?,
				    approver_id  = ?,
				    approved_at  = CURRENT_TIMESTAMP,
				    updated_at   = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBigDecimal(1, approvedHours);
			ps.setLong(2, approverId);
			ps.setLong(3, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.approve() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean reject(Long id, Long approverId) {
		if (id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE overtime_records
				SET status      = 'REJECTED',
				    approver_id = ?,
				    updated_at  = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.reject() ERROR: " + e.getMessage());
		}

		return false;
	}

	public OvertimeRecord findApprovedOTForUserAndDate(Long userId, java.sql.Date date) {
		if (userId == null || date == null) {
			return null;
		}
		String sql = SELECT_BASE + """
				WHERE ot.user_id = ? AND ot.date = ? AND ot.status = 'APPROVED'
				LIMIT 1
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
			System.err.println("OvertimeDAO.findApprovedOTForUserAndDate() ERROR: " + e.getMessage());
		}
		return null;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private OvertimeRecord mapRow(ResultSet rs) throws SQLException {
		OvertimeRecord r = new OvertimeRecord();
		r.setId(rs.getLong("id"));
		r.setUserId(rs.getLong("user_id"));
		r.setRequesterId(rs.getLong("user_id")); // same as userId, used for self-approval guard
		r.setDate(rs.getDate("date"));
		r.setRequestedHours(rs.getBigDecimal("requested_hours"));
		r.setApprovedHours(rs.getBigDecimal("approved_hours")); // null if not yet approved
		r.setReason(rs.getString("reason"));
		r.setStatus(rs.getString("status"));
		long approverId = rs.getLong("approver_id");
		if (!rs.wasNull()) {
			r.setApproverId(approverId);
		}
		r.setApprovedAt(rs.getTimestamp("approved_at"));
		r.setCreatedAt(rs.getTimestamp("created_at"));
		r.setUpdatedAt(rs.getTimestamp("updated_at"));
		r.setEmployeeCode(rs.getString("employee_code"));
		r.setEmployeeName(rs.getString("employee_name"));
		r.setApproverName(rs.getString("approver_name"));
		return r;
	}
}