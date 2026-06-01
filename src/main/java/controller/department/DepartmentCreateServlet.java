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

@WebServlet(name = "DepartmentCreateServlet", urlPatterns = {"/department-create"})
public class DepartmentCreateServlet extends HttpServlet {

	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Đổ danh sách phòng ban active vào dropdown chọn cha
		request.setAttribute("activeDepartments", departmentDAO.getActiveDepartments());

		request.getRequestDispatcher("/views/department/department-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String name = request.getParameter("name");
		String departmentType = request.getParameter("departmentType");
		String parentIdParam = request.getParameter("parentId");

		// ── Validate: tên không được rỗng ─────────────────────────────────────
		if (ValidationUtil.isBlank(name)) {
			request.setAttribute("errorMsg", "Tên phòng ban không được để trống.");
			request.setAttribute("activeDepartments", departmentDAO.getActiveDepartments());
			request.getRequestDispatcher("/views/department/department-create.jsp").forward(request, response);
			return;
		}

		// ── Validate: loại không được rỗng ────────────────────────────────────
		if (ValidationUtil.isBlank(departmentType)
				|| (!departmentType.equals("OFFICE") && !departmentType.equals("FACTORY"))) {
			request.setAttribute("errorMsg", "Loại phòng ban không hợp lệ.");
			request.setAttribute("activeDepartments", departmentDAO.getActiveDepartments());
			request.getRequestDispatcher("/views/department/department-create.jsp").forward(request, response);
			return;
		}

		// ── Validate: tên không trùng ──────────────────────────────────────────
		if (departmentDAO.existsByName(name.trim())) {
			request.setAttribute("errorMsg", "Tên phòng ban '" + name.trim() + "' đã tồn tại. Vui lòng chọn tên khác.");
			request.setAttribute("activeDepartments", departmentDAO.getActiveDepartments());
			request.getRequestDispatcher("/views/department/department-create.jsp").forward(request, response);
			return;
		}

		// ── Parse parentId (optional) ──────────────────────────────────────────
		Long parentId = null;
		if (!ValidationUtil.isBlank(parentIdParam)) {
			try {
				parentId = Long.parseLong(parentIdParam.trim());
			} catch (NumberFormatException e) {
				request.setAttribute("errorMsg", "Phòng ban cha không hợp lệ.");
				request.setAttribute("activeDepartments", departmentDAO.getActiveDepartments());
				request.getRequestDispatcher("/views/department/department-create.jsp").forward(request, response);
				return;
			}
		}

		// ── Build object và insert ─────────────────────────────────────────────
		Department department = new Department();
		department.setName(name.trim());
		department.setDepartmentType(Department.DepartmentType.valueOf(departmentType));
		department.setParentId(parentId);

		boolean success = departmentDAO.insert(department);

		if (success) {
			request.getSession().setAttribute("successMsg", "Thêm phòng ban '" + name.trim() + "' thành công!");
			response.sendRedirect(request.getContextPath() + "/department-list");
		} else {
			request.setAttribute("errorMsg", "Lỗi: Không thể thêm phòng ban. Vui lòng thử lại.");
			request.setAttribute("activeDepartments", departmentDAO.getActiveDepartments());
			request.getRequestDispatcher("/views/department/department-create.jsp").forward(request, response);
		}
	}
}