package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dto.DashboardStats;
import model.LeaveRequest;

public class DashboardDAO {

	public DashboardStats getDashboardStats() {
		String sql = """
				SELECT
				    COUNT(*) AS total_users,
				    SUM(CASE WHEN u.is_active = TRUE THEN 1 ELSE 0 END) AS active_users,
				    SUM(CASE WHEN u.is_active = TRUE AND u.employee_type = 'OFFICE' THEN 1 ELSE 0 END) AS office_users,
				    SUM(CASE WHEN u.is_active = TRUE AND u.employee_type = 'WORKER' THEN 1 ELSE 0 END) AS worker_users,
				    (SELECT COUNT(*) FROM departments WHERE is_active = TRUE) AS departments
				FROM users u""";

		DashboardStats stats = new DashboardStats();

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					stats.setTotalUsers(rs.getInt("total_users"));
					stats.setActiveUsers(rs.getInt("active_users"));
					stats.setOfficeUsers(rs.getInt("office_users"));
					stats.setWorkerUsers(rs.getInt("worker_users"));
					stats.setDepartments(rs.getInt("departments"));
				}
			}
		} catch (SQLException e) {
			System.err.println("DashboardDAO.getStats() ERROR: " + e.getMessage());
		}
		return stats;
	}

	public List<LeaveRequest> getPendingLeaveRequestsForSupervisor(Long supervisorId, int limit) {
		List<LeaveRequest> requests = new ArrayList<>();
		if (supervisorId == null) {
			return requests;
		}

		String sql = """
				SELECT lr.id, lr.user_id, lr.leave_type_id, lr.start_date, lr.end_date, lr.days,
				       lr.reason, lr.status, lr.created_at, lr.updated_at,
				       u.employee_code, u.full_name AS employee_name,
				       d.name AS department_name,
				       lt.code AS leave_type_code, lt.name AS leave_type_name
				FROM leave_requests lr
				INNER JOIN users u ON u.id = lr.user_id
				LEFT JOIN departments d ON d.id = u.department_id
				INNER JOIN leave_types lt ON lt.id = lr.leave_type_id
				WHERE lr.status = 'PENDING'
				  AND u.manager_id = ?
				ORDER BY lr.created_at ASC, lr.id ASC
				LIMIT ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, supervisorId);
			ps.setInt(2, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					requests.add(mapPendingLeaveRequest(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("DashboardDAO.getPendingLeaveRequestsForSupervisor() ERROR: " + e.getMessage());
		}
		return requests;
	}

	private LeaveRequest mapPendingLeaveRequest(ResultSet rs) throws SQLException {
		LeaveRequest request = new LeaveRequest();
		request.setId(rs.getLong("id"));
		request.setUserId(rs.getLong("user_id"));
		request.setLeaveTypeId(rs.getLong("leave_type_id"));
		request.setStartDate(rs.getDate("start_date"));
		request.setEndDate(rs.getDate("end_date"));
		request.setDays(rs.getBigDecimal("days"));
		request.setReason(rs.getString("reason"));
		request.setStatus(rs.getString("status"));
		request.setCreatedAt(rs.getTimestamp("created_at"));
		request.setUpdatedAt(rs.getTimestamp("updated_at"));
		request.setEmployeeCode(rs.getString("employee_code"));
		request.setEmployeeName(rs.getString("employee_name"));
		request.setDepartmentName(rs.getString("department_name"));
		request.setLeaveTypeCode(rs.getString("leave_type_code"));
		request.setLeaveTypeName(rs.getString("leave_type_name"));
		return request;
	}
}
