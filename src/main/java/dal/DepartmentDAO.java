package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Department;

public class DepartmentDAO {

	public List<Department> getActiveDepartments() {
		List<Department> list = new ArrayList<>();
		String sql = "SELECT id, name, department_type, parent_id, is_active FROM departments WHERE is_active = TRUE ORDER BY name ASC";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.getActiveDepartments() ERROR: " + e.getMessage());
		}
		return list;
	}

	public List<Department> searchDepartments(String keyword, String departmentType, int offset, int limit) {
		List<Department> list = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT d.id, d.name, d.department_type, d.parent_id, d.is_active, " + "       p.name AS parent_name "
						+ "FROM departments d " + "LEFT JOIN departments p ON d.parent_id = p.id " + "WHERE 1=1");

		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND d.name LIKE ?");
			params.add("%" + keyword.trim() + "%");
		}
		if (departmentType != null && !departmentType.trim().isEmpty()) {
			sql.append(" AND d.department_type = ?");
			params.add(departmentType.trim().toUpperCase());
		}

		sql.append(" ORDER BY d.name ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Department d = mapRow(rs);
					try {
						d.setParentName(rs.getString("parent_name"));
					} catch (SQLException ignore) {
					}
					list.add(d);
				}
			}
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.searchDepartments() ERROR: " + e.getMessage());
		}
		return list;
	}

	public int countDepartments(String keyword, String departmentType) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM departments d WHERE 1=1");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND d.name LIKE ?");
			params.add("%" + keyword.trim() + "%");
		}
		if (departmentType != null && !departmentType.trim().isEmpty()) {
			sql.append(" AND d.department_type = ?");
			params.add(departmentType.trim().toUpperCase());
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.countDepartments() ERROR: " + e.getMessage());
		}
		return 0;
	}

	public boolean insert(Department department) {
		String sql = "INSERT INTO departments (name, department_type, parent_id, is_active) "
				+ "VALUES (?, ?, ?, TRUE)";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, department.getName().trim());
			ps.setString(2, department.getDepartmentType().name());

			if (department.getParentId() != null) {
				ps.setLong(3, department.getParentId());
			} else {
				ps.setNull(3, java.sql.Types.BIGINT);
			}

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.insert() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean existsByName(String name) {
		String sql = "SELECT COUNT(*) FROM departments WHERE LOWER(name) = LOWER(?)";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, name.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.existsByName() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean update(Department department) {
		String sql = "UPDATE departments SET name = ?, department_type = ?, parent_id = ?, "
				+ "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, department.getName().trim());
			ps.setString(2, department.getDepartmentType().name());

			if (department.getParentId() != null) {
				ps.setLong(3, department.getParentId());
			} else {
				ps.setNull(3, java.sql.Types.BIGINT);
			}

			ps.setLong(4, department.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.update() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean existsByNameExceptId(String name, Long excludeId) {
		String sql = "SELECT COUNT(*) FROM departments " + "WHERE LOWER(name) = LOWER(?) AND id != ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, name.trim());
			ps.setLong(2, excludeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.existsByNameExceptId() ERROR: " + e.getMessage());
		}
		return false;
	}

	public Department getById(Long id) {
		String sql = "SELECT id, name, department_type, parent_id, is_active " + "FROM departments WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		String sql = "UPDATE departments SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("DepartmentDAO.updateStatus() ERROR: " + e.getMessage());
		}
		return false;
	}

	private Department mapRow(ResultSet rs) throws SQLException {
		Department d = new Department();
		d.setId(rs.getLong("id"));
		d.setName(rs.getString("name"));
		d.setIsActive(rs.getBoolean("is_active"));

		long parentId = rs.getLong("parent_id");
		d.setParentId(rs.wasNull() ? null : parentId);

		String type = rs.getString("department_type");
		if (type != null) {
			d.setDepartmentType(Department.DepartmentType.valueOf(type));
		}
		return d;
	}
}
