package controller.contract;

import dal.ContractDAO;
import dto.ContractListItem;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Lists ACTIVE contracts that will expire within the next N days (default 30).
 *
 * HR uses this for upcoming-renewal planning; Supervisor uses this to know
 * which reports are about to roll off. Both roles should have
 * CONTRACT_EXPIRY_VIEW.
 */
@WebServlet(name = "ContractExpiryServlet", urlPatterns = {"/contract-expiry"})
public class ContractExpiryServlet extends HttpServlet {

	private static final int DEFAULT_DAYS = 30;
	private static final int MAX_DAYS = 365;
	private static final int MAX_ROWS = 500;

	private final ContractDAO contractDAO = new ContractDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int days = parseDays(request.getParameter("days"));
		List<ContractListItem> items = contractDAO.findExpiringSoon(days, MAX_ROWS);

		request.setAttribute("items", items);
		request.setAttribute("days", days);
		request.getRequestDispatcher("/views/contract/contract-expiry.jsp").forward(request, response);
	}

	private int parseDays(String raw) {
		if (ValidationUtil.isBlank(raw)) {
			return DEFAULT_DAYS;
		}
		try {
			int v = Integer.parseInt(raw.trim());
			if (v < 1) {
				return DEFAULT_DAYS;
			}
			return Math.min(v, MAX_DAYS);
		} catch (NumberFormatException e) {
			return DEFAULT_DAYS;
		}
	}
}