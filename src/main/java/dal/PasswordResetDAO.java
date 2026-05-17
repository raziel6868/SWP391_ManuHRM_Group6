package dal;

import model.PasswordReset;

public class PasswordResetDAO {
	/**
	 * Tạo một Ticket yêu cầu cấp lại mật khẩu (Lưu vào database).
	 *
	 * @param reset
	 *            Yêu cầu quên mật khẩu
	 * @return true nếu tạo ticket thành công
	 */
	public boolean insert(PasswordReset reset) {
		return false;
	}

	/**
	 * Cập nhật trạng thái của Ticket (Từ PENDING sang RESOLVED hoặc REJECTED).
	 *
	 * @param id
	 *            ID của Ticket
	 * @param status
	 *            Trạng thái mới
	 * @param resolvedBy
	 *            ID của HR/Admin đã giải quyết (hoặc null nếu reset tự động qua
	 *            mail)
	 * @return true nếu cập nhật thành công
	 */
	public boolean updateStatus(Long id, PasswordReset.Status status, Long resolvedBy) {
		return false;
	}
}
