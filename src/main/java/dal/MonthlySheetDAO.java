package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MonthlySheet;

public class MonthlySheetDAO {

	private static final String SELECT_BASE = """
			SELECT ms.*,
			       sub.full_name AS submitted_by_name,
			       hr.full_name AS hr_approved_by_name,
			       closed.full_name AS closed_by_name,
			       COALESCE(agg.total_supervisors, 0) AS total_supervisors,
			       COALESCE(agg.approved_supervisors, 0) AS approved_supervisors
			FROM monthly_sheets ms
			LEFT JOIN users sub ON ms.submitted_by = sub.id
			LEFT JOIN users hr ON ms.hr_approved_by = hr.id
			LEFT JOIN users closed ON ms.closed_by = closed.id
			LEFT JOIN (
			    SELECT monthly_sheet_id,
			           COUNT(*) AS total_supervisors,
			           SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_supervisors
			    FROM monthly_sheet_approvals
			    GROUP BY monthly_sheet_id
			) agg ON ms.id = agg.monthly_sheet_id
			""";

	// ── Query ───────────────────────────────────────────────────────────────────

	public String getStatusByYearMonth(int year, int month) {
		String sql = "SELECT status FROM monthly_sheets WHERE year = ? AND month = ? LIMIT 1";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getString("status");
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getStatusByYearMonth() ERROR: " + e.getMessage());
		}
		return null;
	}

	public boolean isPeriodClosed(int year, int month) {
		return "CLOSED".equals(getStatusByYearMonth(year, month));
	}

	public boolean isEditablePeriod(int year, int month) {
		String status = getStatusByYearMonth(year, month);
		return status == null || "OPEN".equals(status);
	}

	public boolean isSupervisorCorrectionWindow(int year, int month) {
		return "PENDING_SUPERVISOR".equals(getStatusByYearMonth(year, month));
	}

	public boolean isHrCorrectionWindow(int year, int month) {
		return "PENDING_HR".equals(getStatusByYearMonth(year, month));
	}

	public List<MonthlySheet> getAll() {
		List<MonthlySheet> list = new ArrayList<>();
		String sql = SELECT_BASE + " ORDER BY ms.year DESC, ms.month DESC";
		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				list.add(mapRow(rs));
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getAll() ERROR: " + e.getMessage());
		}
		return list;
	}

	public MonthlySheet getById(Long id) {
		if (id == null)
			return null;
		String sql = SELECT_BASE + " WHERE ms.id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getById() ERROR: " + e.getMessage());
		}
		return null;
	}

	public MonthlySheet getByYearMonth(int year, int month) {
		String sql = SELECT_BASE + " WHERE ms.year = ? AND ms.month = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getByYearMonth() ERROR: " + e.getMessage());
		}
		return null;
	}

	public MonthlySheet getOrCreate(int year, int month) {
		MonthlySheet existing = getByYearMonth(year, month);
		if (existing != null)
			return existing;

		String sql = "INSERT INTO monthly_sheets (year, month, status) VALUES (?, ?, 'OPEN')";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getOrCreate() ERROR: " + e.getMessage());
		}
		return getByYearMonth(year, month);
	}

	// ── Workflow transitions ─────────────────────────────────────────────────────

	/**
	 * HR bấm "Gửi duyệt" — OPEN → PENDING_SUPERVISOR. Đồng thời tạo các record
	 * trong monthly_sheet_approvals cho tất cả quản đốc.
	 */
	public boolean submit(Long sheetId, Long submittedBy) {
		try (Connection conn = DBContext.getConnection()) {
			return submit(conn, sheetId, submittedBy);
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.submit() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean submit(Connection conn, Long sheetId, Long submittedBy) throws SQLException {
		String sql = """
				UPDATE monthly_sheets
				SET status = 'PENDING_SUPERVISOR',
				    submitted_by = ?, submitted_at = NOW()
				WHERE id = ? AND status = 'OPEN'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, submittedBy);
			ps.setLong(2, sheetId);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Tất cả quản đốc đã chốt → PENDING_SUPERVISOR → PENDING_HR. Gọi sau khi kiểm
	 * tra allSupervisorsApproved().
	 */
	public boolean advanceToHR(Long sheetId) {
		try (Connection conn = DBContext.getConnection()) {
			return advanceToHR(conn, sheetId);
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.advanceToHR() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean advanceToHR(Connection conn, Long sheetId) throws SQLException {
		String sql = """
				UPDATE monthly_sheets SET status = 'PENDING_HR'
				WHERE id = ? AND status = 'PENDING_SUPERVISOR'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * HR chốt — PENDING_HR → PENDING_DIRECTOR.
	 */
	public boolean hrApprove(Long sheetId, Long hrUserId) {
		try (Connection conn = DBContext.getConnection()) {
			return hrApprove(conn, sheetId, hrUserId);
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.hrApprove() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean hrApprove(Connection conn, Long sheetId, Long hrUserId) throws SQLException {
		String sql = """
				UPDATE monthly_sheets
				SET status = 'PENDING_DIRECTOR',
				    hr_approved_by = ?, hr_approved_at = NOW()
				WHERE id = ? AND status = 'PENDING_HR'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, hrUserId);
			ps.setLong(2, sheetId);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Giám đốc chốt + đóng sổ — PENDING_DIRECTOR → CLOSED.
	 */
	public boolean directorClose(Long sheetId, Long directorId) {
		try (Connection conn = DBContext.getConnection()) {
			return directorClose(conn, sheetId, directorId);
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.directorClose() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean directorClose(Connection conn, Long sheetId, Long directorId) throws SQLException {
		String sql = """
				UPDATE monthly_sheets
				SET status = 'CLOSED',
				    closed_by = ?, closed_at = NOW()
				WHERE id = ? AND status = 'PENDING_DIRECTOR'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, directorId);
			ps.setLong(2, sheetId);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * HR hoặc Giám đốc reject — reset về OPEN. Xóa toàn bộ approvals của supervisor
	 * liên quan đến phòng ban có vấn đề. Nếu departmentId = null thì reset tất cả.
	 */
	public boolean reject(Long sheetId, Long departmentId) {
		String resetSql = """
				UPDATE monthly_sheets
				SET status = 'OPEN',
				    submitted_by = NULL, submitted_at = NULL,
				    hr_approved_by = NULL, hr_approved_at = NULL,
				    closed_by = NULL, closed_at = NULL
				WHERE id = ?
				""";
		String deleteAllApprovalsSql = """
				DELETE FROM monthly_sheet_approvals WHERE monthly_sheet_id = ?
				""";
		String deleteByDeptSql = """
				DELETE msa FROM monthly_sheet_approvals msa
				JOIN users u ON msa.supervisor_id = u.id
				WHERE msa.monthly_sheet_id = ? AND u.department_id = ?
				""";
		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
					ps.setLong(1, sheetId);
					ps.executeUpdate();
				}
				if (departmentId == null) {
					try (PreparedStatement ps = conn.prepareStatement(deleteAllApprovalsSql)) {
						ps.setLong(1, sheetId);
						ps.executeUpdate();
					}
				} else {
					try (PreparedStatement ps = conn.prepareStatement(deleteByDeptSql)) {
						ps.setLong(1, sheetId);
						ps.setLong(2, departmentId);
						ps.executeUpdate();
					}
				}
				conn.commit();
				return true;
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.reject() ERROR: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Kiểm tra tất cả quản đốc đã chốt chưa.
	 */
	public boolean allSupervisorsApproved(Long sheetId) {
		try (Connection conn = DBContext.getConnection()) {
			return allSupervisorsApproved(conn, sheetId);
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.allSupervisorsApproved() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean allSupervisorsApproved(Connection conn, Long sheetId) throws SQLException {
		String sql = """
				SELECT COUNT(*) AS total,
				       SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved
				FROM monthly_sheet_approvals
				WHERE monthly_sheet_id = ?
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int total = rs.getInt("total");
					int approved = rs.getInt("approved");
					return total > 0 && total == approved;
				}
			}
		}
		return false;
	}

	// Legacy methods giữ lại để không break code cũ
	public boolean closeSheet(Long sheetId, Long closedBy) {
		return directorClose(sheetId, closedBy);
	}

	public boolean reopenSheet(Long sheetId) {
		String finalPayrollSql = """
				SELECT COUNT(*)
				FROM monthly_salaries
				WHERE monthly_sheet_id = ?
				  AND status IN ('FINAL', 'PAID')
				""";
		String deleteDraftPayrollSql = """
				DELETE FROM monthly_salaries
				WHERE monthly_sheet_id = ?
				  AND status = 'DRAFT'
				""";
		String resetSql = """
				UPDATE monthly_sheets
				SET status = 'OPEN',
				    submitted_by = NULL, submitted_at = NULL,
				    hr_approved_by = NULL, hr_approved_at = NULL,
				    closed_by = NULL, closed_at = NULL
				WHERE id = ? AND status = 'CLOSED'
				""";
		String deleteApprovalsSql = "DELETE FROM monthly_sheet_approvals WHERE monthly_sheet_id = ?";
		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				try (PreparedStatement ps = conn.prepareStatement(finalPayrollSql)) {
					ps.setLong(1, sheetId);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) {
							conn.rollback();
							return false;
						}
					}
				}

				int updated;
				try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
					ps.setLong(1, sheetId);
					updated = ps.executeUpdate();
				}
				if (updated <= 0) {
					conn.rollback();
					return false;
				}
				try (PreparedStatement ps = conn.prepareStatement(deleteDraftPayrollSql)) {
					ps.setLong(1, sheetId);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = conn.prepareStatement(deleteApprovalsSql)) {
					ps.setLong(1, sheetId);
					ps.executeUpdate();
				}
				conn.commit();
				return true;
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.reopenSheet() ERROR: " + e.getMessage());
		}
		return false;
	}

	// ── Helper ───────────────────────────────────────────────────────────────────

	private MonthlySheet mapRow(ResultSet rs) throws SQLException {
		MonthlySheet s = new MonthlySheet();
		s.setId(rs.getLong("id"));
		s.setYear(rs.getInt("year"));
		s.setMonth(rs.getInt("month"));
		s.setStatus(rs.getString("status"));

		long subBy = rs.getLong("submitted_by");
		if (!rs.wasNull())
			s.setSubmittedBy(subBy);
		s.setSubmittedByName(rs.getString("submitted_by_name"));
		s.setSubmittedAt(rs.getTimestamp("submitted_at"));

		long hrBy = rs.getLong("hr_approved_by");
		if (!rs.wasNull())
			s.setHrApprovedBy(hrBy);
		s.setHrApprovedByName(rs.getString("hr_approved_by_name"));
		s.setHrApprovedAt(rs.getTimestamp("hr_approved_at"));

		long closedBy = rs.getLong("closed_by");
		if (!rs.wasNull())
			s.setClosedBy(closedBy);
		s.setClosedByName(rs.getString("closed_by_name"));
		s.setClosedAt(rs.getTimestamp("closed_at"));

		s.setCreatedAt(rs.getTimestamp("created_at"));
		s.setTotalSupervisors(rs.getInt("total_supervisors"));
		s.setApprovedSupervisors(rs.getInt("approved_supervisors"));
		return s;
	}
}
