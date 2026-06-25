// package controller.holiday;
//
// import dal.HolidayDAO;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;
//
// import java.io.IOException;
//
// @WebServlet(name = "HolidayDeleteServlet", urlPatterns = {"/holiday-delete"})
// public class HolidayDeleteServlet extends HttpServlet {
//
// private final HolidayDAO holidayDAO = new HolidayDAO();
//
// @Override
// protected void doPost(HttpServletRequest request, HttpServletResponse
// response)
// throws ServletException, IOException {
// HttpSession session = request.getSession();
//
// String idStr = request.getParameter("id");
// if (idStr == null || idStr.isEmpty()) {
// session.setAttribute("errorMsg", "ID khong hop le.");
// response.sendRedirect(request.getContextPath() + "/holiday-list");
// return;
// }
//
// try {
// Long id = Long.parseLong(idStr);
// if (holidayDAO.delete(id)) {
// session.setAttribute("successMsg", "Xoa ngay le thanh cong.");
// } else {
// session.setAttribute("errorMsg", "Xoa ngay le that bai.");
// }
// } catch (NumberFormatException e) {
// session.setAttribute("errorMsg", "ID khong hop le.");
// }
//
// response.sendRedirect(request.getContextPath() + "/holiday-list");
// }
// }
