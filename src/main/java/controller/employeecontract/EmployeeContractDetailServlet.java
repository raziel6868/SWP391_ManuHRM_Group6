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
import java.util.List;
import model.ContractType;
import model.EmployeeContract;
import model.Permission;

@WebServlet(name = "EmployeeContractDetailServlet", urlPatterns = {"/contract-detail"})
public class EmployeeContractDetailServlet extends HttpServlet {

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

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
		request.setAttribute("canUpdate", hasPermission(session, "CONTRACT_UPDATE"));
		request.setAttribute("canUpload", hasPermission(session, "CONTRACT_UPLOAD"));
		request.setAttribute("canRenew", hasPermission(session, "CONTRACT_RENEW"));

		request.getRequestDispatcher("/views/employeecontract/employee-contract-detail.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String permissionCode) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (permissionCode.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
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
