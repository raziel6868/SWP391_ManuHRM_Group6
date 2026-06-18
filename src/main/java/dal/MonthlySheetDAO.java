package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MonthlySheetDAO {

	public boolean isPeriodClosed(int year, int month) {
		String sql = """
				SELECT status
				FROM monthly_sheets
				WHERE year = ? AND month = ?
				LIMIT 1
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return "CLOSED".equals(rs.getString("status"));
				}
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.isPeriodClosed() ERROR: " + e.getMessage());
		}

		return false;
	}
}
