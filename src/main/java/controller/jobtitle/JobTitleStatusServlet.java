package controller.jobtitle;

import dal.JobTitleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "JobTitleStatusServlet", urlPatterns = {"/job-title-status"})
public class JobTitleStatusServlet extends HttpServlet {

	private final JobTitleDAO jobTitleDAO = new JobTitleDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath() + "/job-title-list");
			return;
		}

		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean hasStatusPerm = false;
		if (perms != null) {
			for (Permission p : perms) {
				if ("JOB_TITLE_STATUS".equals(p.getCode())) {
					hasStatusPerm = true;
					break;
				}
			}
		}
		if (!hasStatusPerm) {
			session.setAttribute("errorMsg", "Bạn không có quyền thay đổi trạng thái chức danh.");
			response.sendRedirect(request.getContextPath() + "/job-title-list");
			return;
		}

		String idStr = request.getParameter("id");
		String isActiveStr = request.getParameter("isActive");

		if (idStr == null || idStr.isEmpty() || isActiveStr == null || isActiveStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/job-title-list");
			return;
		}

		try {
			Long jobTitleId = Long.parseLong(idStr);
			boolean newStatus = Boolean.parseBoolean(isActiveStr);

			boolean success = jobTitleDAO.updateStatus(jobTitleId, newStatus);
			if (success) {
				session.setAttribute("successMsg",
						newStatus ? "Kích hoạt chức danh thành công!" : "Vô hiệu hóa chức danh thành công!");
			}
		} catch (NumberFormatException e) {
			// Invalid ID, ignore
		}

		response.sendRedirect(request.getContextPath() + "/job-title-list");
	}
}
