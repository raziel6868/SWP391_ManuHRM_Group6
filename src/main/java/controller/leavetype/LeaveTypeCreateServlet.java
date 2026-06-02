package controller.leavetype;

import dal.LeaveTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.LeaveType;
import util.ValidationUtil;

@WebServlet(name = "LeaveTypeCreateServlet", urlPatterns = {"/leave-type-create"})
public class LeaveTypeCreateServlet extends HttpServlet {

	private static final String CODE_REGEX = "^[A-Z][A-Z0-9_]*$";

	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/leavetype/leave-type-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String code = normalizeCode(request.getParameter("code"));
		String name = normalizeText(request.getParameter("name"));
		String description = normalizeText(request.getParameter("description"));
		boolean isPaid = "true".equalsIgnoreCase(request.getParameter("isPaid"));

		request.setAttribute("code", code);
		request.setAttribute("name", name);
		request.setAttribute("description", description);
		request.setAttribute("isPaid", isPaid);

		String validationError = validate(code, name, null);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.getRequestDispatcher("/views/leavetype/leave-type-create.jsp").forward(request, response);
			return;
		}

		LeaveType leaveType = new LeaveType();
		leaveType.setCode(code);
		leaveType.setName(name);
		leaveType.setDescription(description);
		leaveType.setIsPaid(isPaid);
		leaveType.setIsActive(true);

		boolean success = leaveTypeDAO.insert(leaveType);
		if (success) {
			request.getSession().setAttribute("successMsg", "Thêm loại nghỉ thành công.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể thêm loại nghỉ. Vui lòng thử lại.");
		request.getRequestDispatcher("/views/leavetype/leave-type-create.jsp").forward(request, response);
	}

	private String validate(String code, String name, Long id) {
		if (ValidationUtil.isBlank(code)) {
			return "Mã loại nghỉ không được để trống.";
		}
		if (code.length() > 30) {
			return "Mã loại nghỉ không được vượt quá 30 ký tự.";
		}
		if (!ValidationUtil.matchRegex(code, CODE_REGEX)) {
			return "Mã loại nghỉ phải viết hoa, bắt đầu bằng chữ cái và chỉ chứa chữ hoa, số hoặc dấu gạch dưới.";
		}
		if (ValidationUtil.isBlank(name)) {
			return "Tên loại nghỉ không được để trống.";
		}
		if (name.length() > 100) {
			return "Tên loại nghỉ không được vượt quá 100 ký tự.";
		}
		boolean exists = id == null ? leaveTypeDAO.existsByCode(code) : leaveTypeDAO.existsByCodeExceptId(code, id);
		if (exists) {
			return "Mã loại nghỉ đã tồn tại. Vui lòng nhập mã khác.";
		}
		return null;
	}

	private String normalizeCode(String code) {
		if (code == null) {
			return null;
		}
		String trimmed = code.trim().toUpperCase();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
