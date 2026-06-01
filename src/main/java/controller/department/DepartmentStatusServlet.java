package controller.department;

import dal.DepartmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Department;
import model.Permission;
import util.ValidationUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DepartmentStatusServlet", urlPatterns = {"/department-status"})
public class DepartmentStatusServlet extends HttpServlet {

	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		// ── Kiểm tra permission DEPARTMENT_STATUS ─────────────────────────────
		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean hasPerm = false;
		if (perms != null) {
			for (Permission p : perms) {
				if ("DEPARTMENT_STATUS".equals(p.getCode())) {
					hasPerm = true;
					break;
				}
			}
		}
		if (!hasPerm) {
			session.setAttribute("errorMsg", "Bạn không có quyền thay đổi trạng thái phòng ban.");
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		// ── Validate params ────────────────────────────────────────────────────
		String idParam = request.getParameter("id");
		String isActiveParam = request.getParameter("isActive");

		if (ValidationUtil.isBlank(idParam) || ValidationUtil.isBlank(isActiveParam)) {
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		Long id;
		try {
			id = Long.parseLong(idParam.trim());
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		// ── Kiểm tra department tồn tại ───────────────────────────────────────
		Department department = departmentDAO.getById(id);
		if (department == null) {
			session.setAttribute("errorMsg", "Không tìm thấy phòng ban.");
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		// ── Thực hiện cập nhật ────────────────────────────────────────────────
		boolean newStatus = Boolean.parseBoolean(isActiveParam);
		boolean success = departmentDAO.updateStatus(id, newStatus);

		if (success) {
			session.setAttribute("successMsg",
					newStatus
							? "Kích hoạt phòng ban '" + department.getName() + "' thành công!"
							: "Vô hiệu hóa phòng ban '" + department.getName() + "' thành công!");
		} else {
			session.setAttribute("errorMsg", "Lỗi: Không thể cập nhật trạng thái phòng ban. Vui lòng thử lại.");
		}

		response.sendRedirect(request.getContextPath() + "/department-list");
	}
}