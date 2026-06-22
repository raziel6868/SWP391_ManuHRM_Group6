package controller.payslip;

import java.io.IOException;
import java.util.List;
import dal.MonthlySalaryDAO;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

		moveFlash(session, request, "successMsg");
		moveFlash(session, request, "errorMsg");

		String sheetIdParam = request.getParameter("sheetId");
		String userIdParam = request.getParameter("userId");

		if (sheetIdParam == null || sheetIdParam.isEmpty() || userIdParam == null || userIdParam.isEmpty()) {
			MonthlySalary latest = monthlySalaryDAO.getLatestByUser(authUser.getId());
			if (latest != null) {
				response.sendRedirect(request.getContextPath() + "/payslip-view?sheetId=" + latest.getMonthlySheetId()
						+ "&userId=" + authUser.getId());
				return;
			} else {
				request.setAttribute("noPayslip", true);
				request.setAttribute("noPayslipMessage", "Chưa có phiếu lương cho nhân viên này.");
				request.getRequestDispatcher("/views/payroll/payslip-view.jsp").forward(request, response);
				return;
			}
		}

		try {
			Long targetUserId = Long.parseLong(userIdParam);

			if (!authUser.getId().equals(targetUserId) && !hasPermission(permissions, "PAYROLL_VIEW")) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			Long sheetId = Long.parseLong(sheetIdParam);
			MonthlySalary salary = monthlySalaryDAO.getBySheetAndUser(sheetId, targetUserId);
			MonthlySheet sheet = monthlySheetDAO.getById(sheetId);

			if (salary == null) {
				request.setAttribute("noPayslip", true);
				request.setAttribute("noPayslipMessage", "Không tìm thấy phiếu lương.");
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
