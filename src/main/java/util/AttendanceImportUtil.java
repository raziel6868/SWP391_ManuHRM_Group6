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

	public List<AttendanceRecord> parseExcel(InputStream inputStream, int year, int month)
			throws AttendanceImportException {
		List<AttendanceRecord> records = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		Set<String> importedKeys = new HashSet<>();
		String importBatchId = UUID.randomUUID().toString();

		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null || isBlankRow(row, formatter)) {
					continue;
				}

				int displayRow = rowIndex + 1;
				String employeeCode = readText(row.getCell(0), formatter);
				LocalDate date = readDate(row.getCell(1), formatter);
				LocalTime checkIn = readTime(row.getCell(2), formatter);
				LocalTime checkOut = readTime(row.getCell(3), formatter);

				// Chỉ bắt buộc employeeCode và date
				validateRequired(errors, displayRow, employeeCode, date);
				if (employeeCode == null || date == null) {
					continue;
				}

				// Kiem tra ngay co thuoc dung thang/nam duoc chon khong
				// Phai check som truoc conflict check de tranh bao conflict cua thang khac
				if (date.getYear() != year || date.getMonthValue() != month) {
					errors.add("Dong " + displayRow + ": Ngay " + date + " khong thuoc thang " + month + "/" + year
							+ " da chon.");
					continue;
				}

				// checkIn có giá trị → checkOut bắt buộc phải có và phải sau checkIn
				// checkIn null → ABSENT, hợp lệ, không báo lỗi
				if (checkIn != null) {
					if (checkOut == null) {
						errors.add("Dòng " + displayRow + ": Có giờ vào nhưng thiếu giờ ra. Định dạng gợi ý: HH:mm.");
						continue;
					}
					if (!checkOut.isAfter(checkIn)) {
						errors.add("Dòng " + displayRow + ": Giờ ra phải lớn hơn giờ vào.");
						continue;
					}
				}

				String key = employeeCode.toUpperCase() + "|" + date;
				if (!importedKeys.add(key)) {
					errors.add("Dòng " + displayRow + ": Trùng dữ liệu nhân viên/ngày trong file.");
					continue;
				}

				Long userId = attendanceDAO.findActiveUserIdByEmployeeCode(employeeCode);
				if (userId == null) {
					errors.add("Dòng " + displayRow + ": Không tìm thấy nhân viên đang hoạt động có mã " + employeeCode
							+ ".");
					continue;
				}

				Date sqlDate = Date.valueOf(date);

				// Tim ca duoc phan cong chinh xac (khong fallback default shift)
				Shift assignedShift = attendanceDAO.findAssignedShiftOnly(userId, sqlDate);

				// Conflict 3: Co checkin nhung khong co shift assignment
				if (assignedShift == null && checkIn != null) {
					errors.add("Dòng " + displayRow + " [" + employeeCode + " - " + date
							+ "]: Conflict ATTENDANCE_WITHOUT_SHIFT_ASSIGNMENT — nhân viên không có phân ca"
							+ " ngày này nhưng file Excel vẫn có dữ liệu chấm công."
							+ " Vui lòng xoá dòng này khỏi file Excel rồi import lại.");
					continue;
				}

				// Khong co shift assignment va khong co checkin: ghi ABSENT voi default shift
				Shift shift = assignedShift != null ? assignedShift : attendanceDAO.findDefaultShift();
				if (shift == null) {
					errors.add("Dòng " + displayRow + ": Không tìm được ca làm nào trong hệ thống.");
					continue;
				}

				// Conflict 4: Co shift assignment nhung checkin sai khung gio ca (+/- 2 tieng)
				if (assignedShift != null && checkIn != null && assignedShift.getStartTime() != null
						&& assignedShift.getEndTime() != null
						&& !Boolean.TRUE.equals(assignedShift.getIsNightShift())) {
					LocalTime shiftStart = assignedShift.getStartTime().toLocalTime();
					LocalTime shiftEnd = assignedShift.getEndTime().toLocalTime();
					LocalTime windowStart = shiftStart.minusHours(2);
					boolean wrongShift = checkIn.isBefore(windowStart) || checkIn.isAfter(shiftEnd);
					if (wrongShift) {
						errors.add("Dòng " + displayRow + " [" + employeeCode + " - " + date
								+ "]: Conflict WRONG_SHIFT_ATTENDANCE — nhân viên được phân ca "
								+ assignedShift.getName() + " (" + shiftStart + "–" + shiftEnd + ")"
								+ " nhưng giờ vào trong file là " + checkIn + "."
								+ " Vui lòng kiểm tra lại dữ liệu trong file Excel.");
						continue;
					}
				}

				AttendanceRecord record = new AttendanceRecord();
				record.setUserId(userId);
				record.setEmployeeCode(employeeCode);
				record.setDate(sqlDate);
				record.setShiftId(shift.getId());
				record.setCheckIn(checkIn != null ? Time.valueOf(checkIn) : null);
				record.setCheckOut(checkOut != null ? Time.valueOf(checkOut) : null);
				record.setWorkingHours(checkIn != null && checkOut != null
						? calculateWorkingHours(checkIn, checkOut, shift.getBreakMinutes())
						: null);
				record.setStatus(resolveStatus(checkIn, shift.getStartTime()));
				record.setImportBatchId(importBatchId);

				// ── Conflict check 1: Có leave APPROVED nhưng vẫn có attendance ──
				if (checkIn != null && leaveRequestDAO.hasApprovedLeaveOnDate(userId, sqlDate)) {
					errors.add("Dòng " + displayRow + " [" + employeeCode + " - " + date
							+ "]: Conflict ATTENDANCE_ON_APPROVED_LEAVE — nhân viên đã có đơn nghỉ phép"
							+ " được duyệt ngày này nhưng file Excel vẫn có dữ liệu chấm công."
							+ " Vui lòng xoá dòng này khỏi file Excel rồi import lại.");
					continue;
				}

				// ── Conflict check 2: Có OT APPROVED nhưng checkout không đủ muộn ──
				if (checkIn != null && checkOut != null) {
					OvertimeRecord approvedOT = overtimeDAO.findApprovedOTForUserAndDate(userId, sqlDate);
					if (approvedOT != null && approvedOT.getApprovedHours() != null && shift.getEndTime() != null) {
						long otMinutes = approvedOT.getApprovedHours().multiply(BigDecimal.valueOf(60)).longValue();
						LocalTime expectedCheckout = shift.getEndTime().toLocalTime().plusMinutes(otMinutes);
						if (checkOut.isBefore(expectedCheckout)) {
							errors.add("Dòng " + displayRow + " [" + employeeCode + " - " + date
									+ "]: Conflict APPROVED_OT_WITHOUT_MATCHING_ATTENDANCE — nhân viên có OT "
									+ approvedOT.getApprovedHours() + "h được duyệt nhưng giờ ra trong file là "
									+ checkOut + " (cần đến " + expectedCheckout + " trở đi)."
									+ " Vui lòng sửa lại giờ ra trong file Excel rồi import lại.");
							continue;
						}
					}
				}

				records.add(record);
			}
		} catch (IOException e) {
			throw new AttendanceImportException(
					List.of("Không thể đọc file Excel. Vui lòng kiểm tra lại file tải lên."));
		} catch (Exception e) {
			throw new AttendanceImportException(List.of("File Excel không hợp lệ hoặc không đúng định dạng .xlsx."));
		}

		if (!errors.isEmpty()) {
			throw new AttendanceImportException(errors);
		}
		if (records.isEmpty()) {
			throw new AttendanceImportException(List.of("File không có dòng dữ liệu hợp lệ để import."));
		}

		return records;
	}

	private void validateRequired(List<String> errors, int rowNumber, String employeeCode, LocalDate date) {
		if (employeeCode == null) {
			errors.add("Dòng " + rowNumber + ": Thiếu mã nhân viên.");
		}
		if (date == null) {
			errors.add("Dòng " + rowNumber + ": Ngày không hợp lệ. Định dạng gợi ý: yyyy-MM-dd.");
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

	public static class AttendanceImportException extends Exception {
		private final List<String> errors;

		public AttendanceImportException(List<String> errors) {
			super("Attendance import validation failed");
			this.errors = errors;
		}

		public List<String> getErrors() {
			return errors;
		}
	}
}