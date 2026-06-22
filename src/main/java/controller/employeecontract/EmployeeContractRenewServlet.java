package controller.employeecontract;

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
import util.ValidationUtil;

@WebServlet(name = "EmployeeContractRenewServlet", urlPatterns = {"/contract-renew"})
public class EmployeeContractRenewServlet extends HttpServlet {

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		String idParam = request.getParameter("id");
		String newStartDateStr = request.getParameter("newStartDate");
		String newEndDateStr = request.getParameter("newEndDate");
		String newSalaryStr = request.getParameter("newSalary");

		Long id = parseId(idParam);
		if (id == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		String error = validate(newStartDateStr, newEndDateStr, newSalaryStr);
		if (error != null) {
			session.setAttribute("errorMsg", error);
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		Date newStartDate = Date.valueOf(newStartDateStr);
		Date newEndDate = newEndDateStr != null && !newEndDateStr.isBlank() ? Date.valueOf(newEndDateStr) : null;
		BigDecimal newSalary = newSalaryStr != null && !newSalaryStr.isBlank() ? new BigDecimal(newSalaryStr) : null;

		boolean success = contractDAO.renew(id, newStartDate, newEndDate, newSalary, null);
		if (success) {
			session.setAttribute("successMsg", "Gia hạn hợp đồng thành công.");
		} else {
			session.setAttribute("errorMsg",
					"Không thể gia hạn hợp đồng. Hợp đồng có thể không còn active hoặc đã bị xóa.");
		}

		response.sendRedirect(request.getContextPath() + "/contract-list");
	}

	private String validate(String newStartDateStr, String newEndDateStr, String newSalaryStr) {
		if (ValidationUtil.isBlank(newStartDateStr)) {
			return "Ngày bắt đầu mới không được để trống.";
		}
		try {
			Date.valueOf(newStartDateStr);
		} catch (IllegalArgumentException e) {
			return "Định dạng ngày bắt đầu mới không hợp lệ (yyyy-MM-dd).";
		}
		if (!ValidationUtil.isBlank(newEndDateStr)) {
			try {
				Date newEndDate = Date.valueOf(newEndDateStr);
				Date newStartDate = Date.valueOf(newStartDateStr);
				if (newEndDate.before(newStartDate)) {
					return "Ngày kết thúc mới phải sau ngày bắt đầu mới.";
				}
			} catch (IllegalArgumentException e) {
				return "Định dạng ngày kết thúc mới không hợp lệ (yyyy-MM-dd).";
			}
		}
		if (!ValidationUtil.isBlank(newSalaryStr)) {
			try {
				new BigDecimal(newSalaryStr);
			} catch (NumberFormatException e) {
				return "Lương mới phải là số hợp lệ.";
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
