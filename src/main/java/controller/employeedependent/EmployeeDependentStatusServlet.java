package controller.employeedependent;

import dal.EmployeeDependentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.EmployeeDependent;

@WebServlet(name = "EmployeeDependentStatusServlet", urlPatterns = {"/employee-dependent-status"})
public class EmployeeDependentStatusServlet extends HttpServlet {

	private final EmployeeDependentDAO employeeDependentDAO = new EmployeeDependentDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseLong(request.getParameter("id"));
		String isActiveParam = request.getParameter("isActive");

		if (id == null || isActiveParam == null || isActiveParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/employee-dependent-list");
			return;
		}

		EmployeeDependent employeeDependent = employeeDependentDAO.getById(id);
		if (employeeDependent == null) {
			request.getSession().setAttribute("errorMsg", "Người phụ thuộc không tồn tại.");
			response.sendRedirect(request.getContextPath() + "/employee-dependent-list");
			return;
		}

		boolean isActive = Boolean.parseBoolean(isActiveParam);
		boolean success = employeeDependentDAO.updateStatus(id, isActive);
		if (success) {
			request.getSession().setAttribute("successMsg",
					isActive ? "Đã kích hoạt người phụ thuộc." : "Đã vô hiệu hóa người phụ thuộc.");
		} else {
			request.getSession().setAttribute("errorMsg", "Không thể thay đổi trạng thái người phụ thuộc.");
		}

		response.sendRedirect(request.getContextPath() + "/employee-dependent-list");
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
