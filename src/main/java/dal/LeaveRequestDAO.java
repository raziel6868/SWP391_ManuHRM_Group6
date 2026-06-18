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
