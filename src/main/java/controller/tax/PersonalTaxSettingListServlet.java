package controller.tax;

import dal.PersonalTaxSettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Permission;
import model.PersonalTaxSetting;
import model.User;

@WebServlet(name = "PersonalTaxSettingListServlet", urlPatterns = {"/personal-tax-setting-list"})
public class PersonalTaxSettingListServlet extends HttpServlet {

	private final PersonalTaxSettingDAO personalTaxSettingDAO = new PersonalTaxSettingDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (user == null || permissions == null || !hasPermission(permissions, "PERSONAL_TAX_SETTING_VIEW")) {
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlash(session, request, "successMsg");
		moveFlash(session, request, "errorMsg");

		List<PersonalTaxSetting> personalTaxSettings = personalTaxSettingDAO.getAll();
		request.setAttribute("personalTaxSettings", personalTaxSettings);
		request.setAttribute("canSetup", hasPermission(permissions, "PERSONAL_TAX_SETTING_SETUP"));
		request.getRequestDispatcher("/views/tax/personal-tax-setting-list.jsp").forward(request, response);
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
