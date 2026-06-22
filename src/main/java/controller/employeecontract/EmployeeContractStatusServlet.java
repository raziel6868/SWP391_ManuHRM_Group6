package controller.employeecontract;

import dal.EmployeeContractDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.EmployeeContract;

@WebServlet(name = "EmployeeContractStatusServlet", urlPatterns = {"/contract-status"})
public class EmployeeContractStatusServlet extends HttpServlet {

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String idParam = request.getParameter("id");
		String status = request.getParameter("status");

		Long id = parseId(idParam);
		if (id == null) {
			session.setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		if (status == null || status.isBlank()) {
			session.setAttribute("errorMsg", "Trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		EmployeeContract contract = contractDAO.getById(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		String newStatus = status.trim().toUpperCase();
		if (!newStatus.equals("ACTIVE") && !newStatus.equals("EXPIRED") && !newStatus.equals("PENDING_RENEWAL")
				&& !newStatus.equals("TERMINATED")) {
			session.setAttribute("errorMsg", "Trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		boolean success = contractDAO.updateStatus(id, newStatus);

		if (success) {
			session.setAttribute("successMsg", "Cập nhật trạng thái hợp đồng thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật trạng thái hợp đồng.");
		}

		response.sendRedirect(request.getContextPath() + "/contract-list");
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
