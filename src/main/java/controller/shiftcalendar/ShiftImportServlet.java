package controller.shiftcalendar;

import dal.ShiftAssignmentDAO;
import dal.ShiftDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.ShiftAssignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@WebServlet(name = "ShiftImportServlet", urlPatterns = {"/shift-calendar-import"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 20)
public class ShiftImportServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final UserDAO userDAO = new UserDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Part filePart = request.getPart("excelFile");
		if (filePart == null || filePart.getInputStream().available() == 0) {
			response.sendRedirect(request.getContextPath() + "/shift-calendar?error=emptyFile");
			return;
		}

		String fileName = getFileName(filePart);
		List<ShiftAssignment> assignments = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		int successCount = 0;

		try {
			Map<String, Long> userCodeToId = new HashMap<>();
			Map<String, Long> shiftCodeToId = new HashMap<>();

			for (model.User user : userDAO.getActiveUsersForDropdown()) {
				userCodeToId.put(user.getEmployeeCode().trim().toUpperCase(), user.getId());
			}
			for (model.Shift shift : shiftDAO.searchShifts(null, null, true, 0, 1000)) {
				shiftCodeToId.put(shift.getCode().trim().toUpperCase(), shift.getId());
			}

			if (fileName.toLowerCase().endsWith(".csv")) {
				processCSV(filePart.getInputStream(), userCodeToId, shiftCodeToId, assignments, errors);
			} else {
				processExcel(filePart.getInputStream(), userCodeToId, shiftCodeToId, assignments, errors);
			}

			if (!assignments.isEmpty()) {
				successCount = shiftAssignmentDAO.bulkUpsert(assignments);
			}

			request.getSession().setAttribute("importSuccessCount", successCount);
			request.getSession().setAttribute("importErrorCount", errors.size());
			request.getSession().setAttribute("importErrors", errors);
			request.getSession().setAttribute("importTotal", assignments.size());

		} catch (Exception e) {
			System.err.println("ShiftImportServlet ERROR: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/shift-calendar?error=parseError");
			return;
		}

		response.sendRedirect(request.getContextPath() + "/shift-calendar?imported=true");
	}

	private void processCSV(InputStream inputStream, Map<String, Long> userCodeToId, Map<String, Long> shiftCodeToId,
			List<ShiftAssignment> assignments, List<String> errors) throws IOException {

		DateTimeFormatter[] dateFormatters = {DateTimeFormatter.ofPattern("yyyy-MM-dd"),
				DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy"),
				DateTimeFormatter.ofPattern("d/M/yyyy"), DateTimeFormatter.ofPattern("d-M-yyyy")};

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
			String line;
			int rowNum = 0;

			while ((line = reader.readLine()) != null) {
				rowNum++;
				if (rowNum == 1) {
					continue;
				}

				if (line.trim().isEmpty()) {
					continue;
				}

				try {
					String[] parts = line.split(",");
					if (parts.length < 3) {
						errors.add("Dòng " + rowNum + ": Thiếu cột dữ liệu");
						continue;
					}

					String employeeCode = parts[0].trim();
					String dateStr = parts[1].trim();
					String shiftCode = parts[2].trim();

					if (employeeCode.isEmpty() || dateStr.isEmpty() || shiftCode.isEmpty()) {
						errors.add("Dòng " + rowNum + ": Thiếu thông tin bắt buộc");
						continue;
					}

					Long userId = userCodeToId.get(employeeCode.toUpperCase());
					if (userId == null) {
						errors.add("Dòng " + rowNum + ": Không tìm thấy nhân viên '" + employeeCode + "'");
						continue;
					}

					LocalDate date = parseDate(dateStr, dateFormatters, rowNum, errors);
					if (date == null) {
						continue;
					}

					Long shiftId = shiftCodeToId.get(shiftCode.toUpperCase());
					if (shiftId == null) {
						errors.add("Dòng " + rowNum + ": Không tìm thấy ca '" + shiftCode + "'");
						continue;
					}

					ShiftAssignment sa = new ShiftAssignment();
					sa.setUserId(userId);
					sa.setShiftId(shiftId);
					sa.setDate(Date.valueOf(date));
					assignments.add(sa);

				} catch (Exception e) {
					errors.add("Dòng " + rowNum + ": " + e.getMessage());
				}
			}
		}
	}

	private void processExcel(InputStream inputStream, Map<String, Long> userCodeToId, Map<String, Long> shiftCodeToId,
			List<ShiftAssignment> assignments, List<String> errors) throws IOException {

		DateTimeFormatter[] dateFormatters = {DateTimeFormatter.ofPattern("yyyy-MM-dd"),
				DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy"),
				DateTimeFormatter.ofPattern("d/M/yyyy"), DateTimeFormatter.ofPattern("d-M-yyyy")};

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				errors.add("File Excel không có sheet dữ liệu");
				return;
			}

			DataFormatter formatter = new DataFormatter();
			int rowNum = 0;

			for (Row row : sheet) {
				rowNum++;
				if (rowNum == 1) {
					continue;
				}

				try {
					Cell employeeCodeCell = row.getCell(0);
					Cell dateCell = row.getCell(1);
					Cell shiftCodeCell = row.getCell(2);

					if (employeeCodeCell == null && dateCell == null && shiftCodeCell == null) {
						continue;
					}

					String employeeCode = formatter.formatCellValue(employeeCodeCell).trim();
					String dateStr = formatter.formatCellValue(dateCell).trim();
					String shiftCode = formatter.formatCellValue(shiftCodeCell).trim();

					if (employeeCode.isEmpty() || dateStr.isEmpty() || shiftCode.isEmpty()) {
						errors.add("Dòng " + rowNum + ": Thiếu thông tin bắt buộc");
						continue;
					}

					Long userId = userCodeToId.get(employeeCode.toUpperCase());
					if (userId == null) {
						errors.add("Dòng " + rowNum + ": Không tìm thấy nhân viên '" + employeeCode + "'");
						continue;
					}

					LocalDate date = parseDate(dateStr, dateFormatters, rowNum, errors);
					if (date == null) {
						continue;
					}

					Long shiftId = shiftCodeToId.get(shiftCode.toUpperCase());
					if (shiftId == null) {
						errors.add("Dòng " + rowNum + ": Không tìm thấy ca '" + shiftCode + "'");
						continue;
					}

					ShiftAssignment sa = new ShiftAssignment();
					sa.setUserId(userId);
					sa.setShiftId(shiftId);
					sa.setDate(Date.valueOf(date));
					assignments.add(sa);

				} catch (Exception e) {
					errors.add("Dòng " + rowNum + ": " + e.getMessage());
				}
			}
		}
	}

	private LocalDate parseDate(String dateStr, DateTimeFormatter[] formatters, int rowNum, List<String> errors) {
		for (DateTimeFormatter fmt : formatters) {
			try {
				return LocalDate.parse(dateStr, fmt);
			} catch (DateTimeParseException ignored) {
			}
		}
		errors.add("Dòng " + rowNum + ": Định dạng ngày không hợp lệ '" + dateStr + "'");
		return null;
	}

	private String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] tokens = contentDisp.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf("=") + 1).trim().replace("\"", "");
			}
		}
		return "";
	}
}
