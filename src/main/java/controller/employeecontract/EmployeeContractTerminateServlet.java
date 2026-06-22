package controller.employeecontract;

import dal.EmployeeContractDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.EmployeeContract;
import model.Permission;
import model.User;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "EmployeeContractTerminateServlet", urlPatterns = {"/contract-terminate"})
public class EmployeeContractTerminateServlet extends HttpServlet {

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();
	private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (!hasPermission(permissions, "CONTRACT_UPDATE")) {
			response.sendRedirect(request.getContextPath() + "/views/error/403.jsp");
			return;
		}

		String idParam = request.getParameter("id");
		if (idParam == null || idParam.trim().isEmpty()) {
			session.setAttribute("errorMsg", "ID hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		try {
			Long id = Long.parseLong(idParam);
			EmployeeContract contract = contractDAO.getById(id);

			if (contract == null) {
				session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
				response.sendRedirect(request.getContextPath() + "/contract-list");
				return;
			}

			if ("TERMINATED".equals(contract.getStatus()) || "EXPIRED".equals(contract.getStatus())) {
				session.setAttribute("errorMsg", "Hợp đồng đã bị chấm dứt hoặc hết hạn.");
				response.sendRedirect(request.getContextPath() + "/contract-list");
				return;
			}

			request.setAttribute("contract", contract);
			request.getRequestDispatcher("/views/employeecontract/employee-contract-terminate.jsp").forward(request,
					response);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (!hasPermission(permissions, "CONTRACT_UPDATE")) {
			response.sendRedirect(request.getContextPath() + "/views/error/403.jsp");
			return;
		}

		String idParam = request.getParameter("id");
		String terminatedAtStr = request.getParameter("terminatedAt");
		String terminationReason = request.getParameter("terminationReason");

		if (idParam == null || idParam.trim().isEmpty()) {
			session.setAttribute("errorMsg", "ID hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		try {
			Long id = Long.parseLong(idParam);
			EmployeeContract contract = contractDAO.getById(id);

			if (contract == null) {
				session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
				response.sendRedirect(request.getContextPath() + "/contract-list");
				return;
			}

			request.setAttribute("contract", contract);

			if ("TERMINATED".equals(contract.getStatus()) || "EXPIRED".equals(contract.getStatus())) {
				session.setAttribute("errorMsg", "Hợp đồng đã bị chấm dứt hoặc hết hạn.");
				response.sendRedirect(request.getContextPath() + "/contract-list");
				return;
			}

			if (terminatedAtStr == null || terminatedAtStr.trim().isEmpty()) {
				request.setAttribute("errorMsg", "Ngày chấm dứt là bắt buộc.");
				request.getRequestDispatcher("/views/employeecontract/employee-contract-terminate.jsp").forward(request,
						response);
				return;
			}

			Date terminatedAt;
			try {
				LocalDate localDate = LocalDate.parse(terminatedAtStr, DATE_FORMATTER);
				terminatedAt = Date.valueOf(localDate);
			} catch (DateTimeParseException e) {
				request.setAttribute("errorMsg", "Định dạng ngày không hợp lệ (yyyy-MM-dd).");
				request.getRequestDispatcher("/views/employeecontract/employee-contract-terminate.jsp").forward(request,
						response);
				return;
			}

			if (terminationReason == null || terminationReason.trim().isEmpty()) {
				request.setAttribute("errorMsg", "Lý do chấm dứt là bắt buộc.");
				request.getRequestDispatcher("/views/employeecontract/employee-contract-terminate.jsp").forward(request,
						response);
				return;
			}

			Long terminatedBy = authUser != null ? authUser.getId() : null;
			boolean success = contractDAO.terminate(id, terminatedAt, terminatedBy, terminationReason.trim());

			if (success) {
				session.setAttribute("successMsg", "Chấm dứt hợp đồng thành công.");
				response.sendRedirect(request.getContextPath() + "/contract-list");
			} else {
				request.setAttribute("errorMsg", "Không thể chấm dứt hợp đồng. Vui lòng thử lại.");
				request.getRequestDispatcher("/views/employeecontract/employee-contract-terminate.jsp").forward(request,
						response);
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
		}
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (code.equals(p.getCode())) {
				return true;
			}
		}
		return false;
	}
}
