package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.LeaveRequest;

public class LeaveRequestDAO {

	public List<LeaveRequest> searchRequests(String keyword, String status, Long departmentId, int offset, int limit) {
		List<LeaveRequest> requests = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT lr.id, lr.user_id, lr.leave_type_id, lr.start_date, lr.end_date, lr.days,
				       lr.reason, lr.status,
				       lr.level_1_approver_id, lr.level_1_approved_at,
				       lr.approver_id, lr.approved_at,
				       lr.created_at, lr.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       lt.name AS leave_type_name,
				       l1.full_name AS level_1_approver_name,
				       a.full_name AS approver_name
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				JOIN leave_types lt ON lr.leave_type_id = lt.id
				LEFT JOIN users l1 ON lr.level_1_approver_id = l1.id
				LEFT JOIN users a ON lr.approver_id = a.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (u.full_name LIKE ? OR u.employee_code LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (status != null && !status.trim().isEmpty()) {
			sql.append(" AND lr.status = ?");
			params.add(status.trim());
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY lr.id DESC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					requests.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.searchRequests() ERROR: " + e.getMessage());
		}

		return requests;
	}

	public int countRequests(String keyword, String status, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (u.full_name LIKE ? OR u.employee_code LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (status != null && !status.trim().isEmpty()) {
			sql.append(" AND lr.status = ?");
			params.add(status.trim());
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
			System.err.println("LeaveRequestDAO.countRequests() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public List<LeaveRequest> searchByUser(Long userId, int offset, int limit) {
		if (userId == null) {
			return new ArrayList<>();
		}

		List<LeaveRequest> requests = new ArrayList<>();
		String sql = """
				SELECT lr.id, lr.user_id, lr.leave_type_id, lr.start_date, lr.end_date, lr.days,
				       lr.reason, lr.status,
				       lr.level_1_approver_id, lr.level_1_approved_at,
				       lr.approver_id, lr.approved_at,
				       lr.created_at, lr.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       lt.name AS leave_type_name,
				       l1.full_name AS level_1_approver_name,
				       a.full_name AS approver_name
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				JOIN leave_types lt ON lr.leave_type_id = lt.id
				LEFT JOIN users l1 ON lr.level_1_approver_id = l1.id
				LEFT JOIN users a ON lr.approver_id = a.id
				WHERE lr.user_id = ?
				ORDER BY lr.id DESC
				LIMIT ? OFFSET ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, limit);
			ps.setInt(3, offset);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					requests.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.searchByUser() ERROR: " + e.getMessage());
		}

		return requests;
	}

	public LeaveRequest getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT lr.id, lr.user_id, lr.leave_type_id, lr.start_date, lr.end_date, lr.days,
				       lr.reason, lr.status,
				       lr.level_1_approver_id, lr.level_1_approved_at,
				       lr.approver_id, lr.approved_at,
				       lr.created_at, lr.updated_at,
				       u.full_name AS user_full_name, u.employee_code,
				       lt.name AS leave_type_name,
				       l1.full_name AS level_1_approver_name,
				       a.full_name AS approver_name
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				JOIN leave_types lt ON lr.leave_type_id = lt.id
				LEFT JOIN users l1 ON lr.level_1_approver_id = l1.id
				LEFT JOIN users a ON lr.approver_id = a.id
				WHERE lr.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean insert(LeaveRequest request) {
		if (request == null || request.getUserId() == null || request.getLeaveTypeId() == null
				|| request.getStartDate() == null || request.getEndDate() == null) {
			return false;
		}

		String sql = """
				INSERT INTO leave_requests (user_id, leave_type_id, start_date, end_date, days, reason, status)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, request.getUserId());
			ps.setLong(2, request.getLeaveTypeId());
			ps.setDate(3, request.getStartDate());
			ps.setDate(4, request.getEndDate());
			ps.setBigDecimal(5, request.getDays());
			ps.setString(6, request.getReason());
			ps.setString(7, request.getStatus() != null ? request.getStatus() : "PENDING");
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean approveLevel1(Long id, Long approverId) {
		if (id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'APPROVED_LEVEL_1',
				    level_1_approver_id = ?,
				    level_1_approved_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.approveLevel1() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean finalApprove(Connection conn, Long id, Long approverId) throws SQLException {
		if (id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'APPROVED',
				    approver_id = ?,
				    approved_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'APPROVED_LEVEL_1'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean reject(Connection conn, Long id, Long approverId) throws SQLException {
		if (id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'REJECTED',
				    approver_id = ?
				WHERE id = ? AND status IN ('PENDING', 'APPROVED_LEVEL_1')
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean cancel(Connection conn, Long id, Long userId) throws SQLException {
		if (id == null || userId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'CANCELLED'
				WHERE id = ? AND user_id = ? AND status IN ('PENDING', 'APPROVED_LEVEL_1')
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, userId);
			return ps.executeUpdate() > 0;
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private LeaveRequest mapRow(ResultSet rs) throws SQLException {
		LeaveRequest request = new LeaveRequest();
		request.setId(rs.getLong("id"));
		request.setUserId(rs.getLong("user_id"));
		request.setLeaveTypeId(rs.getLong("leave_type_id"));
		request.setStartDate(rs.getDate("start_date"));
		request.setEndDate(rs.getDate("end_date"));
		request.setDays(rs.getBigDecimal("days"));
		request.setReason(rs.getString("reason"));
		request.setStatus(rs.getString("status"));

		long l1Id = rs.getLong("level_1_approver_id");
		if (!rs.wasNull()) {
			request.setLevel1ApproverId(l1Id);
		}
		request.setLevel1ApprovedAt(rs.getTimestamp("level_1_approved_at"));

		long apprId = rs.getLong("approver_id");
		if (!rs.wasNull()) {
			request.setApproverId(apprId);
		}
		request.setApprovedAt(rs.getTimestamp("approved_at"));

		request.setCreatedAt(rs.getTimestamp("created_at"));
		request.setUpdatedAt(rs.getTimestamp("updated_at"));
		request.setUserFullName(rs.getString("user_full_name"));
		request.setEmployeeCode(rs.getString("employee_code"));
		request.setLeaveTypeName(rs.getString("leave_type_name"));
		request.setLevel1ApproverName(rs.getString("level_1_approver_name"));
		request.setApproverName(rs.getString("approver_name"));
		return request;
	}
}
