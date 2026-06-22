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
		List<Holiday> holidays = new ArrayList<>();
		String sql = "SELECT * FROM holidays WHERE YEAR(date) = ? ORDER BY date ASC";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
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

	public boolean delete(Long id) {
		String sql = "DELETE FROM holidays WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
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
		h.setDescription(rs.getString("description"));
		h.setCreatedAt(rs.getTimestamp("created_at"));
		h.setUpdatedAt(rs.getTimestamp("updated_at"));
		return h;
	}
}
