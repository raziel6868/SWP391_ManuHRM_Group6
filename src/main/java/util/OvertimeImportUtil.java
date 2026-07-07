package util;

import dal.AttendanceDAO;
import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import dal.UserDAO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.AttendanceRecord;
import model.User;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Import Excel cho yêu cầu OT của quản đốc. Khác với AttendanceImportUtil
 * (all-or-nothing), file này xử lý ĐỘC LẬP từng dòng: dòng hợp lệ được insert
 * (tự động APPROVED) ngay, dòng trùng dữ liệu đã có trong DB thì bỏ qua, dòng
 * lỗi dữ liệu/vi phạm rule thì bỏ qua và ghi nhận lỗi — không có dòng nào chặn
 * các dòng khác.
 *
 * Không còn phụ thuộc phân ca (shift_assignments) — công ty đã bỏ phân ca, toàn
 * bộ nhân viên làm ca hành chính cố định T2-T6, 7:00-17:00. Vì vậy rule "không
 * làm quá 20:00" được quy đổi thành hằng số: OT tối đa 3h/ngày.
 *
 * Cột Excel: employee_code | date | hours | reason
 */
public class OvertimeImportUtil {

	// Từ khi bỏ phân ca: toàn bộ nhân viên làm ca hành chính T2-T6, 7:00-17:00.
	// OT tối đa trong ngày để đảm bảo nghỉ trước 20:00 = 20:00 - 17:00 = 3 giờ.
	private static final BigDecimal MAX_HOURS_PER_DAY = new BigDecimal("3");
	private static final BigDecimal MAX_HOURS_PER_MONTH = new BigDecimal("40");
	private static final BigDecimal MAX_HOURS_PER_YEAR = new BigDecimal("200");
	private static final LocalTime STANDARD_SHIFT_END = LocalTime.of(17, 0);

	private final UserDAO userDAO;
	private final OvertimeDAO overtimeDAO;
	private final MonthlySheetDAO monthlySheetDAO;
	private final AttendanceDAO attendanceDAO;

	public OvertimeImportUtil() {
		this(new UserDAO(), new OvertimeDAO(), new MonthlySheetDAO(), new AttendanceDAO());
	}

	public OvertimeImportUtil(UserDAO userDAO, OvertimeDAO overtimeDAO, MonthlySheetDAO monthlySheetDAO,
			AttendanceDAO attendanceDAO) {
		this.userDAO = userDAO;
		this.overtimeDAO = overtimeDAO;
		this.monthlySheetDAO = monthlySheetDAO;
		this.attendanceDAO = attendanceDAO;
	}

	/**
	 * Đọc và xử lý file Excel. year/month là kỳ đã chọn trên UI trước khi import
	 * (phải chưa chốt công). creatorId là quản đốc đang thực hiện import — chỉ tạo
	 * được OT cho nhân viên dưới quyền quản lý của chính họ.
	 *
	 * Chỉ ném OvertimeImportException khi file hỏng/đọc không được/không có dòng dữ
	 * liệu nào. Lỗi ở từng dòng không làm dừng việc xử lý các dòng còn lại — được
	 * gom vào OvertimeImportResult.
	 */
	public OvertimeImportResult importExcel(InputStream inputStream, int year, int month, Long creatorId)
			throws OvertimeImportException {
		OvertimeImportResult result = new OvertimeImportResult();
		Set<String> importedKeys = new HashSet<>();
		boolean hasAnyDataRow = false;

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
				BigDecimal hours = readDecimal(row.getCell(2), formatter);
				String reason = readText(row.getCell(3), formatter);

				if (employeeCode == null) {
					result.addError(displayRow, "Thiếu mã nhân viên.");
					continue;
				}
				if (date == null) {
					result.addError(displayRow, "Ngày không hợp lệ. Định dạng gợi ý: yyyy-MM-dd.");
					continue;
				}
				if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
					result.addError(displayRow, "Số giờ OT không hợp lệ (phải lớn hơn 0).");
					continue;
				}
				if (reason == null || reason.isBlank()) {
					result.addError(displayRow, "Thiếu lý do tăng ca.");
					continue;
				}

				if (date.getYear() != year || date.getMonthValue() != month) {
					result.addError(displayRow,
							"Ngày " + date + " không thuộc tháng " + month + "/" + year + " đã chọn.");
					continue;
				}

				LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
				if (date.isBefore(today)) {
					result.addError(displayRow, "Ngày OT " + date + " là ngày trong quá khứ.");
					continue;
				}

