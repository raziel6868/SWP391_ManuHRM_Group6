package dal;

import dto.PayrollAllowanceDetail;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.AllowanceRule;

public class AllowanceRuleDAO {

	private static final String SELECT_BASE = """
			SELECT ar.id, ar.allowance_type_id, ar.apply_scope, ar.employee_type,
			       ar.department_type, ar.department_id, ar.job_title_id, ar.amount,
			       ar.effective_from, ar.effective_to, ar.is_active,
			       ar.created_at, ar.updated_at,
			       at.code AS allowance_code, at.name AS allowance_name,
			       d.name AS department_name, jt.name AS job_title_name
			FROM allowance_rules ar
			JOIN allowance_types at ON ar.allowance_type_id = at.id
			LEFT JOIN departments d ON ar.department_id = d.id
			LEFT JOIN job_titles jt ON ar.job_title_id = jt.id
			""";

	public List<AllowanceRule> getActiveRulesByAllowanceTypeId(Long allowanceTypeId) {
		List<AllowanceRule> rules = new ArrayList<>();
		if (allowanceTypeId == null) {
			return rules;
		}

		String sql = SELECT_BASE + """
				WHERE ar.allowance_type_id = ?
				  AND ar.is_active = TRUE
				ORDER BY ar.apply_scope ASC, jt.name ASC, d.name ASC, ar.id ASC
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, allowanceTypeId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					rules.add(mapRule(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AllowanceRuleDAO.getActiveRulesByAllowanceTypeId() ERROR: " + e.getMessage());
		}
		return rules;
	}

	public List<PayrollAllowanceDetail> getActiveAllowanceDetails(Long userId, int year, int month) {
		List<PayrollAllowanceDetail> details = new ArrayList<>();
		if (userId == null) {
			return details;
		}

		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());

		String sql = """
				SELECT u.id AS user_id, ar.allowance_type_id,
				       at.code AS allowance_code, at.name AS allowance_name,
				       COALESCE(SUM(ar.amount), 0) AS amount,
				       at.is_taxable, at.is_insurance_based
				FROM users u
				LEFT JOIN departments d ON u.department_id = d.id
				JOIN allowance_rules ar ON ar.is_active = TRUE
				JOIN allowance_types at ON ar.allowance_type_id = at.id
				WHERE u.id = ?
				  AND u.is_active = TRUE
				  AND at.is_active = TRUE
				  AND ar.effective_from <= ?
				  AND (ar.effective_to IS NULL OR ar.effective_to >= ?)
				  AND (
				      ar.apply_scope = 'ALL'
				      OR (ar.apply_scope = 'EMPLOYEE_TYPE'
				          AND ar.employee_type = u.employee_type)
				      OR (ar.apply_scope = 'DEPARTMENT_TYPE'
				          AND ar.department_type = d.department_type
				          AND (ar.employee_type IS NULL OR ar.employee_type = u.employee_type))
				      OR (ar.apply_scope = 'DEPARTMENT'
				          AND ar.department_id = u.department_id
				          AND (ar.employee_type IS NULL OR ar.employee_type = u.employee_type))
				      OR (ar.apply_scope = 'JOB_TITLE'
				          AND ar.job_title_id = u.job_title_id)
				  )
				GROUP BY u.id, ar.allowance_type_id,
				         at.code, at.name, at.is_taxable, at.is_insurance_based
				ORDER BY at.name ASC, at.code ASC
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, lastDay);
			ps.setDate(3, firstDay);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					details.add(mapPayrollAllowanceDetail(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("AllowanceRuleDAO.getActiveAllowanceDetails() ERROR: " + e.getMessage());
		}

		return details;
	}

