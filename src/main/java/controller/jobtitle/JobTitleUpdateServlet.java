package controller.jobtitle;

import dal.JobTitleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.JobTitle;
import util.ValidationUtil;

import java.io.IOException;

@WebServlet(name = "JobTitleUpdateServlet", urlPatterns = {"/job-title-update"})
public class JobTitleUpdateServlet extends HttpServlet {

	private final JobTitleDAO jobTitleDAO = new JobTitleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		if (idStr != null && !idStr.isEmpty()) {
			try {
				Long id = Long.parseLong(idStr);
				JobTitle jobTitle = jobTitleDAO.getById(id);
				if (jobTitle != null) {
					request.setAttribute("jobTitle", jobTitle);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		request.getRequestDispatcher("/views/jobtitle/job-title-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String idStr = request.getParameter("id");
		String name = request.getParameter("name");
		String description = request.getParameter("description");

		if (idStr == null || idStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/job-title-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr);
			JobTitle existing = jobTitleDAO.getById(id);
			if (existing == null) {
				response.sendRedirect(request.getContextPath() + "/job-title-list");
				return;
			}

			if (ValidationUtil.isBlank(name)) {
				request.setAttribute("errorMsg", "Tên chức danh không được để trống.");
				request.setAttribute("jobTitle", existing);
				request.getRequestDispatcher("/views/jobtitle/job-title-update.jsp").forward(request, response);
				return;
			}

			String trimmedName = name.trim();
			if (trimmedName.length() > 100) {
				request.setAttribute("errorMsg", "Tên chức danh không được vượt quá 100 ký tự.");
				request.setAttribute("jobTitle", existing);
				request.getRequestDispatcher("/views/jobtitle/job-title-update.jsp").forward(request, response);
				return;
			}

			if (!trimmedName.equals(existing.getName()) && jobTitleDAO.existsByNameExceptId(trimmedName, id)) {
				request.setAttribute("errorMsg",
						"Tên chức danh '" + trimmedName + "' đã tồn tại. Vui lòng chọn tên khác.");
				request.setAttribute("jobTitle", existing);
				request.getRequestDispatcher("/views/jobtitle/job-title-update.jsp").forward(request, response);
				return;
			}

			JobTitle jobTitle = new JobTitle();
			jobTitle.setId(id);
			jobTitle.setName(trimmedName);
			jobTitle.setDescription(description != null ? description.trim() : null);

			boolean success = jobTitleDAO.update(jobTitle);
			if (success) {
				request.getSession().setAttribute("successMsg", "Cập nhật chức danh '" + trimmedName + "' thành công!");
				response.sendRedirect(request.getContextPath() + "/job-title-list");
			} else {
				request.setAttribute("errorMsg", "Lỗi: Không thể cập nhật chức danh. Vui lòng thử lại.");
				request.setAttribute("jobTitle", existing);
				request.getRequestDispatcher("/views/jobtitle/job-title-update.jsp").forward(request, response);
			}
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/job-title-list");
		}
	}
}
