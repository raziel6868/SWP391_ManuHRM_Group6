package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Role;

public class RoleDAO {

	public List<Role> getActiveRoles() {
		List<Role> list = new ArrayList<>();
		String sql = """
				SELECT id, name, display_name, description, is_active, is_system
				FROM roles
				WHERE is_active = TRUE
				ORDER BY id ASC
				""";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Role> searchAndFilter(String keyword, int offset, int limit) {
		List<Role> list = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT id, name, display_name, description, is_active, is_system
				FROM roles
				WHERE 1=1
				""");

		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		if (hasKeyword) {
			sql.append(" AND (name LIKE ? OR display_name LIKE ? OR description LIKE ?)");
		}
		sql.append(" ORDER BY id ASC LIMIT ? OFFSET ?");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			int paramIndex = 1;
			if (hasKeyword) {
				String likeKeyword = "%" + keyword.trim() + "%";
				ps.setString(paramIndex++, likeKeyword);
				ps.setString(paramIndex++, likeKeyword);
				ps.setString(paramIndex++, likeKeyword);
			}
			ps.setInt(paramIndex++, limit);
			ps.setInt(paramIndex++, offset);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public int countTotalRoles(String keyword) {
		int count = 0;
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM roles WHERE 1=1");
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		if (hasKeyword) {
			sql.append(" AND (name LIKE ? OR display_name LIKE ? OR description LIKE ?)");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			if (hasKeyword) {
				String likeKeyword = "%" + keyword.trim() + "%";
				ps.setString(1, likeKeyword);
				ps.setString(2, likeKeyword);
				ps.setString(3, likeKeyword);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					count = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public Role getById(Long id) {
		String sql = """
				SELECT id, name, display_name, description, is_active, is_system
				FROM roles WHERE id = ?
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean update(Role role) {
		String sql = """
				UPDATE roles
				SET name = ?, display_name = ?, description = ?
				WHERE id = ? AND is_system = FALSE
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, role.getName());
			ps.setString(2, role.getDisplayName());
			ps.setString(3, role.getDescription());
			ps.setLong(4, role.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		String sql = "UPDATE roles SET is_active = ? WHERE id = ? AND is_system = FALSE";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Role mapRow(ResultSet rs) throws SQLException {
		Role r = new Role();
		r.setId(rs.getLong("id"));
		r.setName(rs.getString("name"));
		r.setDisplayName(rs.getString("display_name"));
		r.setDescription(rs.getString("description"));
		r.setIsActive(rs.getBoolean("is_active"));
		r.setIsSystem(rs.getBoolean("is_system"));
		return r;
	}
}
