package controller.insurance;

import dal.InsuranceRateDAO;
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
import model.InsuranceRate;
import model.Permission;
import model.User;

@WebServlet(name = "InsuranceRateSetupServlet", urlPatterns = {"/insurance-rate-setup"})
public class InsuranceRateSetupServlet extends HttpServlet {

	private final InsuranceRateDAO insuranceRateDAO = new InsuranceRateDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "INSURANCE_RATE_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		Long id = parseLong(request.getParameter("id"));
		InsuranceRate insuranceRate = null;
		if (id != null) {
			insuranceRate = insuranceRateDAO.getById(id);
			if (insuranceRate == null) {
				session.setAttribute("errorMsg", "Mức đóng bảo hiểm không tồn tại.");
				response.sendRedirect(request.getContextPath() + "/insurance-rate-list");
				return;
			}
		}

		request.setAttribute("insuranceRate", insuranceRate);
		request.getRequestDispatcher("/views/insurance/insurance-rate-setup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "INSURANCE_RATE_SETUP")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		InsuranceRate insuranceRate = new InsuranceRate();
		insuranceRate.setId(parseLong(request.getParameter("id")));
		insuranceRate.setSocialInsuranceEmployeeRate(
				parseRequiredDecimal(request.getParameter("socialInsuranceEmployeeRate")));
		insuranceRate.setHealthInsuranceEmployeeRate(
				parseRequiredDecimal(request.getParameter("healthInsuranceEmployeeRate")));
		insuranceRate.setUnemploymentInsuranceEmployeeRate(
				parseRequiredDecimal(request.getParameter("unemploymentInsuranceEmployeeRate")));
		insuranceRate.setSocialInsuranceEmployerRate(
				parseRequiredDecimal(request.getParameter("socialInsuranceEmployerRate")));
		insuranceRate.setHealthInsuranceEmployerRate(
				parseRequiredDecimal(request.getParameter("healthInsuranceEmployerRate")));
		insuranceRate.setUnemploymentInsuranceEmployerRate(
				parseRequiredDecimal(request.getParameter("unemploymentInsuranceEmployerRate")));
		insuranceRate
				.setSocialHealthInsuranceCap(parseOptionalDecimal(request.getParameter("socialHealthInsuranceCap")));
		insuranceRate
				.setUnemploymentInsuranceCap(parseOptionalDecimal(request.getParameter("unemploymentInsuranceCap")));
		insuranceRate.setEffectiveFrom(parseDate(request.getParameter("effectiveFrom")));
		insuranceRate.setEffectiveTo(parseDate(request.getParameter("effectiveTo")));

		String validationError = validate(insuranceRate);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("insuranceRate", insuranceRate);
			request.getRequestDispatcher("/views/insurance/insurance-rate-setup.jsp").forward(request, response);
			return;
		}

		if (insuranceRateDAO.hasBlockingPeriod(insuranceRate.getId(), insuranceRate.getEffectiveFrom(),
				insuranceRate.getEffectiveTo())) {
			request.setAttribute("errorMsg",
					"Đã tồn tại mức đóng bảo hiểm khác bắt đầu hiệu lực trong khoảng thời gian này.");
			request.setAttribute("insuranceRate", insuranceRate);
			request.getRequestDispatcher("/views/insurance/insurance-rate-setup.jsp").forward(request, response);
			return;
		}

		boolean success = insuranceRateDAO.upsert(insuranceRate);
		if (success) {
			session.setAttribute("successMsg",
					insuranceRate.getId() == null ? "Đã thêm mức đóng bảo hiểm." : "Đã cập nhật mức đóng bảo hiểm.");
			response.sendRedirect(request.getContextPath() + "/insurance-rate-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể lưu mức đóng bảo hiểm. Vui lòng thử lại.");
		request.setAttribute("insuranceRate", insuranceRate);
		request.getRequestDispatcher("/views/insurance/insurance-rate-setup.jsp").forward(request, response);
	}

	private String validate(InsuranceRate insuranceRate) {
		if (insuranceRate.getSocialInsuranceEmployeeRate() == null
				|| insuranceRate.getHealthInsuranceEmployeeRate() == null
				|| insuranceRate.getUnemploymentInsuranceEmployeeRate() == null
				|| insuranceRate.getSocialInsuranceEmployerRate() == null
				|| insuranceRate.getHealthInsuranceEmployerRate() == null
				|| insuranceRate.getUnemploymentInsuranceEmployerRate() == null) {
			return "Vui lòng nhập đầy đủ các tỷ lệ bảo hiểm.";
		}
		if (!isRateValid(insuranceRate.getSocialInsuranceEmployeeRate())
				|| !isRateValid(insuranceRate.getHealthInsuranceEmployeeRate())
				|| !isRateValid(insuranceRate.getUnemploymentInsuranceEmployeeRate())
				|| !isRateValid(insuranceRate.getSocialInsuranceEmployerRate())
				|| !isRateValid(insuranceRate.getHealthInsuranceEmployerRate())
				|| !isRateValid(insuranceRate.getUnemploymentInsuranceEmployerRate())) {
			return "Tỷ lệ bảo hiểm phải nằm trong khoảng từ 0 đến 1.";
		}
		if (insuranceRate.getSocialHealthInsuranceCap() != null
				&& insuranceRate.getSocialHealthInsuranceCap().compareTo(BigDecimal.ZERO) < 0) {
			return "Trần BHXH/BHYT không được nhỏ hơn 0.";
		}
		if (insuranceRate.getUnemploymentInsuranceCap() != null
				&& insuranceRate.getUnemploymentInsuranceCap().compareTo(BigDecimal.ZERO) < 0) {
			return "Trần BHTN không được nhỏ hơn 0.";
		}
		if (insuranceRate.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ là bắt buộc.";
		}
		if (insuranceRate.getEffectiveTo() != null
				&& insuranceRate.getEffectiveTo().before(insuranceRate.getEffectiveFrom())) {
			return "Ngày hiệu lực đến không được trước ngày hiệu lực từ.";
		}
		return null;
	}

	private boolean isRateValid(BigDecimal rate) {
		return rate != null && rate.compareTo(BigDecimal.ZERO) >= 0 && rate.compareTo(BigDecimal.ONE) <= 0;
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
