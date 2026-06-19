package controller.contract;

import dal.ContractDAO;
import dal.ContractTypeDAO;
import dto.ContractListItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Contract;
import model.Permission;

import java.io.IOException;
import java.util.List;

/**
 * Lists employee contracts with keyword + status filter and pagination.
 */
@WebServlet(name = "ContractListServlet", urlPatterns = {"/contract-list"})
public class ContractListServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;

	private final ContractDAO contractDAO = new ContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

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

		String keyword = request.getParameter("keyword");
		String status = request.getParameter("status");
		String pageStr = request.getParameter("page");

		int page = 1;
		if (pageStr != null && !pageStr.isEmpty() && pageStr.matches("\\d+")) {
			page = Integer.parseInt(pageStr);
		}
		if (page < 1) {
			page = 1;
		}
		int offset = (page - 1) * PAGE_SIZE;

		List<ContractListItem> contracts = contractDAO.searchContracts(keyword, status, offset, PAGE_SIZE);
		int total = contractDAO.countContracts(keyword, status);
		int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);
		if (totalPages > 0 && page > totalPages) {
			page = totalPages;
			offset = (page - 1) * PAGE_SIZE;
			contracts = contractDAO.searchContracts(keyword, status, offset, PAGE_SIZE);
		}

		// Expose per-action permissions so the JSP can decide which buttons to render.
		request.setAttribute("hasContractDetailPerm", hasPerm(session, "CONTRACT_VIEW"));
		request.setAttribute("hasContractCreatePerm", hasPerm(session, "CONTRACT_CREATE"));
		request.setAttribute("hasContractUploadPerm", hasPerm(session, "CONTRACT_UPLOAD"));
		request.setAttribute("hasContractRenewPerm", hasPerm(session, "CONTRACT_RENEW"));
		request.setAttribute("hasContractTerminatePerm", hasPerm(session, "CONTRACT_UPDATE"));

		// Quick counts for the small KPI strip on top of the list.
		request.setAttribute("activeCount", contractDAO.countContracts(null, Contract.Status.ACTIVE.name()));
		request.setAttribute("pendingRenewalCount",
				contractDAO.countContracts(null, Contract.Status.PENDING_RENEWAL.name()));
		request.setAttribute("expiringSoonCount", contractDAO.countExpiringSoon(30));
		request.setAttribute("terminatedCount", contractDAO.countContracts(null, Contract.Status.TERMINATED.name()));

		request.setAttribute("contracts", contracts);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", total);
		request.setAttribute("keyword", keyword != null ? keyword : "");
		request.setAttribute("selectedStatus", status != null ? status : "");
		request.setAttribute("statuses", Contract.Status.values());

		// Keep the DAO instance available for the JSP via a request attribute (unused
		// but
		// mirrors the contracttype pattern; left as a hook for future dropdowns).
		request.setAttribute("contractTypes", contractTypeDAO.getActiveContractTypes());

		request.getRequestDispatcher("/views/contract/contract-list.jsp").forward(request, response);
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
