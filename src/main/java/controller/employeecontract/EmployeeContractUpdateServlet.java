package controller.employeecontract;

import dal.ContractTypeDAO;
import dal.EmployeeContractDAO;
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
import model.ContractType;
import model.EmployeeContract;
import util.ValidationUtil;

@WebServlet(name = "EmployeeContractUpdateServlet", urlPatterns = {"/contract-update"})
public class EmployeeContractUpdateServlet extends HttpServlet {

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String idParam = request.getParameter("id");
		Long id = parseId(idParam);

		if (id == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		EmployeeContract contract = contractDAO.getById(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);

		request.setAttribute("contract", contract);
		request.setAttribute("contractTypes", contractTypes);
		request.getRequestDispatcher("/views/employeecontract/employee-contract-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		String idParam = request.getParameter("id");
		Long id = parseId(idParam);

		if (id == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		EmployeeContract existingContract = contractDAO.getById(id);
		if (existingContract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		String contractTypeIdParam = request.getParameter("contractTypeId");
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String salaryStr = request.getParameter("salary");

		request.setAttribute("contract", existingContract);

		String error = validate(contractTypeIdParam, startDateStr, endDateStr, salaryStr);
		if (error != null) {
			List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);
			request.setAttribute("contractTypes", contractTypes);
			request.setAttribute("errorMsg", error);
			request.getRequestDispatcher("/views/employeecontract/employee-contract-update.jsp").forward(request,
					response);
			return;
		}

		Long contractTypeId = Long.parseLong(contractTypeIdParam);
		Date startDate = Date.valueOf(startDateStr);
		Date endDate = endDateStr != null && !endDateStr.isBlank() ? Date.valueOf(endDateStr) : null;
		BigDecimal salary = salaryStr != null && !salaryStr.isBlank() ? new BigDecimal(salaryStr) : null;

		existingContract.setContractTypeId(contractTypeId);
		existingContract.setStartDate(startDate);
		existingContract.setEndDate(endDate);
		existingContract.setSalary(salary);

		boolean success = contractDAO.update(existingContract);
		if (success) {
			session.setAttribute("successMsg", "Cập nhật hợp đồng thành công.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(null, 0, 1000);
		request.setAttribute("contractTypes", contractTypes);
		request.setAttribute("errorMsg", "Không thể cập nhật hợp đồng. Vui lòng thử lại.");
		request.getRequestDispatcher("/views/employeecontract/employee-contract-update.jsp").forward(request, response);
	}

	private String validate(String contractTypeIdParam, String startDateStr, String endDateStr, String salaryStr) {
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
}
