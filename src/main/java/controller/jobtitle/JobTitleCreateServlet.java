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

@WebServlet(name = "JobTitleCreateServlet", urlPatterns = {"/job-title-create"})
public class JobTitleCreateServlet extends HttpServlet {

	private final JobTitleDAO jobTitleDAO = new JobTitleDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/jobtitle/job-title-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String name = request.getParameter("name");
		String description = request.getParameter("description");

		if (ValidationUtil.isBlank(name)) {
			request.setAttribute("errorMsg", "Tên chức danh không được để trống.");
			request.getRequestDispatcher("/views/jobtitle/job-title-create.jsp").forward(request, response);
			return;
		}

		String trimmedName = name.trim();
		if (trimmedName.length() > 100) {
			request.setAttribute("errorMsg", "Tên chức danh không được vượt quá 100 ký tự.");
			request.getRequestDispatcher("/views/jobtitle/job-title-create.jsp").forward(request, response);
			return;
		}

		if (jobTitleDAO.existsByName(trimmedName)) {
			request.setAttribute("errorMsg", "Tên chức danh '" + trimmedName + "' đã tồn tại. Vui lòng chọn tên khác.");
			request.getRequestDispatcher("/views/jobtitle/job-title-create.jsp").forward(request, response);
			return;
		}

		try {
			JobTitle jobTitle = new JobTitle();
			jobTitle.setName(trimmedName);
			jobTitle.setDescription(description != null ? description.trim() : null);
			jobTitle.setIsActive(true);

			boolean success = jobTitleDAO.insert(jobTitle);

			if (success) {
				request.getSession().setAttribute("successMsg", "Thêm chức danh '" + trimmedName + "' thành công!");
				response.sendRedirect(request.getContextPath() + "/job-title-list");
			} else {
				request.setAttribute("errorMsg", "Lỗi: Không thể thêm chức danh. Vui lòng thử lại.");
				request.getRequestDispatcher("/views/jobtitle/job-title-create.jsp").forward(request, response);
			}
		} catch (Exception e) {
			request.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
			request.getRequestDispatcher("/views/jobtitle/job-title-create.jsp").forward(request, response);
		}
	}
}
