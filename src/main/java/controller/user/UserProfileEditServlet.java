/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.user;

import dal.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import model.User;

/**
 *
 * @author Khanh Manh
 */
public class UserProfileEditServlet extends HttpServlet {
   
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Kiểm tra xem user đã đăng nhập chưa, nếu chưa bắt quay lại trang login
        /*if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.html");
            return;
        }*/

        // Lấy lại thông tin mới nhất từ DB dựa trên ID của user trong session
        //User userDetails = userDAO.getById(currentUser.getId());
        User userDetails = userDAO.getById(Long.parseLong("1"));
        request.setAttribute("user", userDetails);

        // Forward tới file jsp tương ứng theo đường dẫn chuẩn của dự án bạn
        request.getRequestDispatcher("/views/user/profile-edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đặt encoding tránh lỗi tiếng Việt có dấu khi lưu vào MySQL
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        /*if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.html");
            return;
        }*/

        // Nhận dữ liệu text từ form gửi lên qua thẻ name=""
        String idStr = request.getParameter("id");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String dobStr = request.getParameter("dob");

        Date dob = null;
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                // Ép kiểu chuỗi ngày từ input type="date" (yyyy-MM-dd) sang java.util.Date
                dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        try {
            Long id = Long.parseLong(idStr);

            // Thực thi cập nhật xuống Database
            boolean isSuccess = userDAO.updateProfile(id, fullName, phone, dob);

            if (isSuccess) {
                // Cập nhật lại thông tin mới nhất vào session để các trang khác đồng bộ theo (ví dụ: Tên hiển thị)
                User updatedUser = userDAO.getById(id);
                session.setAttribute("user", updatedUser);
                
                // Thành công thì quay về trang hiển thị Profile chính
                response.sendRedirect(request.getContextPath() + "/profile");
            } else {
                request.setAttribute("error", "Failed to update profile changes into database. Please try again.");
                request.setAttribute("user", currentUser);
                request.getRequestDispatcher("/views/user/profile-edit.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid user identity session data!");
            request.getRequestDispatcher("/views/user/profile-edit.jsp").forward(request, response);
        }
    }
}
