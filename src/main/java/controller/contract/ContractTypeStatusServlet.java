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

@WebServlet(name = "ContractTypeStatusServlet", urlPatterns = {"/contract-type-status"})
public class ContractTypeStatusServlet extends HttpServlet {

	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}

		@SuppressWarnings("unchecked")
		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean hasStatusPerm = false;
		if (perms != null) {
			for (Permission p : perms) {
				if ("CONTRACT_TYPE_STATUS".equals(p.getCode())) {
					hasStatusPerm = true;
					break;
				}
			}
		}
		if (!hasStatusPerm) {
			session.setAttribute("errorMsg", "Bạn không có quyền thay đổi trạng thái loại hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}

		String idStr = request.getParameter("id");
		String isActiveStr = request.getParameter("isActive");

		if (idStr == null || idStr.isEmpty() || isActiveStr == null || isActiveStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr);
			boolean newStatus = Boolean.parseBoolean(isActiveStr);

			ContractType contractType = contractTypeDAO.getById(id);
			if (contractType == null) {
				session.setAttribute("errorMsg", "Không tìm thấy loại hợp đồng.");
				response.sendRedirect(request.getContextPath() + "/contract-type-list");
				return;
			}

			if (contractTypeDAO.updateStatus(id, newStatus)) {
				session.setAttribute("successMsg",
						newStatus ? "Kích hoạt loại hợp đồng thành công!" : "Vô hiệu hóa loại hợp đồng thành công!");
			} else {
				session.setAttribute("errorMsg", "Không thể cập nhật trạng thái. Vui lòng thử lại.");
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "ID không hợp lệ.");
		}

		response.sendRedirect(request.getContextPath() + "/contract-type-list");
	}
}
