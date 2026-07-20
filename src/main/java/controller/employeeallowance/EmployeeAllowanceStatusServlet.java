package controller.employeeallowance;

import dal.EmployeeAllowanceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.EmployeeAllowance;

@WebServlet(name = "EmployeeAllowanceStatusServlet", urlPatterns = {"/employee-allowance-status"})
public class EmployeeAllowanceStatusServlet extends HttpServlet {

	private final EmployeeAllowanceDAO employeeAllowanceDAO = new EmployeeAllowanceDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseLong(request.getParameter("id"));
		String isActiveParam = request.getParameter("isActive");

		if (id == null || isActiveParam == null || isActiveParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/employee-allowance-list");
			return;
		}

		EmployeeAllowance employeeAllowance = employeeAllowanceDAO.getById(id);
		if (employeeAllowance == null) {
			request.getSession().setAttribute("errorMsg", "Phụ cấp nhân viên không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/employee-allowance-list");
			return;
		}

		boolean isActive = Boolean.parseBoolean(isActiveParam);
		boolean success = employeeAllowanceDAO.updateStatus(id, isActive);

		if (success) {
			request.getSession().setAttribute("successMsg",
					isActive ? "Kích hoạt phụ cấp nhân viên thành công." : "Vô hiệu hóa phụ cấp nhân viên thành công.");
		} else {
			request.getSession().setAttribute("errorMsg", "Không thể thay đổi trạng thái phụ cấp nhân viên.");
		}

		response.sendRedirect(request.getContextPath() + "/employee-allowance-list");
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
