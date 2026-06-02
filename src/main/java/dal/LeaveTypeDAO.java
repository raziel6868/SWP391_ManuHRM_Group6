package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.LeaveType;

public class LeaveTypeDAO {

	public List<LeaveType> searchLeaveTypes(String keyword, Boolean isPaid, Boolean isActive, int offset, int limit) {
		List<LeaveType> leaveTypes = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT id, code, name, description, is_paid, is_active, created_at, updated_at
				FROM leave_types
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ? OR description LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isPaid != null) {
			sql.append(" AND is_paid = ?");
			params.add(isPaid);
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
					leaveTypes.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.searchLeaveTypes() ERROR: " + e.getMessage());
		}

		return leaveTypes;
	}

	public int countLeaveTypes(String keyword, Boolean isPaid, Boolean isActive) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM leave_types
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ? OR description LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isPaid != null) {
			sql.append(" AND is_paid = ?");
			params.add(isPaid);
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
			System.err.println("LeaveTypeDAO.countLeaveTypes() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public LeaveType getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT id, code, name, description, is_paid, is_active, created_at, updated_at
				FROM leave_types
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
			System.err.println("LeaveTypeDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean existsByCode(String code) {
		if (code == null || code.isBlank()) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM leave_types WHERE code = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.existsByCode() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean existsByCodeExceptId(String code, Long id) {
		if (code == null || code.isBlank() || id == null) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM leave_types WHERE code = ? AND id <> ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			ps.setLong(2, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.existsByCodeExceptId() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean insert(LeaveType leaveType) {
		if (leaveType == null || leaveType.getCode() == null || leaveType.getName() == null) {
			return false;
		}

		String sql = """
				INSERT INTO leave_types (code, name, description, is_paid, is_active)
				VALUES (?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, leaveType.getCode());
			ps.setString(2, leaveType.getName());
			ps.setString(3, leaveType.getDescription());
			ps.setBoolean(4, leaveType.getIsPaid() != null && leaveType.getIsPaid());
			ps.setBoolean(5, leaveType.getIsActive() == null || leaveType.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean update(LeaveType leaveType) {
		if (leaveType == null || leaveType.getId() == null || leaveType.getCode() == null
				|| leaveType.getName() == null) {
			return false;
		}

		String sql = """
				UPDATE leave_types
				SET code = ?, name = ?, description = ?, is_paid = ?
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, leaveType.getCode());
			ps.setString(2, leaveType.getName());
			ps.setString(3, leaveType.getDescription());
			ps.setBoolean(4, leaveType.getIsPaid() != null && leaveType.getIsPaid());
			ps.setLong(5, leaveType.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}

		String sql = "UPDATE leave_types SET is_active = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.updateStatus() ERROR: " + e.getMessage());
		}

		return false;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private LeaveType mapRow(ResultSet rs) throws SQLException {
		LeaveType leaveType = new LeaveType();
		leaveType.setId(rs.getLong("id"));
		leaveType.setCode(rs.getString("code"));
		leaveType.setName(rs.getString("name"));
		leaveType.setDescription(rs.getString("description"));
		leaveType.setIsPaid(rs.getBoolean("is_paid"));
		leaveType.setIsActive(rs.getBoolean("is_active"));
		leaveType.setCreatedAt(rs.getTimestamp("created_at"));
		leaveType.setUpdatedAt(rs.getTimestamp("updated_at"));
		return leaveType;
	}
}
