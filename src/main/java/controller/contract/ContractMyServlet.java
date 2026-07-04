package controller.contract;

import dal.ContractDAO;
import dto.ContractDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

/**
 * Entry point for self-service contract access. It avoids exposing the full
 * contract list to employees/supervisors and sends them straight to their own
 * latest contract detail.
 */
@WebServlet(name = "ContractMyServlet", urlPatterns = {"/my-contract"})
public class ContractMyServlet extends HttpServlet {

	private final ContractDAO contractDAO = new ContractDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		if (authUser == null || authUser.getId() == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		contractDAO.refreshLifecycleStatuses();
		ContractDetail contract = contractDAO.getLatestDetailByUser(authUser.getId());
		if (contract == null) {
			session.setAttribute("errorMsg", "Bạn chưa có hợp đồng lao động trong hệ thống.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + contract.getId());
	}
}
