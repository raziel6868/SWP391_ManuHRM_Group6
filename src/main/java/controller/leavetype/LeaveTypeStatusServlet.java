package controller.leavetype;

import dal.LeaveTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import model.LeaveType;

@WebServlet(name = "LeaveTypeStatusServlet", urlPatterns = {"/leave-type-status"})
public class LeaveTypeStatusServlet extends HttpServlet {

	private final LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Long id = parseId(request.getParameter("id"));
		String isActiveParam = request.getParameter("isActive");

		if (id == null || isActiveParam == null || isActiveParam.isBlank()) {
			request.getSession().setAttribute("errorMsg", "Yêu cầu thay đổi trạng thái không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		LeaveType leaveType = leaveTypeDAO.getById(id);
		if (leaveType == null) {
			request.getSession().setAttribute("errorMsg", "Loại nghỉ không tồn tại hoặc đã bị xóa.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		boolean isActive = Boolean.parseBoolean(isActiveParam);
		if (isActive) {
			String activationError = validateActivation(leaveType);
			if (activationError != null) {
				request.getSession().setAttribute("errorMsg", activationError);
				response.sendRedirect(request.getContextPath() + "/leave-type-list");
				return;
			}
		}
		if (isActive && Boolean.TRUE.equals(leaveType.getIsAnnualLeave())
				&& leaveTypeDAO.hasActiveAnnualLeaveTypeExceptId(id)) {
			request.getSession().setAttribute("errorMsg", "Chỉ được có một loại phép năm đang hoạt động.");
			response.sendRedirect(request.getContextPath() + "/leave-type-list");
			return;
		}

		boolean success = leaveTypeDAO.updateStatus(id, isActive);

		if (success) {
			request.getSession().setAttribute("successMsg",
					isActive ? "Kích hoạt loại nghỉ thành công." : "Vô hiệu hóa loại nghỉ thành công.");
		} else {
			request.getSession().setAttribute("errorMsg", "Không thể thay đổi trạng thái loại nghỉ.");
		}

		response.sendRedirect(request.getContextPath() + "/leave-type-list");
	}

	private String validateActivation(LeaveType leaveType) {
		boolean isPaid = Boolean.TRUE.equals(leaveType.getIsPaid());
		boolean isAnnualLeave = Boolean.TRUE.equals(leaveType.getIsAnnualLeave());
		boolean requiresBalance = Boolean.TRUE.equals(leaveType.getRequiresBalance());
		boolean hasSeniorityBonus = Boolean.TRUE.equals(leaveType.getHasSeniorityBonus());

		if (isPaid && !"COMPANY".equals(leaveType.getSalaryPaidBy())) {
			return "Loại nghỉ có hưởng lương công ty phải có nguồn chi trả COMPANY.";
		}
		if (!isPaid && "COMPANY".equals(leaveType.getSalaryPaidBy())) {
			return "Loại nghỉ do công ty trả lương phải được đánh dấu là có hưởng lương.";
		}
		if (requiresBalance && !isAnnualLeave) {
			return "Hệ thống hiện chỉ hỗ trợ trừ quỹ cho loại phép năm.";
		}
		if (hasSeniorityBonus && !isAnnualLeave) {
			return "Thâm niên chỉ áp dụng cho loại phép năm.";
		}
		if (leaveType.getBaseDays() != null && leaveType.getBaseDays().compareTo(BigDecimal.ZERO) < 0) {
			return "Số ngày cơ bản không được âm.";
		}
		if (leaveType.getMaxDays() != null && leaveType.getMaxDays().compareTo(BigDecimal.ZERO) <= 0) {
			return "Số ngày tối đa phải lớn hơn 0.";
		}
		if (hasSeniorityBonus) {
			if (leaveType.getSeniorityIntervalYears() == null || leaveType.getSeniorityIntervalYears() <= 0) {
				return "Khoảng năm thâm niên phải lớn hơn 0.";
			}
			if (leaveType.getSeniorityBonusDays() == null
					|| leaveType.getSeniorityBonusDays().compareTo(BigDecimal.ZERO) <= 0) {
				return "Số ngày cộng thâm niên phải lớn hơn 0.";
			}
		}
		if (!isAnnualLeave && leaveType.getBaseDays() != null
				&& leaveType.getBaseDays().compareTo(BigDecimal.ZERO) > 0) {
			return "Số ngày cơ bản chỉ dùng cho loại phép năm.";
		}
		if (!isAnnualLeave) {
			return null;
		}
		if (!requiresBalance) {
			return "Loại phép năm bắt buộc phải trừ quỹ phép.";
		}
		if (!isPaid || !"COMPANY".equals(leaveType.getSalaryPaidBy())) {
			return "Loại phép năm phải là nghỉ hưởng lương do công ty chi trả.";
		}
		if (leaveType.getBaseDays() == null || leaveType.getBaseDays().compareTo(BigDecimal.ZERO) <= 0) {
			return "Loại phép năm phải có số ngày cơ bản lớn hơn 0.";
		}
		if (leaveType.getMaxDays() != null) {
			return "Loại phép năm không cần cấu hình tối đa ngày/đơn; hệ thống kiểm tra theo quỹ phép còn lại.";
		}
		if (!"WORKING_DAY".equals(leaveType.getDayCountMethod())) {
			return "Loại phép năm phải tính theo WORKING_DAY.";
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
}
