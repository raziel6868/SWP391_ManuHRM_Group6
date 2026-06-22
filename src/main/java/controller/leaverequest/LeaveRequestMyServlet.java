package controller.leaverequest;

import dal.LeaveBalanceDAO;
import dal.LeaveRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.LeaveBalance;
import model.LeaveRequest;
import model.User;

@WebServlet(name = "LeaveRequestMyServlet", urlPatterns = {"/leave-request-my"})
public class LeaveRequestMyServlet extends HttpServlet {

	private final LeaveRequestDAO requestDAO = new LeaveRequestDAO();
	private final LeaveBalanceDAO balanceDAO = new LeaveBalanceDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		if (authUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		int currentYear = java.time.Year.now().getValue();
		List<LeaveBalance> balances = balanceDAO.getByUserAndYear(authUser.getId(), currentYear);
		List<LeaveRequest> requests = requestDAO.searchByUser(authUser.getId(), 0, 100);

		request.setAttribute("balances", balances);
		request.setAttribute("requests", requests);
		request.setAttribute("currentYear", currentYear);

		request.getRequestDispatcher("/views/leaverequest/leave-request-my.jsp").forward(request, response);
	}

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
