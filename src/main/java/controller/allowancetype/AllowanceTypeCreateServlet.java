package controller.allowancetype;

import dal.AllowanceTypeDAO;
import dal.DepartmentDAO;
import dal.JobTitleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.AllowanceType;
import model.Department;
import model.JobTitle;
import util.ValidationUtil;

@WebServlet(name = "AllowanceTypeCreateServlet", urlPatterns = {"/allowance-type-create"})
public class AllowanceTypeCreateServlet extends HttpServlet {

	private static final String CODE_REGEX = "^[A-Z][A-Z0-9_]*$";

	private final AllowanceTypeDAO allowanceTypeDAO = new AllowanceTypeDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();
	private final JobTitleDAO jobTitleDAO = new JobTitleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		prepareForm(request, AllowanceTypeFormHelper.emptyForm());
		request.getRequestDispatcher("/views/allowancetype/allowance-type-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String code = normalizeCode(request.getParameter("code"));
		String name = normalizeText(request.getParameter("name"));
		String description = normalizeText(request.getParameter("description"));
		boolean isTaxable = "true".equalsIgnoreCase(request.getParameter("isTaxable"));
		boolean isInsuranceBased = "true".equalsIgnoreCase(request.getParameter("isInsuranceBased"));

		request.setAttribute("code", code);
		request.setAttribute("name", name);
		request.setAttribute("description", description);
		request.setAttribute("isTaxable", isTaxable);
		request.setAttribute("isInsuranceBased", isInsuranceBased);

		List<JobTitle> jobTitles = jobTitleDAO.getActiveJobTitles();
		AllowanceTypeFormHelper.RuleFormData ruleForm = AllowanceTypeFormHelper.fromRequest(request, jobTitles);

		String validationError = validate(code, name, null);
		if (validationError == null) {
			validationError = AllowanceTypeFormHelper.validateRules(ruleForm);
		}
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			prepareForm(request, ruleForm, jobTitles);
			request.getRequestDispatcher("/views/allowancetype/allowance-type-create.jsp").forward(request, response);
			return;
		}

		AllowanceType allowanceType = new AllowanceType();
		allowanceType.setCode(code);
		allowanceType.setName(name);
		allowanceType.setDescription(description);
		allowanceType.setIsTaxable(isTaxable);
		allowanceType.setIsInsuranceBased(isInsuranceBased);
		allowanceType.setIsActive(true);

		boolean success = allowanceTypeDAO.insertWithRules(allowanceType, ruleForm.getRules());
		if (success) {
			request.getSession().setAttribute("successMsg", "Thêm loại phụ cấp thành công.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể thêm loại phụ cấp. Vui lòng thử lại.");
		prepareForm(request, ruleForm, jobTitles);
		request.getRequestDispatcher("/views/allowancetype/allowance-type-create.jsp").forward(request, response);
	}

	private void prepareForm(HttpServletRequest request, AllowanceTypeFormHelper.RuleFormData ruleForm) {
		prepareForm(request, ruleForm, jobTitleDAO.getActiveJobTitles());
	}

	private void prepareForm(HttpServletRequest request, AllowanceTypeFormHelper.RuleFormData ruleForm,
			List<JobTitle> jobTitles) {
		List<Department> departments = departmentDAO.getActiveDepartments();
		AllowanceTypeFormHelper.populateRequest(request, ruleForm, jobTitles, departments);
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
		boolean exists = id == null
				? allowanceTypeDAO.existsByCode(code)
				: allowanceTypeDAO.existsByCodeExceptId(code, id);
		if (exists) {
			return "Mã loại phụ cấp đã tồn tại. Vui lòng nhập mã khác.";
		}
		return null;
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