	public void replaceActiveRules(Connection conn, Long allowanceTypeId, List<AllowanceRule> rules)
			throws SQLException {
		if (allowanceTypeId == null) {
			return;
		}

		String deactivateSql = """
				UPDATE allowance_rules
				SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP
				WHERE allowance_type_id = ? AND is_active = TRUE
				""";
		try (PreparedStatement ps = conn.prepareStatement(deactivateSql)) {
			ps.setLong(1, allowanceTypeId);
			ps.executeUpdate();
		}

		if (rules == null || rules.isEmpty()) {
			return;
		}

		String insertSql = """
				INSERT INTO allowance_rules
				    (allowance_type_id, apply_scope, employee_type, department_type,
				     department_id, job_title_id, amount, effective_from, effective_to, is_active)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
				""";
		try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
			for (AllowanceRule rule : rules) {
				rule.setAllowanceTypeId(allowanceTypeId);
				setMutationParams(ps, rule);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void setMutationParams(PreparedStatement ps, AllowanceRule rule) throws SQLException {
		ps.setLong(1, rule.getAllowanceTypeId());
		ps.setString(2, rule.getApplyScope());
		setNullableString(ps, 3, rule.getEmployeeType());
		setNullableString(ps, 4, rule.getDepartmentType());
		setNullableLong(ps, 5, rule.getDepartmentId());
		setNullableLong(ps, 6, rule.getJobTitleId());
		ps.setBigDecimal(7, rule.getAmount());
		ps.setDate(8, rule.getEffectiveFrom());
		if (rule.getEffectiveTo() != null) {
			ps.setDate(9, rule.getEffectiveTo());
		} else {
			ps.setNull(9, Types.DATE);
		}
	}

	private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
		if (value == null || value.isBlank()) {
			ps.setNull(index, Types.VARCHAR);
			return;
		}
		ps.setString(index, value);
	}

	private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
		if (value == null) {
			ps.setNull(index, Types.BIGINT);
			return;
		}
		ps.setLong(index, value);
	}

	private AllowanceRule mapRule(ResultSet rs) throws SQLException {
		AllowanceRule rule = new AllowanceRule();
		rule.setId(rs.getLong("id"));
		rule.setAllowanceTypeId(rs.getLong("allowance_type_id"));
		rule.setApplyScope(rs.getString("apply_scope"));
		rule.setEmployeeType(rs.getString("employee_type"));
		rule.setDepartmentType(rs.getString("department_type"));
		rule.setDepartmentId(readNullableLong(rs, "department_id"));
		rule.setJobTitleId(readNullableLong(rs, "job_title_id"));
		rule.setAmount(rs.getBigDecimal("amount"));
		rule.setEffectiveFrom(rs.getDate("effective_from"));
		rule.setEffectiveTo(rs.getDate("effective_to"));
		rule.setIsActive(rs.getBoolean("is_active"));
		rule.setCreatedAt(rs.getTimestamp("created_at"));
		rule.setUpdatedAt(rs.getTimestamp("updated_at"));
		rule.setAllowanceCode(rs.getString("allowance_code"));
		rule.setAllowanceName(rs.getString("allowance_name"));
		rule.setDepartmentName(rs.getString("department_name"));
		rule.setJobTitleName(rs.getString("job_title_name"));
		return rule;
	}

	private PayrollAllowanceDetail mapPayrollAllowanceDetail(ResultSet rs) throws SQLException {
		PayrollAllowanceDetail detail = new PayrollAllowanceDetail();
		detail.setUserId(rs.getLong("user_id"));
		detail.setAllowanceTypeId(rs.getLong("allowance_type_id"));
		detail.setAllowanceCode(rs.getString("allowance_code"));
		detail.setAllowanceName(rs.getString("allowance_name"));
		detail.setAmount(rs.getBigDecimal("amount"));
		detail.setTaxable(rs.getBoolean("is_taxable"));
		detail.setInsuranceBased(rs.getBoolean("is_insurance_based"));
		return detail;
	}

	private Long readNullableLong(ResultSet rs, String columnName) throws SQLException {
		long value = rs.getLong(columnName);
		return rs.wasNull() ? null : value;
	}
}