				DayOfWeek dow = date.getDayOfWeek();
				if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
					result.addError(displayRow,
							"Ngày " + date + " là " + (dow == DayOfWeek.SATURDAY ? "Thứ 7" : "Chủ nhật")
									+ ", công ty làm việc T2-T6 nên không thể tạo OT ngày này.");
					continue;
				}

				if (hours.compareTo(MAX_HOURS_PER_DAY) > 0) {
					result.addError(displayRow, "Số giờ OT (" + hours + "h) vượt quá tối đa " + MAX_HOURS_PER_DAY
							+ "h/ngày (ca hành chính 7:00-17:00, OT không được quá 20:00).");
					continue;
				}

				String key = employeeCode.toUpperCase() + "|" + date;
				if (!importedKeys.add(key)) {
					result.addDuplicate(displayRow, "Trùng nhân viên/ngày với 1 dòng khác trong cùng file ("
							+ employeeCode + " - " + date + ").");
					continue;
				}

				User targetUser = userDAO.getByEmployeeCode(employeeCode);
				if (targetUser == null || !Boolean.TRUE.equals(targetUser.getIsActive())) {
					result.addError(displayRow, "Không tìm thấy nhân viên đang hoạt động có mã " + employeeCode + ".");
					continue;
				}
				if (targetUser.getManagerId() == null || !targetUser.getManagerId().equals(creatorId)) {
					result.addError(displayRow, "Nhân viên " + employeeCode + " không thuộc quyền quản lý của bạn.");
					continue;
				}

				Date sqlDate = Date.valueOf(date);

				if (overtimeDAO.existsActiveForUserAndDate(targetUser.getId(), sqlDate, null)) {
					result.addDuplicate(displayRow, "Nhân viên " + targetUser.getFullName() + " (" + employeeCode
							+ ") đã có OT ngày " + date + " trong hệ thống.");
					continue;
				}

				// Nếu ngày đó đã có chấm công thật (đã import trước đó), giờ ra phải đủ
				// hỗ trợ số giờ OT đang xin (ngược lại với conflict check bên
				// AttendanceImportUtil — validate 2 chiều để tránh trạng thái mâu thuẫn).
				AttendanceRecord existingAttendance = attendanceDAO.findByUserAndDate(targetUser.getId(), sqlDate);
				if (existingAttendance != null && existingAttendance.getCheckIn() == null) {
					result.addError(displayRow, "Nhân viên " + employeeCode + " được ghi nhận VẮNG MẶT ngày " + date
							+ ", không thể tạo OT cho ngày này.");
					continue;
				}
				if (existingAttendance != null && existingAttendance.getCheckOut() != null) {
					long otMinutes = hours.multiply(BigDecimal.valueOf(60)).longValue();
					LocalTime expectedCheckout = STANDARD_SHIFT_END.plusMinutes(otMinutes);
					if (existingAttendance.getCheckOut().toLocalTime().isBefore(expectedCheckout)) {
						result.addError(displayRow, "Nhân viên " + employeeCode + " đã chấm công ra lúc "
								+ existingAttendance.getCheckOut().toLocalTime() + " ngày " + date
								+ ", không đủ hỗ trợ " + hours + "h OT (cần ra từ " + expectedCheckout + " trở đi).");
						continue;
					}
				}

				BigDecimal monthTotal = overtimeDAO.sumHoursInMonth(targetUser.getId(), year, month, null);
				if (monthTotal.add(hours).compareTo(MAX_HOURS_PER_MONTH) > 0) {
					result.addError(displayRow,
							"Nhân viên " + employeeCode + " đã có " + monthTotal + "h OT trong tháng " + month + "/"
									+ year + ", cộng thêm " + hours + "h sẽ vượt trần 40h/tháng.");
					continue;
				}

				BigDecimal yearTotal = overtimeDAO.sumHoursInYear(targetUser.getId(), year, null);
				if (yearTotal.add(hours).compareTo(MAX_HOURS_PER_YEAR) > 0) {
					result.addError(displayRow, "Nhân viên " + employeeCode + " đã có " + yearTotal + "h OT trong năm "
							+ year + ", cộng thêm " + hours + "h sẽ vượt trần 200h/năm.");
					continue;
				}

				boolean inserted = overtimeDAO.insertAutoApproved(targetUser.getId(), sqlDate, hours, reason.trim(),
						creatorId);
				if (inserted) {
					result.successCount++;
				} else {
					result.addError(displayRow, "Không thể lưu dòng này. Vui lòng thử lại.");
				}
			}
		} catch (IOException e) {
			throw new OvertimeImportException("Không thể đọc file Excel. Vui lòng kiểm tra lại file tải lên.");
		} catch (Exception e) {
			throw new OvertimeImportException("File Excel không hợp lệ hoặc không đúng định dạng .xlsx.");
		}

		if (!hasAnyDataRow) {
			throw new OvertimeImportException("File không có dòng dữ liệu nào để import.");
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

	private BigDecimal readDecimal(Cell cell, DataFormatter formatter) {
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.NUMERIC) {
			return BigDecimal.valueOf(cell.getNumericCellValue());
		}
		String value = readText(cell, formatter);
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value.replace(",", ".").trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Kết quả import: đếm số dòng thành công / trùng / lỗi + chi tiết. */
	public static class OvertimeImportResult {
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
	public static class OvertimeImportException extends Exception {
		public OvertimeImportException(String message) {
			super(message);
		}
	}
}
