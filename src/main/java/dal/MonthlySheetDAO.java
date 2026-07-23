package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.MonthlySheet;
import util.WorkScheduleConfig;

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
	private static final int MAX_CLOSE_CONFLICT_MESSAGES = 20;

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

	public boolean isEditablePeriodForSupervisor(int year, int month, Long supervisorId) {
		String status = getStatusByYearMonth(year, month);
		if (status == null || "OPEN".equals(status)) {
			return true;
		}
		if (supervisorId == null || !"PENDING_SUPERVISOR".equals(status)) {
			return false;
		}

		String sql = """
				SELECT COUNT(*)
				FROM monthly_sheets ms
				JOIN monthly_sheet_approvals msa ON msa.monthly_sheet_id = ms.id
				WHERE ms.year = ?
				  AND ms.month = ?
				  AND ms.status = 'PENDING_SUPERVISOR'
				  AND msa.supervisor_id = ?
				  AND msa.status = 'PENDING'
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.setLong(3, supervisorId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.isEditablePeriodForSupervisor() ERROR: " + e.getMessage());
		}
		return false;
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

	public List<Integer> getAvailableYears() {
		List<Integer> years = new ArrayList<>();
		String sql = "SELECT DISTINCT year FROM monthly_sheets ORDER BY year DESC";
		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				years.add(rs.getInt("year"));
			}
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.getAvailableYears() ERROR: " + e.getMessage());
		}
		return years;
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

	public MonthlySheet findLockedPeriodInRange(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			return null;
		}
		LocalDate currentMonth = startDate.toLocalDate().withDayOfMonth(1);
		LocalDate endMonth = endDate.toLocalDate().withDayOfMonth(1);
		while (!currentMonth.isAfter(endMonth)) {
			MonthlySheet sheet = getByYearMonth(currentMonth.getYear(), currentMonth.getMonthValue());
			if (sheet != null && !"OPEN".equals(sheet.getStatus())) {
				return sheet;
			}
			currentMonth = currentMonth.plusMonths(1);
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
	 * trong monthly_sheet_approvals cho tất cả trưởng phòng.
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
	 * Tất cả trưởng phòng đã chốt → PENDING_SUPERVISOR → PENDING_HR. Gọi sau khi
	 * kiểm tra allSupervisorsApproved().
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
	 * HR chốt — PENDING_HR → CLOSED.
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
				SET status = 'CLOSED',
				    hr_approved_by = ?, hr_approved_at = NOW(),
				    closed_by = ?, closed_at = NOW()
				WHERE id = ? AND status = 'PENDING_HR'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, hrUserId);
			ps.setLong(2, hrUserId);
			ps.setLong(3, sheetId);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Người có quyền reject — reset về OPEN. Vì sheet đã quay lại từ đầu, xóa toàn
	 * bộ approval cũ để lần gửi duyệt tiếp theo không giữ trạng thái APPROVED cũ.
	 */
	public boolean reject(Long sheetId, Long departmentId) {
		try (Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			try {
				boolean rejected = departmentId == null
						? rejectAllDepartments(conn, sheetId)
						: rejectOneDepartment(conn, sheetId, departmentId);
				if (!rejected) {
					conn.rollback();
					return false;
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

	private boolean rejectAllDepartments(Connection conn, Long sheetId) throws SQLException {
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
		try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
			ps.setLong(1, sheetId);
			if (ps.executeUpdate() <= 0) {
				return false;
			}
		}
		try (PreparedStatement ps = conn.prepareStatement(deleteAllApprovalsSql)) {
			ps.setLong(1, sheetId);
			ps.executeUpdate();
		}
		return true;
	}

	private boolean rejectOneDepartment(Connection conn, Long sheetId, Long departmentId) throws SQLException {
		String countApprovalsSql = """
				SELECT COUNT(DISTINCT msa.id)
				FROM monthly_sheet_approvals msa
				JOIN users head ON head.id = msa.supervisor_id
				WHERE msa.monthly_sheet_id = ?
				  AND head.department_id = ?
				""";
		String resetSheetSql = """
				UPDATE monthly_sheets
				SET status = 'PENDING_SUPERVISOR',
				    hr_approved_by = NULL, hr_approved_at = NULL,
				    closed_by = NULL, closed_at = NULL
				WHERE id = ?
				  AND status IN ('PENDING_SUPERVISOR', 'PENDING_HR')
				""";
		String resetDepartmentApprovalsSql = """
				UPDATE monthly_sheet_approvals msa
				JOIN users head ON head.id = msa.supervisor_id
				SET msa.status = 'PENDING',
				    msa.approved_at = NULL,
				    msa.updated_at = NOW()
				WHERE msa.monthly_sheet_id = ?
				  AND head.department_id = ?
				""";

		int matchingApprovals = 0;
		try (PreparedStatement ps = conn.prepareStatement(countApprovalsSql)) {
			ps.setLong(1, sheetId);
			ps.setLong(2, departmentId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					matchingApprovals = rs.getInt(1);
				}
			}
		}
		if (matchingApprovals <= 0) {
			return false;
		}

		try (PreparedStatement ps = conn.prepareStatement(resetSheetSql)) {
			ps.setLong(1, sheetId);
			if (ps.executeUpdate() <= 0) {
				return false;
			}
		}
		try (PreparedStatement ps = conn.prepareStatement(resetDepartmentApprovalsSql)) {
			ps.setLong(1, sheetId);
			ps.setLong(2, departmentId);
			ps.executeUpdate();
		}
		return true;
	}

	/**
	 * Kiểm tra tất cả trưởng phòng đã chốt chưa.
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

	public List<String> findCloseConflicts(int year, int month) {
		try (Connection conn = DBContext.getConnection()) {
			return findCloseConflicts(conn, year, month);
		} catch (SQLException e) {
			System.err.println("MonthlySheetDAO.findCloseConflicts() ERROR: " + e.getMessage());
		}
		return new ArrayList<>();
	}

	public List<String> findCloseConflicts(Connection conn, int year, int month) throws SQLException {
		List<String> conflicts = new ArrayList<>();
		appendLeaveAttendanceConflicts(conn, year, month, conflicts);
		appendLeaveOvertimeConflicts(conn, year, month, conflicts);
		appendOvertimeAttendanceConflicts(conn, year, month, conflicts);
		if (conflicts.size() >= MAX_CLOSE_CONFLICT_MESSAGES) {
			conflicts.add("Hệ thống chỉ hiển thị tối đa " + MAX_CLOSE_CONFLICT_MESSAGES
					+ " conflict đầu tiên. Vui lòng xử lý rồi kiểm tra lại.");
		}
		return conflicts;
	}

	private void appendLeaveAttendanceConflicts(Connection conn, int year, int month, List<String> conflicts)
			throws SQLException {
		String sql = """
				SELECT u.employee_code, u.full_name, ar.date AS conflict_date, lr.status AS leave_status
				FROM leave_requests lr
				JOIN users u ON lr.user_id = u.id
				JOIN attendance_records ar
				  ON ar.user_id = lr.user_id
				 AND ar.date BETWEEN lr.start_date AND lr.end_date
				WHERE lr.status IN ('APPROVED_LEVEL_1', 'APPROVED')
				  AND YEAR(ar.date) = ?
				  AND MONTH(ar.date) = ?
				  AND ar.check_in IS NOT NULL
				ORDER BY ar.date ASC, u.employee_code ASC
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					if (!addCloseConflict(conflicts,
							"Leave/Attendance: " + employeeLabel(rs) + " có đơn nghỉ phép "
									+ leaveStatusLabel(rs.getString("leave_status")) + " nhưng vẫn có chấm công ngày "
									+ rs.getDate("conflict_date") + ".")) {
						return;
					}
				}
			}
		}
	}

	private void appendLeaveOvertimeConflicts(Connection conn, int year, int month, List<String> conflicts)
			throws SQLException {
		String sql = """
				SELECT u.employee_code, u.full_name, ot.date AS conflict_date, lr.status AS leave_status
				FROM overtime_records ot
				JOIN users u ON ot.user_id = u.id
				JOIN leave_requests lr
				  ON lr.user_id = ot.user_id
				 AND ot.date BETWEEN lr.start_date AND lr.end_date
				WHERE ot.status = 'APPROVED'
				  AND lr.status IN ('APPROVED_LEVEL_1', 'APPROVED')
				  AND YEAR(ot.date) = ?
				  AND MONTH(ot.date) = ?
				ORDER BY ot.date ASC, u.employee_code ASC
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					if (!addCloseConflict(conflicts,
							"Leave/OT: " + employeeLabel(rs) + " có OT approved nhưng cũng có đơn nghỉ phép "
									+ leaveStatusLabel(rs.getString("leave_status")) + " ngày "
									+ rs.getDate("conflict_date") + ".")) {
						return;
					}
				}
			}
		}
	}

	private void appendOvertimeAttendanceConflicts(Connection conn, int year, int month, List<String> conflicts)
			throws SQLException {
		String sql = """
				SELECT u.employee_code, u.full_name, ot.date AS conflict_date, ot.approved_hours,
				       ar.id AS attendance_id, ar.check_in, ar.check_out
				FROM overtime_records ot
				JOIN users u ON ot.user_id = u.id
				LEFT JOIN attendance_records ar
				  ON ar.user_id = ot.user_id
				 AND ar.date = ot.date
				WHERE ot.status = 'APPROVED'
				  AND YEAR(ot.date) = ?
				  AND MONTH(ot.date) = ?
				ORDER BY ot.date ASC, u.employee_code ASC
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					if (!appendOneOvertimeAttendanceConflict(rs, conflicts)) {
						return;
					}
				}
			}
		}
	}

	private boolean appendOneOvertimeAttendanceConflict(ResultSet rs, List<String> conflicts) throws SQLException {
		String employee = employeeLabel(rs);
		java.sql.Date conflictDate = rs.getDate("conflict_date");
		BigDecimal approvedHours = rs.getBigDecimal("approved_hours");
		rs.getLong("attendance_id");
		boolean hasAttendance = !rs.wasNull();

		if (approvedHours == null || approvedHours.compareTo(BigDecimal.ZERO) <= 0) {
			return addCloseConflict(conflicts, "OT/Attendance: " + employee + " có OT approved ngày " + conflictDate
					+ " nhưng thiếu số giờ OT hợp lệ.");
		}
		if (!hasAttendance) {
			return addCloseConflict(conflicts, "OT/Attendance: " + employee + " có " + formatHours(approvedHours)
					+ "h OT approved ngày " + conflictDate + " nhưng chưa có chấm công.");
		}

		Time checkIn = rs.getTime("check_in");
		Time checkOut = rs.getTime("check_out");
		if (checkIn == null) {
			return addCloseConflict(conflicts, "OT/Attendance: " + employee + " có " + formatHours(approvedHours)
					+ "h OT approved ngày " + conflictDate + " nhưng attendance đang là vắng/chưa có giờ vào.");
		}
		if (checkOut == null) {
			return addCloseConflict(conflicts, "OT/Attendance: " + employee + " có " + formatHours(approvedHours)
					+ "h OT approved ngày " + conflictDate + " nhưng attendance chưa có giờ ra.");
		}

		long otMinutes = approvedHours.multiply(BigDecimal.valueOf(60)).longValue();
		LocalTime expectedCheckout = WorkScheduleConfig.OVERTIME_START.plusMinutes(otMinutes);
		if (checkOut.toLocalTime().isBefore(expectedCheckout)) {
			return addCloseConflict(conflicts,
					"OT/Attendance: " + employee + " có " + formatHours(approvedHours) + "h OT approved ngày "
							+ conflictDate + " nhưng giờ ra " + checkOut.toLocalTime() + " chưa đủ, cần từ "
							+ expectedCheckout + " trở đi.");
		}
		return true;
	}

	private boolean addCloseConflict(List<String> conflicts, String message) {
		if (conflicts.size() >= MAX_CLOSE_CONFLICT_MESSAGES) {
			return false;
		}
		conflicts.add(message);
		return true;
	}

	private String employeeLabel(ResultSet rs) throws SQLException {
		return rs.getString("employee_code") + " - " + rs.getString("full_name");
	}

	private String leaveStatusLabel(String status) {
		if ("APPROVED_LEVEL_1".equals(status)) {
			return "đã duyệt cấp 1";
		}
		if ("APPROVED".equals(status)) {
			return "đã duyệt cuối";
		}
		return status;
	}

	private String formatHours(BigDecimal hours) {
		if (hours == null) {
			return "0";
		}
		return hours.stripTrailingZeros().toPlainString();
	}

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
