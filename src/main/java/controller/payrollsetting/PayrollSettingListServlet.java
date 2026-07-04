package controller.payrollsetting;

import dal.PayrollSettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.PayrollSetting;
import model.Permission;
import model.User;

@WebServlet(name = "PayrollSettingListServlet", urlPatterns = {"/payroll-setting-list"})
public class PayrollSettingListServlet extends HttpServlet {

	private final PayrollSettingDAO payrollSettingDAO = new PayrollSettingDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PAYROLL_SETTING_VIEW")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlash(session, request, "successMsg");
		moveFlash(session, request, "errorMsg");

		List<PayrollSetting> payrollSettings = payrollSettingDAO.getAll();
		request.setAttribute("payrollSettings", payrollSettings);
		request.setAttribute("canSetup", hasPermission(permissions, "PAYROLL_SETTING_SETUP"));
		request.getRequestDispatcher("/views/payrollsetting/payroll-setting-list.jsp").forward(request, response);
	}

	private void moveFlash(HttpSession session, HttpServletRequest request, String key) {
		Object value = session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}

	private boolean hasPermission(List<Permission> permissions, String code) {
		if (permissions == null) {
			return false;
		}
		for (Permission permission : permissions) {
			if (code.equals(permission.getCode())) {
				return true;
			}
		}
		return false;
	}
}
