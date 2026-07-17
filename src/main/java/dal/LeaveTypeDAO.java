package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.LeaveType;

public class LeaveTypeDAO {

	private static final String SELECT_COLUMNS = """
			id, code, name, description, is_paid, salary_paid_by, is_annual_leave,
			requires_balance, base_days, max_days, has_seniority_bonus,
			seniority_interval_years, seniority_bonus_days, day_count_method,
			is_active, created_at, updated_at
			""";

	public List<LeaveType> searchLeaveTypes(String keyword, Boolean isPaid, Boolean isActive, int offset, int limit) {
		List<LeaveType> leaveTypes = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT " + SELECT_COLUMNS + """
				FROM leave_types
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ? OR description LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isPaid != null) {
			sql.append(" AND is_paid = ?");
			params.add(isPaid);
		}

		if (isActive != null) {
			sql.append(" AND is_active = ?");
			params.add(isActive);
		}

		sql.append(" ORDER BY id ASC LIMIT ? OFFSET ?");
		params.add(limit);
		params.add(offset);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					leaveTypes.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.searchLeaveTypes() ERROR: " + e.getMessage());
		}

		return leaveTypes;
	}

	public int countLeaveTypes(String keyword, Boolean isPaid, Boolean isActive) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM leave_types
				WHERE 1 = 1
				""");
		List<Object> params = new ArrayList<>();

		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (code LIKE ? OR name LIKE ? OR description LIKE ?)");
			String likeKeyword = "%" + keyword.trim() + "%";
			params.add(likeKeyword);
			params.add(likeKeyword);
			params.add(likeKeyword);
		}

		if (isPaid != null) {
			sql.append(" AND is_paid = ?");
			params.add(isPaid);
		}

		if (isActive != null) {
			sql.append(" AND is_active = ?");
			params.add(isActive);
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
			System.err.println("LeaveTypeDAO.countLeaveTypes() ERROR: " + e.getMessage());
		}

		return 0;
	}

	public LeaveType getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = """
				SELECT %s
				FROM leave_types
				WHERE id = ?
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	public LeaveType getAnnualLeaveType() {
		String sql = """
				SELECT %s
				FROM leave_types
				WHERE is_annual_leave = TRUE
				  AND requires_balance = TRUE
				  AND is_active = TRUE
				ORDER BY id ASC
				LIMIT 1
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.getAnnualLeaveType() ERROR: " + e.getMessage());
		}

		return null;
	}

	public boolean existsByCodeExceptId(String code, Long id) {
		if (code == null || code.isBlank() || id == null) {
			return false;
		}

		String sql = "SELECT COUNT(*) FROM leave_types WHERE code = ? AND id <> ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code.trim());
			ps.setLong(2, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.existsByCodeExceptId() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean hasActiveAnnualLeaveTypeExceptId(Long id) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM leave_types
				WHERE is_annual_leave = TRUE
				  AND is_active = TRUE
				""");
		List<Object> params = new ArrayList<>();
		if (id != null) {
			sql.append(" AND id <> ?");
			params.add(id);
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.hasActiveAnnualLeaveTypeExceptId() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean update(LeaveType leaveType) {
		if (leaveType == null || leaveType.getId() == null || leaveType.getCode() == null
				|| leaveType.getName() == null) {
			return false;
		}

		String sql = """
				UPDATE leave_types
				SET code = ?,
				    name = ?,
				    description = ?,
				    is_paid = ?,
				    salary_paid_by = ?,
				    is_annual_leave = ?,
				    requires_balance = ?,
				    base_days = ?,
				    max_days = ?,
				    has_seniority_bonus = ?,
				    seniority_interval_years = ?,
				    seniority_bonus_days = ?,
				    day_count_method = ?
				WHERE id = ?
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, leaveType.getCode());
			ps.setString(2, leaveType.getName());
			ps.setString(3, leaveType.getDescription());
			ps.setBoolean(4, leaveType.getIsPaid() != null && leaveType.getIsPaid());
			ps.setString(5, normalizeSalaryPaidBy(leaveType.getSalaryPaidBy(), leaveType.getIsPaid()));
			ps.setBoolean(6, Boolean.TRUE.equals(leaveType.getIsAnnualLeave()));
			ps.setBoolean(7, Boolean.TRUE.equals(leaveType.getRequiresBalance()));
			ps.setBigDecimal(8, leaveType.getBaseDays());
			ps.setBigDecimal(9, leaveType.getMaxDays());
			ps.setBoolean(10, Boolean.TRUE.equals(leaveType.getHasSeniorityBonus()));
			ps.setInt(11, leaveType.getSeniorityIntervalYears() == null ? 5 : leaveType.getSeniorityIntervalYears());
			ps.setBigDecimal(12,
					leaveType.getSeniorityBonusDays() == null
							? java.math.BigDecimal.ONE
							: leaveType.getSeniorityBonusDays());
			ps.setString(13, normalizeDayCountMethod(leaveType.getDayCountMethod()));
			ps.setLong(14, leaveType.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	public boolean updateStatus(Long id, boolean isActive) {
		if (id == null) {
			return false;
		}

		String sql = "UPDATE leave_types SET is_active = ? WHERE id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, isActive);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("LeaveTypeDAO.updateStatus() ERROR: " + e.getMessage());
		}

		return false;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private LeaveType mapRow(ResultSet rs) throws SQLException {
		LeaveType leaveType = new LeaveType();
		leaveType.setId(rs.getLong("id"));
		leaveType.setCode(rs.getString("code"));
		leaveType.setName(rs.getString("name"));
		leaveType.setDescription(rs.getString("description"));
		leaveType.setIsPaid(rs.getBoolean("is_paid"));
		leaveType.setSalaryPaidBy(rs.getString("salary_paid_by"));
		leaveType.setIsAnnualLeave(rs.getBoolean("is_annual_leave"));
		leaveType.setRequiresBalance(rs.getBoolean("requires_balance"));
		leaveType.setBaseDays(rs.getBigDecimal("base_days"));
		leaveType.setMaxDays(rs.getBigDecimal("max_days"));
		leaveType.setHasSeniorityBonus(rs.getBoolean("has_seniority_bonus"));
		leaveType.setSeniorityIntervalYears(rs.getInt("seniority_interval_years"));
		leaveType.setSeniorityBonusDays(rs.getBigDecimal("seniority_bonus_days"));
		leaveType.setDayCountMethod(rs.getString("day_count_method"));
		leaveType.setIsActive(rs.getBoolean("is_active"));
		leaveType.setCreatedAt(rs.getTimestamp("created_at"));
		leaveType.setUpdatedAt(rs.getTimestamp("updated_at"));
		return leaveType;
	}

	private String normalizeSalaryPaidBy(String salaryPaidBy, Boolean isPaid) {
		if ("SOCIAL_INSURANCE".equals(salaryPaidBy) || "NONE".equals(salaryPaidBy) || "COMPANY".equals(salaryPaidBy)) {
			return salaryPaidBy;
		}
		return Boolean.TRUE.equals(isPaid) ? "COMPANY" : "NONE";
	}

	private String normalizeDayCountMethod(String dayCountMethod) {
		return "CALENDAR_DAY".equals(dayCountMethod) ? "CALENDAR_DAY" : "WORKING_DAY";
	}
}
