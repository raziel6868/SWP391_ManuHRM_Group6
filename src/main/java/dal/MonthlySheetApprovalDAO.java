package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MonthlySheetApproval;

public class MonthlySheetApprovalDAO {

	private static final String SELECT_BASE = """
			SELECT msa.*,
			       u.full_name       AS supervisor_name,
			       u.employee_code   AS supervisor_employee_code,
			       d.name            AS department_name
			FROM monthly_sheet_approvals msa
			JOIN users u ON msa.supervisor_id = u.id
			LEFT JOIN departments d ON u.department_id = d.id
			""";

	// ── Query ───────────────────────────────────────────────────────────────────

	public List<MonthlySheetApproval> getBySheetId(Long sheetId) {
		List<MonthlySheetApproval> list = new ArrayList<>();
		String sql = SELECT_BASE + " WHERE msa.monthly_sheet_id = ? ORDER BY u.full_name ASC";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetApprovalDAO.getBySheetId() ERROR: " + e.getMessage());
		}
		return list;
	}

	public MonthlySheetApproval getBySupervisorAndSheet(Long sheetId, Long supervisorId) {
		String sql = SELECT_BASE + " WHERE msa.monthly_sheet_id = ? AND msa.supervisor_id = ?";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			ps.setLong(2, supervisorId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetApprovalDAO.getBySupervisorAndSheet() ERROR: " + e.getMessage());
		}
		return null;
	}

	// ── Create ───────────────────────────────────────────────────────────────────

	/**
	 * Tạo record PENDING cho từng quản đốc khi HR bấm "Gửi duyệt". Lấy tất cả user
	 * có role PRODUCTION_SUPERVISOR (role_id=3).
	 */
	public int createForSheetSupervisors(Connection conn, Long sheetId, int year, int month) throws SQLException {
		String sql = """
				INSERT IGNORE INTO monthly_sheet_approvals (monthly_sheet_id, supervisor_id, status)
				SELECT DISTINCT ?, sup.id, 'PENDING'
				FROM attendance_records ar
				JOIN users emp ON ar.user_id = emp.id
				JOIN users sup ON emp.manager_id = sup.id
				WHERE YEAR(ar.date) = ? AND MONTH(ar.date) = ?
				  AND sup.role_id = 3 AND sup.is_active = TRUE
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			return ps.executeUpdate();
		}
	}

	// ── Approve ─────────────────────────────────────────────────────────────────

	/**
	 * Quản đốc bấm "Chốt" — chuyển PENDING → APPROVED.
	 */
	public boolean approve(Long sheetId, Long supervisorId) {
		try (Connection conn = DBContext.getConnection()) {
			return approve(conn, sheetId, supervisorId);
		} catch (SQLException e) {
			System.err.println("MonthlySheetApprovalDAO.approve() ERROR: " + e.getMessage());
		}
		return false;
	}

	public boolean approve(Connection conn, Long sheetId, Long supervisorId) throws SQLException {
		String sql = """
				UPDATE monthly_sheet_approvals
				SET status = 'APPROVED', approved_at = NOW(), updated_at = NOW()
				WHERE monthly_sheet_id = ? AND supervisor_id = ? AND status = 'PENDING'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sheetId);
			ps.setLong(2, supervisorId);
			return ps.executeUpdate() > 0;
		}
	}

	// ── Helper ───────────────────────────────────────────────────────────────────

	private MonthlySheetApproval mapRow(ResultSet rs) throws SQLException {
		MonthlySheetApproval a = new MonthlySheetApproval();
		a.setId(rs.getLong("id"));
		a.setMonthlySheetId(rs.getLong("monthly_sheet_id"));
		a.setSupervisorId(rs.getLong("supervisor_id"));
		a.setSupervisorName(rs.getString("supervisor_name"));
		a.setSupervisorEmployeeCode(rs.getString("supervisor_employee_code"));
		a.setDepartmentName(rs.getString("department_name"));
		a.setStatus(rs.getString("status"));
		a.setApprovedAt(rs.getTimestamp("approved_at"));
		a.setCreatedAt(rs.getTimestamp("created_at"));
		a.setUpdatedAt(rs.getTimestamp("updated_at"));
		return a;
	}
}
