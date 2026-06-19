package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.ContractDetail;
import dto.ContractListItem;
import model.Contract;

/**
 * DAO for the Iter 2 employee-contracts table. Keep all SQL here and follow the
 * neutral return convention: empty list / null / false / 0.
 */
public class ContractDAO {

	private static final String SELECT_DETAIL_SQL = """
			SELECT c.id, c.user_id, u.employee_code, u.full_name,
			       d.name AS department_name, jt.name AS job_title_name,
			       m.full_name AS manager_name,
			       c.contract_type_id, ct.code AS contract_type_code, ct.name AS contract_type_name,
			       c.start_date, c.end_date, c.salary, c.file_path, c.status,
			       c.terminated_at, c.terminated_by, tb.full_name AS terminated_by_name,
			       c.terminate_reason,
			       c.renewal_of_id, rc.id AS renewal_of_code,
			       rc.start_date AS renewal_of_start_date, rc.end_date AS renewal_of_end_date,
			       c.created_at, c.updated_at
			  FROM contracts c
			  JOIN users u        ON u.id = c.user_id
			  LEFT JOIN departments d  ON d.id = u.department_id
			  LEFT JOIN job_titles jt  ON jt.id = u.job_title_id
			  LEFT JOIN users m        ON m.id = u.manager_id
			  JOIN contract_types ct   ON ct.id = c.contract_type_id
			  LEFT JOIN users tb       ON tb.id = c.terminated_by
			  LEFT JOIN contracts rc   ON rc.id = c.renewal_of_id
			""";

	// ============================ LIST ============================

	public List<ContractListItem> searchContracts(String keyword, String status, int offset, int limit) {
		StringBuilder sql = new StringBuilder("""
				SELECT c.id, c.user_id, u.employee_code, u.full_name,
				       d.name AS department_name,
				       ct.code AS contract_type_code, ct.name AS contract_type_name,
				       c.start_date, c.end_date, c.salary, c.status, c.file_path
				  FROM contracts c
				  JOIN users u           ON u.id = c.user_id
				  LEFT JOIN departments d ON d.id = u.department_id
				  JOIN contract_types ct ON ct.id = c.contract_type_id
				""");

		List<Object> params = new ArrayList<>();
		appendFilter(sql, params, keyword, status);
		sql.append(" ORDER BY c.end_date IS NULL DESC, c.end_date ASC, c.id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		return queryListItem(sql.toString(), params);
	}

	public int countContracts(String keyword, String status) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM contracts c");
		List<Object> params = new ArrayList<>();
		appendFilter(sql, params, keyword, status);
		return count(sql.toString(), params);
	}

	public List<ContractListItem> findExpiringSoon(int daysAhead, int offset, int limit) {
		String sql = """
				SELECT c.id, c.user_id, u.employee_code, u.full_name,
				       d.name AS department_name,
				       ct.code AS contract_type_code, ct.name AS contract_type_name,
				       c.start_date, c.end_date, c.salary, c.status, c.file_path,
				       DATEDIFF(c.end_date, CURRENT_DATE) AS days_remaining
				  FROM contracts c
				  JOIN users u           ON u.id = c.user_id
				  LEFT JOIN departments d ON d.id = u.department_id
				  JOIN contract_types ct ON ct.id = c.contract_type_id
				 WHERE c.status = 'ACTIVE'
				   AND c.end_date IS NOT NULL
				   AND c.end_date >= CURRENT_DATE
				   AND c.end_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
				 ORDER BY c.end_date ASC, c.id ASC
				 LIMIT ? OFFSET ?""";
		return queryListItem(sql, List.of(daysAhead, limit, offset));
	}

	/**
	 * Convenience overload - returns up to {@code maxRows} expiring contracts
	 * without pagination, sorted soonest first. For the /contract-expiry screen.
	 */
	public List<ContractListItem> findExpiringSoon(int daysAhead, int maxRows) {
		return findExpiringSoon(daysAhead, 0, maxRows);
	}

	public int countExpiringSoon(int daysAhead) {
		String sql = """
				SELECT COUNT(*) FROM contracts c
				 WHERE c.status = 'ACTIVE'
				   AND c.end_date IS NOT NULL
				   AND c.end_date >= CURRENT_DATE
				   AND c.end_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)""";
		return count(sql, List.of(daysAhead));
	}

	// ============================ DETAIL ============================

	public ContractDetail getDetail(Long id) {
		if (id == null) {
			return null;
		}
		String sql = SELECT_DETAIL_SQL + " WHERE c.id = ?";
		return queryDetail(sql, List.of(id));
	}

