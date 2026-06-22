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

@WebServlet(name = "HolidayCreateServlet", urlPatterns = {"/holiday-create"})
public class HolidayCreateServlet extends HttpServlet {

	private final HolidayDAO holidayDAO = new HolidayDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/holiday/holiday-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		String dateStr = request.getParameter("date");
		String name = request.getParameter("name");
		String isRecurringStr = request.getParameter("isRecurring");
		String description = request.getParameter("description");

		if (dateStr == null || dateStr.isEmpty() || name == null || name.isEmpty()) {
			session.setAttribute("errorMsg", "Ngay va ten ngay le khong duoc de trong.");
			response.sendRedirect(request.getContextPath() + "/holiday-create");
			return;
		}

		try {
			LocalDate date = LocalDate.parse(dateStr);

			if (holidayDAO.existsByDate(date)) {
				session.setAttribute("errorMsg", "Ngay le nay da ton tai.");
				response.sendRedirect(request.getContextPath() + "/holiday-create");
				return;
			}

			Holiday holiday = new Holiday();
			holiday.setDate(java.sql.Date.valueOf(date));
			holiday.setName(name.trim());
			holiday.setRecurring("true".equals(isRecurringStr));
			holiday.setDescription(description != null ? description.trim() : "");

			if (holidayDAO.insert(holiday)) {
				session.setAttribute("successMsg", "Them ngay le thanh cong.");
			} else {
				session.setAttribute("errorMsg", "Them ngay le that bai.");
			}
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Du lieu khong hop le.");
		}

		response.sendRedirect(request.getContextPath() + "/holiday-list");
	}
}
