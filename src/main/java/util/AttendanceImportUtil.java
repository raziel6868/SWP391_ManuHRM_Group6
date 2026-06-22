package util;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import dal.ShiftAssignmentDAO;
import dal.UserDAO;
import dal.ShiftDAO;
import model.AttendanceRecord;
import model.Shift;
import model.User;

public class AttendanceImportUtil {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final int LATE_THRESHOLD_MINUTES = 15;

	private final UserDAO userDAO = new UserDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();
	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();

	public List<AttendanceRecord> parseExcel(InputStream inputStream, String batchId) throws Exception {
		List<AttendanceRecord> records = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}

				String employeeCode = getCellValue(row, 0);
				String dateStr = getCellValue(row, 1);
				String checkInStr = getCellValue(row, 2);
				String checkOutStr = getCellValue(row, 3);

				if (employeeCode == null || employeeCode.trim().isEmpty()) {
					continue;
				}

				User user = userDAO.getByEmployeeCode(employeeCode.trim());
				if (user == null) {
					continue;
				}

				Date date = parseDate(dateStr);
				if (date == null) {
					continue;
				}

				Time checkIn = parseTime(checkInStr);
				Time checkOut = parseTime(checkOutStr);

				Long shiftId = resolveShiftId(user.getId(), date);
				Shift shift = shiftId != null ? getShift(shiftId) : null;

				String status = deriveStatus(shift, checkIn);
				BigDecimal workingHours = calculateWorkingHours(checkIn, checkOut, shift);

				AttendanceRecord record = new AttendanceRecord();
				record.setUserId(user.getId());
				record.setDate(date);
				record.setShiftId(shiftId);
				record.setCheckIn(checkIn);
				record.setCheckOut(checkOut);
				record.setWorkingHours(workingHours);
				record.setStatus(status);
				record.setImportBatchId(batchId);

				records.add(record);
			}
		}

		return records;
	}

	private String getCellValue(Row row, int cellIndex) {
		if (row.getCell(cellIndex) == null) {
			return null;
		}
		return row.getCell(cellIndex).toString().trim();
	}

	private Date parseDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		try {
			LocalDate localDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
			return Date.valueOf(localDate);
		} catch (Exception e) {
			try {
				double excelDate = Double.parseDouble(dateStr.trim());
				java.util.Date utilDate = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(excelDate);
				return new java.sql.Date(utilDate.getTime());
			} catch (Exception ex) {
				return null;
			}
		}
	}

	private Time parseTime(String timeStr) {
		if (timeStr == null || timeStr.trim().isEmpty()) {
			return null;
		}
		try {
			LocalTime localTime = LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
			return Time.valueOf(localTime);
		} catch (Exception e) {
			try {
				double excelTime = Double.parseDouble(timeStr.trim());
				java.util.Date utilDate = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(excelTime);
				return new Time(utilDate.getTime());
			} catch (Exception ex) {
				return null;
			}
		}
	}

	private Long resolveShiftId(Long userId, Date date) {
		var assignments = shiftAssignmentDAO.searchAssignments(null, userId, null, date, date, 0, 1);
		if (!assignments.isEmpty()) {
			return assignments.get(0).getShiftId();
		}

		List<Shift> officeShifts = shiftDAO.searchShifts(null, false, true, 0, 1);
		if (!officeShifts.isEmpty()) {
			return officeShifts.get(0).getId();
		}

		List<Shift> allShifts = shiftDAO.searchShifts(null, null, true, 0, 1);
		if (!allShifts.isEmpty()) {
			return allShifts.get(0).getId();
		}

		return null;
	}

	private Shift getShift(Long shiftId) {
		return shiftDAO.getById(shiftId);
	}

	private String deriveStatus(Shift shift, Time checkIn) {
		if (checkIn == null) {
			return "ABSENT";
		}

		if (shift == null || shift.getStartTime() == null) {
			return "NORMAL";
		}

		LocalTime shiftStart = shift.getStartTime().toLocalTime();
		LocalTime actualIn = checkIn.toLocalTime();

		long lateMinutes = ChronoUnit.MINUTES.between(shiftStart, actualIn);
		if (lateMinutes > LATE_THRESHOLD_MINUTES) {
			return "LATE";
		}

		return "NORMAL";
	}

	private BigDecimal calculateWorkingHours(Time checkIn, Time checkOut, Shift shift) {
		if (checkIn == null || checkOut == null) {
			return BigDecimal.ZERO;
		}

		long totalMinutes = ChronoUnit.MINUTES.between(checkIn.toLocalTime(), checkOut.toLocalTime());
		if (totalMinutes < 0) {
			totalMinutes += 24 * 60;
		}

		if (shift != null && shift.getBreakMinutes() != null) {
			totalMinutes -= shift.getBreakMinutes();
		}

		if (totalMinutes < 0) {
			totalMinutes = 0;
		}

		return BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
	}
}
