package controller.contract;

import dal.ContractDAO;
import dal.ContractTypeDAO;
import dal.UserDAO;
import model.Contract;
import model.ContractType;
import model.User;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * Form for creating a new employee contract. Validates required fields,
 * enforces business rules (user active, no overlapping ACTIVE contract,
 * start_date <= end_date when both provided), and persists with status ACTIVE
 * by default.
 */
@WebServlet(name = "ContractCreateServlet", urlPatterns = {"/contract-create"})
public class ContractCreateServlet extends HttpServlet {

	private final ContractDAO contractDAO = new ContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> users = userDAO.getActiveUsersForDropdown();
		List<ContractType> contractTypes = contractTypeDAO.getActiveContractTypes();

		request.setAttribute("users", users);
		request.setAttribute("contractTypes", contractTypes);
		request.getRequestDispatcher("/views/contract/contract-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long userId = parseLong(request.getParameter("userId"));
		Long contractTypeId = parseLong(request.getParameter("contractTypeId"));
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String salaryStr = request.getParameter("salary");

		Date startDate = parseDate(startDateStr);
		Date endDate = parseDate(endDateStr);
		BigDecimal salary = parseBigDecimal(salaryStr);

		if (userId == null) {
			returnWithError(request, response, "Vui lòng chọn nhân viên.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}
		if (contractTypeId == null) {
			returnWithError(request, response, "Vui lòng chọn loại hợp đồng.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}
		if (startDate == null) {
			returnWithError(request, response, "Ngày bắt đầu không hợp lệ.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}
		if (endDate != null && endDate.before(startDate)) {
			returnWithError(request, response, "Ngày kết thúc phải sau ngày bắt đầu.", userId, contractTypeId,
					startDateStr, endDateStr, salaryStr);
			return;
		}
		if (salary != null && salary.signum() < 0) {
			returnWithError(request, response, "Mức lương không được âm.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}

		Contract existing = contractDAO.getActiveByUser(userId);
		if (existing != null) {
			returnWithError(request, response,
					"Nhân viên này đã có hợp đồng đang hiệu lực. Hãy chấm dứt hoặc gia hạn thay vì tạo mới.", userId,
					contractTypeId, startDateStr, endDateStr, salaryStr);
			return;
		}

		Contract c = new Contract();
		c.setUserId(userId);
		c.setContractTypeId(contractTypeId);
		c.setStartDate(startDate);
		c.setEndDate(endDate);
		c.setSalary(salary);
		c.setStatus(Contract.Status.ACTIVE);

		if (contractDAO.insert(c)) {
			request.getSession().setAttribute("successMsg", "Tạo hợp đồng mới thành công!");
			response.sendRedirect(request.getContextPath() + "/contract-list");
		} else {
			returnWithError(request, response, "Lỗi: Không thể tạo hợp đồng. Vui lòng thử lại.", userId, contractTypeId,
					startDateStr, endDateStr, salaryStr);
		}
	}

	private void returnWithError(HttpServletRequest request, HttpServletResponse response, String message, Long userId,
			Long contractTypeId, String startDateStr, String endDateStr, String salaryStr)
			throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("selectedUserId", userId);
		request.setAttribute("selectedContractTypeId", contractTypeId);
		request.setAttribute("startDate", startDateStr);
		request.setAttribute("endDate", endDateStr);
		request.setAttribute("salary", salaryStr);
		request.setAttribute("users", userDAO.getActiveUsersForDropdown());
		request.setAttribute("contractTypes", contractTypeDAO.getActiveContractTypes());
		request.getRequestDispatcher("/views/contract/contract-create.jsp").forward(request, response);
	}

	private Long parseLong(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Date parseDate(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return Date.valueOf(s.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return new BigDecimal(s.trim().replace(",", ""));
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
