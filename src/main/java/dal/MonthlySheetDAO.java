package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.MonthlySheet;

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

	public List<MonthlySheet> getAll() {
		List<MonthlySheet> sheets = new ArrayList<>();
		String sql = """
				SELECT ms.*,
				       u.full_name AS closed_by_name
				FROM monthly_sheets ms
				LEFT JOIN users u ON ms.closed_by = u.id
				ORDER BY ms.year DESC, ms.month DESC
				""";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				MonthlySheet sheet = mapRow(rs);
				sheets.add(sheet);
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getAll() ERROR: " + e.getMessage());
		}

		return sheets;
	}

	public MonthlySheet getById(Long id) {
		String sql = """
				SELECT ms.*,
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
			System.err.println("MonthlySheetDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public MonthlySheet getByYearMonth(int year, int month) {
		String sql = """
				SELECT ms.*,
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
			System.err.println("MonthlySheetDAO.getByYearMonth() ERROR: " + e.getMessage());
		}

		return null;
	}

	public MonthlySheet getOrCreate(int year, int month) {
		MonthlySheet existing = getByYearMonth(year, month);
		if (existing != null) {
			return existing;
		}

		String sql = "INSERT INTO monthly_sheets (year, month, status, created_at) VALUES (?, ?, 'OPEN', NOW())";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getOrCreate() INSERT ERROR: " + e.getMessage());
		}

		return getByYearMonth(year, month);
	}

	public boolean closeSheet(Long sheetId, Long closedBy) {
		String sql = "UPDATE monthly_sheets SET status = 'CLOSED', closed_by = ?, closed_at = NOW() WHERE id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, closedBy);
			ps.setLong(2, sheetId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.closeSheet() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean reopenSheet(Long sheetId) {
		String sql = "UPDATE monthly_sheets SET status = 'OPEN', closed_by = NULL, closed_at = NULL WHERE id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.reopenSheet() ERROR: " + e.getMessage());
			return false;
		}
	}

	private MonthlySheet mapRow(ResultSet rs) throws SQLException {
		MonthlySheet sheet = new MonthlySheet();
		sheet.setId(rs.getLong("id"));
		sheet.setYear(rs.getInt("year"));
		sheet.setMonth(rs.getInt("month"));
		sheet.setStatus(rs.getString("status"));
		Timestamp closedAt = rs.getTimestamp("closed_at");
		sheet.setClosedAt(closedAt);
		sheet.setClosedBy(rs.getObject("closed_by") != null ? rs.getLong("closed_by") : null);
		sheet.setClosedByName(rs.getString("closed_by_name"));
		sheet.setCreatedAt(rs.getTimestamp("created_at"));
		return sheet;
	}
}
