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
import java.util.List;

@WebServlet(name = "HolidayListServlet", urlPatterns = {"/holiday-list"})
public class HolidayListServlet extends HttpServlet {

	private final HolidayDAO holidayDAO = new HolidayDAO();

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
		String yearStr = request.getParameter("year");
		String pageStr = request.getParameter("page");

		int year = LocalDate.now().getYear();
		if (yearStr != null && !yearStr.isEmpty()) {
			try {
				year = Integer.parseInt(yearStr);
			} catch (NumberFormatException e) {
				year = LocalDate.now().getYear();
			}
		}

		int page = 1;
		if (pageStr != null && !pageStr.isEmpty()) {
			try {
				page = Integer.parseInt(pageStr);
			} catch (NumberFormatException e) {
				page = 1;
			}
		}
		if (page < 1) {
			page = 1;
		}

		int pageSize = 10;
		List<Holiday> allHolidays = holidayDAO.search(keyword, year);
		List<Holiday> holidays = paginate(allHolidays, page, pageSize);
		int totalPages = (int) Math.ceil((double) allHolidays.size() / pageSize);

		request.setAttribute("holidays", holidays);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("year", year);
		request.setAttribute("keyword", keyword);

		request.getRequestDispatcher("/views/holiday/holiday-list.jsp").forward(request, response);
	}

	private List<Holiday> paginate(List<Holiday> list, int page, int pageSize) {
		int fromIndex = (page - 1) * pageSize;
		if (fromIndex >= list.size()) {
			return List.of();
		}
		int toIndex = Math.min(fromIndex + pageSize, list.size());
		return list.subList(fromIndex, toIndex);
	}
}
