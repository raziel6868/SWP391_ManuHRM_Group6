package model;

import java.sql.Timestamp;

public class PasswordTicket {
    private int id;
    private String employeeCode;
    private String fullName; // Dùng để lưu tên nhân viên khi JOIN với bảng users
    private String status;   // PENDING, RESOLVED, REJECTED
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Construtor không tham số (Default Constructor)
    public PasswordTicket() {
    }

    // Constructor đầy đủ tham số
    public PasswordTicket(int id, String employeeCode, String fullName, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- GETTERS AND SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Hàm toString() để phục vụ việc in log debug khi cần thiết
    @Override
    public String toString() {
        return "PasswordTicket{" +
                "id=" + id +
                ", employeeCode='" + employeeCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}