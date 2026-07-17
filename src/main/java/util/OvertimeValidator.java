package util;

import dal.AttendanceDAO;
import dal.LeaveRequestDAO;
import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import model.AttendanceRecord;
import model.User;

/**
 * Gom toàn bộ rule nghiệp vụ khi tạo/sửa 1 bản ghi OT, để OvertimeImportUtil,
 * OvertimeEditServlet và OvertimeBulkCreateServlet dùng chung 1 nguồn duy nhất
 * — tránh lặp lại (và lệch nhau) rule ở nhiều nơi.
 *
 * Rule hiện hành: OT tối đa
 * {@link WorkScheduleConfig#MAX_OT_HOURS_PER_DAY}/ngày, 40h/tháng, 200h/năm.
 * Không tạo OT vào Thứ 7/Chủ nhật. Được phép tạo OT cho ngày quá khứ. Không
 * trùng OT đã có, không tạo OT ngày đã có đơn nghỉ phép đang/đã duyệt, không
 * tạo OT ngày nhân viên vắng mặt, và nếu ngày đó đã chấm công ra thì giờ ra
 * phải đủ hỗ trợ số giờ OT xin. Chỉ tạo/sửa được khi kỳ công đang ở trạng thái
 * OPEN.
 */
public class OvertimeValidator {

	public static final BigDecimal MAX_HOURS_PER_MONTH = new BigDecimal("40");
	public static final BigDecimal MAX_HOURS_PER_YEAR = new BigDecimal("200");

	private final OvertimeDAO overtimeDAO;
	private final MonthlySheetDAO monthlySheetDAO;
	private final AttendanceDAO attendanceDAO;
	private final LeaveRequestDAO leaveRequestDAO;

	public OvertimeValidator() {
		this(new OvertimeDAO(), new MonthlySheetDAO(), new AttendanceDAO(), new LeaveRequestDAO());
	}

	public OvertimeValidator(OvertimeDAO overtimeDAO, MonthlySheetDAO monthlySheetDAO, AttendanceDAO attendanceDAO,
			LeaveRequestDAO leaveRequestDAO) {
		this.overtimeDAO = overtimeDAO;
		this.monthlySheetDAO = monthlySheetDAO;
		this.attendanceDAO = attendanceDAO;
		this.leaveRequestDAO = leaveRequestDAO;
	}

	/**
	 * Loại kết quả validate: hợp lệ / trùng dữ liệu (bỏ qua êm) / lỗi (bỏ qua kèm
	 * lý do).
	 */
	public enum OutcomeType {
		OK, DUPLICATE, ERROR
	}

	public static class Outcome {
		public final OutcomeType type;
		public final String message;

		private Outcome(OutcomeType type, String message) {
			this.type = type;
			this.message = message;
		}

		public static Outcome ok() {
			return new Outcome(OutcomeType.OK, null);
		}

		public static Outcome duplicate(String message) {
			return new Outcome(OutcomeType.DUPLICATE, message);
		}

		public static Outcome error(String message) {
			return new Outcome(OutcomeType.ERROR, message);
		}
	}

	/**
	 * Validate đầy đủ rule cho 1 bản ghi OT sắp tạo/sửa.
	 *
	 * @param excludeId
	 *            dùng khi đang SỬA 1 bản ghi đã có (bỏ qua chính nó khi tính
	 *            trùng/tổng giờ tháng/năm) — truyền null khi TẠO MỚI.
	 */
	public Outcome validate(User targetUser, Long creatorId, LocalDate date, BigDecimal hours, String reason,
			Long excludeId) {

		if (targetUser == null || !Boolean.TRUE.equals(targetUser.getIsActive())) {
			return Outcome.error("Không tìm thấy nhân viên đang hoạt động.");
		}
		if (targetUser.getManagerId() == null || !targetUser.getManagerId().equals(creatorId)) {
			return Outcome.error("Nhân viên " + targetUser.getEmployeeCode() + " không thuộc quyền quản lý của bạn.");
		}
		if (date == null) {
			return Outcome.error("Ngày không hợp lệ.");
		}
		if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
			return Outcome.error("Số giờ OT không hợp lệ (phải lớn hơn 0).");
		}
		if (reason == null || reason.isBlank()) {
			return Outcome.error("Thiếu lý do tăng ca.");
		}
		if (hours.compareTo(WorkScheduleConfig.MAX_OT_HOURS_PER_DAY) > 0) {
			return Outcome.error("Số giờ OT (" + hours + "h) vượt quá tối đa " + WorkScheduleConfig.MAX_OT_HOURS_PER_DAY
					+ "h/ngày.");
		}

