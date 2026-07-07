package util;

import dal.AttendanceDAO;
import dal.LeaveRequestDAO;
import dal.OvertimeDAO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
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
 * Import Excel chấm công. Khác với bản cũ (all-or-nothing, xóa sạch cả tháng
 * rồi insert lại từ đầu), file này xử lý ĐỘC LẬP từng dòng — giống cách làm với
 * OvertimeImportUtil: - Dòng hợp lệ, (nhân viên, ngày) CHƯA có trong DB →
 * insert. - Dòng mà (nhân viên, ngày) đã có trong DB và giờ checkin/checkout
 * GIỐNG HỆT → bỏ qua, tính là "trùng" (không phải lỗi). - Dòng mà (nhân viên,
 * ngày) đã có trong DB nhưng giờ KHÁC → conflict, gộp chung vào nhóm lỗi (không
 * ghi đè dữ liệu cũ) — muốn sửa dữ liệu đã có phải dùng chức năng "Yêu cầu
 * chỉnh sửa công", không sửa qua import.
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
	 * Đọc và xử lý file Excel chấm công. year/month là kỳ đã chọn trên UI (phải
	 * chưa chốt công — servlet kiểm tra trước khi gọi hàm này).
	 *
	 * Chỉ ném AttendanceImportException khi lỗi ở cấp toàn bộ file (không đọc được
	 * / không có dòng dữ liệu nào). Lỗi từng dòng không làm dừng việc xử lý các
	 * dòng còn lại — được gom vào AttendanceImportResult.
	 */
	public AttendanceImportResult importExcel(InputStream inputStream, int year, int month)
			throws AttendanceImportException {
		AttendanceImportResult result = new AttendanceImportResult();
		Set<String> importedKeys = new HashSet<>();
		String importBatchId = UUID.randomUUID().toString();
		boolean hasAnyDataRow = false;

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
				result.totalDataRows++;

				String employeeCode = readText(row.getCell(0), formatter);
				LocalDate date = readDate(row.getCell(1), formatter);
				LocalTime checkIn = readTime(row.getCell(2), formatter);
				LocalTime checkOut = readTime(row.getCell(3), formatter);

				if (employeeCode == null) {
					result.addError(displayRow, "Thiếu mã nhân viên.");
					continue;
				}
				if (date == null) {
					result.addError(displayRow, "Ngày không hợp lệ. Định dạng gợi ý: yyyy-MM-dd.");
					continue;
				}

				if (date.getYear() != year || date.getMonthValue() != month) {
					result.addError(displayRow,
							"Ngày " + date + " không thuộc tháng " + month + "/" + year + " đã chọn.");
					continue;
				}

				LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
				if (date.isAfter(today)) {
					result.addError(displayRow,
							"Ngày " + date + " là ngày trong tương lai, chưa thể có dữ liệu chấm công.");
					continue;
				}

				DayOfWeek dow = date.getDayOfWeek();
				if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
					result.addError(displayRow,
							"Ngày " + date + " là " + (dow == DayOfWeek.SATURDAY ? "Thứ 7" : "Chủ nhật")
									+ ", công ty làm việc T2-T6 nên không thể nhập chấm công ngày này.");
					continue;
				}

				// checkIn có giá trị → checkOut bắt buộc phải có và phải sau checkIn.
				// checkIn null → ABSENT, hợp lệ, không báo lỗi.
				if (checkIn != null) {
					if (checkOut == null) {
						result.addError(displayRow, "Có giờ vào nhưng thiếu giờ ra. Định dạng gợi ý: HH:mm.");
						continue;
					}
					if (!checkOut.isAfter(checkIn)) {
						result.addError(displayRow, "Giờ ra phải lớn hơn giờ vào.");
						continue;
					}
				}

				String key = employeeCode.toUpperCase() + "|" + date;
				if (!importedKeys.add(key)) {
					result.addError(displayRow, "Trùng dữ liệu nhân viên/ngày với 1 dòng khác trong cùng file.");
					continue;
				}

				Long userId = attendanceDAO.findActiveUserIdByEmployeeCode(employeeCode);
				if (userId == null) {
					result.addError(displayRow, "Không tìm thấy nhân viên đang hoạt động có mã " + employeeCode + ".");
					continue;
				}

				if (shift == null) {
					result.addError(displayRow, "Không tìm được ca làm mặc định nào trong hệ thống.");
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
						result.addDuplicate(displayRow, "Nhân viên " + employeeCode + " đã có chấm công ngày " + date
								+ " giống hệt dữ liệu này trong hệ thống.");
					} else {
						result.addError(displayRow,
								"Nhân viên " + employeeCode + " đã có chấm công ngày " + date
										+ " nhưng KHÁC dữ liệu trong file (giờ vào/ra không khớp)."
										+ " Không ghi đè — nếu cần sửa, dùng chức năng \"Yêu cầu chỉnh sửa công\".");
					}
					continue;
				}

				// ── Conflict: có leave APPROVED nhưng vẫn có chấm công ──
				if (checkIn != null && leaveRequestDAO.hasApprovedLeaveOnDate(userId, sqlDate)) {
					result.addError(displayRow, "Nhân viên " + employeeCode + " đã có đơn nghỉ phép được duyệt ngày "
							+ date + " nhưng file vẫn có dữ liệu chấm công.");
					continue;
				}

				// ── Conflict: có OT APPROVED nhưng checkout không đủ muộn ──
				if (checkIn != null && checkOut != null) {
					OvertimeRecord approvedOT = overtimeDAO.findApprovedOTForUserAndDate(userId, sqlDate);
					if (approvedOT != null && approvedOT.getApprovedHours() != null && shift.getEndTime() != null) {
						long otMinutes = approvedOT.getApprovedHours().multiply(BigDecimal.valueOf(60)).longValue();
						LocalTime expectedCheckout = shift.getEndTime().toLocalTime().plusMinutes(otMinutes);
						if (checkOut.isBefore(expectedCheckout)) {
							result.addError(displayRow,
									"Nhân viên " + employeeCode + " có OT " + approvedOT.getApprovedHours()
											+ "h được duyệt ngày " + date + " nhưng giờ ra trong file là " + checkOut
											+ " (cần đến " + expectedCheckout + " trở đi).");
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

				boolean inserted = attendanceDAO.insert(record);
				if (inserted) {
					result.successCount++;
				} else {
					result.addError(displayRow, "Không thể lưu dòng này. Vui lòng thử lại.");
				}
			}
		} catch (IOException e) {
			throw new AttendanceImportException("Không thể đọc file Excel. Vui lòng kiểm tra lại file tải lên.");
		} catch (Exception e) {
			throw new AttendanceImportException("File Excel không hợp lệ hoặc không đúng định dạng .xlsx.");
		}

		if (!hasAnyDataRow) {
			throw new AttendanceImportException("File không có dòng dữ liệu nào để import.");
		}

		return result;
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
	 * Kết quả import: đếm số dòng thành công / trùng / lỗi (bao gồm cả conflict) +
	 * chi tiết.
	 */
	public static class AttendanceImportResult {
		private int totalDataRows;
		private int successCount;
		private final List<String> errorMessages = new ArrayList<>();
		private final List<String> duplicateMessages = new ArrayList<>();

		public void addError(int row, String message) {
			errorMessages.add("Dòng " + row + ": " + message);
		}

		public void addDuplicate(int row, String message) {
			duplicateMessages.add("Dòng " + row + ": " + message);
		}

		public int getTotalDataRows() {
			return totalDataRows;
		}

		public int getSuccessCount() {
			return successCount;
		}

		public int getDuplicateCount() {
			return duplicateMessages.size();
		}

		public int getErrorCount() {
			return errorMessages.size();
		}

		public List<String> getErrorMessages() {
			return errorMessages;
		}

		public List<String> getDuplicateMessages() {
			return duplicateMessages;
		}
	}

	/** Chỉ ném khi lỗi ở cấp toàn bộ file (không đọc được / không có dữ liệu). */
	public static class AttendanceImportException extends Exception {
		public AttendanceImportException(String message) {
			super(message);
		}
	}
}
