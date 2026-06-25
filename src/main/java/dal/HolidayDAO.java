package dal;

import model.Holiday;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HolidayDAO {

	private Connection getConnection() throws SQLException {
		return DBContext.getConnection();
	}

	public List<Holiday> getAll() {
		List<Holiday> holidays = new ArrayList<>();
		String sql = "SELECT * FROM holidays ORDER BY date ASC";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				holidays.add(mapResultSet(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return holidays;
	}

	public List<Holiday> getByYear(int year) {
		return search(null, year);
	}

	public List<Holiday> search(String keyword, Integer year) {
		return search(keyword, year, true);
	}

	public List<Holiday> search(String keyword, Integer year, boolean activeOnly) {
		List<Holiday> holidays = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT * FROM holidays WHERE 1=1");
		List<Object> params = new ArrayList<>();

		if (activeOnly) {
			sql.append(" AND is_active = TRUE");
		}
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (name LIKE ? OR description LIKE ?)");
			String kw = "%" + keyword.trim() + "%";
			params.add(kw);
			params.add(kw);
		}
		if (year != null) {
			sql.append(" AND YEAR(date) = ?");
			params.add(year);
		}
		sql.append(" ORDER BY date ASC");

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					holidays.add(mapResultSet(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return holidays;
	}

	public List<Holiday> getByDateRange(LocalDate start, LocalDate end) {
		List<Holiday> holidays = new ArrayList<>();
		String sql = "SELECT * FROM holidays WHERE date BETWEEN ? AND ? ORDER BY date ASC";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(start));
			ps.setDate(2, Date.valueOf(end));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					holidays.add(mapResultSet(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return holidays;
	}

	public Holiday getById(Long id) {
		String sql = "SELECT * FROM holidays WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapResultSet(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Holiday getByDate(LocalDate date) {
		String sql = "SELECT * FROM holidays WHERE date = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(date));
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapResultSet(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isHoliday(LocalDate date) {
		String sql = "SELECT COUNT(*) FROM holidays WHERE date = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(date));
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean insert(Holiday holiday) {
		String sql = "INSERT INTO holidays (date, name, is_recurring, description) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(holiday.getDate().toLocalDate()));
			ps.setString(2, holiday.getName());
			ps.setBoolean(3, holiday.isRecurring());
			ps.setString(4, holiday.getDescription());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean update(Holiday holiday) {
		String sql = "UPDATE holidays SET date = ?, name = ?, is_recurring = ?, description = ? WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(holiday.getDate().toLocalDate()));
			ps.setString(2, holiday.getName());
			ps.setBoolean(3, holiday.isRecurring());
			ps.setString(4, holiday.getDescription());
			ps.setLong(5, holiday.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean toggleActive(Long id, boolean active) {
		String sql = "UPDATE holidays SET is_active = ? WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, active);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean existsByDate(LocalDate date) {
		String sql = "SELECT COUNT(*) FROM holidays WHERE date = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(date));
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean existsByDateExceptId(LocalDate date, Long excludeId) {
		String sql = "SELECT COUNT(*) FROM holidays WHERE date = ? AND id != ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(date));
			ps.setLong(2, excludeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Holiday mapResultSet(ResultSet rs) throws SQLException {
		Holiday h = new Holiday();
		h.setId(rs.getLong("id"));
		h.setDate(rs.getDate("date"));
		h.setName(rs.getString("name"));
		h.setRecurring(rs.getBoolean("is_recurring"));
		h.setActive(rs.getBoolean("is_active"));
		h.setDescription(rs.getString("description"));
		h.setCreatedAt(rs.getTimestamp("created_at"));
		h.setUpdatedAt(rs.getTimestamp("updated_at"));
		return h;
	}
}
