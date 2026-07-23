package controller.allowancetype;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.AllowanceRule;
import model.Department;
import model.JobTitle;

final class AllowanceTypeFormHelper {

	static final String SCOPE_ALL = "ALL";
	static final String SCOPE_EMPLOYEE_TYPE = "EMPLOYEE_TYPE";
	static final String SCOPE_DEPARTMENT_TYPE = "DEPARTMENT_TYPE";
	static final String SCOPE_DEPARTMENT = "DEPARTMENT";
	static final String SCOPE_JOB_TITLE = "JOB_TITLE";

	private AllowanceTypeFormHelper() {
	}

	static RuleFormData emptyForm() {
		RuleFormData form = new RuleFormData();
		form.rule.setApplyScope(SCOPE_ALL);
		form.rule.setEffectiveFrom(Date.valueOf(LocalDate.now()));
		form.effectiveFromValue = form.rule.getEffectiveFrom().toString();
		return form;
	}

	static RuleFormData fromRequest(HttpServletRequest request, List<JobTitle> jobTitles) {
		return fromRequest(request, jobTitles, null);
	}

	static RuleFormData fromRequest(HttpServletRequest request, List<JobTitle> jobTitles,
			List<AllowanceRule> existingRules) {
		RuleFormData form = new RuleFormData();
		String scope = normalizeScope(request.getParameter("applyScope"));
		Date effectiveFrom = parseDate(request.getParameter("effectiveFrom"));
		Date effectiveTo = parseDate(request.getParameter("effectiveTo"));

		form.rule.setApplyScope(scope);
		form.rule.setEmployeeType(normalizeNullableEnum(request.getParameter("employeeType")));
		form.rule.setDepartmentType(normalizeNullableEnum(request.getParameter("departmentType")));
		form.rule.setDepartmentId(parseLong(request.getParameter("departmentId")));
		form.rule.setEffectiveFrom(effectiveFrom);
		form.rule.setEffectiveTo(effectiveTo);
		form.amountValue = normalizeText(request.getParameter("amount"));
		form.effectiveFromValue = normalizeText(request.getParameter("effectiveFrom"));
		form.effectiveToValue = normalizeText(request.getParameter("effectiveTo"));

		if (effectiveFrom == null) {
			form.error = "Ngày hiệu lực từ không hợp lệ.";
			return form;
		}
		if (effectiveTo != null && effectiveTo.before(effectiveFrom)) {
			form.error = "Ngày hiệu lực đến phải sau hoặc bằng ngày hiệu lực từ.";
			return form;
		}

		if (SCOPE_JOB_TITLE.equals(scope)) {
			buildJobTitleRules(request, jobTitles, form, effectiveFrom, effectiveTo,
					buildExistingJobTitleAmountById(existingRules));
		} else {
			buildSingleRule(form, scope, effectiveFrom, effectiveTo);
		}
		return form;
	}

	static RuleFormData fromRules(List<AllowanceRule> rules) {
		RuleFormData form = emptyForm();
		if (rules == null || rules.isEmpty()) {
			return form;
		}

		AllowanceRule firstRule = rules.get(0);
		form.rule.setApplyScope(normalizeScope(firstRule.getApplyScope()));
		form.rule.setEmployeeType(firstRule.getEmployeeType());
		form.rule.setDepartmentType(firstRule.getDepartmentType());
		form.rule.setDepartmentId(firstRule.getDepartmentId());
		form.rule.setEffectiveFrom(firstRule.getEffectiveFrom());
		form.rule.setEffectiveTo(firstRule.getEffectiveTo());
		form.effectiveFromValue = firstRule.getEffectiveFrom() != null ? firstRule.getEffectiveFrom().toString() : null;
		form.effectiveToValue = firstRule.getEffectiveTo() != null ? firstRule.getEffectiveTo().toString() : null;

		if (SCOPE_JOB_TITLE.equals(form.rule.getApplyScope())) {
			for (AllowanceRule rule : rules) {
				if (rule.getJobTitleId() != null) {
					form.jobTitleAmountById.put(rule.getJobTitleId(), formatAmount(rule.getAmount()));
				}
			}
			form.rules.addAll(rules);
		} else {
			form.amountValue = formatAmount(firstRule.getAmount());
			form.rule.setAmount(firstRule.getAmount());
			form.rules.add(firstRule);
		}
		return form;
	}

	static void populateRequest(HttpServletRequest request, RuleFormData form, List<JobTitle> jobTitles,
			List<Department> departments) {
		request.setAttribute("rule", form.rule);
		request.setAttribute("ruleAmountValue", form.amountValue);
		request.setAttribute("ruleEffectiveFromValue", form.effectiveFromValue);
		request.setAttribute("ruleEffectiveToValue", form.effectiveToValue);
		request.setAttribute("jobTitleAmountById", form.jobTitleAmountById);
		request.setAttribute("jobTitles", jobTitles);
		request.setAttribute("departments", departments);
	}

