package controller.jobtitle;

import dal.JobTitleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.JobTitle;
import model.Permission;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "JobTitleListServlet", urlPatterns = {"/job-title-list"})
public class JobTitleListServlet extends HttpServlet {

	private final JobTitleDAO jobTitleDAO = new JobTitleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String successMsg = (String) session.getAttribute("successMsg");
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}

		String keyword = request.getParameter("keyword");
		String pageStr = request.getParameter("page");

		int page = 1;
		if (pageStr != null && !pageStr.isEmpty()) {
			try {
				page = Integer.parseInt(pageStr);
			} catch (NumberFormatException e) {
				page = 1;
			}
		}

		int limit = 10;
		int offset = (page - 1) * limit;

		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		boolean hasUpdatePerm = false;
		boolean hasStatusPerm = false;
		if (perms != null) {
			for (Permission p : perms) {
				if ("JOB_TITLE_UPDATE".equals(p.getCode())) {
					hasUpdatePerm = true;
				}
				if ("JOB_TITLE_STATUS".equals(p.getCode())) {
					hasStatusPerm = true;
				}
			}
		}
		request.setAttribute("hasUpdatePerm", hasUpdatePerm);
		request.setAttribute("hasStatusPerm", hasStatusPerm);

		List<JobTitle> jobTitles = jobTitleDAO.searchJobTitles(keyword, offset, limit);
		int totalJobTitles = jobTitleDAO.countJobTitles(keyword);
		int totalPages = (int) Math.ceil((double) totalJobTitles / limit);

		request.setAttribute("jobTitles", jobTitles);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("keyword", keyword);

		request.getRequestDispatcher("/views/jobtitle/job-title-list.jsp").forward(request, response);
	}
}
