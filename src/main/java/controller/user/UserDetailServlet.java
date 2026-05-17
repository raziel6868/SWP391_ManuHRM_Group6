package controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import dal.UserDAO;
import model.User;

@WebServlet(name = "UserDetailServlet", urlPatterns = {"/user-detail"})
public class UserDetailServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isBlank()) {
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

        User user = userDAO.getById(id);

        if (user == null) {
            response.sendRedirect("user-list");
            return;
        }

        request.setAttribute("user", user);
        request.getRequestDispatcher("/views/user/user-detail.jsp").forward(request, response);
    }
}
