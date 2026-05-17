package controller.user;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "UserStatusServlet",
        urlPatterns = {"/user-status"})
public class UserStatusServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                        String idParam = request.getParameter("id");
        String activeParam = request.getParameter("isActive");

        if (idParam == null || activeParam == null) {
            response.sendRedirect("user-list");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect("user-list");
            return;
        }

        boolean isActive = "true".equals(activeParam);

        boolean success = userDAO.updateStatus(id, isActive);

        String referer = request.getParameter("referer");
        if (success && "detail".equals(referer)) {
            response.sendRedirect("user-detail?id=" + id);
        } else {
            response.sendRedirect("user-list");
        }
    }
}
