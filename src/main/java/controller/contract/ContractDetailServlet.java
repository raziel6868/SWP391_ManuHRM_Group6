package controller.contract;

import dal.ContractDAO;
import dto.ContractDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;

import java.io.IOException;
import java.util.List;

/**
 * Shows a single employee contract with all joined fields. The action buttons
 * on the JSP are gated by per-permission flags so the same page works for HR
 * (full actions), SYSADMIN, and read-only roles.
 */
@WebServlet(name = "ContractDetailServlet", urlPatterns = {"/contract-detail"})
public class ContractDetailServlet extends HttpServlet {

	private final ContractDAO contractDAO = new ContractDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String successMsg = (String) session.getAttribute("successMsg");
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}

		String idStr = request.getParameter("id");
		if (idStr == null || idStr.isEmpty()) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		Long id;
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Mã hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		request.setAttribute("hasContractUploadPerm", hasPerm(session, "CONTRACT_UPLOAD"));
		request.setAttribute("hasContractUpdatePerm", hasPerm(session, "CONTRACT_UPDATE"));
		request.setAttribute("hasContractRenewPerm", hasPerm(session, "CONTRACT_RENEW"));
		request.setAttribute("hasContractTerminatePerm", hasPerm(session, "CONTRACT_TERMINATE"));
		request.setAttribute("hasContractStatusPerm", hasPerm(session, "CONTRACT_STATUS"));

		request.setAttribute("contract", contract);
		request.getRequestDispatcher("/views/contract/contract-detail.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPerm(HttpSession session, String code) {
		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		if (perms == null) {
			return false;
		}
		for (Permission p : perms) {
			if (code.equals(p.getCode())) {
				return true;
			}
		}
		return false;
	}
}
