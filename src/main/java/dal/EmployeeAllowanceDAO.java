package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.EmployeeAllowance;

public class EmployeeAllowanceDAO {

	public static final String ATTENDANCE_BONUS_CODE = "ATTENDANCE_BONUS";

	private static final String SELECT_BASE = """
			SELECT ea.id, ea.user_id, ea.allowance_type_id, ea.amount,
			       ea.effective_from, ea.effective_to, ea.is_active,
			       ea.created_at, ea.updated_at,
			       u.employee_code, u.full_name AS employee_name,
			       d.name AS department_name,
			       at.code AS allowance_code, at.name AS allowance_name,
			       at.is_active AS allowance_type_active,
			       at.is_taxable, at.is_insurance_based
			FROM employee_allowances ea
			JOIN users u ON ea.user_id = u.id
			LEFT JOIN departments d ON u.department_id = d.id
			JOIN allowance_types at ON ea.allowance_type_id = at.id
			""";

	public List<EmployeeAllowance> search(String keyword, Long departmentId, Boolean isActive, int offset, int limit) {
		List<EmployeeAllowance> allowances = new ArrayList<>();
		StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1 = 1");
		List<Object> params = new ArrayList<>();

		appendFilters(sql, params, keyword, departmentId, isActive);
		sql.append(" ORDER BY u.full_name ASC, at.name ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					allowances.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.search() ERROR: " + e.getMessage());
		}

