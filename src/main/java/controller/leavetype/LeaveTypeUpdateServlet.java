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

		LeaveType submitted = buildLeaveType(request, existingLeaveType);
		submitted.setId(id);
		submitted.setIsActive(existingLeaveType.getIsActive());

		String validationError = validate(submitted);
		if (validationError != null) {
			request.setAttribute("errorMsg", validationError);
			request.setAttribute("leaveType", submitted);
			request.getRequestDispatcher("/views/leavetype/leave-type-update.jsp").forward(request, response);
			return;
		}

		boolean success = leaveTypeDAO.update(submitted);
		if (success) {
			request.getSession().setAttribute("successMsg", "Cập nhật loại nghỉ thành công.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		request.setAttribute("errorMsg", "Không thể cập nhật loại nghỉ. Vui lòng thử lại.");
		request.setAttribute("leaveType", submitted);
		request.getRequestDispatcher("/views/leavetype/leave-type-update.jsp").forward(request, response);
	}

	private LeaveType buildLeaveType(HttpServletRequest request, LeaveType existingLeaveType) {
		LeaveType leaveType = new LeaveType();
		leaveType.setCode(existingLeaveType.getCode());
		leaveType.setName(normalizeText(request.getParameter("name")));
		leaveType.setDescription(normalizeText(request.getParameter("description")));
		leaveType.setIsPaid(existingLeaveType.getIsPaid());
		leaveType.setSalaryPaidBy(existingLeaveType.getSalaryPaidBy());
		leaveType.setIsAnnualLeave(existingLeaveType.getIsAnnualLeave());
		leaveType.setRequiresBalance(existingLeaveType.getRequiresBalance());
		leaveType.setBaseDays(existingLeaveType.getBaseDays());
		leaveType.setMaxDays(existingLeaveType.getMaxDays());
		leaveType.setHasSeniorityBonus(existingLeaveType.getHasSeniorityBonus());
		leaveType.setSeniorityIntervalYears(existingLeaveType.getSeniorityIntervalYears());
		leaveType.setSeniorityBonusDays(existingLeaveType.getSeniorityBonusDays());
		leaveType.setDayCountMethod(existingLeaveType.getDayCountMethod());
		return leaveType;
	}

	private String validate(LeaveType leaveType) {
		if (ValidationUtil.isBlank(leaveType.getName())) {
			return "Tên loại nghỉ không được để trống.";
		}
		if (leaveType.getName().length() > 100) {
			return "Tên loại nghỉ không được vượt quá 100 ký tự.";
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

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