	static String validateRules(RuleFormData form) {
		if (form.error != null) {
			return form.error;
		}
		if (form.rule.getEffectiveFrom() == null) {
			return "Ngày hiệu lực từ không được để trống.";
		}

		String scope = form.rule.getApplyScope();
		if (SCOPE_EMPLOYEE_TYPE.equals(scope) && form.rule.getEmployeeType() == null) {
			return "Vui lòng chọn loại nhân viên áp dụng.";
		}
		if (SCOPE_DEPARTMENT_TYPE.equals(scope) && form.rule.getDepartmentType() == null) {
			return "Vui lòng chọn loại phòng ban áp dụng.";
		}
		if (SCOPE_DEPARTMENT.equals(scope) && form.rule.getDepartmentId() == null) {
			return "Vui lòng chọn phòng ban áp dụng.";
		}
		if (SCOPE_JOB_TITLE.equals(scope) && form.rules.isEmpty()) {
			return "Vui lòng nhập ít nhất một mức phụ cấp theo chức danh.";
		}
		if (!SCOPE_JOB_TITLE.equals(scope) && form.rules.isEmpty()) {
			return "Vui lòng nhập số tiền phụ cấp.";
		}
		return null;
	}

	private static void buildSingleRule(RuleFormData form, String scope, Date effectiveFrom, Date effectiveTo) {
		BigDecimal amount = parseAmount(form.amountValue);
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			form.error = "Số tiền phụ cấp phải lớn hơn 0.";
			return;
		}

		AllowanceRule rule = form.rule;
		rule.setApplyScope(scope);
		rule.setAmount(amount);
		rule.setEffectiveFrom(effectiveFrom);
		rule.setEffectiveTo(effectiveTo);
		if (SCOPE_ALL.equals(scope)) {
			rule.setEmployeeType(null);
			rule.setDepartmentType(null);
			rule.setDepartmentId(null);
			rule.setJobTitleId(null);
		} else if (SCOPE_EMPLOYEE_TYPE.equals(scope)) {
			rule.setDepartmentType(null);
			rule.setDepartmentId(null);
			rule.setJobTitleId(null);
		} else if (SCOPE_DEPARTMENT_TYPE.equals(scope)) {
			rule.setDepartmentId(null);
			rule.setJobTitleId(null);
		} else if (SCOPE_DEPARTMENT.equals(scope)) {
			rule.setDepartmentType(null);
			rule.setJobTitleId(null);
		}
		form.rules.add(rule);
	}

	private static void buildJobTitleRules(HttpServletRequest request, List<JobTitle> jobTitles, RuleFormData form,
			Date effectiveFrom, Date effectiveTo, Map<Long, BigDecimal> existingAmountByJobTitleId) {
		form.rule.setEmployeeType(null);
		form.rule.setDepartmentType(null);
		form.rule.setDepartmentId(null);
		if (jobTitles == null) {
			return;
		}

		for (JobTitle jobTitle : jobTitles) {
			String value = normalizeText(request.getParameter("jobTitleAmount_" + jobTitle.getId()));
			if (value == null && existingAmountByJobTitleId.containsKey(jobTitle.getId())) {
				value = formatAmount(existingAmountByJobTitleId.get(jobTitle.getId()));
			}
			if (value == null) {
				continue;
			}
			form.jobTitleAmountById.put(jobTitle.getId(), value);
			BigDecimal amount = parseAmount(value);
			if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
				form.error = "Số tiền phụ cấp theo chức danh phải lớn hơn 0.";
				return;
			}
			AllowanceRule rule = new AllowanceRule();
			rule.setApplyScope(SCOPE_JOB_TITLE);
			rule.setJobTitleId(jobTitle.getId());
			rule.setAmount(amount);
			rule.setEffectiveFrom(effectiveFrom);
			rule.setEffectiveTo(effectiveTo);
			form.rules.add(rule);
		}
	}

	private static String normalizeScope(String value) {
		String normalized = normalizeNullableEnum(value);
		if (SCOPE_EMPLOYEE_TYPE.equals(normalized) || SCOPE_DEPARTMENT_TYPE.equals(normalized)
				|| SCOPE_DEPARTMENT.equals(normalized) || SCOPE_JOB_TITLE.equals(normalized)) {
			return normalized;
		}
		return SCOPE_ALL;
	}

	private static Map<Long, BigDecimal> buildExistingJobTitleAmountById(List<AllowanceRule> existingRules) {
		Map<Long, BigDecimal> existingAmountByJobTitleId = new LinkedHashMap<>();
		if (existingRules == null) {
			return existingAmountByJobTitleId;
		}
		for (AllowanceRule rule : existingRules) {
			if (rule.getJobTitleId() != null && rule.getAmount() != null) {
				existingAmountByJobTitleId.put(rule.getJobTitleId(), rule.getAmount());
			}
		}
		return existingAmountByJobTitleId;
	}

	private static String normalizeNullableEnum(String value) {
		String normalized = normalizeText(value);
		return normalized != null ? normalized.toUpperCase() : null;
	}

	private static String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static Long parseLong(String value) {
		String normalized = normalizeText(value);
		if (normalized == null) {
			return null;
		}
		try {
			return Long.parseLong(normalized);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Date parseDate(String value) {
		String normalized = normalizeText(value);
		if (normalized == null) {
			return null;
		}
		try {
			return Date.valueOf(normalized);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static BigDecimal parseAmount(String value) {
		String normalized = normalizeText(value);
		if (normalized == null) {
			return null;
		}
		try {
			return new BigDecimal(normalized.replace(",", ""));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String formatAmount(BigDecimal amount) {
		if (amount == null) {
			return null;
		}
		return amount.stripTrailingZeros().toPlainString();
	}

	static class RuleFormData {
		private final AllowanceRule rule = new AllowanceRule();
		private final List<AllowanceRule> rules = new ArrayList<>();
		private final Map<Long, String> jobTitleAmountById = new LinkedHashMap<>();
		private String amountValue;
		private String effectiveFromValue;
		private String effectiveToValue;
		private String error;

		List<AllowanceRule> getRules() {
			return rules;
		}
	}
}
