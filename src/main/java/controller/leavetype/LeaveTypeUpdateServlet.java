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

@WebServlet(name = "LeaveTypeUpdateServlet", urlPatterns = {"/leave-type-update"})
public class LeaveTypeUpdateServlet extends HttpServlet {

	private static final String CODE_REGEX = "^[A-Z][A-Z0-9_]*$";

	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		if (id == null) {
			request.getSession().setAttribute("errorMsg", "Không tìm thấy loại nghỉ cần cập nhật.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		LeaveType leaveType = leaveTypeDAO.getById(id);
		if (leaveType == null) {
			request.getSession().setAttribute("errorMsg", "Loại nghỉ không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		request.setAttribute("leaveType", leaveType);
		request.getRequestDispatcher("/views/leavetype/leave-type-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long id = parseId(request.getParameter("id"));
		if (id == null) {
			request.getSession().setAttribute("errorMsg", "Không tìm thấy loại nghỉ cần cập nhật.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		LeaveType existingLeaveType = leaveTypeDAO.getById(id);
		if (existingLeaveType == null) {
			request.getSession().setAttribute("errorMsg", "Loại nghỉ không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		String code = normalizeCode(request.getParameter("code"));
		String name = normalizeText(request.getParameter("name"));
		String description = normalizeText(request.getParameter("description"));
		boolean isPaid = "true".equalsIgnoreCase(request.getParameter("isPaid"));

		existingLeaveType.setCode(code);
		existingLeaveType.setName(name);
		existingLeaveType.setDescription(description);
		existingLeaveType.setIsPaid(isPaid);

		String validationError = validate(code, name, id);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("leaveType", existingLeaveType);
			request.getRequestDispatcher("/views/leavetype/leave-type-update.jsp").forward(request, response);
			return;
		}

		boolean success = leaveTypeDAO.update(existingLeaveType);
		if (success) {
			request.getSession().setAttribute("successMsg", "Cập nhật loại nghỉ thành công.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể cập nhật loại nghỉ. Vui lòng thử lại.");
		request.setAttribute("leaveType", existingLeaveType);
		request.getRequestDispatcher("/views/leavetype/leave-type-update.jsp").forward(request, response);
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
		if (leaveTypeDAO.existsByCodeExceptId(code, id)) {
			return "Mã loại nghỉ đã tồn tại. Vui lòng nhập mã khác.";
		}
		return null;
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
