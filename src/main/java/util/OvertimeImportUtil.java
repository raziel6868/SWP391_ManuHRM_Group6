package util;

import dal.AttendanceDAO;
import dal.LeaveRequestDAO;
import dal.MonthlySheetDAO;
import dal.OvertimeDAO;
import dal.UserDAO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Import Excel cho yêu cầu OT của quản đốc. Xử lý ĐỘC LẬP từng dòng — dòng hợp
 * lệ insert (tự động APPROVED) ngay, dòng trùng dữ liệu đã có trong DB thì bỏ
 * qua, dòng lỗi dữ liệu/vi phạm rule thì bỏ qua và ghi nhận lỗi — không có dòng
 * nào chặn các dòng khác trong cùng file.
 *
 * Toàn bộ rule nghiệp vụ dùng chung (2h/ngày, 40h/tháng, 200h/năm, trùng OT,
 * conflict nghỉ phép/chấm công, kỳ công phải OPEN...) nằm ở
 * {@link OvertimeValidator} — file này chỉ xử lý phần đặc thù riêng của Excel:
 * đọc cột, ngày phải thuộc đúng tháng đã chọn trên UI, và trùng dữ liệu ngay
 * trong cùng file.
 *
 * Cột Excel: employee_code | date | hours | reason
 */
public class OvertimeImportUtil {

	private final UserDAO userDAO;
	private final OvertimeDAO overtimeDAO;
	private final OvertimeValidator overtimeValidator;

	public OvertimeImportUtil() {
		this(new UserDAO(), new OvertimeDAO(), new OvertimeValidator(new OvertimeDAO(), new MonthlySheetDAO(),
				new AttendanceDAO(), new LeaveRequestDAO()));
	}

	public OvertimeImportUtil(UserDAO userDAO, OvertimeDAO overtimeDAO, MonthlySheetDAO monthlySheetDAO,
			AttendanceDAO attendanceDAO) {
		this(userDAO, overtimeDAO,
				new OvertimeValidator(overtimeDAO, monthlySheetDAO, attendanceDAO, new LeaveRequestDAO()));
	}

	public OvertimeImportUtil(UserDAO userDAO, OvertimeDAO overtimeDAO, OvertimeValidator overtimeValidator) {
		this.userDAO = userDAO;
		this.overtimeDAO = overtimeDAO;
		this.overtimeValidator = overtimeValidator;
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
				if (hours == null) {
					result.addError(displayRow, "Số giờ OT không hợp lệ.");
					continue;
				}
				if (reason == null) {
					result.addError(displayRow, "Thiếu lý do tăng ca.");
					continue;
				}

				// ── Đặc thù Excel: ngày phải thuộc đúng tháng/năm đã chọn trên UI ──
				if (date.getYear() != year || date.getMonthValue() != month) {
					result.addError(displayRow,
							"Ngày " + date + " không thuộc tháng " + month + "/" + year + " đã chọn.");
					continue;
				}

				// ── Đặc thù Excel: trùng nhân viên/ngày ngay trong cùng file ──
				String key = employeeCode.toUpperCase() + "|" + date;
				if (!importedKeys.add(key)) {
					result.addError(displayRow, "Trùng nhân viên/ngày với 1 dòng khác trong cùng file (" + employeeCode
							+ " - " + date + ").");
					continue;
				}

				User targetUser = userDAO.getByEmployeeCode(employeeCode);
				if (targetUser == null || !Boolean.TRUE.equals(targetUser.getIsActive())) {
					result.addError(displayRow, "Không tìm thấy nhân viên đang hoạt động có mã " + employeeCode + ".");
					continue;
				}

				// ── Toàn bộ rule nghiệp vụ dùng chung ──
				OvertimeValidator.Outcome outcome = overtimeValidator.validate(targetUser, creatorId, date, hours,
						reason, null);
				if (outcome.type == OvertimeValidator.OutcomeType.DUPLICATE) {
					result.addDuplicate(displayRow, outcome.message);
					continue;
				}
				if (outcome.type == OvertimeValidator.OutcomeType.ERROR) {
					result.addError(displayRow, outcome.message);
					continue;
				}

				Date sqlDate = Date.valueOf(date);
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