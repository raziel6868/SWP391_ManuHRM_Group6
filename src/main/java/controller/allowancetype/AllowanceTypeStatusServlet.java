package controller.allowancetype;

import dal.AllowanceTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.AllowanceType;

@WebServlet(name = "AllowanceTypeStatusServlet", urlPatterns = {"/allowance-type-status"})
public class AllowanceTypeStatusServlet extends HttpServlet {

	private final AllowanceTypeDAO allowanceTypeDAO = new AllowanceTypeDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		String isActiveParam = request.getParameter("isActive");

		if (id == null || isActiveParam == null || isActiveParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		AllowanceType allowanceType = allowanceTypeDAO.getById(id);
		if (allowanceType == null) {
			request.getSession().setAttribute("errorMsg", "Loại phụ cấp không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/allowance-type-list");
			return;
		}

		boolean isActive = Boolean.parseBoolean(isActiveParam);
		boolean success = allowanceTypeDAO.updateStatus(id, isActive);

		if (success) {
			request.getSession().setAttribute("successMsg",
					isActive ? "Kích hoạt loại phụ cấp thành công." : "Vô hiệu hóa loại phụ cấp thành công.");
		} else {
			request.getSession().setAttribute("errorMsg", "Không thể thay đổi trạng thái loại phụ cấp.");
		}

		response.sendRedirect(request.getContextPath() + "/allowance-type-list");
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
