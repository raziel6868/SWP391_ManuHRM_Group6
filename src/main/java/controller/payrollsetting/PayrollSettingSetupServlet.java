package controller.payrollsetting;

import dal.PayrollSettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import model.PayrollSetting;
import model.Permission;
import model.User;

@WebServlet(name = "PayrollSettingSetupServlet", urlPatterns = {"/payroll-setting-setup"})
public class PayrollSettingSetupServlet extends HttpServlet {

	private static final BigDecimal DEFAULT_STANDARD_WORK_DAYS = new BigDecimal("22.00");

	private final PayrollSettingDAO payrollSettingDAO = new PayrollSettingDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PAYROLL_SETTING_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		PayrollSetting payrollSetting = null;
		if (id != null) {
			payrollSetting = payrollSettingDAO.getById(id);
			if (payrollSetting == null) {
				session.setAttribute("errorMsg", "Cấu hình payroll không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/payroll-setting-list");
				return;
			}
		}

		prepareForm(request, payrollSetting);
		request.getRequestDispatcher("/views/payrollsetting/payroll-setting-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PAYROLL_SETTING_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		PayrollSetting payrollSetting = buildSetting(request);
		String validationError = validate(payrollSetting);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			prepareForm(request, payrollSetting);
			request.getRequestDispatcher("/views/payrollsetting/payroll-setting-setup.jsp").forward(request, response);
			return;
		}

		if (payrollSettingDAO.hasBlockingPeriod(payrollSetting.getId(), payrollSetting.getEffectiveFrom(),
				payrollSetting.getEffectiveTo())) {
			request.setAttribute("errorMsg",
					"Đã tồn tại cấu hình payroll khác bắt đầu hiệu lực trong khoảng thời gian này.");
			prepareForm(request, payrollSetting);
			request.getRequestDispatcher("/views/payrollsetting/payroll-setting-setup.jsp").forward(request, response);
			return;
		}

		if (payrollSettingDAO.upsert(payrollSetting)) {
			session.setAttribute("successMsg",
					payrollSetting.getId() == null ? "Đã thêm cấu hình payroll." : "Đã cập nhật cấu hình payroll.");
			response.sendRedirect(request.getContextPath() + "/payroll-setting-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể lưu cấu hình payroll. Vui lòng thử lại.");
		prepareForm(request, payrollSetting);
		request.getRequestDispatcher("/views/payrollsetting/payroll-setting-setup.jsp").forward(request, response);
	}

	private void prepareForm(HttpServletRequest request, PayrollSetting payrollSetting) {
		request.setAttribute("payrollSetting", payrollSetting);
		request.setAttribute("standardWorkHoursPerDayValue",
				formatDecimalInput(payrollSetting != null ? payrollSetting.getStandardWorkHoursPerDay() : null));
		request.setAttribute("normalOvertimeRateValue",
				formatDecimalInput(payrollSetting != null ? payrollSetting.getNormalOvertimeRate() : null));
		request.setAttribute("attendanceBonusAmountValue",
				formatDecimalInput(payrollSetting != null ? payrollSetting.getAttendanceBonusAmount() : null));
	}

	private String formatDecimalInput(BigDecimal value) {
		if (value == null) {
			return "";
		}
		return value.stripTrailingZeros().toPlainString();
	}

	private PayrollSetting buildSetting(HttpServletRequest request) {
		PayrollSetting payrollSetting = new PayrollSetting();
		payrollSetting.setId(parseLong(request.getParameter("id")));
		payrollSetting.setStandardWorkDays(DEFAULT_STANDARD_WORK_DAYS);
		payrollSetting
				.setStandardWorkHoursPerDay(parseRequiredDecimal(request.getParameter("standardWorkHoursPerDay")));
		payrollSetting.setNormalOvertimeRate(parseRequiredDecimal(request.getParameter("normalOvertimeRate")));
		payrollSetting.setAttendanceBonusAmount(parseRequiredDecimal(request.getParameter("attendanceBonusAmount")));
		payrollSetting.setEffectiveFrom(parseDate(request.getParameter("effectiveFrom")));
		payrollSetting.setEffectiveTo(parseDate(request.getParameter("effectiveTo")));
		return payrollSetting;
	}

	private String validate(PayrollSetting payrollSetting) {
		if (payrollSetting.getStandardWorkHoursPerDay() == null || payrollSetting.getNormalOvertimeRate() == null
				|| payrollSetting.getAttendanceBonusAmount() == null) {
			return "Vui lòng nhập đầy đủ các tham số payroll.";
		}
		if (payrollSetting.getStandardWorkHoursPerDay().compareTo(BigDecimal.ZERO) <= 0) {
			return "Số giờ công chuẩn mỗi ngày phải lớn hơn 0.";
		}
		if (payrollSetting.getNormalOvertimeRate().compareTo(BigDecimal.ZERO) <= 0) {
			return "Hệ số OT phải lớn hơn 0.";
		}
		if (payrollSetting.getAttendanceBonusAmount().compareTo(BigDecimal.ZERO) < 0) {
			return "Mức thưởng chuyên cần không được âm.";
		}
		if (payrollSetting.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ là bắt buộc.";
		}
		if (payrollSetting.getEffectiveTo() != null
				&& payrollSetting.getEffectiveTo().before(payrollSetting.getEffectiveFrom())) {
			return "Ngày hiệu lực đến không được trước ngày hiệu lực từ.";
		}
		return null;
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseRequiredDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return parseOptionalDecimal(value);
	}

	private BigDecimal parseOptionalDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(value.replace(",", "").trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Date parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Date.valueOf(value.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (code.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}
}
