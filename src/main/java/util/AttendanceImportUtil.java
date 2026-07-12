package util;

import dal.AttendanceDAO;
import dal.DBContext;
import dal.LeaveRequestDAO;
import dal.OvertimeDAO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import model.AttendanceRecord;
import model.OvertimeRecord;
import model.Shift;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Import Excel chấm công — kiểu ALL-OR-NOTHING: đọc và validate TOÀN BỘ các
 * dòng dữ liệu trước (không dừng ở lỗi đầu tiên, gom hết lỗi của mọi dòng sai
 * lại), rồi mới quyết định: - Nếu còn bất kỳ dòng nào lỗi thật (không đọc được
 * / thiếu dữ liệu / conflict với DB, leave, OT...) → HỦY TOÀN BỘ, không insert
 * bất kỳ dòng nào, trả về danh sách đầy đủ các lỗi để HR/quản đốc sửa 1 lần. -
 * Nếu không còn lỗi nào → insert tất cả các dòng hợp lệ (trừ dòng "trùng y hệt
 * dữ liệu đã có" — dòng này không phải lỗi, chỉ bỏ qua) trong CÙNG 1
 * transaction. Insert thất bại giữa chừng (lỗi DB) sẽ rollback hết, không để
 * lại dữ liệu insert dở dang.
 *
 * Các validate/conflict-check giữ nguyên như bản trước đó (xử lý độc lập từng
 * dòng): so khớp với dữ liệu đã có trong DB, leave đã duyệt, OT đã duyệt... Chỉ
 * có CÁCH THỰC THI (all-or-nothing thay vì độc lập từng dòng) là thay đổi.
 *
 * Không còn phụ thuộc phân ca (shift_assignments) — công ty đã bỏ phân ca, luôn
 * dùng ca mặc định (findDefaultShift(), là ca "OFFICE" 08:00–17:00, break 60
 * phút) để tính trạng thái đi muộn và giờ công.
 */
public class AttendanceImportUtil {

	private static final int LATE_THRESHOLD_MINUTES = 15;

	private final AttendanceDAO attendanceDAO;
	private final LeaveRequestDAO leaveRequestDAO;
	private final OvertimeDAO overtimeDAO;

	public AttendanceImportUtil() {
		this(new AttendanceDAO(), new LeaveRequestDAO(), new OvertimeDAO());
	}

	public AttendanceImportUtil(AttendanceDAO attendanceDAO) {
		this(attendanceDAO, new LeaveRequestDAO(), new OvertimeDAO());
	}

	public AttendanceImportUtil(AttendanceDAO attendanceDAO, LeaveRequestDAO leaveRequestDAO, OvertimeDAO overtimeDAO) {
		this.attendanceDAO = attendanceDAO;
		this.leaveRequestDAO = leaveRequestDAO;
		this.overtimeDAO = overtimeDAO;
	}

	/**
	 * Đọc, validate TOÀN BỘ file rồi insert all-or-nothing. year/month là kỳ đã
	 * chọn trên UI (phải chưa chốt công — servlet kiểm tra trước khi gọi hàm này).
	 *
	 * Ném AttendanceImportException khi: lỗi ở cấp toàn bộ file (không đọc được /
	 * không có dòng dữ liệu nào), HOẶC khi có ít nhất 1 dòng lỗi validate (kèm TOÀN
	 * BỘ danh sách lỗi của mọi dòng sai) — cả 2 trường hợp đều KHÔNG insert bất kỳ
	 * dòng nào.
	 */
	public AttendanceImportResult importExcel(InputStream inputStream, int year, int month)
			throws AttendanceImportException {
		List<String> errorMessages = new ArrayList<>();
		List<String> duplicateMessages = new ArrayList<>();
		List<AttendanceRecord> toInsert = new ArrayList<>();
		Set<String> importedKeys = new HashSet<>();
		String importBatchId = UUID.randomUUID().toString();
		boolean hasAnyDataRow = false;
		int totalDataRows = 0;

		Shift shift = attendanceDAO.findDefaultShift();

		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null || isBlankRow(row, formatter)) {
					continue;
				}
				hasAnyDataRow = true;
				int displayRow = rowIndex + 1;
				totalDataRows++;

				String employeeCode = readText(row.getCell(0), formatter);
				LocalDate date = readDate(row.getCell(1), formatter);
				LocalTime checkIn = readTime(row.getCell(2), formatter);
				LocalTime checkOut = readTime(row.getCell(3), formatter);

				if (employeeCode == null) {
					errorMessages.add("Dòng " + displayRow + ": Thiếu mã nhân viên.");
					continue;
				}
				if (date == null) {
					errorMessages.add("Dòng " + displayRow + ": Ngày không hợp lệ. Định dạng gợi ý: yyyy-MM-dd.");
					continue;
				}

				if (date.getYear() != year || date.getMonthValue() != month) {
					errorMessages.add("Dòng " + displayRow + ": Ngày " + date + " không thuộc tháng " + month + "/"
							+ year + " đã chọn.");
					continue;
				}

				LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
				if (date.isAfter(today)) {
					errorMessages.add("Dòng " + displayRow + ": Ngày " + date
							+ " là ngày trong tương lai, chưa thể có dữ liệu chấm công.");
					continue;
				}

				DayOfWeek dow = date.getDayOfWeek();
				if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
					errorMessages.add("Dòng " + displayRow + ": Ngày " + date + " là "
							+ (dow == DayOfWeek.SATURDAY ? "Thứ 7" : "Chủ nhật")
							+ ", công ty làm việc T2-T6 nên không thể nhập chấm công ngày này.");
					continue;
				}

				// checkIn có giá trị → checkOut bắt buộc phải có và phải sau checkIn.
				// checkIn null → ABSENT, hợp lệ, không báo lỗi.
				if (checkIn != null) {
					if (checkOut == null) {
						errorMessages
								.add("Dòng " + displayRow + ": Có giờ vào nhưng thiếu giờ ra. Định dạng gợi ý: HH:mm.");
						continue;
					}
					if (!checkOut.isAfter(checkIn)) {
						errorMessages.add("Dòng " + displayRow + ": Giờ ra phải lớn hơn giờ vào.");
						continue;
					}
				}

				String key = employeeCode.toUpperCase() + "|" + date;
				if (!importedKeys.add(key)) {
					errorMessages.add(
							"Dòng " + displayRow + ": Trùng dữ liệu nhân viên/ngày với 1 dòng khác trong cùng file.");
					continue;
				}

				Long userId = attendanceDAO.findActiveUserIdByEmployeeCode(employeeCode);
				if (userId == null) {
					errorMessages.add("Dòng " + displayRow + ": Không tìm thấy nhân viên đang hoạt động có mã "
							+ employeeCode + ".");
					continue;
				}

				if (shift == null) {
					errorMessages.add("Dòng " + displayRow + ": Không tìm được ca làm mặc định nào trong hệ thống.");
					continue;
				}

				Date sqlDate = Date.valueOf(date);
				Time newCheckIn = checkIn != null ? Time.valueOf(checkIn) : null;
				Time newCheckOut = checkOut != null ? Time.valueOf(checkOut) : null;

				// ── Đã có dữ liệu chấm công ngày này trong DB chưa? ──
				AttendanceRecord existing = attendanceDAO.findByUserAndDate(userId, sqlDate);
				if (existing != null) {
					boolean same = Objects.equals(existing.getCheckIn(), newCheckIn)
							&& Objects.equals(existing.getCheckOut(), newCheckOut);
					if (same) {
						duplicateMessages.add("Dòng " + displayRow + ": Nhân viên " + employeeCode
								+ " đã có chấm công ngày " + date + " giống hệt dữ liệu này trong hệ thống.");
					} else {
						errorMessages
								.add("Dòng " + displayRow + ": Nhân viên " + employeeCode + " đã có chấm công ngày "
										+ date + " nhưng KHÁC dữ liệu trong file (giờ vào/ra không khớp)."
										+ " Không ghi đè — nếu cần sửa, dùng chức năng \"Yêu cầu chỉnh sửa công\".");
					}
					continue;
				}

				// ── Conflict: có leave APPROVED nhưng vẫn có chấm công ──
				if (checkIn != null && leaveRequestDAO.hasApprovedLeaveOnDate(userId, sqlDate)) {
					errorMessages.add("Dòng " + displayRow + ": Nhân viên " + employeeCode
							+ " đã có đơn nghỉ phép được duyệt ngày " + date + " nhưng file vẫn có dữ liệu chấm công.");
					continue;
				}

				// ── Conflict: có OT APPROVED nhưng checkout không đủ muộn ──
				if (checkIn != null && checkOut != null) {
					OvertimeRecord approvedOT = overtimeDAO.findApprovedOTForUserAndDate(userId, sqlDate);
					if (approvedOT != null && approvedOT.getApprovedHours() != null && shift.getEndTime() != null) {
						long otMinutes = approvedOT.getApprovedHours().multiply(BigDecimal.valueOf(60)).longValue();
						LocalTime expectedCheckout = shift.getEndTime().toLocalTime().plusMinutes(otMinutes);
						if (checkOut.isBefore(expectedCheckout)) {
							errorMessages.add("Dòng " + displayRow + ": Nhân viên " + employeeCode + " có OT "
									+ approvedOT.getApprovedHours() + "h được duyệt ngày " + date
									+ " nhưng giờ ra trong file là " + checkOut + " (cần đến " + expectedCheckout
									+ " trở đi).");
							continue;
						}
					}
				}

				AttendanceRecord record = new AttendanceRecord();
				record.setUserId(userId);
				record.setEmployeeCode(employeeCode);
				record.setDate(sqlDate);
				record.setShiftId(shift.getId());
				record.setCheckIn(newCheckIn);
				record.setCheckOut(newCheckOut);
				record.setWorkingHours(checkIn != null && checkOut != null
						? calculateWorkingHours(checkIn, checkOut, shift.getBreakMinutes())
						: null);
				record.setStatus(resolveStatus(checkIn, shift.getStartTime()));
				record.setImportBatchId(importBatchId);

				toInsert.add(record);
			}
		} catch (IOException e) {
			throw new AttendanceImportException("Không thể đọc file Excel. Vui lòng kiểm tra lại file tải lên.");
		} catch (Exception e) {
			throw new AttendanceImportException("File Excel không hợp lệ hoặc không đúng định dạng .xlsx.");
		}

		if (!hasAnyDataRow) {
			throw new AttendanceImportException("File không có dòng dữ liệu nào để import.");
		}

		// Có ít nhất 1 dòng lỗi thật → hủy toàn bộ, không insert gì cả.
		if (!errorMessages.isEmpty()) {
			throw new AttendanceImportException(totalDataRows, errorMessages, duplicateMessages);
		}

		// Không còn lỗi nào → insert toàn bộ dòng hợp lệ trong CÙNG 1 transaction.
		int insertedCount = insertAllOrNothing(toInsert);

		AttendanceImportResult result = new AttendanceImportResult();
		result.totalDataRows = totalDataRows;
		result.successCount = insertedCount;
		result.duplicateMessages.addAll(duplicateMessages);
		return result;
	}

	/**
	 * Insert toàn bộ danh sách record trong 1 transaction duy nhất: hoặc tất cả
	 * thành công, hoặc rollback hết (không để lại dữ liệu insert dở dang).
	 */
	private int insertAllOrNothing(List<AttendanceRecord> records) throws AttendanceImportException {
		if (records.isEmpty()) {
			return 0;
		}
		Connection conn = null;
		try {
			conn = DBContext.getConnection();
			if (conn == null) {
				throw new SQLException("Không thể kết nối database.");
			}
			conn.setAutoCommit(false);
			for (AttendanceRecord record : records) {
				boolean inserted = attendanceDAO.insert(conn, record);
				if (!inserted) {
					conn.rollback();
					throw new AttendanceImportException(
							"Không thể lưu dữ liệu import. Đã hủy toàn bộ, vui lòng thử lại.");
				}
			}
			conn.commit();
			return records.size();
		} catch (SQLException e) {
			rollbackQuietly(conn);
			throw new AttendanceImportException(
					"Lỗi cơ sở dữ liệu khi import. Đã hủy toàn bộ, không có dòng nào được lưu.");
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	private void rollbackQuietly(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException ignore) {
			}
		}
	}

	private boolean isBlankRow(Row row, DataFormatter formatter) {
		for (int i = 0; i < 4; i++) {
			if (readText(row.getCell(i), formatter) != null) {
				return false;
			}
		}
		return true;
	}

	private String readText(Cell cell, DataFormatter formatter) {
		if (cell == null) {
			return null;
		}
		String value = formatter.formatCellValue(cell);
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	private LocalDate readDate(Cell cell, DataFormatter formatter) {
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
			return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}

		String value = readText(cell, formatter);
		if (value == null) {
			return null;
		}

		List<DateTimeFormatter> formatters = List.of(DateTimeFormatter.ISO_LOCAL_DATE,
				DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("d/M/yyyy"));
		for (DateTimeFormatter dateFormatter : formatters) {
			try {
				return LocalDate.parse(value, dateFormatter);
			} catch (DateTimeParseException ignore) {
			}
		}
		return null;
	}

	private LocalTime readTime(Cell cell, DataFormatter formatter) {
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
			LocalDateTime dateTime = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
					.toLocalDateTime();
			return dateTime.toLocalTime().withSecond(0).withNano(0);
		}

		String value = readText(cell, formatter);
		if (value == null) {
			return null;
		}

		List<DateTimeFormatter> formatters = List.of(DateTimeFormatter.ofPattern("HH:mm"),
				DateTimeFormatter.ofPattern("H:mm"), DateTimeFormatter.ofPattern("HH:mm:ss"),
				DateTimeFormatter.ofPattern("H:mm:ss"));
		for (DateTimeFormatter timeFormatter : formatters) {
			try {
				return LocalTime.parse(value, timeFormatter).withSecond(0).withNano(0);
			} catch (DateTimeParseException ignore) {
			}
		}
		return null;
	}

	private BigDecimal calculateWorkingHours(LocalTime checkIn, LocalTime checkOut, Integer breakMinutes) {
		long minutes = Duration.between(checkIn, checkOut).toMinutes();
		minutes -= breakMinutes != null ? breakMinutes : 0;
		if (minutes < 0) {
			minutes = 0;
		}
		return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

	/**
	 * Ca mặc định (findDefaultShift) luôn là "OFFICE" 08:00–17:00 → đi muộn quá 15
	 * phút mới tính LATE.
	 */
	private String resolveStatus(LocalTime checkIn, Time shiftStartTime) {
		if (checkIn == null) {
			return "ABSENT";
		}
		if (shiftStartTime == null) {
			return "NORMAL";
		}
		LocalTime lateTime = shiftStartTime.toLocalTime().plusMinutes(LATE_THRESHOLD_MINUTES);
		return checkIn.isAfter(lateTime) ? "LATE" : "NORMAL";
	}

	/**
	 * Kết quả import THÀNH CÔNG (all-or-nothing: chỉ được trả về khi không còn dòng
	 * lỗi nào — mọi dòng hợp lệ đã insert hết).
	 */
	public static class AttendanceImportResult {
		private int totalDataRows;
		private int successCount;
		private final List<String> duplicateMessages = new ArrayList<>();

		public int getTotalDataRows() {
			return totalDataRows;
		}

		public int getSuccessCount() {
			return successCount;
		}

		public int getDuplicateCount() {
			return duplicateMessages.size();
		}

		public List<String> getDuplicateMessages() {
			return duplicateMessages;
		}
	}

	/**
	 * Ném khi import KHÔNG thành công — dù là lỗi cấp file (không đọc được / không
	 * có dữ liệu / lỗi DB khi insert) hay có dòng lỗi validate. Trong mọi trường
	 * hợp, KHÔNG có dòng nào được lưu vào DB (all-or-nothing).
	 *
	 * - errors.size() == 1 và totalDataRows == 0: lỗi cấp file (dùng constructor
	 * 1-tham-số). - errors có thể nhiều dòng: lỗi validate của từng dòng dữ liệu,
	 * kèm duplicateMessages để hiển thị tham khảo (không phải lỗi).
	 */
	public static class AttendanceImportException extends Exception {
		private final int totalDataRows;
		private final List<String> errors;
		private final List<String> duplicateMessages;

		public AttendanceImportException(String message) {
			super(message);
			this.totalDataRows = 0;
			this.errors = List.of(message);
			this.duplicateMessages = List.of();
		}

		public AttendanceImportException(int totalDataRows, List<String> errors, List<String> duplicateMessages) {
			super("Attendance import validation failed: " + errors.size() + " lỗi");
			this.totalDataRows = totalDataRows;
			this.errors = errors;
			this.duplicateMessages = duplicateMessages;
		}

		public int getTotalDataRows() {
			return totalDataRows;
		}

		public List<String> getErrors() {
			return errors;
		}

		public List<String> getDuplicateMessages() {
			return duplicateMessages;
		}
	}
}