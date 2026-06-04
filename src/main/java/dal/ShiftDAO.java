package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Shift;

public class ShiftDAO {

	public List<Shift> searchShifts(String keyword, Boolean isNightShift, Boolean isActive, int offset, int limit) {
		List<Shift> shifts = new ArrayList<>();
		StringBuilder sql = new StringBuilder(
				"""
						SELECT id, code, name, start_time, end_time, break_minutes, is_night_shift, is_active, created_at, updated_at
						FROM shifts
						WHERE 1 = 1
						""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isNightShift != null) {
			sql.append(" AND is_night_shift = ?");
			params.add(isNightShift);
		}

		if (isActive != null) {
			sql.append(" AND is_active = ?");
			params.add(isActive);
		}

		sql.append(" ORDER BY id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					shifts.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftDAO.searchShifts() ERROR: " + e.getMessage());
		}

		return shifts;
	}

	public int countShifts(String keyword, Boolean isNightShift, Boolean isActive) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM shifts
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isNightShift != null) {
			sql.append(" AND is_night_shift = ?");
			params.add(isNightShift);
		}

		if (isActive != null) {
			sql.append(" AND is_active = ?");
			params.add(isActive);
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
			System.err.println("ShiftDAO.countShifts() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public Shift getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT id, code, name, start_time, end_time, break_minutes, is_night_shift, is_active, created_at, updated_at
				FROM shifts
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean existsByCode(String code) {
		if (code == null || code.isBlank()) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM shifts WHERE code = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftDAO.existsByCode() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean existsByCodeExceptId(String code, Long id) {
		if (code == null || code.isBlank() || id == null) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM shifts WHERE code = ? AND id <> ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			ps.setLong(2, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("ShiftDAO.existsByCodeExceptId() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean insert(Shift shift) {
		if (shift == null || shift.getCode() == null || shift.getName() == null || shift.getStartTime() == null
				|| shift.getEndTime() == null) {
			return false;
		}

		String sql = """
				INSERT INTO shifts (code, name, start_time, end_time, break_minutes, is_night_shift, is_active)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, shift.getCode());
			ps.setString(2, shift.getName());
			ps.setTime(3, shift.getStartTime());
			ps.setTime(4, shift.getEndTime());
			ps.setInt(5, shift.getBreakMinutes() != null ? shift.getBreakMinutes() : 0);
			ps.setBoolean(6, shift.getIsNightShift() != null && shift.getIsNightShift());
			ps.setBoolean(7, shift.getIsActive() == null || shift.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ShiftDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean update(Shift shift) {
		if (shift == null || shift.getId() == null || shift.getCode() == null || shift.getName() == null
				|| shift.getStartTime() == null || shift.getEndTime() == null) {
			return false;
		}

		String sql = """
				UPDATE shifts
				SET code = ?, name = ?, start_time = ?, end_time = ?, break_minutes = ?, is_night_shift = ?
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, shift.getCode());
			ps.setString(2, shift.getName());
			ps.setTime(3, shift.getStartTime());
			ps.setTime(4, shift.getEndTime());
			ps.setInt(5, shift.getBreakMinutes() != null ? shift.getBreakMinutes() : 0);
			ps.setBoolean(6, shift.getIsNightShift() != null && shift.getIsNightShift());
			ps.setLong(7, shift.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ShiftDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}

		String sql = "UPDATE shifts SET is_active = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ShiftDAO.updateStatus() ERROR: " + e.getMessage());
		}

		return false;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private Shift mapRow(ResultSet rs) throws SQLException {
		Shift shift = new Shift();
		shift.setId(rs.getLong("id"));
		shift.setCode(rs.getString("code"));
		shift.setName(rs.getString("name"));
		shift.setStartTime(rs.getTime("start_time"));
		shift.setEndTime(rs.getTime("end_time"));
		shift.setBreakMinutes(rs.getInt("break_minutes"));
		shift.setIsNightShift(rs.getBoolean("is_night_shift"));
		shift.setIsActive(rs.getBoolean("is_active"));
		shift.setCreatedAt(rs.getTimestamp("created_at"));
		shift.setUpdatedAt(rs.getTimestamp("updated_at"));
		return shift;
	}
}
