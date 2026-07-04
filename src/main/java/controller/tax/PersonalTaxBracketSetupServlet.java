package controller.tax;

import dal.PersonalTaxBracketDAO;
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
import model.PersonalTaxBracket;
import model.User;

@WebServlet(name = "PersonalTaxBracketSetupServlet", urlPatterns = {"/personal-tax-bracket-setup"})
public class PersonalTaxBracketSetupServlet extends HttpServlet {

	private final PersonalTaxBracketDAO personalTaxBracketDAO = new PersonalTaxBracketDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PERSONAL_TAX_BRACKET_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		PersonalTaxBracket personalTaxBracket = null;
		if (id != null) {
			personalTaxBracket = personalTaxBracketDAO.getById(id);
			if (personalTaxBracket == null) {
				session.setAttribute("errorMsg", "Bậc thuế không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/personal-tax-bracket-list");
				return;
			}
		}

		request.setAttribute("personalTaxBracket", personalTaxBracket);
		request.getRequestDispatcher("/views/tax/personal-tax-bracket-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PERSONAL_TAX_BRACKET_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		PersonalTaxBracket personalTaxBracket = new PersonalTaxBracket();
		personalTaxBracket.setId(parseLong(request.getParameter("id")));
		personalTaxBracket.setBracketOrder(parseInteger(request.getParameter("bracketOrder")));
		personalTaxBracket.setIncomeFrom(parseRequiredDecimal(request.getParameter("incomeFrom")));
		personalTaxBracket.setIncomeTo(parseOptionalDecimal(request.getParameter("incomeTo")));
		personalTaxBracket.setTaxRate(parseRequiredDecimal(request.getParameter("taxRate")));
		personalTaxBracket.setEffectiveFrom(parseDate(request.getParameter("effectiveFrom")));
		personalTaxBracket.setEffectiveTo(parseDate(request.getParameter("effectiveTo")));

		String validationError = validate(personalTaxBracket);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("personalTaxBracket", personalTaxBracket);
			request.getRequestDispatcher("/views/tax/personal-tax-bracket-setup.jsp").forward(request, response);
			return;
		}

		if (personalTaxBracketDAO.hasBlockingPeriod(personalTaxBracket.getId(), personalTaxBracket.getBracketOrder(),
				personalTaxBracket.getEffectiveFrom(), personalTaxBracket.getEffectiveTo())) {
			request.setAttribute("errorMsg",
					"Đã tồn tại bậc thuế khác cùng thứ tự bắt đầu hiệu lực trong khoảng thời gian này.");
			request.setAttribute("personalTaxBracket", personalTaxBracket);
			request.getRequestDispatcher("/views/tax/personal-tax-bracket-setup.jsp").forward(request, response);
			return;
		}

		boolean success = personalTaxBracketDAO.upsert(personalTaxBracket);
		if (success) {
			session.setAttribute("successMsg",
					personalTaxBracket.getId() == null ? "Đã thêm bậc thuế." : "Đã cập nhật bậc thuế.");
			response.sendRedirect(request.getContextPath() + "/personal-tax-bracket-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể lưu bậc thuế. Vui lòng thử lại.");
		request.setAttribute("personalTaxBracket", personalTaxBracket);
		request.getRequestDispatcher("/views/tax/personal-tax-bracket-setup.jsp").forward(request, response);
	}

	private String validate(PersonalTaxBracket personalTaxBracket) {
		if (personalTaxBracket.getBracketOrder() == null || personalTaxBracket.getBracketOrder() <= 0) {
			return "Thứ tự bậc thuế phải lớn hơn 0.";
		}
		if (personalTaxBracket.getIncomeFrom() == null
				|| personalTaxBracket.getIncomeFrom().compareTo(BigDecimal.ZERO) < 0) {
			return "Mức thu nhập từ không được nhỏ hơn 0.";
		}
		if (personalTaxBracket.getIncomeTo() != null
				&& personalTaxBracket.getIncomeTo().compareTo(personalTaxBracket.getIncomeFrom()) <= 0) {
			return "Mức thu nhập đến phải lớn hơn mức thu nhập từ.";
		}
		if (personalTaxBracket.getTaxRate() == null || personalTaxBracket.getTaxRate().compareTo(BigDecimal.ZERO) < 0
				|| personalTaxBracket.getTaxRate().compareTo(BigDecimal.ONE) > 0) {
			return "Thuế suất phải nằm trong khoảng từ 0 đến 1.";
		}
		if (personalTaxBracket.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ là bắt buộc.";
		}
		if (personalTaxBracket.getEffectiveTo() != null
				&& personalTaxBracket.getEffectiveTo().before(personalTaxBracket.getEffectiveFrom())) {
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

	private Integer parseInteger(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
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
