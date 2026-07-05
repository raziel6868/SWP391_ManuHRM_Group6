package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.OvertimeRecord;

public class OvertimeDAO {

	private static final String SELECT_BASE = """
			SELECT ot.id, ot.user_id, ot.date, ot.requested_hours, ot.approved_hours,
			       ot.reason, ot.status, ot.approver_id, ot.approved_at,
			       ot.created_at, ot.updated_at,
			       u.employee_code, u.full_name AS employee_name,
			       approver.full_name AS approver_name
			FROM overtime_records ot
			JOIN users u ON ot.user_id = u.id
			LEFT JOIN users approver ON ot.approver_id = approver.id
			""";

	/**
	 * Tạo mới 1 bản ghi OT và tự động APPROVED ngay (quản đốc tạo là xong, không
	 * cần HR duyệt nữa). approverId ở đây chính là người tạo (quản đốc).
	 */
	public boolean insertAutoApproved(Long userId, java.sql.Date date, BigDecimal hours, String reason,
			Long creatorId) {
		if (userId == null || date == null || hours == null || creatorId == null) {
			return false;
		}

		String sql = """
				INSERT INTO overtime_records
				    (user_id, date, requested_hours, approved_hours, reason, status, approver_id, approved_at)
				VALUES (?, ?, ?, ?, ?, 'APPROVED', ?, CURRENT_TIMESTAMP)
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			ps.setBigDecimal(3, hours);
			ps.setBigDecimal(4, hours);
			ps.setString(5, reason);
			ps.setLong(6, creatorId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.insertAutoApproved() ERROR: " + e.getMessage());
		}

		return false;
	}

	/**
	 * Sửa giờ / lý do của 1 bản ghi OT đã tạo. Chỉ áp dụng cho bản ghi đang
	 * APPROVED (chưa bị hủy). approvedHours luôn đồng bộ theo requestedHours vì giờ
	 * không còn khái niệm "duyệt khác số giờ xin".
	 */
	public boolean update(Long id, BigDecimal hours, String reason) {
		if (id == null || hours == null) {
			return false;
		}

		String sql = """
				UPDATE overtime_records
				SET requested_hours = ?,
				    approved_hours  = ?,
				    reason          = ?,
				    updated_at      = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'APPROVED'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBigDecimal(1, hours);
			ps.setBigDecimal(2, hours);
			ps.setString(3, reason);
			ps.setLong(4, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.update() ERROR: " + e.getMessage());
		}

		return false;
	}

	/**
	 * Hủy 1 bản ghi OT đã tạo nhầm. Dùng lại status REJECTED có sẵn trong DB, nhưng
	 * ý nghĩa bây giờ là "đã hủy" chứ không phải "bị từ chối duyệt".
	 */
	public boolean cancel(Long id, Long cancelledBy) {
		if (id == null || cancelledBy == null) {
			return false;
		}

		String sql = """
				UPDATE overtime_records
				SET status      = 'REJECTED',
				    approver_id = ?,
				    updated_at  = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'APPROVED'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, cancelledBy);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.cancel() ERROR: " + e.getMessage());
		}

		return false;
	}

	public OvertimeRecord getById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = SELECT_BASE + " WHERE ot.id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.getById() ERROR: " + e.getMessage());
		}

		return null;
	}

	/**
	 * Lấy toàn bộ OT đang hiệu lực (APPROVED) của CHÍNH 1 nhân viên trong 1 tháng,
	 * dùng cho trang "Tăng ca của tôi" (nhân viên tự xem lịch OT cá nhân dạng
	 * calendar).
	 */
	public List<OvertimeRecord> getActiveByUserAndMonth(Long userId, int year, int month) {
		List<OvertimeRecord> list = new ArrayList<>();
		String sql = SELECT_BASE
				+ " WHERE ot.user_id = ? AND ot.status = 'APPROVED' AND YEAR(ot.date) = ? AND MONTH(ot.date) = ?"
				+ " ORDER BY ot.date ASC";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.getActiveByUserAndMonth() ERROR: " + e.getMessage());
		}

		return list;
	}

	/**
	 * Lấy toàn bộ OT đang hiệu lực (APPROVED) trong 1 tháng để hiển thị dạng lưới
	 * (nhân viên x ngày). Nếu managerId != null thì chỉ lấy nhân viên dưới quyền
	 * quản đốc đó; null nghĩa là xem toàn bộ (dành cho HR/Sysadmin xem, không
	 * tạo/sửa/hủy được).
	 */
	public List<OvertimeRecord> getActiveByManagerAndMonth(Long managerId, int year, int month) {
		List<OvertimeRecord> list = new ArrayList<>();
		StringBuilder sql = new StringBuilder(SELECT_BASE);
		sql.append(" WHERE ot.status = 'APPROVED' AND YEAR(ot.date) = ? AND MONTH(ot.date) = ?");
		List<Object> params = new ArrayList<>();
		params.add(year);
		params.add(month);

		if (managerId != null) {
			sql.append(" AND u.manager_id = ?");
			params.add(managerId);
		}
		sql.append(" ORDER BY ot.id ASC");

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			setParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.getActiveByManagerAndMonth() ERROR: " + e.getMessage());
		}

		return list;
	}

	/**
	 * Kiểm tra nhân viên đã có OT đang hiệu lực (APPROVED) trong ngày này chưa
	 * (dùng để chặn trùng khi import Excel). excludeId dùng khi sửa 1 bản ghi (bỏ
	 * qua chính nó khi kiểm tra).
	 */
	public boolean existsActiveForUserAndDate(Long userId, java.sql.Date date, Long excludeId) {
		if (userId == null || date == null) {
			return false;
		}

		StringBuilder sql = new StringBuilder(
				"SELECT COUNT(*) FROM overtime_records WHERE user_id = ? AND date = ? AND status = 'APPROVED'");
		if (excludeId != null) {
			sql.append(" AND id <> ?");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			if (excludeId != null) {
				ps.setLong(3, excludeId);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.existsActiveForUserAndDate() ERROR: " + e.getMessage());
		}

		return false;
	}

	/**
	 * Tổng giờ OT đã APPROVED của 1 nhân viên trong 1 tháng cụ thể. excludeId dùng
	 * để loại trừ chính bản ghi đang sửa ra khỏi tổng khi validate.
	 */
	public BigDecimal sumHoursInMonth(Long userId, int year, int month, Long excludeId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COALESCE(SUM(requested_hours), 0) FROM overtime_records
				WHERE user_id = ? AND status = 'APPROVED' AND YEAR(date) = ? AND MONTH(date) = ?
				""");
		if (excludeId != null) {
			sql.append(" AND id <> ?");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			if (excludeId != null) {
				ps.setLong(4, excludeId);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.sumHoursInMonth() ERROR: " + e.getMessage());
		}

		return BigDecimal.ZERO;
	}

