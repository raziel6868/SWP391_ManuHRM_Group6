package controller.role;

import dal.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Role;
import util.ValidationUtil;

@WebServlet(name = "RoleCreateServlet", urlPatterns = {"/role-create"})
public class RoleCreateServlet extends HttpServlet {

	private final RoleDAO roleDAO = new RoleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String name = request.getParameter("name");
		String displayName = request.getParameter("displayName");
		String description = request.getParameter("description");

		// Validate rỗng
		if (ValidationUtil.isBlank(name)) {
			request.setAttribute("errorMsg", "Mã hệ thống không được để trống.");
			request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
			return;
		}
		if (ValidationUtil.isBlank(displayName)) {
			request.setAttribute("errorMsg", "Tên hiển thị không được để trống.");
			request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
			return;
		}

		// Validate format mã hệ thống: uppercase, không dấu, có thể có số và underscore
		String normalizedName = name.trim().toUpperCase();
		if (!ValidationUtil.matchRegex(normalizedName, "^[A-Z][A-Z0-9_]*$")) {
			request.setAttribute("errorMsg",
					"Mã hệ thống phải bắt đầu bằng chữ in hoa, chỉ chứa chữ in hoa, số và dấu gạch dưới.");
			request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
			return;
		}

		// Kiểm tra trùng lặp
		if (roleDAO.existsByName(normalizedName)) {
			request.setAttribute("errorMsg", "Mã hệ thống '" + normalizedName + "' đã tồn tại. Vui lòng chọn mã khác.");
			request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
			return;
		}

		// Tạo role với is_system = false (role nghiệp vụ tạo từ web)
		try {
			Role role = new Role();
			role.setName(normalizedName);
			role.setDisplayName(displayName.trim());
			role.setDescription(description != null ? description.trim() : null);
			role.setIsActive(true);
			role.setIsSystem(false);

			boolean success = roleDAO.insert(role);

			if (success) {
				request.getSession().setAttribute("successMsg", "Thêm vai trò '" + displayName + "' thành công!");
				response.sendRedirect(request.getContextPath() + "/role-list");
			} else {
				request.setAttribute("errorMsg", "Lỗi: Không thể thêm vai trò. Vui lòng thử lại.");
				request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
			}
		} catch (Exception e) {
			request.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
			request.getRequestDispatcher("/views/role/role-create.jsp").forward(request, response);
		}
	}
}
