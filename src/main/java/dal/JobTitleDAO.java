package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.JobTitle;

public class JobTitleDAO {

	public List<JobTitle> getActiveJobTitles() {
		List<JobTitle> list = new ArrayList<>();
		String sql = "SELECT id, name, description, is_active FROM job_titles WHERE is_active = TRUE ORDER BY name";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.getActiveJobTitles() ERROR: " + e.getMessage());
		}
		return list;
	}

	public JobTitle getById(Long id) {
		if (id == null)
			return null;
		String sql = "SELECT id, name, description, is_active FROM job_titles WHERE id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("JobTitleDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	private JobTitle mapRow(ResultSet rs) throws SQLException {
		JobTitle jt = new JobTitle();
		jt.setId(rs.getLong("id"));
		jt.setName(rs.getString("name"));
		jt.setDescription(rs.getString("description"));
		jt.setIsActive(rs.getBoolean("is_active"));
		return jt;
	}
}
