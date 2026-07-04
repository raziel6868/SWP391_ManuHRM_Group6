package controller.allowancetype;

import dal.AllowanceTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.AllowanceType;
import util.ValidationUtil;

@WebServlet(name = "AllowanceTypeUpdateServlet", urlPatterns = {"/allowance-type-update"})
public class AllowanceTypeUpdateServlet extends HttpServlet {

	private static final String CODE_REGEX = "^[A-Z][A-Z0-9_]*$";

	private final AllowanceTypeDAO allowanceTypeDAO = new AllowanceTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		if (id == null) {
			request.getSession().setAttribute("errorMsg", "Không tìm thấy loại phụ cấp cần cập nhật.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		AllowanceType allowanceType = allowanceTypeDAO.getById(id);
		if (allowanceType == null) {
			request.getSession().setAttribute("errorMsg", "Loại phụ cấp không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		request.setAttribute("allowanceType", allowanceType);
		request.getRequestDispatcher("/views/allowancetype/allowance-type-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long id = parseId(request.getParameter("id"));
		if (id == null) {
			request.getSession().setAttribute("errorMsg", "Không tìm thấy loại phụ cấp cần cập nhật.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		AllowanceType existingAllowanceType = allowanceTypeDAO.getById(id);
		if (existingAllowanceType == null) {
			request.getSession().setAttribute("errorMsg", "Loại phụ cấp không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		String code = normalizeCode(request.getParameter("code"));
		String name = normalizeText(request.getParameter("name"));
		String description = normalizeText(request.getParameter("description"));
		boolean isTaxable = "true".equalsIgnoreCase(request.getParameter("isTaxable"));
		boolean isInsuranceBased = "true".equalsIgnoreCase(request.getParameter("isInsuranceBased"));

		existingAllowanceType.setCode(code);
		existingAllowanceType.setName(name);
		existingAllowanceType.setDescription(description);
		existingAllowanceType.setIsTaxable(isTaxable);
		existingAllowanceType.setIsInsuranceBased(isInsuranceBased);

		String validationError = validate(code, name, id);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("allowanceType", existingAllowanceType);
			request.getRequestDispatcher("/views/allowancetype/allowance-type-update.jsp").forward(request, response);
			return;
		}

		boolean success = allowanceTypeDAO.update(existingAllowanceType);
		if (success) {
			request.getSession().setAttribute("successMsg", "Cập nhật loại phụ cấp thành công.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể cập nhật loại phụ cấp. Vui lòng thử lại.");
		request.setAttribute("allowanceType", existingAllowanceType);
		request.getRequestDispatcher("/views/allowancetype/allowance-type-update.jsp").forward(request, response);
	}

	private String validate(String code, String name, Long id) {
		if (ValidationUtil.isBlank(code)) {
			return "Mã loại phụ cấp không được để trống.";
		}
		if (code.length() > 30) {
			return "Mã loại phụ cấp không được vượt quá 30 ký tự.";
		}
		if (!ValidationUtil.matchRegex(code, CODE_REGEX)) {
			return "Mã loại phụ cấp phải viết hoa, bắt đầu bằng chữ cái và chỉ chứa chữ hoa, số hoặc dấu gạch dưới.";
		}
		if (ValidationUtil.isBlank(name)) {
			return "Tên loại phụ cấp không được để trống.";
		}
		if (name.length() > 100) {
			return "Tên loại phụ cấp không được vượt quá 100 ký tự.";
		}
		if (allowanceTypeDAO.existsByCodeExceptId(code, id)) {
			return "Mã loại phụ cấp đã tồn tại. Vui lòng nhập mã khác.";
		}
		return null;
	}

	private Long parseId(String idParam) {
		if (idParam == null || idParam.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(idParam);
		} catch (NumberFormatException e) {
			return null;
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