		return allowances;
	}

	public int count(String keyword, Long departmentId, Boolean isActive) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM employee_allowances ea
				JOIN users u ON ea.user_id = u.id
				JOIN allowance_types at ON ea.allowance_type_id = at.id
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		appendFilters(sql, params, keyword, departmentId, isActive);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.count() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public EmployeeAllowance getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = SELECT_BASE + " WHERE ea.id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean insert(EmployeeAllowance allowance) {
		if (allowance == null || allowance.getUserId() == null || allowance.getAllowanceTypeId() == null
				|| allowance.getAmount() == null || allowance.getEffectiveFrom() == null) {
			return false;
		}

		String sql = """
				INSERT INTO employee_allowances
				    (user_id, allowance_type_id, amount, effective_from, effective_to, is_active)
				VALUES (?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, allowance);
			ps.setBoolean(6, allowance.getIsActive() == null || allowance.getIsActive());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.insert() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean update(EmployeeAllowance allowance) {
		if (allowance == null || allowance.getId() == null || allowance.getUserId() == null
				|| allowance.getAllowanceTypeId() == null || allowance.getAmount() == null
				|| allowance.getEffectiveFrom() == null) {
			return false;
		}

		String sql = """
				UPDATE employee_allowances
				SET user_id = ?, allowance_type_id = ?, amount = ?,
				    effective_from = ?, effective_to = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, allowance);
			ps.setLong(6, allowance.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}

		String sql = "UPDATE employee_allowances SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.updateStatus() ERROR: " + e.getMessage());
		}

		return false;
	}

	public BigDecimal sumActiveAllowances(Long userId, int year, int month) {
		return sumAllowances(userId, year, month, null, true);
	}

	public BigDecimal sumActiveAllowances(Long userId, int year, int month, boolean includeAttendanceBonus) {
		return sumAllowances(userId, year, month, null, includeAttendanceBonus);
	}

	public BigDecimal sumInsuranceBasedAllowances(Long userId, int year, int month) {
		return sumAllowances(userId, year, month, "INSURANCE", true);
	}

	public BigDecimal sumInsuranceBasedAllowances(Long userId, int year, int month, boolean includeAttendanceBonus) {
		return sumAllowances(userId, year, month, "INSURANCE", includeAttendanceBonus);
	}

	public BigDecimal sumTaxableAllowances(Long userId, int year, int month) {
		return sumAllowances(userId, year, month, "TAXABLE", true);
	}

	public BigDecimal sumNonTaxableAllowances(Long userId, int year, int month) {
		return sumAllowances(userId, year, month, "NON_TAXABLE", true);
	}

	public BigDecimal sumNonTaxableAllowances(Long userId, int year, int month, boolean includeAttendanceBonus) {
		return sumAllowances(userId, year, month, "NON_TAXABLE", includeAttendanceBonus);
	}

	private BigDecimal sumAllowances(Long userId, int year, int month, String mode, boolean includeAttendanceBonus) {
		if (userId == null) {
			return BigDecimal.ZERO;
		}

		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());

		StringBuilder sql = new StringBuilder("""
				SELECT COALESCE(SUM(ea.amount), 0) AS total_allowance
				FROM employee_allowances ea
				JOIN allowance_types at ON ea.allowance_type_id = at.id
				WHERE ea.user_id = ?
				  AND ea.is_active = TRUE
				  AND at.is_active = TRUE
				  AND ea.effective_from <= ?
				  AND (ea.effective_to IS NULL OR ea.effective_to >= ?)
				""");
		List<Object> params = new ArrayList<>();
		params.add(userId);
		params.add(lastDay);
		params.add(firstDay);

		if (!includeAttendanceBonus) {
			sql.append(" AND at.code <> ?");
			params.add(ATTENDANCE_BONUS_CODE);
		}

		if ("INSURANCE".equals(mode)) {
			sql.append(" AND at.is_insurance_based = TRUE");
		} else if ("TAXABLE".equals(mode)) {
			sql.append(" AND at.is_taxable = TRUE");
		} else if ("NON_TAXABLE".equals(mode)) {
			sql.append(" AND at.is_taxable = FALSE");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					BigDecimal total = rs.getBigDecimal("total_allowance");
					return total != null ? total : BigDecimal.ZERO;
				}
			}
		} catch (SQLException e) {
			System.err.println("EmployeeAllowanceDAO.sumAllowances() ERROR: " + e.getMessage());
		}

		return BigDecimal.ZERO;
	}

	private void appendFilters(StringBuilder sql, List<Object> params, String keyword, Long departmentId,
			Boolean isActive) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR at.code LIKE ? OR at.name LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (departmentId != null) {
			sql.append(" AND u.department_id = ?");
			params.add(departmentId);
		}

		if (isActive != null) {
			sql.append(" AND ea.is_active = ?");
			params.add(isActive);
		}
	}

	private void setMutationParams(PreparedStatement ps, EmployeeAllowance allowance) throws SQLException {
		ps.setLong(1, allowance.getUserId());
		ps.setLong(2, allowance.getAllowanceTypeId());
		ps.setBigDecimal(3, allowance.getAmount());
		ps.setDate(4, allowance.getEffectiveFrom());
		if (allowance.getEffectiveTo() != null) {
			ps.setDate(5, allowance.getEffectiveTo());
		} else {
			ps.setNull(5, java.sql.Types.DATE);
		}
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private EmployeeAllowance mapRow(ResultSet rs) throws SQLException {
		EmployeeAllowance allowance = new EmployeeAllowance();
		allowance.setId(rs.getLong("id"));
		allowance.setUserId(rs.getLong("user_id"));
		allowance.setAllowanceTypeId(rs.getLong("allowance_type_id"));
		allowance.setAmount(rs.getBigDecimal("amount"));
		allowance.setEffectiveFrom(rs.getDate("effective_from"));
		allowance.setEffectiveTo(rs.getDate("effective_to"));
		allowance.setIsActive(rs.getBoolean("is_active"));
		allowance.setCreatedAt(rs.getTimestamp("created_at"));
		allowance.setUpdatedAt(rs.getTimestamp("updated_at"));
		allowance.setEmployeeCode(rs.getString("employee_code"));
		allowance.setEmployeeName(rs.getString("employee_name"));
		allowance.setDepartmentName(rs.getString("department_name"));
		allowance.setAllowanceCode(rs.getString("allowance_code"));
		allowance.setAllowanceName(rs.getString("allowance_name"));
		allowance.setAllowanceTypeActive(rs.getBoolean("allowance_type_active"));
		allowance.setIsTaxable(rs.getBoolean("is_taxable"));
		allowance.setIsInsuranceBased(rs.getBoolean("is_insurance_based"));
		return allowance;
	}
}
