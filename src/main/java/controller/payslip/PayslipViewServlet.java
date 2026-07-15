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

		try {
			Long targetUserId = resolveTargetUserId(authUser, canViewAllPayslips, userIdParam, response);
			if (targetUserId == null) {
				return;
			}

			List<MonthlySalary> payslipPeriods = canViewAllPayslips
					? monthlySalaryDAO.getByUser(targetUserId)
					: monthlySalaryDAO.getFinalizedByUser(targetUserId);
			request.setAttribute("payslipPeriods", payslipPeriods);
			request.setAttribute("targetUserId", targetUserId);

			if (!hasValue(sheetIdParam)) {
				redirectToLatestPayslipOrShowEmpty(request, response, canViewAllPayslips, targetUserId, payslipPeriods);
				return;
			}

			Long sheetId = Long.parseLong(sheetIdParam.trim());
			MonthlySalary salary = monthlySalaryDAO.getBySheetAndUser(sheetId, targetUserId);
			MonthlySheet sheet = monthlySheetDAO.getById(sheetId);
			request.setAttribute("selectedSheetId", sheetId);

			if (salary == null) {
				request.setAttribute("noPayslip", true);
				request.setAttribute("noPayslipMessage", "Kh\u00f4ng t\u00ecm th\u1ea5y phi\u1ebfu l\u01b0\u01a1ng.");
			} else if (!canViewAllPayslips && !isFinalizedPayrollStatus(salary.getStatus())) {
				request.setAttribute("noPayslip", true);
				request.setAttribute("noPayslipMessage",
						"Phi\u1ebfu l\u01b0\u01a1ng c\u1ee7a th\u00e1ng n\u00e0y ch\u01b0a \u0111\u01b0\u1ee3c ch\u1ed1t.");
			} else {
				request.setAttribute("salary", salary);
				request.setAttribute("sheet", sheet);
			}
		} catch (NumberFormatException e) {
			request.setAttribute("noPayslip", true);
			request.setAttribute("noPayslipMessage", "Tham s\u1ed1 kh\u00f4ng h\u1ee3p l\u1ec7.");
		}

		request.getRequestDispatcher("/views/payroll/payslip-view.jsp").forward(request, response);
	}

	private Long resolveTargetUserId(User authUser, boolean canViewAllPayslips, String userIdParam,
			HttpServletResponse response) throws IOException {
		Long targetUserId = authUser.getId();
		if (hasValue(userIdParam)) {
			targetUserId = Long.parseLong(userIdParam.trim());
		}

		if (!authUser.getId().equals(targetUserId) && !canViewAllPayslips) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		return targetUserId;
	}

	private void redirectToLatestPayslipOrShowEmpty(HttpServletRequest request, HttpServletResponse response,
			boolean canViewAllPayslips, Long targetUserId, List<MonthlySalary> payslipPeriods)
			throws ServletException, IOException {
		if (!payslipPeriods.isEmpty()) {
			MonthlySalary latest = payslipPeriods.get(0);
			response.sendRedirect(request.getContextPath() + "/payslip-view?sheetId=" + latest.getMonthlySheetId()
					+ "&userId=" + targetUserId);
			return;
		}

		request.setAttribute("noPayslip", true);
		request.setAttribute("noPayslipMessage", canViewAllPayslips
				? "Ch\u01b0a c\u00f3 phi\u1ebfu l\u01b0\u01a1ng cho nh\u00e2n vi\u00ean n\u00e0y."
				: "Ch\u01b0a c\u00f3 phi\u1ebfu l\u01b0\u01a1ng \u0111\u00e3 ch\u1ed1t cho nh\u00e2n vi\u00ean n\u00e0y.");
		request.getRequestDispatcher("/views/payroll/payslip-view.jsp").forward(request, response);
	}

	private boolean hasValue(String value) {
		return value != null && !value.trim().isEmpty();
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
