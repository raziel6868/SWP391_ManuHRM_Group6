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
import model.Contract;
import model.User;
import util.ValidationUtil;

/**
 * Self-service renewal request. Employees and production supervisors can mark
 * their own active contract as pending renewal; HR later approves by completing
 * the normal renew form.
 */
@WebServlet(name = "ContractRenewRequestServlet", urlPatterns = {"/contract-renew-request"})
public class ContractRenewRequestServlet extends HttpServlet {

	private final ContractDAO contractDAO = new ContractDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");

		Long id = parseLong(request.getParameter("id"));
		if (id == null) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (authUser == null || authUser.getId() == null || !authUser.getId().equals(contract.getUserId())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		if (!isRequestableStatus(contract.getStatus())) {
			session.setAttribute("errorMsg", "Chỉ hợp đồng đang hiệu lực mới được gửi yêu cầu gia hạn.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		if (contractDAO.requestRenewal(id, authUser.getId())) {
			session.setAttribute("successMsg", "Đã gửi yêu cầu gia hạn hợp đồng đến HR.");
		} else {
			session.setAttribute("errorMsg", "Không thể gửi yêu cầu gia hạn. Hợp đồng có thể đã đổi trạng thái.");
		}
		response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
	}

	private Long parseLong(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private boolean isRequestableStatus(String status) {
		return Contract.Status.ACTIVE.name().equals(status) || Contract.Status.EXPIRING_SOON.name().equals(status);
	}
}
