package controller.shiftassignment;

import dal.ShiftAssignmentDAO;
import dal.UserDAO;
import dal.ShiftDAO;
import dal.DepartmentDAO;
import model.ShiftAssignment;
import model.Shift;
import model.Department;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import util.ValidationUtil;

@WebServlet(name = "ShiftAssignmentAssignServlet", urlPatterns = {"/shift-assignment-assign"})
public class ShiftAssignmentAssignServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final UserDAO userDAO = new UserDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();
	private final DepartmentDAO departmentDAO = new DepartmentDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Department> departments = departmentDAO.getActiveDepartments();
		List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);

		request.setAttribute("departments", departments);
		request.setAttribute("shifts", shifts);

		moveFlashMessage(request);

		request.getRequestDispatcher("/views/shiftassignment/shift-assignment-assign.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		String userIdParam = request.getParameter("userId");
		String shiftIdParam = request.getParameter("shiftId");
		String dateStr = request.getParameter("date");

		StringBuilder errorMsg = new StringBuilder();

		if (ValidationUtil.isBlank(userIdParam)) {
			errorMsg.append("Nhan vien la truong bat buoc.<br>");
		}
		if (ValidationUtil.isBlank(shiftIdParam)) {
			errorMsg.append("Ca lam viec la truong bat buoc.<br>");
		}
		if (ValidationUtil.isBlank(dateStr)) {
			errorMsg.append("Ngay la truong bat buoc.<br>");
		}

		if (errorMsg.length() > 0) {
			request.setAttribute("errorMsg", errorMsg.toString());
			List<Department> departments = departmentDAO.getActiveDepartments();
			List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);
			request.setAttribute("departments", departments);
			request.setAttribute("shifts", shifts);
			request.setAttribute("selectedUserId", userIdParam);
			request.setAttribute("selectedShiftId", shiftIdParam);
			request.setAttribute("selectedDate", dateStr);
			request.getRequestDispatcher("/views/shiftassignment/shift-assignment-assign.jsp").forward(request,
					response);
			return;
		}

		Long userId = Long.parseLong(userIdParam);
		Long shiftId = Long.parseLong(shiftIdParam);
		LocalDate date;
		try {
			date = LocalDate.parse(dateStr);
		} catch (DateTimeParseException e) {
			session.setAttribute("errorMsg", "Dinh dang ngay khong hop le. Su dung YYYY-MM-DD.");
			response.sendRedirect(request.getContextPath() + "/shift-assignment-assign");
			return;
		}

		ShiftAssignment existing = shiftAssignmentDAO.getByUserAndDate(userId, Date.valueOf(date));
		boolean isUpdate = existing != null;

		if (shiftAssignmentDAO.upsert(userId, shiftId, Date.valueOf(date))) {
			if (isUpdate) {
				session.setAttribute("successMsg", "Cap nhat lich phan ca thanh cong!");
			} else {
				session.setAttribute("successMsg", "Phan ca thanh cong!");
			}
		} else {
			session.setAttribute("errorMsg", "Khong the phan ca. Vui long thu lai.");
		}

		response.sendRedirect(request.getContextPath() + "/shift-assignment-list");
	}

	private void moveFlashMessage(HttpServletRequest request) {
		jakarta.servlet.http.HttpSession session = request.getSession();
		String successMsg = (String) session.getAttribute("successMsg");
		if (successMsg != null) {
			request.setAttribute("successMsg", successMsg);
			session.removeAttribute("successMsg");
		}
		String errorMsg = (String) session.getAttribute("errorMsg");
		if (errorMsg != null) {
			request.setAttribute("errorMsg", errorMsg);
			session.removeAttribute("errorMsg");
		}
	}
}
