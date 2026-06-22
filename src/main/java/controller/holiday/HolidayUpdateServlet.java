package controller.holiday;

import dal.HolidayDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Holiday;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet(name = "HolidayUpdateServlet", urlPatterns = {"/holiday-update"})
public class HolidayUpdateServlet extends HttpServlet {

	private final HolidayDAO holidayDAO = new HolidayDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		if (idStr == null || idStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/holiday-list");
			return;
		}

		try {
			Long id = Long.parseLong(idStr);
			Holiday holiday = holidayDAO.getById(id);
			if (holiday == null) {
				response.sendRedirect(request.getContextPath() + "/holiday-list");
				return;
			}
			request.setAttribute("holiday", holiday);
			request.getRequestDispatcher("/views/holiday/holiday-update.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/holiday-list");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String idStr = request.getParameter("id");
		String dateStr = request.getParameter("date");
		String name = request.getParameter("name");
		String isRecurringStr = request.getParameter("isRecurring");
		String description = request.getParameter("description");

		if (idStr == null || idStr.isEmpty() || dateStr == null || dateStr.isEmpty() || name == null
				|| name.isEmpty()) {
			session.setAttribute("errorMsg", "Ngay va ten ngay le khong duoc de trong.");
			response.sendRedirect(request.getContextPath() + "/holiday-update?id=" + idStr);
			return;
		}

		try {
			Long id = Long.parseLong(idStr);
			LocalDate date = LocalDate.parse(dateStr);

			if (holidayDAO.existsByDateExceptId(date, id)) {
				session.setAttribute("errorMsg", "Ngay le nay da ton tai.");
				response.sendRedirect(request.getContextPath() + "/holiday-update?id=" + idStr);
				return;
			}

			Holiday holiday = new Holiday();
			holiday.setId(id);
			holiday.setDate(java.sql.Date.valueOf(date));
			holiday.setName(name.trim());
			holiday.setRecurring("true".equals(isRecurringStr));
			holiday.setDescription(description != null ? description.trim() : "");

			if (holidayDAO.update(holiday)) {
				session.setAttribute("successMsg", "Cap nhat ngay le thanh cong.");
			} else {
				session.setAttribute("errorMsg", "Cap nhat ngay le that bai.");
			}
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Du lieu khong hop le.");
		}

		response.sendRedirect(request.getContextPath() + "/holiday-list");
	}
}
