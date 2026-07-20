package controller.monthlysheet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Permission;
import model.User;

@WebServlet(name = "MonthlySheetCloseServlet", urlPatterns = {"/monthly-sheet-close"})
public class MonthlySheetCloseServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User authUser = (User) session.getAttribute("authUser");
		@SuppressWarnings("unchecked")
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");

		if (authUser == null || !hasPermission(permissions, "MONTHLY_SHEET_CLOSE")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		session.setAttribute("errorMsg",
				"Đường dẫn legacy /monthly-sheet-close không còn được dùng. Vui lòng đóng sổ qua bước HR chốt.");
		response.sendRedirect(request.getContextPath() + "/monthly-sheet-list");
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
