package controller.tax;

import dal.PersonalTaxSettingDAO;
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
import model.Permission;
import model.PersonalTaxSetting;
import model.User;

@WebServlet(name = "PersonalTaxSettingSetupServlet", urlPatterns = {"/personal-tax-setting-setup"})
public class PersonalTaxSettingSetupServlet extends HttpServlet {

	private final PersonalTaxSettingDAO personalTaxSettingDAO = new PersonalTaxSettingDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PERSONAL_TAX_SETTING_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		PersonalTaxSetting personalTaxSetting = null;
		if (id != null) {
			personalTaxSetting = personalTaxSettingDAO.getById(id);
			if (personalTaxSetting == null) {
				session.setAttribute("errorMsg", "Thiết lập giảm trừ không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/personal-tax-setting-list");
				return;
			}
		}

		request.setAttribute("personalTaxSetting", personalTaxSetting);
		request.getRequestDispatcher("/views/tax/personal-tax-setting-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PERSONAL_TAX_SETTING_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		PersonalTaxSetting personalTaxSetting = new PersonalTaxSetting();
		personalTaxSetting.setId(parseLong(request.getParameter("id")));
		personalTaxSetting.setPersonalDeduction(parseRequiredDecimal(request.getParameter("personalDeduction")));
		personalTaxSetting.setDependentDeduction(parseRequiredDecimal(request.getParameter("dependentDeduction")));
		personalTaxSetting.setEffectiveFrom(parseDate(request.getParameter("effectiveFrom")));
		personalTaxSetting.setEffectiveTo(parseDate(request.getParameter("effectiveTo")));

		String validationError = validate(personalTaxSetting);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("personalTaxSetting", personalTaxSetting);
			request.getRequestDispatcher("/views/tax/personal-tax-setting-setup.jsp").forward(request, response);
			return;
		}

		if (personalTaxSettingDAO.hasBlockingPeriod(personalTaxSetting.getId(), personalTaxSetting.getEffectiveFrom(),
				personalTaxSetting.getEffectiveTo())) {
			request.setAttribute("errorMsg",
					"Đã tồn tại thiết lập giảm trừ khác bắt đầu hiệu lực trong khoảng thời gian này.");
			request.setAttribute("personalTaxSetting", personalTaxSetting);
			request.getRequestDispatcher("/views/tax/personal-tax-setting-setup.jsp").forward(request, response);
			return;
		}

		boolean success = personalTaxSettingDAO.upsert(personalTaxSetting);
		if (success) {
			session.setAttribute("successMsg",
					personalTaxSetting.getId() == null
							? "Đã thêm thiết lập giảm trừ."
							: "Đã cập nhật thiết lập giảm trừ.");
			response.sendRedirect(request.getContextPath() + "/personal-tax-setting-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể lưu thiết lập giảm trừ. Vui lòng thử lại.");
		request.setAttribute("personalTaxSetting", personalTaxSetting);
		request.getRequestDispatcher("/views/tax/personal-tax-setting-setup.jsp").forward(request, response);
	}

	private String validate(PersonalTaxSetting personalTaxSetting) {
		if (personalTaxSetting.getPersonalDeduction() == null || personalTaxSetting.getDependentDeduction() == null) {
			return "Vui lòng nhập đầy đủ mức giảm trừ.";
		}
		if (personalTaxSetting.getPersonalDeduction().compareTo(BigDecimal.ZERO) < 0
				|| personalTaxSetting.getDependentDeduction().compareTo(BigDecimal.ZERO) < 0) {
			return "Mức giảm trừ không được nhỏ hơn 0.";
		}
		if (personalTaxSetting.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ là bắt buộc.";
		}
		if (personalTaxSetting.getEffectiveTo() != null
				&& personalTaxSetting.getEffectiveTo().before(personalTaxSetting.getEffectiveFrom())) {
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