	/** Tổng giờ OT đã APPROVED của 1 nhân viên trong 1 năm. */
	public BigDecimal sumHoursInYear(Long userId, int year, Long excludeId) {
		StringBuilder sql = new StringBuilder("""
				SELECT COALESCE(SUM(requested_hours), 0) FROM overtime_records
				WHERE user_id = ? AND status = 'APPROVED' AND YEAR(date) = ?
				""");
		if (excludeId != null) {
			sql.append(" AND id <> ?");
		}

		try (Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setLong(1, userId);
			ps.setInt(2, year);
			if (excludeId != null) {
				ps.setLong(3, excludeId);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.sumHoursInYear() ERROR: " + e.getMessage());
		}

		return BigDecimal.ZERO;
	}

	/**
	 * Tìm OT đã APPROVED của nhân viên trong một ngày. Dùng để phát hiện conflict
	 * APPROVED_OT_WITHOUT_MATCHING_ATTENDANCE khi import chấm công (checkout chưa
	 * đủ muộn). Trả về null nếu không có OT approved. Giữ nguyên cho
	 * AttendanceImportUtil đang dùng.
	 */
	public OvertimeRecord findApprovedOTForUserAndDate(Long userId, java.sql.Date date) {
		if (userId == null || date == null) {
			return null;
		}
		String sql = SELECT_BASE + """
				WHERE ot.user_id = ? AND ot.date = ? AND ot.status = 'APPROVED'
				LIMIT 1
				""";
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setDate(2, date);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("OvertimeDAO.findApprovedOTForUserAndDate() ERROR: " + e.getMessage());
		}
		return null;
	}

	private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}

	private OvertimeRecord mapRow(ResultSet rs) throws SQLException {
		OvertimeRecord r = new OvertimeRecord();
		r.setId(rs.getLong("id"));
		r.setUserId(rs.getLong("user_id"));
		r.setRequesterId(rs.getLong("user_id")); // same as userId, giữ để tương thích code cũ
		r.setDate(rs.getDate("date"));
		r.setRequestedHours(rs.getBigDecimal("requested_hours"));
		r.setApprovedHours(rs.getBigDecimal("approved_hours"));
		r.setReason(rs.getString("reason"));
		r.setStatus(rs.getString("status"));
		long approverId = rs.getLong("approver_id");
		if (!rs.wasNull()) {
			r.setApproverId(approverId);
		}
		r.setApprovedAt(rs.getTimestamp("approved_at"));
		r.setCreatedAt(rs.getTimestamp("created_at"));
		r.setUpdatedAt(rs.getTimestamp("updated_at"));
		r.setEmployeeCode(rs.getString("employee_code"));
		r.setEmployeeName(rs.getString("employee_name"));
		r.setApproverName(rs.getString("approver_name")); // giờ là "người tạo OT"
		return r;
	}
}