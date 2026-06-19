package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ContractType;

public class ContractTypeDAO {

	private static final String SELECT_COLUMNS = """
			id, code, name, description, is_active, created_at, updated_at""";
	// === QUERY: Danh sach & tim kiem ===
	public List<ContractType> searchContractTypes(String keyword, int offset, int limit) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return queryList("SELECT " + SELECT_COLUMNS + " FROM contract_types ORDER BY id ASC LIMIT ? OFFSET ?",
					List.of(limit, offset));
		}
		String like = "%" + keyword.trim() + "%";
		String sql = """
				SELECT """ + SELECT_COLUMNS + """
				 FROM contract_types
				WHERE code LIKE ? OR name LIKE ? OR description LIKE ?
				ORDER BY id ASC LIMIT ? OFFSET ?""";
		return queryList(sql, List.of(like, like, like, limit, offset));
	}

	public int countContractTypes(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return count("SELECT COUNT(*) FROM contract_types", null);
		}
		String like = "%" + keyword.trim() + "%";
		return count("""
				SELECT COUNT(*) FROM contract_types
				WHERE code LIKE ? OR name LIKE ? OR description LIKE ?""", List.of(like, like, like));
	}

	public ContractType getById(Long id) {
		if (id == null) {
			return null;
		}
		String sql = "SELECT " + SELECT_COLUMNS + " FROM contract_types WHERE id = ?";
		return queryOne(sql, List.of(id));
	}

	public List<ContractType> getActiveContractTypes() {
		String sql = "SELECT " + SELECT_COLUMNS + " FROM contract_types WHERE is_active = TRUE ORDER BY name ASC";
		return queryList(sql, null);
	}
	// === QUERY: Kiem tra ton tai ===
	public boolean existsByCode(String code) {
		if (code == null || code.isBlank()) {
			return false;
		}
		return count("SELECT COUNT(*) FROM contract_types WHERE code = ?", List.of(code.trim())) > 0;
	}

	public boolean existsByCodeExceptId(String code, Long id) {
		if (code == null || code.isBlank() || id == null) {
			return false;
		}
		return count("SELECT COUNT(*) FROM contract_types WHERE code = ? AND id <> ?", List.of(code.trim(), id)) > 0;
	}

	public boolean insert(ContractType contractType) {
		if (contractType == null || contractType.getCode() == null || contractType.getName() == null) {
			return false;
		}
		String sql = """
				INSERT INTO contract_types (code, name, description, is_active)
				VALUES (?, ?, ?, ?)""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, contractType.getCode().trim());
			ps.setString(2, contractType.getName().trim());
			ps.setString(3, contractType.getDescription());
			ps.setBoolean(4, contractType.getIsActive() == null || contractType.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractTypeDAO.insert() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean update(ContractType contractType) {
		if (contractType == null || contractType.getId() == null || contractType.getCode() == null
				|| contractType.getName() == null) {
			return false;
		}
		String sql = """
				UPDATE contract_types SET code = ?, name = ?, description = ?
				WHERE id = ?""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, contractType.getCode().trim());
			ps.setString(2, contractType.getName().trim());
			ps.setString(3, contractType.getDescription());
			ps.setLong(4, contractType.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractTypeDAO.update() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}
		String sql = "UPDATE contract_types SET is_active = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractTypeDAO.updateStatus() ERROR: " + e.getMessage());
			return false;
		}
	}

	private List<ContractType> queryList(String sql, List<Object> params) {
		List<ContractType> list = new ArrayList<>();
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("ContractTypeDAO.queryList() ERROR: " + e.getMessage());
		}
		return list;
	}

	private ContractType queryOne(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("ContractTypeDAO.queryOne() ERROR: " + e.getMessage());
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
			System.err.println("ContractTypeDAO.count() ERROR: " + e.getMessage());
		}
		return 0;
	}
	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		if (params == null || params.isEmpty()) {
			return;
		}
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private ContractType mapRow(ResultSet rs) throws SQLException {
		ContractType ct = new ContractType();
		ct.setId(rs.getLong("id"));
		ct.setCode(rs.getString("code"));
		ct.setName(rs.getString("name"));
		ct.setDescription(rs.getString("description"));
		ct.setIsActive(rs.getBoolean("is_active"));
		ct.setCreatedAt(rs.getTimestamp("created_at"));
		ct.setUpdatedAt(rs.getTimestamp("updated_at"));
		return ct;
	}

}
