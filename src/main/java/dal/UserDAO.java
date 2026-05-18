package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    public List<User> searchAndFilter(
            String keyword,
            Long departmentId,
            Long roleId,
            Boolean isActive,
            int offset,
            int limit) {

        List<User> users = new ArrayList<>();

        StringBuilder sql =
                new StringBuilder(
                        """
                SELECT u.id, u.employee_code, u.username, u.full_name,
                       u.phone, u.job_title, u.employee_type, u.is_active,
                       u.department_id, u.role_id,
                       d.name  AS department_name,
                       r.name  AS role_name,
                       r.display_name AS role_display_name
                FROM users u
                LEFT JOIN departments d ON u.department_id = d.id
                LEFT JOIN roles r       ON u.role_id       = r.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (departmentId != null) {
            sql.append(" AND u.department_id = ?");
            params.add(departmentId);
        }
        if (roleId != null) {
            sql.append(" AND u.role_id = ?");
            params.add(roleId);
        }
        if (isActive != null) {
            sql.append(" AND u.is_active = ?");
            params.add(isActive);
        }

        sql.append(" ORDER BY u.id ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("UserDAO.searchAndFilter() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    public int countSearchAndFilter(
            String keyword, Long departmentId, Long roleId, Boolean isActive) {

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.employee_code LIKE ? OR u.full_name LIKE ? OR u.username LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (departmentId != null) {
            sql.append(" AND u.department_id = ?");
            params.add(departmentId);
        }
        if (roleId != null) {
            sql.append(" AND u.role_id = ?");
            params.add(roleId);
        }
        if (isActive != null) {
            sql.append(" AND u.is_active = ?");
            params.add(isActive);
        }

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("UserDAO.countSearchAndFilter() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public User getById(Long id) {
        String sql =
                """
                SELECT u.id, u.employee_code, u.username, u.full_name,
                       u.phone, u.dob, u.job_title, u.employee_type, u.is_active,
                       u.department_id, u.role_id, u.manager_id,
                       u.created_at, u.updated_at,
                       d.name         AS department_name,
                       r.name         AS role_name,
                       r.display_name AS role_display_name,
                       m.full_name    AS manager_name
                FROM users u
                LEFT JOIN departments d ON u.department_id = d.id
                LEFT JOIN roles r       ON u.role_id       = r.id
                LEFT JOIN users m       ON u.manager_id    = m.id
                WHERE u.id = ?
                """;

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapRow(rs);
                u.setDob(rs.getDate("dob"));
                u.setManagerId(rs.getLong("manager_id"));
                u.setManagerName(rs.getString("manager_name"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                u.setUpdatedAt(rs.getTimestamp("updated_at"));
                return u;
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.getById() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    public static void main(String[] args) {
        System.out.println(new UserDAO().getById(Long.parseLong("1")).getJobTitle());
    }

    public User getByUsername(String username) {
        return null;
    }

    public boolean insert(User user) {
        return false;
    }

    public boolean updateProfile(User user) {
        return false;
    }

    public boolean updatePassword(Long id, String newPasswordHash) {
        return false;
    }

    public boolean updateStatus(Long id, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.updateStatus() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmployeeCode(rs.getString("employee_code"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setJobTitle(rs.getString("job_title"));
        u.setIsActive(rs.getBoolean("is_active"));
        u.setDepartmentId(rs.getLong("department_id"));
        u.setRoleId(rs.getLong("role_id"));

        String empType = rs.getString("employee_type");
        if (empType != null) u.setEmployeeType(User.EmployeeType.valueOf(empType));

        u.setDepartmentName(rs.getString("department_name"));
        u.setRoleName(rs.getString("role_name"));
        u.setRoleDisplayName(rs.getString("role_display_name"));

        return u;
    }
    public boolean updateProfile(Long id, String fullName, String phone, java.util.Date dob) {
    String sql = "UPDATE users SET full_name = ?, phone = ?, dob = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    // Sử dụng khối bảo vệ an toàn kết nối DB
    Connection conn = DBContext.getConnection();
    if (conn == null) {
        System.err.println("UserDAO.updateProfile() CANNOT proceed because DB connection is null!");
        return false;
    }
    
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, fullName);
        ps.setString(2, phone);
        
        // Kiểm tra tránh lỗi NullPointerException nếu người dùng không chọn ngày sinh
        if (dob != null) {
            ps.setDate(3, new java.sql.Date(dob.getTime()));
        } else {
            ps.setNull(3, java.sql.Types.DATE);
        }
        
        ps.setLong(4, id);
        
        int rowsUpdated = ps.executeUpdate();
        return rowsUpdated > 0; // Trả về true nếu cập nhật thành công ít nhất 1 dòng
    } catch (SQLException e) {
        System.err.println("Error inside UserDAO.updateProfile:");
        e.printStackTrace();
    }
    return false;
}
}
