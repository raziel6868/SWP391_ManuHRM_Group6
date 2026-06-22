package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.LeaveRequest;

public class LeaveRequestDAO {

	private static final String SELECT_COLUMNS = """
			lr.id, lr.user_id, lr.leave_type_id, lr.start_date, lr.end_date, lr.days,
			lr.reason, lr.status, lr.level_1_approver_id, lr.level_1_approved_at,
			lr.approver_id, lr.approved_at, lr.created_at, lr.updated_at,
			u.employee_code, u.full_name AS employee_name,
			d.name AS department_name,
			lt.code AS leave_type_code, lt.name AS leave_type_name,
			l1.full_name AS level_1_approver_name,
			final_approver.full_name AS approver_name
			""";

	private static final String BASE_FROM = """
			FROM leave_requests lr
			INNER JOIN users u ON u.id = lr.user_id
			LEFT JOIN departments d ON d.id = u.department_id
			INNER JOIN leave_types lt ON lt.id = lr.leave_type_id
			LEFT JOIN users l1 ON l1.id = lr.level_1_approver_id
			LEFT JOIN users final_approver ON final_approver.id = lr.approver_id
			""";

	public boolean insert(LeaveRequest request) {
		if (request == null || request.getUserId() == null || request.getLeaveTypeId() == null
				|| request.getStartDate() == null || request.getEndDate() == null || request.getDays() == null) {
			return false;
		}

		String sql = """
				INSERT INTO leave_requests (user_id, leave_type_id, start_date, end_date, days, reason, status)
				VALUES (?, ?, ?, ?, ?, ?, 'PENDING')
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, request.getUserId());
			ps.setLong(2, request.getLeaveTypeId());
			ps.setDate(3, request.getStartDate());
			ps.setDate(4, request.getEndDate());
			ps.setBigDecimal(5, request.getDays());
			ps.setString(6, request.getReason());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.insert() ERROR: " + e.getMessage());
		}
		return false;
	}

	public List<LeaveRequest> searchRequests(String keyword, String status, Long departmentId, int offset, int limit) {
		List<LeaveRequest> requests = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT " + SELECT_COLUMNS + BASE_FROM + " WHERE 1 = 1");
		List<Object> params = new ArrayList<>();
		appendFilters(sql, params, keyword, status, departmentId);
		sql.append(" ORDER BY lr.created_at DESC, lr.id DESC LIMIT ? OFFSET ?");
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
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) " + BASE_FROM + " WHERE 1 = 1");
		List<Object> params = new ArrayList<>();
		appendFilters(sql, params, keyword, status, departmentId);

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
		List<LeaveRequest> requests = new ArrayList<>();
		if (userId == null) {
			return requests;
		}

		String sql = "SELECT " + SELECT_COLUMNS + BASE_FROM
				+ " WHERE lr.user_id = ? ORDER BY lr.created_at DESC, lr.id DESC LIMIT ? OFFSET ?";

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

	public int countByUser(Long userId) {
		if (userId == null) {
			return 0;
		}

		String sql = "SELECT COUNT(*) FROM leave_requests WHERE user_id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.countByUser() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public LeaveRequest getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = "SELECT " + SELECT_COLUMNS + BASE_FROM + " WHERE lr.id = ?";
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

	public boolean approveLevel1(Long id, Long approverId) {
		if (id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'APPROVED_LEVEL_1',
				    level_1_approver_id = ?,
				    level_1_approved_at = CURRENT_TIMESTAMP,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				  AND status = 'PENDING'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.approveLevel1() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean finalApprove(Connection conn, Long id, Long approverId) throws SQLException {
		if (conn == null || id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'APPROVED',
				    approver_id = ?,
				    approved_at = CURRENT_TIMESTAMP,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				  AND status = 'APPROVED_LEVEL_1'
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() == 1;
		}
	}

	public boolean reject(Long id, Long approverId) {
		try (Connection conn = DBContext.getConnection()) {
			return reject(conn, id, approverId);
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.reject() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean reject(Connection conn, Long id, Long approverId) throws SQLException {
		if (conn == null || id == null || approverId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'REJECTED',
				    approver_id = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				  AND status IN ('PENDING', 'APPROVED_LEVEL_1')
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, approverId);
			ps.setLong(2, id);
			return ps.executeUpdate() == 1;
		}
	}

	public boolean cancel(Long id, Long userId) {
		if (id == null || userId == null) {
			return false;
		}

		String sql = """
				UPDATE leave_requests
				SET status = 'CANCELLED',
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				  AND user_id = ?
				  AND status IN ('PENDING', 'APPROVED_LEVEL_1')
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, userId);
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.cancel() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean isRequesterManagedBy(Long requestId, Long managerId) {
		if (requestId == null || managerId == null) {
			return false;
		}

		String sql = """
				SELECT COUNT(*)
				FROM leave_requests lr
				INNER JOIN users u ON u.id = lr.user_id
				WHERE lr.id = ?
				  AND u.manager_id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, requestId);
			ps.setLong(2, managerId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveRequestDAO.isRequesterManagedBy() ERROR: " + e.getMessage());
		}
		return false;
	}

	private void appendFilters(StringBuilder sql, List<Object> params, String keyword, String status,
			Long departmentId) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			String likeKeyword = "%" + keyword.trim() + "%";
			sql.append("""
					 AND (u.employee_code LIKE ?
					      OR u.full_name LIKE ?
					      OR lt.code LIKE ?
					      OR lt.name LIKE ?)
					""");
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}
		if (status != null && !status.trim().isEmpty()) {
			sql.append(" AND lr.status = ?");
			params.add(status.trim().toUpperCase());
		}
		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
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

		long level1ApproverId = rs.getLong("level_1_approver_id");
		request.setLevel1ApproverId(rs.wasNull() ? null : level1ApproverId);
		request.setLevel1ApprovedAt(rs.getTimestamp("level_1_approved_at"));

		long approverId = rs.getLong("approver_id");
		request.setApproverId(rs.wasNull() ? null : approverId);
		request.setApprovedAt(rs.getTimestamp("approved_at"));

		request.setCreatedAt(rs.getTimestamp("created_at"));
		request.setUpdatedAt(rs.getTimestamp("updated_at"));
		request.setEmployeeCode(rs.getString("employee_code"));
		request.setEmployeeName(rs.getString("employee_name"));
		request.setDepartmentName(rs.getString("department_name"));
		request.setLeaveTypeCode(rs.getString("leave_type_code"));
		request.setLeaveTypeName(rs.getString("leave_type_name"));
		request.setLevel1ApproverName(rs.getString("level_1_approver_name"));
		request.setApproverName(rs.getString("approver_name"));
		return request;
	}
}
