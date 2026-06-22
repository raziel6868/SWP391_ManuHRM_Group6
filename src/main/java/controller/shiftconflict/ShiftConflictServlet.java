package controller.shiftconflict;

import dal.ShiftAssignmentDAO;
import dal.UserDAO;
import dal.ShiftDAO;
import model.ShiftAssignment;
import model.User;
import model.Shift;
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

@WebServlet(name = "ShiftConflictServlet", urlPatterns = {"/shift-conflict"})
public class ShiftConflictServlet extends HttpServlet {

	private final ShiftAssignmentDAO shiftAssignmentDAO = new ShiftAssignmentDAO();
	private final UserDAO userDAO = new UserDAO();
	private final ShiftDAO shiftDAO = new ShiftDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> users = userDAO.searchUsers(null, null, null, true, null, 0, 100);
		List<Shift> shifts = shiftDAO.searchShifts(null, null, true, 0, 100);

		request.setAttribute("users", users);
		request.setAttribute("shifts", shifts);

		String userIdParam = request.getParameter("userId");
		String startDateParam = request.getParameter("startDate");
		String endDateParam = request.getParameter("endDate");
		String shiftIdParam = request.getParameter("shiftId");

		if (userIdParam != null && !userIdParam.isBlank() && startDateParam != null && !startDateParam.isBlank()
				&& endDateParam != null && !endDateParam.isBlank()) {

			Long userId = Long.parseLong(userIdParam);
			LocalDate startDate = LocalDate.parse(startDateParam);
			LocalDate endDate = LocalDate.parse(endDateParam);
			Long shiftId = shiftIdParam != null && !shiftIdParam.isBlank() ? Long.parseLong(shiftIdParam) : null;

			List<ShiftAssignment> existingAssignments = shiftAssignmentDAO.getByUserIdAndDateRange(userId,
					Date.valueOf(startDate), Date.valueOf(endDate));

			ShiftAssignment conflict = null;
			for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
				for (ShiftAssignment sa : existingAssignments) {
					if (sa.getDate().toLocalDate().equals(date)) {
						conflict = sa;
						break;
					}
				}
				if (conflict != null) {
					break;
				}
			}

			request.setAttribute("hasConflict", conflict != null);
			request.setAttribute("conflictAssignment", conflict);
			request.setAttribute("existingAssignments", existingAssignments);
			request.setAttribute("selectedUserId", userId);
			request.setAttribute("selectedStartDate", startDateParam);
			request.setAttribute("selectedEndDate", endDateParam);
			request.setAttribute("selectedShiftId", shiftId);
		}

		request.getRequestDispatcher("/views/shiftconflict/shift-conflict.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		String userIdParam = request.getParameter("userId");
		String startDateParam = request.getParameter("startDate");
		String endDateParam = request.getParameter("endDate");
		String shiftIdParam = request.getParameter("shiftId");

		StringBuilder errorMsg = new StringBuilder();

		if (ValidationUtil.isBlank(userIdParam)) {
			errorMsg.append("Nhan vien la truong bat buoc.<br>");
		}
		if (ValidationUtil.isBlank(startDateParam)) {
			errorMsg.append("Ngay bat dau la truong bat buoc.<br>");
		}
		if (ValidationUtil.isBlank(endDateParam)) {
			errorMsg.append("Ngay ket thuc la truong bat buoc.<br>");
		}
		if (ValidationUtil.isBlank(shiftIdParam)) {
			errorMsg.append("Ca lam viec la truong bat buoc.<br>");
		}

		if (errorMsg.length() > 0) {
			session.setAttribute("errorMsg", errorMsg.toString());
			response.sendRedirect(request.getContextPath() + "/shift-conflict");
			return;
		}

		Long userId = Long.parseLong(userIdParam);
		LocalDate startDate;
		LocalDate endDate;
		Long shiftId = Long.parseLong(shiftIdParam);

		try {
			startDate = LocalDate.parse(startDateParam);
			endDate = LocalDate.parse(endDateParam);
		} catch (DateTimeParseException e) {
			session.setAttribute("errorMsg", "Dinh dang ngay khong hop le. Su dung YYYY-MM-DD.");
			response.sendRedirect(request.getContextPath() + "/shift-conflict");
			return;
		}

		if (endDate.isBefore(startDate)) {
			session.setAttribute("errorMsg", "Ngay ket thuc phai lon hon hoac bang ngay bat dau.");
			response.sendRedirect(request.getContextPath() + "/shift-conflict");
			return;
		}

		List<ShiftAssignment> existingAssignments = shiftAssignmentDAO.getByUserIdAndDateRange(userId,
				Date.valueOf(startDate), Date.valueOf(endDate));

		boolean hasConflict = false;
		ShiftAssignment conflictAssignment = null;

		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			for (ShiftAssignment sa : existingAssignments) {
				if (sa.getDate().toLocalDate().equals(date)) {
					hasConflict = true;
					conflictAssignment = sa;
					break;
				}
			}
			if (hasConflict) {
				break;
			}
		}

		if (hasConflict) {
			session.setAttribute("warningMsg", "Xung dot phat hien: Nhan vien da co lich lam viec vao ngay "
					+ conflictAssignment.getDate() + " voi ca " + conflictAssignment.getShiftName());
			response.sendRedirect(request.getContextPath() + "/shift-conflict");
			return;
		}

		int assignedCount = 0;
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			if (shiftAssignmentDAO.upsert(userId, shiftId, Date.valueOf(date))) {
				assignedCount++;
			}
		}

		session.setAttribute("successMsg", "Da gan " + assignedCount + " ngay cho nhan vien thanh cong!");
		response.sendRedirect(request.getContextPath() + "/shift-assignment-list");
	}
}
