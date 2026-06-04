package controller.shift;

import dal.ShiftDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import model.Shift;
import util.ValidationUtil;

@WebServlet(name = "ShiftCreateServlet", urlPatterns = {"/shift-create"})
public class ShiftCreateServlet extends HttpServlet {

	private static final String CODE_REGEX = "^[A-Z][A-Z0-9_]*$";
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/shift/shift-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String code = normalizeCode(request.getParameter("code"));
		String name = normalizeText(request.getParameter("name"));
		String startTimeStr = normalizeText(request.getParameter("startTime"));
		String endTimeStr = normalizeText(request.getParameter("endTime"));
		String breakMinutesStr = normalizeText(request.getParameter("breakMinutes"));
		boolean isNightShift = "true".equalsIgnoreCase(request.getParameter("isNightShift"));

		request.setAttribute("code", code);
		request.setAttribute("name", name);
		request.setAttribute("startTime", startTimeStr);
		request.setAttribute("endTime", endTimeStr);
		request.setAttribute("breakMinutes", breakMinutesStr);
		request.setAttribute("isNightShift", isNightShift);

		String validationError = validate(code, name, startTimeStr, endTimeStr, breakMinutesStr, null);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.getRequestDispatcher("/views/shift/shift-create.jsp").forward(request, response);
			return;
		}

		Time startTime = parseTime(startTimeStr);
		Time endTime = parseTime(endTimeStr);
		Integer breakMinutes = parseBreakMinutes(breakMinutesStr);

		Shift shift = new Shift();
		shift.setCode(code);
		shift.setName(name);
		shift.setStartTime(startTime);
		shift.setEndTime(endTime);
		shift.setBreakMinutes(breakMinutes);
		shift.setIsNightShift(isNightShift);
		shift.setIsActive(true);

		boolean success = shiftDAO.insert(shift);
		if (success) {
			request.getSession().setAttribute("successMsg", "Thêm ca làm việc thành công.");
			response.sendRedirect(request.getContextPath() + "/shift-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể thêm ca làm việc. Vui lòng thử lại.");
		request.getRequestDispatcher("/views/shift/shift-create.jsp").forward(request, response);
	}

	private String validate(String code, String name, String startTimeStr, String endTimeStr, String breakMinutesStr,
			Long id) {
		if (ValidationUtil.isBlank(code)) {
			return "Mã ca làm việc không được để trống.";
		}
		if (code.length() > 30) {
			return "Mã ca làm việc không được vượt quá 30 ký tự.";
		}
		if (!ValidationUtil.matchRegex(code, CODE_REGEX)) {
			return "Mã ca làm việc phải viết hoa, bắt đầu bằng chữ cái và chỉ chứa chữ hoa, số hoặc dấu gạch dưới.";
		}
		if (ValidationUtil.isBlank(name)) {
			return "Tên ca làm việc không được để trống.";
		}
		if (name.length() > 100) {
			return "Tên ca làm việc không được vượt quá 100 ký tự.";
		}
		if (ValidationUtil.isBlank(startTimeStr)) {
			return "Giờ bắt đầu không được để trống.";
		}
		if (!isValidTimeFormat(startTimeStr)) {
			return "Giờ bắt đầu không hợp lệ. Vui lòng nhập định dạng HH:mm (ví dụ: 08:00).";
		}
		if (ValidationUtil.isBlank(endTimeStr)) {
			return "Giờ kết thúc không được để trống.";
		}
		if (!isValidTimeFormat(endTimeStr)) {
			return "Giờ kết thúc không hợp lệ. Vui lòng nhập định dạng HH:mm (ví dụ: 17:00).";
		}
		if (breakMinutesStr != null && !breakMinutesStr.isEmpty()) {
			try {
				int bm = Integer.parseInt(breakMinutesStr);
				if (bm < 0) {
					return "Số phút nghỉ không được nhỏ hơn 0.";
				}
				if (bm > 480) {
					return "Số phút nghỉ không được vượt quá 480.";
				}
			} catch (NumberFormatException e) {
				return "Số phút nghỉ phải là số nguyên.";
			}
		}
		boolean exists = id == null ? shiftDAO.existsByCode(code) : shiftDAO.existsByCodeExceptId(code, id);
		if (exists) {
			return "Mã ca làm việc đã tồn tại. Vui lòng nhập mã khác.";
		}
		return null;
	}

	private boolean isValidTimeFormat(String value) {
		try {
			LocalTime.parse(value, TIME_FORMATTER);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	private Time parseTime(String timeStr) {
		if (timeStr == null || timeStr.isBlank()) {
			return null;
		}
		try {
			return Time.valueOf(LocalTime.parse(timeStr, TIME_FORMATTER));
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private Integer parseBreakMinutes(String breakMinutesStr) {
		if (breakMinutesStr == null || breakMinutesStr.isBlank()) {
			return 0;
		}
		try {
			return Integer.parseInt(breakMinutesStr);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String normalizeCode(String code) {
		if (code == null) {
			return null;
		}
		String trimmed = code.trim().toUpperCase();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
