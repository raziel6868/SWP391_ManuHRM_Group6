package controller.employeecontract;

import dal.ContractTypeDAO;
import dal.DepartmentDAO;
import dal.EmployeeContractDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import model.ContractType;
import model.Department;
import model.EmployeeContract;
import model.User;
import util.ValidationUtil;

@WebServlet(name = "EmployeeContractCreateServlet", urlPatterns = {"/contract-create"})
public class EmployeeContractCreateServlet extends HttpServlet {

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();
	private final UserDAO userDAO = new UserDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> employees = userDAO.searchUsers(null, null, null, true, null, 0, 1000);
		List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);
		List<Department> departments = departmentDAO.getActiveDepartments();

		request.setAttribute("employees", employees);
		request.setAttribute("contractTypes", contractTypes);
		request.setAttribute("departments", departments);

		request.getRequestDispatcher("/views/employeecontract/employee-contract-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String userIdParam = request.getParameter("userId");
		String contractTypeIdParam = request.getParameter("contractTypeId");
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String salaryStr = request.getParameter("salary");

		request.setAttribute("userId", userIdParam);
		request.setAttribute("contractTypeId", contractTypeIdParam);
		request.setAttribute("startDate", startDateStr);
		request.setAttribute("endDate", endDateStr);
		request.setAttribute("salary", salaryStr);

		String error = validate(userIdParam, contractTypeIdParam, startDateStr, endDateStr, salaryStr);
		if (error != null) {
			List<User> employees = userDAO.searchUsers(null, null, null, true, null, 0, 1000);
			List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);
			List<Department> departments = departmentDAO.getActiveDepartments();
			request.setAttribute("employees", employees);
			request.setAttribute("contractTypes", contractTypes);
			request.setAttribute("departments", departments);
			request.setAttribute("errorMsg", error);
			request.getRequestDispatcher("/views/employeecontract/employee-contract-create.jsp").forward(request,
					response);
			return;
		}

		Long userId = Long.parseLong(userIdParam);
		Long contractTypeId = Long.parseLong(contractTypeIdParam);
		Date startDate = Date.valueOf(startDateStr);
		Date endDate = endDateStr != null && !endDateStr.isBlank() ? Date.valueOf(endDateStr) : null;
		BigDecimal salary = salaryStr != null && !salaryStr.isBlank() ? new BigDecimal(salaryStr) : null;

		if (contractDAO.existsByUserAndActiveContract(userId)) {
			List<User> employees = userDAO.searchUsers(null, null, null, true, null, 0, 1000);
			List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);
			List<Department> departments = departmentDAO.getActiveDepartments();
			request.setAttribute("employees", employees);
			request.setAttribute("contractTypes", contractTypes);
			request.setAttribute("departments", departments);
			request.setAttribute("errorMsg",
					"Nhân viên này đã có hợp đồng đang hoạt động. Vui lòng gia hạn hoặc chọn nhân viên khác.");
			request.getRequestDispatcher("/views/employeecontract/employee-contract-create.jsp").forward(request,
					response);
			return;
		}

		EmployeeContract contract = new EmployeeContract();
		contract.setUserId(userId);
		contract.setContractTypeId(contractTypeId);
		contract.setStartDate(startDate);
		contract.setEndDate(endDate);
		contract.setSalary(salary);
		contract.setStatus("ACTIVE");

		boolean success = contractDAO.insert(contract);
		if (success) {
			request.getSession().setAttribute("successMsg", "Tạo hợp đồng thành công.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		List<User> employees = userDAO.searchUsers(null, null, null, true, null, 0, 1000);
		List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);
		List<Department> departments = departmentDAO.getActiveDepartments();
		request.setAttribute("employees", employees);
		request.setAttribute("contractTypes", contractTypes);
		request.setAttribute("departments", departments);
		request.setAttribute("errorMsg", "Không thể tạo hợp đồng. Vui lòng thử lại.");
		request.getRequestDispatcher("/views/employeecontract/employee-contract-create.jsp").forward(request, response);
	}

	private String validate(String userIdParam, String contractTypeIdParam, String startDateStr, String endDateStr,
			String salaryStr) {
		if (ValidationUtil.isBlank(userIdParam)) {
			return "Vui lòng chọn nhân viên.";
		}
		if (ValidationUtil.isBlank(contractTypeIdParam)) {
			return "Vui lòng chọn loại hợp đồng.";
		}
		if (ValidationUtil.isBlank(startDateStr)) {
			return "Ngày bắt đầu không được để trống.";
		}
		try {
			Date.valueOf(startDateStr);
		} catch (IllegalArgumentException e) {
			return "Định dạng ngày bắt đầu không hợp lệ (yyyy-MM-dd).";
		}
		if (!ValidationUtil.isBlank(endDateStr)) {
			try {
				Date endDate = Date.valueOf(endDateStr);
				Date startDate = Date.valueOf(startDateStr);
				if (endDate.before(startDate)) {
					return "Ngày kết thúc phải sau ngày bắt đầu.";
				}
			} catch (IllegalArgumentException e) {
				return "Định dạng ngày kết thúc không hợp lệ (yyyy-MM-dd).";
			}
		}
		if (!ValidationUtil.isBlank(salaryStr)) {
			try {
				new BigDecimal(salaryStr);
			} catch (NumberFormatException e) {
				return "Lương phải là số hợp lệ.";
			}
		}
		return null;
	}
}
