package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import model.PersonalTaxBracket;

public class PersonalTaxBracketDAO {

	private static final String SELECT_COLUMNS = """
			id,
			bracket_order,
			income_from,
			income_to,
			tax_rate,
			effective_from,
			effective_to,
			created_at,
			updated_at
			""";

	public List<PersonalTaxBracket> getActiveBracketsForPeriod(int year, int month) {
		List<PersonalTaxBracket> brackets = new ArrayList<>();
		YearMonth yearMonth = YearMonth.of(year, month);
		Date firstDay = Date.valueOf(yearMonth.atDay(1));
		Date lastDay = Date.valueOf(yearMonth.atEndOfMonth());
		String sql = """
				SELECT %s
				FROM personal_tax_brackets
				WHERE effective_from <= ?
				  AND (effective_to IS NULL OR effective_to >= ?)
				ORDER BY bracket_order ASC, id ASC
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, lastDay);
			ps.setDate(2, firstDay);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					brackets.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("PersonalTaxBracketDAO.getActiveBracketsForPeriod() ERROR: " + e.getMessage());
		}
		return brackets;
	}

	public List<PersonalTaxBracket> getAll() {
		List<PersonalTaxBracket> brackets = new ArrayList<>();
		String sql = """
				SELECT %s
				FROM personal_tax_brackets
				ORDER BY effective_from DESC, bracket_order ASC, id DESC
				""".formatted(SELECT_COLUMNS);

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				brackets.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("PersonalTaxBracketDAO.getAll() ERROR: " + e.getMessage());
		}
		return brackets;
	}

	public PersonalTaxBracket getById(Long id) {
		if (id == null) {
			return null;
		}
		String sql = """
				SELECT %s
				FROM personal_tax_brackets
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
			System.err.println("PersonalTaxBracketDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean hasBlockingPeriod(Long excludeId, Integer bracketOrder, Date effectiveFrom, Date effectiveTo) {
		if (bracketOrder == null || effectiveFrom == null) {
			return false;
		}
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM personal_tax_brackets
				WHERE bracket_order = ?
				  AND effective_from >= ?
				  AND id <> ?
				""");
		if (effectiveTo != null) {
			sql.append(" AND effective_from <= ?");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setInt(1, bracketOrder);
			ps.setDate(2, effectiveFrom);
			ps.setLong(3, excludeId != null ? excludeId : -1L);
			if (effectiveTo != null) {
				ps.setDate(4, effectiveTo);
			}
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("PersonalTaxBracketDAO.hasBlockingPeriod() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean upsert(PersonalTaxBracket bracket) {
		if (bracket == null || bracket.getBracketOrder() == null || bracket.getEffectiveFrom() == null) {
			return false;
		}

		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				if (hasBlockingPeriod(conn, bracket.getId(), bracket.getBracketOrder(), bracket.getEffectiveFrom(),
						bracket.getEffectiveTo())) {
					conn.rollback();
					return false;
				}
				closePreviousOverlappingBracket(conn, bracket.getId(), bracket.getBracketOrder(),
						bracket.getEffectiveFrom());
				boolean success = bracket.getId() == null ? insert(conn, bracket) : update(conn, bracket);
				if (success) {
					conn.commit();
					return true;
				}
				conn.rollback();
			} catch (SQLException e) {
				conn.rollback();
				System.err.println("PersonalTaxBracketDAO.upsert() ERROR: " + e.getMessage());
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.err.println("PersonalTaxBracketDAO.upsert() connection ERROR: " + e.getMessage());
		}
		return false;
	}

	private boolean hasBlockingPeriod(Connection conn, Long excludeId, Integer bracketOrder, Date effectiveFrom,
			Date effectiveTo) throws SQLException {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM personal_tax_brackets
				WHERE bracket_order = ?
				  AND effective_from >= ?
				  AND id <> ?
				""");
		if (effectiveTo != null) {
			sql.append(" AND effective_from <= ?");
		}
		try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setInt(1, bracketOrder);
			ps.setDate(2, effectiveFrom);
			ps.setLong(3, excludeId != null ? excludeId : -1L);
			if (effectiveTo != null) {
				ps.setDate(4, effectiveTo);
			}
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	private void closePreviousOverlappingBracket(Connection conn, Long excludeId, Integer bracketOrder,
			Date effectiveFrom) throws SQLException {
		String sql = """
				UPDATE personal_tax_brackets
				SET effective_to = DATE_SUB(?, INTERVAL 1 DAY),
				    updated_at = CURRENT_TIMESTAMP
				WHERE bracket_order = ?
				  AND effective_from < ?
				  AND (effective_to IS NULL OR effective_to >= ?)
				  AND id <> ?
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, effectiveFrom);
			ps.setInt(2, bracketOrder);
			ps.setDate(3, effectiveFrom);
			ps.setDate(4, effectiveFrom);
			ps.setLong(5, excludeId != null ? excludeId : -1L);
			ps.executeUpdate();
		}
	}

	private boolean insert(Connection conn, PersonalTaxBracket bracket) throws SQLException {
		String sql = """
				INSERT INTO personal_tax_brackets
				    (bracket_order, income_from, income_to, tax_rate, effective_from, effective_to)
				VALUES (?, ?, ?, ?, ?, ?)
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, bracket);
			return ps.executeUpdate() > 0;
		}
	}

	private boolean update(Connection conn, PersonalTaxBracket bracket) throws SQLException {
		String sql = """
				UPDATE personal_tax_brackets
				SET bracket_order = ?,
				    income_from = ?,
				    income_to = ?,
				    tax_rate = ?,
				    effective_from = ?,
				    effective_to = ?,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setMutationParams(ps, bracket);
			ps.setLong(7, bracket.getId());
			return ps.executeUpdate() > 0;
		}
	}

	private void setMutationParams(PreparedStatement ps, PersonalTaxBracket bracket) throws SQLException {
		ps.setInt(1, bracket.getBracketOrder());
		ps.setBigDecimal(2, bracket.getIncomeFrom());
		if (bracket.getIncomeTo() != null) {
			ps.setBigDecimal(3, bracket.getIncomeTo());
		} else {
			ps.setNull(3, java.sql.Types.DECIMAL);
		}
		ps.setBigDecimal(4, bracket.getTaxRate());
		ps.setDate(5, bracket.getEffectiveFrom());
		if (bracket.getEffectiveTo() != null) {
			ps.setDate(6, bracket.getEffectiveTo());
		} else {
			ps.setNull(6, java.sql.Types.DATE);
		}
	}

	private PersonalTaxBracket mapRow(ResultSet rs) throws SQLException {
		PersonalTaxBracket bracket = new PersonalTaxBracket();
		bracket.setId(rs.getLong("id"));
		bracket.setBracketOrder(rs.getInt("bracket_order"));
		bracket.setIncomeFrom(rs.getBigDecimal("income_from"));
		bracket.setIncomeTo(rs.getBigDecimal("income_to"));
		bracket.setTaxRate(rs.getBigDecimal("tax_rate"));
		bracket.setEffectiveFrom(rs.getDate("effective_from"));
		bracket.setEffectiveTo(rs.getDate("effective_to"));
		bracket.setCreatedAt(rs.getTimestamp("created_at"));
		bracket.setUpdatedAt(rs.getTimestamp("updated_at"));
		return bracket;
	}
}
