package model;

import java.util.Date;
import java.sql.Timestamp;

public class User {
    public enum EmployeeType {
        OFFICE, WORKER
    }

    private Long id;
    private String employeeCode;
    private String username;
    private String passwordHash;
    private String fullName;
    private String phone;
    private Date dob;
    private String jobTitle;
    private Long departmentId;
    private Long managerId;
    private EmployeeType employeeType;
    private Long roleId;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
