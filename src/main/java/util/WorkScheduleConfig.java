package util;

import java.time.LocalTime;

/**
 * Cấu hình giờ làm chuẩn cố định, dùng thay cho bảng {@code shifts} (đã bỏ tính
 * năng phân ca). Toàn công ty dùng chung 1 khung giờ: 08:00–17:00, break 60
 * phút. Attendance và Overtime dùng chung các hằng số này để tính giờ công,
 * trạng thái đi muộn (LATE), và giờ checkout mong đợi khi có OT.
 */
public final class WorkScheduleConfig {

	private WorkScheduleConfig() {
	}

	public static final LocalTime STANDARD_START = LocalTime.of(8, 0);
	public static final LocalTime STANDARD_END = LocalTime.of(17, 0);
	public static final int BREAK_MINUTES = 60;
	public static final int LATE_THRESHOLD_MINUTES = 15;
}