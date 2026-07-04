package controller.payslip;

import dal.MonthlySalaryDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import model.MonthlySalary;
import model.MonthlySheet;
import model.Permission;
import model.User;

@WebServlet(name = "PayslipViewServlet", urlPatterns = {"/payslip-view"})
public class PayslipViewServlet extends HttpServlet {

	private final MonthlySalaryDAO monthlySalaryDAO = new MonthlySalaryDAO();
	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || permissions == null || !hasPermission(permissions, "PAYSLIP_VIEW")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		boolean canViewAllPayslips = hasPermission(permissions, "PAYROLL_VIEW");

		moveFlash(session, request, "successMsg");
		moveFlash(session, request, "errorMsg");
		request.setAttribute("printDate", new Date());

		String sheetIdParam = request.getParameter("sheetId");
		String userIdParam = request.getParameter("userId");

		if (sheetIdParam == null || sheetIdParam.isEmpty() || userIdParam == null || userIdParam.isEmpty()) {
			MonthlySalary latest = canViewAllPayslips
					? monthlySalaryDAO.getLatestByUser(authUser.getId())
					: monthlySalaryDAO.getLatestFinalizedByUser(authUser.getId());
			if (latest != null) {
				response.sendRedirect(request.getContextPath() + "/payslip-view?sheetId=" + latest.getMonthlySheetId()
						+ "&userId=" + authUser.getId());
				return;
			}

			request.setAttribute("noPayslip", true);
			request.setAttribute("noPayslipMessage",
					canViewAllPayslips
							? "Chưa có phiếu lương cho nhân viên này."
							: "Chưa có phiếu lương đã chốt cho nhân viên này.");
			request.getRequestDispatcher("/views/payroll/payslip-view.jsp").forward(request, response);
			return;
		}

		try {
			Long targetUserId = Long.parseLong(userIdParam);
			if (!authUser.getId().equals(targetUserId) && !canViewAllPayslips) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			Long sheetId = Long.parseLong(sheetIdParam);
			MonthlySalary salary = monthlySalaryDAO.getBySheetAndUser(sheetId, targetUserId);
			MonthlySheet sheet = monthlySheetDAO.getById(sheetId);

			if (salary == null) {
				request.setAttribute("noPayslip", true);
				request.setAttribute("noPayslipMessage", "Không tìm thấy phiếu lương.");
			} else if (!canViewAllPayslips && !isFinalizedPayrollStatus(salary.getStatus())) {
				request.setAttribute("noPayslip", true);
				request.setAttribute("noPayslipMessage", "Phiếu lương của tháng này chưa được chốt.");
			} else {
				request.setAttribute("salary", salary);
				request.setAttribute("sheet", sheet);
			}

		} catch (NumberFormatException e) {
			request.setAttribute("noPayslip", true);
			request.setAttribute("noPayslipMessage", "Tham số không hợp lệ.");
		}

		request.getRequestDispatcher("/views/payroll/payslip-view.jsp").forward(request, response);
	}

	private boolean isFinalizedPayrollStatus(String status) {
		return "FINAL".equals(status) || "PAID".equals(status);
	}

	private void moveFlash(HttpSession session, HttpServletRequest request, String key) {
		Object val = session.getAttribute(key);
		if (val != null) {
			request.setAttribute(key, val);
			session.removeAttribute(key);
		}
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (p.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}
}
