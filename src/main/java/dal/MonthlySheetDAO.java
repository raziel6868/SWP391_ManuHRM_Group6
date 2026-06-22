package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MonthlySheet;

public class MonthlySheetDAO {

	public List<MonthlySheet> getAll() {
		List<MonthlySheet> sheets = new ArrayList<>();
		String sql = """
				SELECT ms.id, ms.year, ms.month, ms.status, ms.closed_at, ms.closed_by, ms.created_at,
				       u.full_name AS closed_by_name
				FROM monthly_sheets ms
				LEFT JOIN users u ON ms.closed_by = u.id
				ORDER BY ms.year DESC, ms.month DESC
				""";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				sheets.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("Error getting all monthly sheets: " + e.getMessage());
		}

		return sheets;
	}

	public MonthlySheet getById(Long id) {
		String sql = """
				SELECT ms.id, ms.year, ms.month, ms.status, ms.closed_at, ms.closed_by, ms.created_at,
				       u.full_name AS closed_by_name
				FROM monthly_sheets ms
				LEFT JOIN users u ON ms.closed_by = u.id
				WHERE ms.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting monthly sheet by id: " + e.getMessage());
		}

		return null;
	}

	public MonthlySheet getByYearMonth(int year, int month) {
		String sql = """
				SELECT ms.id, ms.year, ms.month, ms.status, ms.closed_at, ms.closed_by, ms.created_at,
				       u.full_name AS closed_by_name
				FROM monthly_sheets ms
				LEFT JOIN users u ON ms.closed_by = u.id
				WHERE ms.year = ? AND ms.month = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting monthly sheet by year/month: " + e.getMessage());
		}

		return null;
	}

	public MonthlySheet getOrCreate(int year, int month) {
		MonthlySheet existing = getByYearMonth(year, month);
		if (existing != null) {
			return existing;
		}

		String insertSql = "INSERT INTO monthly_sheets (year, month, status) VALUES (?, ?, 'OPEN')";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			int affected = ps.executeUpdate();

			if (affected > 0) {
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) {
						MonthlySheet sheet = new MonthlySheet();
						sheet.setId(rs.getLong(1));
						sheet.setYear(year);
						sheet.setMonth(month);
						sheet.setStatus("OPEN");
						return sheet;
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error creating monthly sheet: " + e.getMessage());
		}

		return null;
	}

	public boolean closeSheet(Long id, Long closedBy) {
		String sql = "UPDATE monthly_sheets SET status = 'CLOSED', closed_by = ?, closed_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'OPEN'";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, closedBy);
			ps.setLong(2, id);
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error closing monthly sheet: " + e.getMessage());
			return false;
		}
	}

	public boolean reopenSheet(Long id) {
		String sql = "UPDATE monthly_sheets SET status = 'OPEN', closed_by = NULL, closed_at = NULL WHERE id = ? AND status = 'CLOSED'";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Error reopening monthly sheet: " + e.getMessage());
			return false;
		}
	}

	public boolean isPeriodClosed(int year, int month) {
		String sql = "SELECT COUNT(*) FROM monthly_sheets WHERE year = ? AND month = ? AND status = 'CLOSED'";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error checking period closed status: " + e.getMessage());
		}

		return false;
	}

	private MonthlySheet mapRow(ResultSet rs) throws SQLException {
		MonthlySheet ms = new MonthlySheet();
		ms.setId(rs.getLong("id"));
		ms.setYear(rs.getInt("year"));
		ms.setMonth(rs.getInt("month"));
		ms.setStatus(rs.getString("status"));
		ms.setClosedAt(rs.getTimestamp("closed_at"));
		ms.setClosedBy(rs.getObject("closed_by") != null ? rs.getLong("closed_by") : null);
		ms.setClosedByName(rs.getString("closed_by_name"));
		ms.setCreatedAt(rs.getTimestamp("created_at"));
		return ms;
	}
}
