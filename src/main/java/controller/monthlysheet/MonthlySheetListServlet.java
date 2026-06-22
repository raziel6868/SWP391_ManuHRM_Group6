package controller.monthlysheet;

import java.io.IOException;
import java.util.List;
import dal.MonthlySheetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.MonthlySheet;
import model.Permission;

@WebServlet(name = "MonthlySheetListServlet", urlPatterns = {"/monthly-sheet-list"})
public class MonthlySheetListServlet extends HttpServlet {

	private final MonthlySheetDAO monthlySheetDAO = new MonthlySheetDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (!hasPermission(session, "MONTHLY_SHEET_VIEW")) {
			session.setAttribute("errorMsg", "Bạn không có quyền truy cập trang này.");
			response.sendRedirect(request.getContextPath() + "/home");
			return;
		}

		moveFlashMessage(session, request, "successMsg");
		moveFlashMessage(session, request, "errorMsg");

		List<MonthlySheet> sheets = monthlySheetDAO.getAll();
		request.setAttribute("sheets", sheets);
		request.setAttribute("canClose", hasPermission(session, "MONTHLY_SHEET_CLOSE"));
		request.setAttribute("canReopen", hasPermission(session, "MONTHLY_SHEET_REOPEN"));

		request.getRequestDispatcher("/views/monthlysheet/monthly-sheet-list.jsp").forward(request, response);
	}

	@SuppressWarnings("unchecked")
	private boolean hasPermission(HttpSession session, String code) {
		List<Permission> permissions = (List<Permission>) session.getAttribute("permissions");
		if (permissions == null) {
			return false;
		}
		for (Permission p : permissions) {
			if (code.equals(p.getCode())) {
				return true;
			}
		}
		return false;
	}

	private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
		String value = (String) session.getAttribute(key);
		if (value != null) {
			request.setAttribute(key, value);
			session.removeAttribute(key);
		}
	}
}
