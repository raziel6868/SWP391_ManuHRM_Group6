package controller.contract;

import dal.ContractTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ContractType;
import model.Permission;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ContractTypeListServlet", urlPatterns = {"/contract-type-list"})
public class ContractTypeListServlet extends HttpServlet {

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
		String pageStr = request.getParameter("page");

		int page = 1;
		if (pageStr != null && !pageStr.isEmpty()) {
			try {
				page = Integer.parseInt(pageStr);
			} catch (NumberFormatException e) {
				page = 1;
			}
		}
		if (page < 1) {
			page = 1;
		}

		int limit = 10;
		int offset = (page - 1) * limit;

		@SuppressWarnings("unchecked")
		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean hasContractTypeStatusPerm = false;
		if (perms != null) {
			for (Permission p : perms) {
				if ("CONTRACT_TYPE_STATUS".equals(p.getCode())) {
					hasContractTypeStatusPerm = true;
					break;
				}
			}
		}
		request.setAttribute("hasContractTypeStatusPerm", hasContractTypeStatusPerm);

		List<ContractType> contractTypes = contractTypeDAO.searchContractTypes(keyword, offset, limit);
		int total = contractTypeDAO.countContractTypes(keyword);
		int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / limit);
		if (totalPages > 0 && page > totalPages) {
			page = totalPages;
			offset = (page - 1) * limit;
			contractTypes = contractTypeDAO.searchContractTypes(keyword, offset, limit);
		}

		request.setAttribute("contractTypes", contractTypes);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("keyword", keyword != null ? keyword : "");

		request.getRequestDispatcher("/views/contract/contracttype-list.jsp").forward(request, response);
	}
}