		DayOfWeek dow = date.getDayOfWeek();
		if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
			return Outcome.error("Ngày " + date + " là " + (dow == DayOfWeek.SATURDAY ? "Thứ 7" : "Chủ nhật")
					+ ", công ty làm việc T2-T6 nên không thể tạo OT ngày này.");
		}

		if (!monthlySheetDAO.isEditablePeriod(date.getYear(), date.getMonthValue())) {
			return Outcome.error("Tháng " + date.getMonthValue() + "/" + date.getYear()
					+ " không còn ở trạng thái OPEN, không thể tạo/sửa OT.");
		}

		Date sqlDate = Date.valueOf(date);

		if (overtimeDAO.existsActiveForUserAndDate(targetUser.getId(), sqlDate, excludeId)) {
			return Outcome.duplicate("Nhân viên " + targetUser.getFullName() + " (" + targetUser.getEmployeeCode()
					+ ") đã có OT ngày " + date + " trong hệ thống.");
		}

		if (leaveRequestDAO.hasApprovedOrLevel1LeaveOnDate(targetUser.getId(), sqlDate)) {
			return Outcome.error("Nhân viên " + targetUser.getEmployeeCode()
					+ " đã có đơn nghỉ phép đang/đã duyệt ngày " + date + ", không thể tạo OT cho ngày này.");
		}

		AttendanceRecord existingAttendance = attendanceDAO.findByUserAndDate(targetUser.getId(), sqlDate);
		if (existingAttendance != null && existingAttendance.getCheckIn() == null) {
			return Outcome.error("Nhân viên " + targetUser.getEmployeeCode() + " được ghi nhận VẮNG MẶT ngày " + date
					+ ", không thể tạo OT cho ngày này.");
		}
		if (existingAttendance != null && existingAttendance.getCheckOut() != null) {
			long otMinutes = hours.multiply(BigDecimal.valueOf(60)).longValue();
			LocalTime expectedCheckout = WorkScheduleConfig.OVERTIME_START.plusMinutes(otMinutes);
			if (existingAttendance.getCheckOut().toLocalTime().isBefore(expectedCheckout)) {
				return Outcome.error("Nhân viên " + targetUser.getEmployeeCode() + " đã chấm công ra lúc "
						+ existingAttendance.getCheckOut().toLocalTime() + " ngày " + date + ", không đủ hỗ trợ "
						+ hours + "h OT (cần ra từ " + expectedCheckout + " trở đi).");
			}
		}

		BigDecimal monthTotal = overtimeDAO.sumHoursInMonth(targetUser.getId(), date.getYear(), date.getMonthValue(),
				excludeId);
		if (monthTotal.add(hours).compareTo(MAX_HOURS_PER_MONTH) > 0) {
			return Outcome.error("Nhân viên " + targetUser.getEmployeeCode() + " đã có " + monthTotal
					+ "h OT trong tháng " + date.getMonthValue() + "/" + date.getYear() + ", cộng thêm " + hours
					+ "h sẽ vượt trần " + MAX_HOURS_PER_MONTH + "h/tháng.");
		}

		BigDecimal yearTotal = overtimeDAO.sumHoursInYear(targetUser.getId(), date.getYear(), excludeId);
		if (yearTotal.add(hours).compareTo(MAX_HOURS_PER_YEAR) > 0) {
			return Outcome.error("Nhân viên " + targetUser.getEmployeeCode() + " đã có " + yearTotal + "h OT trong năm "
					+ date.getYear() + ", cộng thêm " + hours + "h sẽ vượt trần " + MAX_HOURS_PER_YEAR + "h/năm.");
		}

		return Outcome.ok();
	}
}