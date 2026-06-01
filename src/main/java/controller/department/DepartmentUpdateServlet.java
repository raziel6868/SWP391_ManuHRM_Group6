package controller.department;

import dal.DepartmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Department;
import util.ValidationUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "DepartmentUpdateServlet", urlPatterns = {"/department-update"})
public class DepartmentUpdateServlet extends HttpServlet {

	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idParam = request.getParameter("id");

		// ── Validate id ────────────────────────────────────────────────────────
		if (ValidationUtil.isBlank(idParam)) {
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

		Department department = departmentDAO.getById(id);
		if (department == null) {
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		// ── Dropdown parent: loại bỏ chính nó ─────────────────────────────────
		List<Department> allActive = departmentDAO.getActiveDepartments();
		List<Department> parentOptions = allActive.stream().filter(d -> !d.getId().equals(id))
				.collect(Collectors.toList());

		request.setAttribute("department", department);
		request.setAttribute("parentOptions", parentOptions);

		request.getRequestDispatcher("/views/department/department-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String idParam = request.getParameter("id");
		String name = request.getParameter("name");
		String departmentType = request.getParameter("departmentType");
		String parentIdParam = request.getParameter("parentId");

		// ── Validate id ────────────────────────────────────────────────────────
		if (ValidationUtil.isBlank(idParam)) {
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

		Department existing = departmentDAO.getById(id);
		if (existing == null) {
			response.sendRedirect(request.getContextPath() + "/department-list");
			return;
		}

		// ── Validate: tên không rỗng ───────────────────────────────────────────
		if (ValidationUtil.isBlank(name)) {
			request.setAttribute("errorMsg", "Tên phòng ban không được để trống.");
			forwardBackToForm(request, response, id, existing);
			return;
		}

		// ── Validate: loại hợp lệ ─────────────────────────────────────────────
		if (ValidationUtil.isBlank(departmentType)
				|| (!departmentType.equals("OFFICE") && !departmentType.equals("FACTORY"))) {
			request.setAttribute("errorMsg", "Loại phòng ban không hợp lệ.");
			forwardBackToForm(request, response, id, existing);
			return;
		}

		// ── Validate: tên không trùng với department khác ─────────────────────
		if (departmentDAO.existsByNameExceptId(name.trim(), id)) {
			request.setAttribute("errorMsg", "Tên phòng ban '" + name.trim() + "' đã tồn tại. Vui lòng chọn tên khác.");
			forwardBackToForm(request, response, id, existing);
			return;
		}

		// ── Parse parentId (optional) ──────────────────────────────────────────
		Long parentId = null;
		if (!ValidationUtil.isBlank(parentIdParam)) {
			try {
				parentId = Long.parseLong(parentIdParam.trim());
			} catch (NumberFormatException e) {
				request.setAttribute("errorMsg", "Phòng ban cha không hợp lệ.");
				forwardBackToForm(request, response, id, existing);
				return;
			}

			// ── Validate: không được chọn chính nó làm parent ─────────────────
			if (parentId.equals(id)) {
				request.setAttribute("errorMsg", "Phòng ban không thể là cha của chính nó.");
				forwardBackToForm(request, response, id, existing);
				return;
			}
		}

		// ── Build object và update ─────────────────────────────────────────────
		existing.setName(name.trim());
		existing.setDepartmentType(Department.DepartmentType.valueOf(departmentType));
		existing.setParentId(parentId);

		boolean success = departmentDAO.update(existing);

		if (success) {
			request.getSession().setAttribute("successMsg", "Cập nhật phòng ban '" + name.trim() + "' thành công!");
			response.sendRedirect(request.getContextPath() + "/department-list");
		} else {
			request.setAttribute("errorMsg", "Lỗi: Không thể cập nhật phòng ban. Vui lòng thử lại.");
			forwardBackToForm(request, response, id, existing);
		}
	}

	// ── Helper: load lại form khi có lỗi ──────────────────────────────────────
	private void forwardBackToForm(HttpServletRequest request, HttpServletResponse response, Long id,
			Department current) throws ServletException, IOException {
		List<Department> allActive = departmentDAO.getActiveDepartments();
		List<Department> parentOptions = allActive.stream().filter(d -> !d.getId().equals(id))
				.collect(Collectors.toList());

		request.setAttribute("department", current);
		request.setAttribute("parentOptions", parentOptions);
		request.getRequestDispatcher("/views/department/department-update.jsp").forward(request, response);
	}
}