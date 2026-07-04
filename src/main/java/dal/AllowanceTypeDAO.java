package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.AllowanceType;

public class AllowanceTypeDAO {

	public List<AllowanceType> search(String keyword, Boolean isActive, int offset, int limit) {
		List<AllowanceType> allowanceTypes = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT id, code, name, description, is_taxable, is_insurance_based,
				       is_active, created_at, updated_at
				FROM allowance_types
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		appendFilters(sql, params, keyword, isActive);
		sql.append(" ORDER BY id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					allowanceTypes.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.search() ERROR: " + e.getMessage());
		}

		return allowanceTypes;
	}

	public int count(String keyword, Boolean isActive) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM allowance_types
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		appendFilters(sql, params, keyword, isActive);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.count() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public List<AllowanceType> getActiveTypes() {
		List<AllowanceType> allowanceTypes = new ArrayList<>();
		String sql = """
				SELECT id, code, name, description, is_taxable, is_insurance_based,
				       is_active, created_at, updated_at
				FROM allowance_types
				WHERE is_active = TRUE
				ORDER BY name ASC
				""";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				allowanceTypes.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.getActiveTypes() ERROR: " + e.getMessage());
		}

		return allowanceTypes;
	}

	public AllowanceType getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT id, code, name, description, is_taxable, is_insurance_based,
				       is_active, created_at, updated_at
				FROM allowance_types
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
			System.err.println("AllowanceTypeDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean existsByCode(String code) {
		if (code == null || code.isBlank()) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM allowance_types WHERE code = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.existsByCode() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean existsByCodeExceptId(String code, Long id) {
		if (code == null || code.isBlank() || id == null) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM allowance_types WHERE code = ? AND id <> ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			ps.setLong(2, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.existsByCodeExceptId() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean insert(AllowanceType allowanceType) {
		if (allowanceType == null || allowanceType.getCode() == null || allowanceType.getName() == null) {
			return false;
		}

		String sql = """
				INSERT INTO allowance_types
				    (code, name, description, is_taxable, is_insurance_based, is_active)
				VALUES (?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, allowanceType.getCode());
			ps.setString(2, allowanceType.getName());
			ps.setString(3, allowanceType.getDescription());
			ps.setBoolean(4, Boolean.TRUE.equals(allowanceType.getIsTaxable()));
			ps.setBoolean(5, Boolean.TRUE.equals(allowanceType.getIsInsuranceBased()));
			ps.setBoolean(6, allowanceType.getIsActive() == null || allowanceType.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean update(AllowanceType allowanceType) {
		if (allowanceType == null || allowanceType.getId() == null || allowanceType.getCode() == null
				|| allowanceType.getName() == null) {
			return false;
		}

		String sql = """
				UPDATE allowance_types
				SET code = ?, name = ?, description = ?,
				    is_taxable = ?, is_insurance_based = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, allowanceType.getCode());
			ps.setString(2, allowanceType.getName());
			ps.setString(3, allowanceType.getDescription());
			ps.setBoolean(4, Boolean.TRUE.equals(allowanceType.getIsTaxable()));
			ps.setBoolean(5, Boolean.TRUE.equals(allowanceType.getIsInsuranceBased()));
			ps.setLong(6, allowanceType.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}

		String sql = "UPDATE allowance_types SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("AllowanceTypeDAO.updateStatus() ERROR: " + e.getMessage());
		}

		return false;
	}

	private void appendFilters(StringBuilder sql, List<Object> params, String keyword, Boolean isActive) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ? OR description LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isActive != null) {
			sql.append(" AND is_active = ?");
			params.add(isActive);
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private AllowanceType mapRow(ResultSet rs) throws SQLException {
		AllowanceType allowanceType = new AllowanceType();
		allowanceType.setId(rs.getLong("id"));
		allowanceType.setCode(rs.getString("code"));
		allowanceType.setName(rs.getString("name"));
		allowanceType.setDescription(rs.getString("description"));
		allowanceType.setIsTaxable(rs.getBoolean("is_taxable"));
		allowanceType.setIsInsuranceBased(rs.getBoolean("is_insurance_based"));
		allowanceType.setIsActive(rs.getBoolean("is_active"));
		allowanceType.setCreatedAt(rs.getTimestamp("created_at"));
		allowanceType.setUpdatedAt(rs.getTimestamp("updated_at"));
		return allowanceType;
	}
}
