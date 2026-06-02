package dal;

import model.JobTitle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JobTitleDAO {

	public List<JobTitle> getActiveJobTitles() {
		String sql = """
				SELECT id, name, description, is_active, created_at, updated_at
				FROM job_titles WHERE is_active = TRUE ORDER BY name ASC""";
		return getJobTitles(sql, null);
	}

	public List<JobTitle> searchJobTitles(String keyword, int offset, int limit) {
		if (keyword == null || keyword.trim().isEmpty()) {
			String sql = """
					SELECT id, name, description, is_active, created_at, updated_at
					FROM job_titles
					ORDER BY id ASC LIMIT ? OFFSET ?""";
			return getJobTitles(sql, List.of(limit, offset));
		}
		String like = "%" + keyword.trim() + "%";
		String sql = """
				SELECT id, name, description, is_active, created_at, updated_at
				FROM job_titles
				WHERE name LIKE ? OR description LIKE ?
				ORDER BY id ASC LIMIT ? OFFSET ?""";
		return getJobTitles(sql, List.of(like, like, limit, offset));
	}

	public int countJobTitles(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return count("SELECT COUNT(*) FROM job_titles", null);
		}
		String like = "%" + keyword.trim() + "%";
		return count("""
				SELECT COUNT(*) FROM job_titles
				WHERE name LIKE ? OR description LIKE ?""", List.of(like, like));
	}

	public JobTitle getById(Long id) {
		if (id == null)
			return null;
		String sql = """
				SELECT id, name, description, is_active, created_at, updated_at
				FROM job_titles WHERE id = ?""";
		return getJobTitle(sql, List.of(id));
	}

	public boolean existsByName(String name) {
		if (name == null || name.isBlank())
			return false;
		return count("SELECT COUNT(*) FROM job_titles WHERE name = ?", List.of(name.trim())) > 0;
	}

	public boolean existsByNameExceptId(String name, Long id) {
		if (name == null || name.isBlank() || id == null)
			return false;
		return count("SELECT COUNT(*) FROM job_titles WHERE name = ? AND id != ?", List.of(name.trim(), id)) > 0;
	}

	public boolean insert(JobTitle jobTitle) {
		if (jobTitle == null || jobTitle.getName() == null)
			return false;
		String sql = """
				INSERT INTO job_titles (name, description, is_active)
				VALUES (?, ?, ?)""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, jobTitle.getName().trim());
			ps.setString(2, jobTitle.getDescription() != null ? jobTitle.getDescription().trim() : null);
			ps.setBoolean(3, jobTitle.getIsActive() != null && jobTitle.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.insert() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean update(JobTitle jobTitle) {
		if (jobTitle == null || jobTitle.getId() == null)
			return false;
		String sql = """
				UPDATE job_titles SET name = ?, description = ?
				WHERE id = ?""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, jobTitle.getName().trim());
			ps.setString(2, jobTitle.getDescription() != null ? jobTitle.getDescription().trim() : null);
			ps.setLong(3, jobTitle.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.update() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null)
			return false;
		String sql = "UPDATE job_titles SET is_active = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.updateStatus() ERROR: " + e.getMessage());
			return false;
		}
	}

	private List<JobTitle> getJobTitles(String sql, List<Object> params) {
		List<JobTitle> list = new ArrayList<>();
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.getJobTitles() ERROR: " + e.getMessage());
		}
		return list;
	}

	private JobTitle getJobTitle(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.getJobTitle() ERROR: " + e.getMessage());
		}
		return null;
	}

	private int count(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.count() ERROR: " + e.getMessage());
		}
		return 0;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		if (params == null || params.isEmpty())
			return;
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private JobTitle mapRow(ResultSet rs) throws SQLException {
		JobTitle jt = new JobTitle();
		jt.setId(rs.getLong("id"));
		jt.setName(rs.getString("name"));
		jt.setDescription(rs.getString("description"));
		jt.setIsActive(rs.getBoolean("is_active"));
		jt.setCreatedAt(rs.getTimestamp("created_at"));
		jt.setUpdatedAt(rs.getTimestamp("updated_at"));
		return jt;
	}
}
