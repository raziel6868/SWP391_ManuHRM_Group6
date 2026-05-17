package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Department;

public class DepartmentDAO {
    /**
     * Lấy danh sách tất cả phòng ban đang hoạt động để đổ vào Dropdown chọn phòng ban.
     *
     * @return Danh sách các đối tượng Department
     */
    public List<Department> getActiveDepartments() {
        List<Department> list = new ArrayList<>();
        String sql =
                "SELECT id, name, department_type, parent_id, is_active FROM departments WHERE is_active = TRUE ORDER BY name ASC";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Department d = new Department();
                d.setId(rs.getLong("id"));
                d.setName(rs.getString("name"));
                d.setIsActive(rs.getBoolean("is_active"));
                Long parentId = rs.getLong("parent_id");
                d.setParentId(rs.wasNull() ? null : parentId);

                String type = rs.getString("department_type");
                if (type != null) {
                    d.setDepartmentType(Department.DepartmentType.valueOf(type));
                }

                list.add(d);
            }

        } catch (SQLException e) {
            System.err.println("DepartmentDAO.getActiveDepartments() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
        List<Department> list = new ArrayList<>();
        String sql =
                "SELECT id, name, department_type, parent_id, is_active FROM departments WHERE is_active = TRUE ORDER BY name ASC";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Department d = new Department();
                d.setId(rs.getLong("id"));
                d.setName(rs.getString("name"));
                d.setIsActive(rs.getBoolean("is_active"));
                Long parentId = rs.getLong("parent_id");
                d.setParentId(rs.wasNull() ? null : parentId);

                String type = rs.getString("department_type");
                if (type != null) {
                    d.setDepartmentType(Department.DepartmentType.valueOf(type));
                }

                list.add(d);
            }

        } catch (SQLException e) {
            System.err.println("DepartmentDAO.getActiveDepartments() ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
