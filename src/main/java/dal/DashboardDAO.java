package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dto.DashboardStats;

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

}
