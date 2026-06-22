package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import model.EmployeeContract;

public class EmployeeContractDAO {

	public List<EmployeeContract> searchContracts(String keyword, String status, Long departmentId, int offset,
			int limit) {
		List<EmployeeContract> contracts = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT c.id, c.user_id, c.contract_type_id, ct.name AS contract_type_name,
				       u.full_name AS user_full_name, u.employee_code,
				       c.start_date, c.end_date, c.salary, c.file_path, c.status,
				       c.terminated_at, c.terminated_by, tb.full_name AS terminated_by_name,
				       c.terminate_reason,
				       c.created_at, c.updated_at
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				JOIN contract_types ct ON c.contract_type_id = ct.id
				LEFT JOIN users tb ON c.terminated_by = tb.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (u.full_name LIKE ? OR u.employee_code LIKE ? OR ct.name LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (status != null && !status.trim().isEmpty()) {
			sql.append(" AND c.status = ?");
			params.add(status.trim());
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		sql.append(" ORDER BY c.id DESC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					contracts.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.searchContracts() ERROR: " + e.getMessage());
		}

		return contracts;
	}

	public int countContracts(String keyword, String status, Long departmentId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				JOIN contract_types ct ON c.contract_type_id = ct.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (u.full_name LIKE ? OR u.employee_code LIKE ? OR ct.name LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (status != null && !status.trim().isEmpty()) {
			sql.append(" AND c.status = ?");
			params.add(status.trim());
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
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
			System.err.println("EmployeeContractDAO.countContracts() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public EmployeeContract getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT c.id, c.user_id, c.contract_type_id, ct.name AS contract_type_name,
				       u.full_name AS user_full_name, u.employee_code,
				       c.start_date, c.end_date, c.salary, c.file_path, c.status,
				       c.terminated_at, c.terminated_by, tb.full_name AS terminated_by_name,
				       c.terminate_reason,
				       c.created_at, c.updated_at
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				JOIN contract_types ct ON c.contract_type_id = ct.id
				LEFT JOIN users tb ON c.terminated_by = tb.id
				WHERE c.id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean insert(EmployeeContract contract) {
		if (contract == null || contract.getUserId() == null || contract.getContractTypeId() == null
				|| contract.getStartDate() == null) {
			return false;
		}

		String sql = """
				INSERT INTO contracts (user_id, contract_type_id, start_date, end_date, salary, file_path, status)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, contract.getUserId());
			ps.setLong(2, contract.getContractTypeId());
			ps.setDate(3, contract.getStartDate());
			ps.setDate(4, contract.getEndDate());
			ps.setBigDecimal(5, contract.getSalary());
			ps.setString(6, contract.getFilePath());
			ps.setString(7, contract.getStatus() != null ? contract.getStatus() : "ACTIVE");
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean update(EmployeeContract contract) {
		if (contract == null || contract.getId() == null) {
			return false;
		}

		String sql = """
				UPDATE contracts
				SET contract_type_id = ?, start_date = ?, end_date = ?, salary = ?, file_path = ?, status = ?
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, contract.getContractTypeId());
			ps.setDate(2, contract.getStartDate());
			ps.setDate(3, contract.getEndDate());
			ps.setBigDecimal(4, contract.getSalary());
			ps.setString(5, contract.getFilePath());
			ps.setString(6, contract.getStatus());
			ps.setLong(7, contract.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean renew(Long id, Date newStartDate, Date newEndDate, BigDecimal newSalary, Long renewalOfId) {
		if (id == null || newStartDate == null) {
			return false;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				String expireSql = "UPDATE contracts SET status = 'EXPIRED' WHERE id = ? AND status = 'ACTIVE'";
				try (PreparedStatement ps = conn.prepareStatement(expireSql)) {
					ps.setLong(1, id);
					int updated = ps.executeUpdate();
					if (updated == 0) {
						conn.rollback();
						return false;
					}
				}

				EmployeeContract existing = getById(id);
				if (existing == null) {
					conn.rollback();
					return false;
				}

				String insertSql = """
						INSERT INTO contracts (user_id, contract_type_id, start_date, end_date, salary, file_path, status)
						VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
						""";
				try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
					ps.setLong(1, existing.getUserId());
					ps.setLong(2, existing.getContractTypeId());
					ps.setDate(3, newStartDate);
					ps.setDate(4, newEndDate);
					ps.setBigDecimal(5, newSalary);
					ps.setString(6, existing.getFilePath());
					ps.executeUpdate();
				}

				conn.commit();
				return true;
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.renew() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateFilePath(Long id, String filePath) {
		if (id == null) {
			return false;
		}

		String sql = "UPDATE contracts SET file_path = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, filePath);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.updateFilePath() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateStatus(Long id, String status) {
		if (id == null || status == null) {
			return false;
		}

		String sql = "UPDATE contracts SET status = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.updateStatus() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean terminate(Long id, Date terminatedAt, Long terminatedBy, String terminationReason) {
		if (id == null) {
			return false;
		}

		String sql = """
				UPDATE contracts
				SET status = 'TERMINATED', terminated_at = ?, terminated_by = ?, terminate_reason = ?
				WHERE id = ? AND status NOT IN ('TERMINATED', 'EXPIRED')
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, terminatedAt);
			if (terminatedBy != null) {
				ps.setLong(2, terminatedBy);
			} else {
				ps.setNull(2, java.sql.Types.BIGINT);
			}
			ps.setString(3, terminationReason);
			ps.setLong(4, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.terminate() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean existsByUserAndActiveContract(Long userId) {
		if (userId == null) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM contracts WHERE user_id = ? AND status = 'ACTIVE'";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.existsByUserAndActiveContract() ERROR: " + e.getMessage());
		}

		return false;
	}

	public EmployeeContract getActiveContractByUserId(Long userId) {
		if (userId == null) {
			return null;
		}

		String sql = """
				SELECT c.id, c.user_id, c.contract_type_id, ct.name AS contract_type_name,
				       u.full_name AS user_full_name, u.employee_code,
				       c.start_date, c.end_date, c.salary, c.file_path, c.status,
				       c.terminated_at, c.terminated_by, tb.full_name AS terminated_by_name,
				       c.terminate_reason,
				       c.created_at, c.updated_at
				FROM contracts c
				JOIN users u ON c.user_id = u.id
				JOIN contract_types ct ON c.contract_type_id = ct.id
				LEFT JOIN users tb ON c.terminated_by = tb.id
				WHERE c.user_id = ? AND c.status = 'ACTIVE'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.getActiveContractByUserId() ERROR: " + e.getMessage());
		}

		return null;
	}

	public int countExpiringSoon(int daysAhead) {
		String sql = """
				SELECT COUNT(*)
				FROM contracts
				WHERE status = 'ACTIVE'
				  AND end_date IS NOT NULL
				  AND end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, daysAhead);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeContractDAO.countExpiringSoon() ERROR: " + e.getMessage());
		}
		return 0;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private EmployeeContract mapRow(ResultSet rs) throws SQLException {
		EmployeeContract contract = new EmployeeContract();
		contract.setId(rs.getLong("id"));
		contract.setUserId(rs.getLong("user_id"));
		contract.setContractTypeId(rs.getLong("contract_type_id"));
		contract.setContractTypeName(rs.getString("contract_type_name"));
		contract.setUserFullName(rs.getString("user_full_name"));
		contract.setEmployeeCode(rs.getString("employee_code"));
		contract.setStartDate(rs.getDate("start_date"));
		contract.setEndDate(rs.getDate("end_date"));
		contract.setSalary(rs.getBigDecimal("salary"));
		contract.setFilePath(rs.getString("file_path"));
		contract.setStatus(rs.getString("status"));
		contract.setTerminatedAt(rs.getDate("terminated_at"));
		long terminatedByVal = rs.getLong("terminated_by");
		if (!rs.wasNull()) {
			contract.setTerminatedBy(terminatedByVal);
		}
		contract.setTerminatedByName(rs.getString("terminated_by_name"));
		contract.setTerminationReason(rs.getString("terminate_reason"));
		contract.setCreatedAt(rs.getTimestamp("created_at"));
		contract.setUpdatedAt(rs.getTimestamp("updated_at"));
		return contract;
	}
}
