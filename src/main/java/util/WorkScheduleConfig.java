package util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Cấu hình giờ làm chuẩn cố định. Toàn công ty dùng chung 1 khung giờ:
 * 08:00–17:00, break 60 phút. OT bắt đầu sau giờ làm, từ 17:00 đến 19:00, tối
 * đa 2h/ngày. Attendance và Overtime dùng chung các hằng số này để tính giờ
 * công, trạng thái đi muộn (LATE), và giờ checkout mong đợi khi có OT.
 */
public final class WorkScheduleConfig {

	private WorkScheduleConfig() {
	}

	public static final LocalTime STANDARD_START = LocalTime.of(8, 0);
	public static final LocalTime STANDARD_END = LocalTime.of(17, 0);
	public static final LocalTime OVERTIME_START = STANDARD_END;
	public static final LocalTime OVERTIME_END = LocalTime.of(19, 0);
	public static final int MAX_OT_MINUTES_PER_DAY = (int) Duration.between(OVERTIME_START, OVERTIME_END).toMinutes();
	public static final BigDecimal MAX_OT_HOURS_PER_DAY = BigDecimal.valueOf(MAX_OT_MINUTES_PER_DAY)
			.divide(BigDecimal.valueOf(60));
	public static final int BREAK_MINUTES = 60;
	public static final int LATE_THRESHOLD_MINUTES = 15;
}