	public Contract getById(Long id) {
		if (id == null) {
			return null;
		}
		String sql = """
				SELECT id, user_id, contract_type_id, start_date, end_date, salary,
				       file_path, status, terminated_at, terminated_by, terminate_reason,
				       renewal_of_id, created_at, updated_at
				  FROM contracts WHERE id = ?""";
		return queryOne(sql, List.of(id));
	}

	public Contract getActiveByUser(Long userId) {
		if (userId == null) {
			return null;
		}
		String sql = """
				SELECT id, user_id, contract_type_id, start_date, end_date, salary,
				       file_path, status, terminated_at, terminated_by, terminate_reason,
				       renewal_of_id, created_at, updated_at
				  FROM contracts
				 WHERE user_id = ? AND status = 'ACTIVE'
				 ORDER BY start_date DESC, id DESC
				 LIMIT 1""";
		return queryOne(sql, List.of(userId));
	}

	// ============================ MUTATION ============================

	public boolean insert(Contract c) {
		if (c == null || c.getUserId() == null || c.getContractTypeId() == null || c.getStartDate() == null) {
			return false;
		}
		String sql = """
				INSERT INTO contracts
				  (user_id, contract_type_id, start_date, end_date, salary, file_path, status,
				   terminated_at, terminated_by, terminate_reason, renewal_of_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, c.getUserId());
			ps.setLong(2, c.getContractTypeId());
			ps.setDate(3, c.getStartDate());
			ps.setDate(4, c.getEndDate());
			if (c.getSalary() != null) {
				ps.setBigDecimal(5, c.getSalary());
			} else {
				ps.setNull(5, java.sql.Types.DECIMAL);
			}
			ps.setString(6, c.getFilePath());
			ps.setString(7, c.getStatus() == null ? Contract.Status.ACTIVE.name() : c.getStatus().name());
			ps.setDate(8, c.getTerminatedAt());
			if (c.getTerminatedBy() != null) {
				ps.setLong(9, c.getTerminatedBy());
			} else {
				ps.setNull(9, java.sql.Types.BIGINT);
			}
			ps.setString(10, c.getTerminateReason());
			if (c.getRenewalOfId() != null) {
				ps.setLong(11, c.getRenewalOfId());
			} else {
				ps.setNull(11, java.sql.Types.BIGINT);
			}
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractDAO.insert() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean update(Contract c) {
		if (c == null || c.getId() == null) {
			return false;
		}
		String sql = """
				UPDATE contracts
				   SET contract_type_id = ?, start_date = ?, end_date = ?, salary = ?
				 WHERE id = ?""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, c.getContractTypeId());
			ps.setDate(2, c.getStartDate());
			ps.setDate(3, c.getEndDate());
			if (c.getSalary() != null) {
				ps.setBigDecimal(4, c.getSalary());
			} else {
				ps.setNull(4, java.sql.Types.DECIMAL);
			}
			ps.setLong(5, c.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractDAO.update() ERROR: " + e.getMessage());
			return false;
		}
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
			System.err.println("ContractDAO.updateFilePath() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean updateStatus(Long id, Contract.Status status) {
		if (id == null || status == null) {
			return false;
		}
		String sql = "UPDATE contracts SET status = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractDAO.updateStatus() ERROR: " + e.getMessage());
			return false;
		}
	}

	public boolean terminate(Long id, Date terminatedAt, Long terminatedBy, String reason) {
		if (id == null) {
			return false;
		}
		String sql = """
				UPDATE contracts
				   SET status = 'TERMINATED',
				       terminated_at = ?, terminated_by = ?, terminate_reason = ?
				 WHERE id = ? AND status <> 'TERMINATED'""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, terminatedAt);
			if (terminatedBy != null) {
				ps.setLong(2, terminatedBy);
			} else {
				ps.setNull(2, java.sql.Types.BIGINT);
			}
			ps.setString(3, reason);
			ps.setLong(4, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractDAO.terminate() ERROR: " + e.getMessage());
			return false;
		}
	}

	public int markExpired() {
		String sql = """
				UPDATE contracts
				   SET status = 'EXPIRED'
				 WHERE status = 'ACTIVE'
				   AND end_date IS NOT NULL
				   AND end_date < CURRENT_DATE""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			return ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("ContractDAO.markExpired() ERROR: " + e.getMessage());
			return 0;
		}
	}

	// ============================ HELPERS ============================

	private void appendFilter(StringBuilder sql, List<Object> params, String keyword, String status) {
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		boolean hasStatus = status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status);
		if (!hasKeyword && !hasStatus) {
			return;
		}
		sql.append(" WHERE ");
		if (hasKeyword) {
			sql.append("(u.employee_code LIKE ? OR u.full_name LIKE ? OR ct.name LIKE ?)");
			String like = "%" + keyword.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (hasStatus) {
			if (hasKeyword) {
				sql.append(" AND ");
			}
			sql.append("c.status = ?");
			params.add(status.trim().toUpperCase());
		}
	}

	private List<ContractListItem> queryListItem(String sql, List<Object> params) {
		List<ContractListItem> list = new ArrayList<>();
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ContractListItem item = new ContractListItem();
					item.setId(rs.getLong("id"));
					item.setUserId(rs.getLong("user_id"));
					item.setEmployeeCode(rs.getString("employee_code"));
					item.setFullName(rs.getString("full_name"));
					item.setDepartmentName(rs.getString("department_name"));
					item.setContractTypeCode(rs.getString("contract_type_code"));
					item.setContractTypeName(rs.getString("contract_type_name"));
					item.setStartDate(rs.getDate("start_date"));
					item.setEndDate(rs.getDate("end_date"));
					item.setSalary(rs.getBigDecimal("salary"));
					item.setStatus(rs.getString("status"));
					item.setFilePath(rs.getString("file_path"));
					try {
						int days = rs.getInt("days_remaining");
						if (!rs.wasNull()) {
							item.setDaysRemaining(days);
						}
					} catch (SQLException ignored) {
						// Column not present in SELECT (e.g. legacy list query)
					}
					list.add(item);
				}
			}
		} catch (SQLException e) {
			System.err.println("ContractDAO.queryListItem() ERROR: " + e.getMessage());
		}
		return list;
	}

	private ContractDetail queryDetail(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ContractDetail d = new ContractDetail();
					d.setId(rs.getLong("id"));
					d.setUserId(rs.getLong("user_id"));
					d.setEmployeeCode(rs.getString("employee_code"));
					d.setFullName(rs.getString("full_name"));
					d.setDepartmentName(rs.getString("department_name"));
					d.setJobTitleName(rs.getString("job_title_name"));
					d.setManagerName(rs.getString("manager_name"));
					d.setContractTypeId(rs.getLong("contract_type_id"));
					d.setContractTypeCode(rs.getString("contract_type_code"));
					d.setContractTypeName(rs.getString("contract_type_name"));
					d.setStartDate(rs.getDate("start_date"));
					d.setEndDate(rs.getDate("end_date"));
					BigDecimal sal = rs.getBigDecimal("salary");
					d.setSalary(sal);
					d.setFilePath(rs.getString("file_path"));
					d.setStatus(rs.getString("status"));
					d.setTerminatedAt(rs.getDate("terminated_at"));
					long tb = rs.getLong("terminated_by");
					d.setTerminatedBy(rs.wasNull() ? null : tb);
					d.setTerminatedByName(rs.getString("terminated_by_name"));
					d.setTerminateReason(rs.getString("terminate_reason"));
					long renewalId = rs.getLong("renewal_of_id");
					d.setRenewalOfId(rs.wasNull() ? null : renewalId);
					long renewalCode = rs.getLong("renewal_of_code");
					d.setRenewalOfCode(rs.wasNull() ? null : String.valueOf(renewalCode));
					d.setRenewalOfStartDate(rs.getDate("renewal_of_start_date"));
					d.setRenewalOfEndDate(rs.getDate("renewal_of_end_date"));
					d.setCreatedAt(rs.getTimestamp("created_at"));
					d.setUpdatedAt(rs.getTimestamp("updated_at"));
					return d;
				}
			}
		} catch (SQLException e) {
			System.err.println("ContractDAO.queryDetail() ERROR: " + e.getMessage());
		}
		return null;
	}

	private Contract queryOne(String sql, List<Object> params) {
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("ContractDAO.queryOne() ERROR: " + e.getMessage());
		}
		return null;
	}

	private Contract mapRow(ResultSet rs) throws SQLException {
		Contract c = new Contract();
		c.setId(rs.getLong("id"));
		c.setUserId(rs.getLong("user_id"));
		c.setContractTypeId(rs.getLong("contract_type_id"));
		c.setStartDate(rs.getDate("start_date"));
		c.setEndDate(rs.getDate("end_date"));
		c.setSalary(rs.getBigDecimal("salary"));
		c.setFilePath(rs.getString("file_path"));
		String status = rs.getString("status");
		c.setStatus(status == null ? null : Contract.Status.valueOf(status));
		c.setTerminatedAt(rs.getDate("terminated_at"));
		long tb = rs.getLong("terminated_by");
		c.setTerminatedBy(rs.wasNull() ? null : tb);
		c.setTerminateReason(rs.getString("terminate_reason"));
		long rid = rs.getLong("renewal_of_id");
		c.setRenewalOfId(rs.wasNull() ? null : rid);
		c.setCreatedAt(rs.getTimestamp("created_at"));
		c.setUpdatedAt(rs.getTimestamp("updated_at"));
		return c;
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
			System.err.println("ContractDAO.count() ERROR: " + e.getMessage());
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
}
