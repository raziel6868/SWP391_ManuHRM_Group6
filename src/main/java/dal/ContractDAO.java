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
			SELECT c.id, COALESCE(c.contract_code, CONCAT('HĐLĐ', LPAD(c.id, 2, '0'))) AS contract_code,
			       c.user_id, u.employee_code, u.full_name,
			       d.name AS department_name, jt.name AS job_title_name,
			       m.full_name AS manager_name,
			       c.contract_type_id, ct.code AS contract_type_code, ct.name AS contract_type_name,
			       c.start_date, c.end_date, c.salary, c.file_path, c.status,
			       c.terminated_at, c.terminated_by, tb.full_name AS terminated_by_name,
			       c.terminate_reason,
			       c.renewal_of_id,
			       COALESCE(rc.contract_code, CONCAT('HĐLĐ', LPAD(rc.id, 2, '0'))) AS renewal_of_code,
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
		return searchContracts(keyword, status, null, offset, limit);
	}

	public List<ContractListItem> searchContracts(String keyword, String status, Long userId, int offset, int limit) {
		StringBuilder sql = new StringBuilder("""
				SELECT c.id, COALESCE(c.contract_code, CONCAT('HĐLĐ', LPAD(c.id, 2, '0'))) AS contract_code,
				       c.user_id, u.employee_code, u.full_name,
				       d.name AS department_name,
				       ct.code AS contract_type_code, ct.name AS contract_type_name,
				       c.start_date, c.end_date, c.salary, c.status, c.file_path
				  FROM contracts c
				  JOIN users u           ON u.id = c.user_id
				  LEFT JOIN departments d ON d.id = u.department_id
				  JOIN contract_types ct ON ct.id = c.contract_type_id
				""");

		List<Object> params = new ArrayList<>();
		appendFilter(sql, params, keyword, status, userId);
		sql.append(" ORDER BY c.end_date IS NULL DESC, c.end_date ASC, c.id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		return queryListItem(sql.toString(), params);
	}

	public int countContracts(String keyword, String status) {
		return countContracts(keyword, status, null);
	}

	public int countContracts(String keyword, String status, Long userId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*) FROM contracts c
				JOIN users u ON u.id = c.user_id
				JOIN contract_types ct ON ct.id = c.contract_type_id
				""");
		List<Object> params = new ArrayList<>();
		appendFilter(sql, params, keyword, status, userId);
		return count(sql.toString(), params);
	}

	public List<ContractListItem> findExpiringSoon(int daysAhead, int offset, int limit) {
		String sql = """
				SELECT c.id, COALESCE(c.contract_code, CONCAT('HĐLĐ', LPAD(c.id, 2, '0'))) AS contract_code,
				       c.user_id, u.employee_code, u.full_name,
				       d.name AS department_name,
				       ct.code AS contract_type_code, ct.name AS contract_type_name,
				       c.start_date, c.end_date, c.salary, c.status, c.file_path,
				       DATEDIFF(c.end_date, CURRENT_DATE) AS days_remaining
				  FROM contracts c
				  JOIN users u           ON u.id = c.user_id
				  LEFT JOIN departments d ON d.id = u.department_id
				  JOIN contract_types ct ON ct.id = c.contract_type_id
				 WHERE c.status IN ('ACTIVE', 'EXPIRING_SOON')
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
		return countExpiringSoon(daysAhead, null);
	}

	public int countExpiringSoon(int daysAhead, Long userId) {
		String sql = """
				SELECT COUNT(*) FROM contracts c
				 WHERE c.status IN ('ACTIVE', 'EXPIRING_SOON')
				   AND c.end_date IS NOT NULL
				   AND c.end_date >= CURRENT_DATE
				   AND c.end_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)""";
		List<Object> params = new ArrayList<>();
		params.add(daysAhead);
		if (userId != null) {
			sql += " AND c.user_id = ?";
			params.add(userId);
		}
		return count(sql, params);
	}

	// ============================ DETAIL ============================

	public ContractDetail getDetail(Long id) {
		if (id == null) {
			return null;
		}
		String sql = SELECT_DETAIL_SQL + " WHERE c.id = ?";
		return queryDetail(sql, List.of(id));
	}

	public ContractDetail getLatestDetailByUser(Long userId) {
		if (userId == null) {
			return null;
		}
		String sql = SELECT_DETAIL_SQL + """
				WHERE c.user_id = ?
				ORDER BY CASE c.status
				             WHEN 'ACTIVE' THEN 1
				             WHEN 'EXPIRING_SOON' THEN 2
				             WHEN 'PENDING_RENEWAL' THEN 3
				             WHEN 'EXPIRED' THEN 4
				             ELSE 4
				         END,
				         c.start_date DESC, c.id DESC
				LIMIT 1""";
		return queryDetail(sql, List.of(userId));
	}

	public Contract getById(Long id) {
		if (id == null) {
			return null;
		}
		String sql = """
				SELECT id, contract_code, user_id, contract_type_id, start_date, end_date, salary,
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
				SELECT id, contract_code, user_id, contract_type_id, start_date, end_date, salary,
				       file_path, status, terminated_at, terminated_by, terminate_reason,
				       renewal_of_id, created_at, updated_at
				  FROM contracts
				 WHERE user_id = ? AND status IN ('ACTIVE', 'EXPIRING_SOON', 'PENDING_RENEWAL')
				 ORDER BY start_date DESC, id DESC
				 LIMIT 1""";
		return queryOne(sql, List.of(userId));
	}

	public int countFixedTermByUser(Long userId) {
		if (userId == null) {
			return 0;
		}
		String sql = """
				SELECT COUNT(*)
				  FROM contracts c
				  JOIN contract_types ct ON ct.id = c.contract_type_id
				 WHERE c.user_id = ?
				   AND ct.code = 'FIXED_TERM'""";
		return count(sql, List.of(userId));
	}

	public boolean hasUnterminatedIndefiniteByUser(Long userId) {
		if (userId == null) {
			return false;
		}
		String sql = """
				SELECT COUNT(*)
				  FROM contracts c
				  JOIN contract_types ct ON ct.id = c.contract_type_id
				 WHERE c.user_id = ?
				   AND ct.code = 'INDEFINITE'
				   AND c.status <> 'TERMINATED'""";
		return count(sql, List.of(userId)) > 0;
	}

	// ============================ MUTATION ============================

	public boolean insert(Contract c) {
		return insertReturningId(c) != null;
	}

	/**
	 * Like {@link #insert(Contract)} but also returns the auto-generated primary
	 * key of the newly inserted row. Returns null on failure.
	 */
	public Long insertReturningId(Contract c) {
		if (c == null || c.getUserId() == null || c.getContractTypeId() == null || c.getStartDate() == null) {
			return null;
		}
		String sql = """
				INSERT INTO contracts
				  (user_id, contract_type_id, start_date, end_date, salary, file_path, status,
				   terminated_at, terminated_by, terminate_reason, renewal_of_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			conn.setAutoCommit(false);
			try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
				int rows = ps.executeUpdate();
				if (rows > 0) {
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (rs.next()) {
							Long newId = rs.getLong(1);
							if (updateGeneratedCode(conn, newId)) {
								conn.commit();
								return newId;
							}
						}
					}
				}
				conn.rollback();
				return null;
			}
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException rollbackError) {
					System.err.println("ContractDAO.insertReturningId() rollback ERROR: " + rollbackError.getMessage());
				}
			}
			System.err.println("ContractDAO.insertReturningId() ERROR: " + e.getMessage());
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException closeError) {
					System.err.println("ContractDAO.insertReturningId() close ERROR: " + closeError.getMessage());
				}
			}
		}
	}

	public Long renewReturningId(Long previousId, Contract renewed) {
		if (previousId == null || renewed == null || renewed.getUserId() == null || renewed.getContractTypeId() == null
				|| renewed.getStartDate() == null) {
			return null;
		}
		String expireSql = """
				UPDATE contracts
				   SET status = 'EXPIRED',
				       updated_at = CURRENT_TIMESTAMP
				 WHERE id = ?
				   AND status IN ('ACTIVE', 'EXPIRING_SOON', 'EXPIRED', 'PENDING_RENEWAL')""";
		String insertSql = """
				INSERT INTO contracts
				  (user_id, contract_type_id, start_date, end_date, salary, file_path, status,
				   terminated_at, terminated_by, terminate_reason, renewal_of_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			conn.setAutoCommit(false);
			try (PreparedStatement expirePs = conn.prepareStatement(expireSql)) {
				expirePs.setLong(1, previousId);
				if (expirePs.executeUpdate() == 0) {
					conn.rollback();
					return null;
				}
			}
			try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
				ps.setLong(1, renewed.getUserId());
				ps.setLong(2, renewed.getContractTypeId());
				ps.setDate(3, renewed.getStartDate());
				ps.setDate(4, renewed.getEndDate());
				if (renewed.getSalary() != null) {
					ps.setBigDecimal(5, renewed.getSalary());
				} else {
					ps.setNull(5, java.sql.Types.DECIMAL);
				}
				ps.setString(6, renewed.getFilePath());
				ps.setString(7,
						renewed.getStatus() == null ? Contract.Status.ACTIVE.name() : renewed.getStatus().name());
				ps.setDate(8, renewed.getTerminatedAt());
				if (renewed.getTerminatedBy() != null) {
					ps.setLong(9, renewed.getTerminatedBy());
				} else {
					ps.setNull(9, java.sql.Types.BIGINT);
				}
				ps.setString(10, renewed.getTerminateReason());
				ps.setLong(11, previousId);
				int rows = ps.executeUpdate();
				if (rows == 0) {
					conn.rollback();
					return null;
				}
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) {
						Long newId = rs.getLong(1);
						if (!updateGeneratedCode(conn, newId)) {
							conn.rollback();
							return null;
						}
						conn.commit();
						return newId;
					}
				}
			}
			conn.rollback();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException rollbackError) {
					System.err.println("ContractDAO.renewReturningId() rollback ERROR: " + rollbackError.getMessage());
				}
			}
			System.err.println("ContractDAO.renewReturningId() ERROR: " + e.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException closeError) {
					System.err.println("ContractDAO.renewReturningId() close ERROR: " + closeError.getMessage());
				}
			}
		}
		return null;
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

	public boolean requestRenewal(Long id, Long userId) {
		if (id == null || userId == null) {
			return false;
		}
		String sql = """
				UPDATE contracts
				   SET status = 'PENDING_RENEWAL'
				 WHERE id = ?
				   AND user_id = ?
				   AND status IN ('ACTIVE', 'EXPIRING_SOON')""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, userId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("ContractDAO.requestRenewal() ERROR: " + e.getMessage());
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
				 WHERE status IN ('ACTIVE', 'EXPIRING_SOON')
				   AND end_date IS NOT NULL
				   AND end_date < CURRENT_DATE""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			return ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("ContractDAO.markExpired() ERROR: " + e.getMessage());
			return 0;
		}
	}

	public int markExpiringSoon(int daysAhead) {
		String sql = """
				UPDATE contracts
				   SET status = 'EXPIRING_SOON'
				 WHERE status = 'ACTIVE'
				   AND end_date IS NOT NULL
				   AND end_date >= CURRENT_DATE
				   AND end_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, daysAhead);
			return ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("ContractDAO.markExpiringSoon() ERROR: " + e.getMessage());
			return 0;
		}
	}

	public int restoreActiveFromExpiringSoon(int daysAhead) {
		String sql = """
				UPDATE contracts
				   SET status = 'ACTIVE'
				 WHERE status = 'EXPIRING_SOON'
				   AND (end_date IS NULL OR end_date > DATE_ADD(CURRENT_DATE, INTERVAL ? DAY))""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, daysAhead);
			return ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("ContractDAO.restoreActiveFromExpiringSoon() ERROR: " + e.getMessage());
			return 0;
		}
	}

	public void refreshLifecycleStatuses() {
		markExpired();
		restoreActiveFromExpiringSoon(30);
		markExpiringSoon(30);
	}

	// ============================ HELPERS ============================

	private void appendFilter(StringBuilder sql, List<Object> params, String keyword, String status, Long userId) {
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		boolean hasStatus = status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status);
		boolean hasUser = userId != null;
		if (!hasKeyword && !hasStatus && !hasUser) {
			return;
		}
		sql.append(" WHERE ");
		boolean needsAnd = false;
		if (hasKeyword) {
			sql.append("(c.contract_code LIKE ? OR u.employee_code LIKE ? OR u.full_name LIKE ? OR ct.name LIKE ?)");
			String like = "%" + keyword.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
			params.add(like);
			needsAnd = true;
		}
		if (hasStatus) {
			if (needsAnd) {
				sql.append(" AND ");
			}
			sql.append("c.status = ?");
			params.add(status.trim().toUpperCase());
			needsAnd = true;
		}
		if (hasUser) {
			if (needsAnd) {
				sql.append(" AND ");
			}
			sql.append("c.user_id = ?");
			params.add(userId);
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
					item.setContractCode(rs.getString("contract_code"));
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
					d.setContractCode(rs.getString("contract_code"));
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
					Long terminatedByVal = rs.getObject("terminated_by", Long.class);
					d.setTerminatedBy(terminatedByVal);
					d.setTerminatedByName(rs.getString("terminated_by_name"));
					d.setTerminateReason(rs.getString("terminate_reason"));
					Long renewalOfIdVal = rs.getObject("renewal_of_id", Long.class);
					d.setRenewalOfId(renewalOfIdVal);
					d.setRenewalOfCode(rs.getString("renewal_of_code"));
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
		c.setContractCode(rs.getString("contract_code"));
		c.setUserId(rs.getLong("user_id"));
		c.setContractTypeId(rs.getLong("contract_type_id"));
		c.setStartDate(rs.getDate("start_date"));
		c.setEndDate(rs.getDate("end_date"));
		c.setSalary(rs.getBigDecimal("salary"));
		c.setFilePath(rs.getString("file_path"));
		String status = rs.getString("status");
		c.setStatus(status == null ? null : Contract.Status.valueOf(status));
		c.setTerminatedAt(rs.getDate("terminated_at"));
		Long terminatedByVal = rs.getObject("terminated_by", Long.class);
		c.setTerminatedBy(terminatedByVal);
		c.setTerminateReason(rs.getString("terminate_reason"));
		Long renewalOfIdVal = rs.getObject("renewal_of_id", Long.class);
		c.setRenewalOfId(renewalOfIdVal);
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

	private boolean updateGeneratedCode(Connection conn, Long id) throws SQLException {
		if (conn == null || id == null) {
			return false;
		}
		String sql = "UPDATE contracts SET contract_code = CONCAT('HĐLĐ', LPAD(?, 2, '0')) WHERE id = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		}
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
