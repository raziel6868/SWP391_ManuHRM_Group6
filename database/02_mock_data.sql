USE manufacturing_hrm;

-- Xóa Data cũ an toàn
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE password_resets;
TRUNCATE TABLE users;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE permissions;
TRUNCATE TABLE roles;
TRUNCATE TABLE departments;
SET FOREIGN_KEY_CHECKS = 1;

-- Phòng Ban
INSERT INTO departments (id, name, department_type, parent_id, is_active) VALUES 
(1, 'Ban Giám Đốc', 'OFFICE', NULL, 1),
(2, 'Phòng Nhân Sự (HR)', 'OFFICE', 1, 1),
(3, 'Phòng Kế Toán', 'OFFICE', 1, 1),
(4, 'Xưởng Lắp Ráp A', 'FACTORY', NULL, 1),
(5, 'Tổ Cắt Hàn (Xưởng A)', 'FACTORY', 4, 1);

-- 4 Role cốt lõi
INSERT INTO roles (id, name, display_name, description, is_system, is_active) VALUES 
(1, 'SYSADMIN', 'Quản trị Hệ thống', 'Nắm toàn quyền IT, cấu hình hệ thống', 1, 1),
(2, 'HR_MANAGER', 'Quản lý Nhân sự', 'Tuyển dụng, cập nhật hồ sơ, khóa tài khoản', 1, 1),
(3, 'LINE_MANAGER', 'Quản đốc/Tổ trưởng', 'Quản lý xưởng, duyệt phép, đánh giá công nhân', 0, 1),
(4, 'EMPLOYEE', 'Nhân viên/Công nhân', 'Chỉ xem hồ sơ cá nhân và phiếu lương', 0, 1);

-- Các quyền trong hệ thống
INSERT INTO permissions (id, code, name, url_pattern, module) VALUES 
(1, 'USER_VIEW', 'Xem danh sách Nhân sự', '/admin/users', 'USER'),
(2, 'USER_CREATE', 'Thêm mới Nhân sự', '/admin/users/create', 'USER'),
(3, 'USER_UPDATE', 'Cập nhật Hồ sơ', '/admin/users/update', 'USER'),
(4, 'USER_STATUS', 'Khóa/Mở tài khoản', '/admin/users/status', 'USER'),
(5, 'ROLE_VIEW', 'Xem danh sách Vai trò', '/admin/roles', 'ROLE'),
(6, 'ROLE_UPDATE', 'Cập nhật Vai trò', '/admin/roles/update', 'ROLE'),
(7, 'ROLE_PERMISSION', 'Phân quyền Động', '/admin/roles/permissions', 'ROLE');

-- Phân quyền động (Dynamic RBAC)
-- SYSADMIN: Có đủ 7 quyền
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(1,1), (1,2), (1,3), (1,4), (1,5), (1,6), (1,7);

-- HR_MANAGER: Chỉ có quyền quản lý User (1 đến 4)
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(2,1), (2,2), (2,3), (2,4);

-- LINE_MANAGER: Chỉ được xem User (Quyền 1)
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(3,1);

-- Nhân sự (Users)
-- Mật khẩu mặc định: 123456
INSERT INTO users (id, employee_code, username, password_hash, full_name, phone, job_title, department_id, manager_id, employee_type, role_id, is_active, must_change_password) VALUES
(1, 'AD001', 'admin', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Quân Nguyễn', '0901111111', 'IT System Admin', 1, NULL, 'OFFICE', 1, 1, 0),
(2, 'HR001', 'hr_lan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Lê Thị Lan', '0902222222', 'Trưởng phòng Nhân sự', 2, 1, 'OFFICE', 2, 1, 0),
(3, 'MNG001', 'tuan_qdx', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Trần Văn Tuấn', '0903333333', 'Quản đốc Xưởng A', 4, 1, 'OFFICE', 3, 1, 0),
(4, 'CN001', 'an_cn', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Văn An', '0904444444', 'Thợ Hàn Bậc 3/7', 5, 3, 'WORKER', 4, 1, 0),
(5, 'CN002', 'binh_cn', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Phạm Thái Bình', '0905555555', 'Thợ Lắp Ráp Bậc 2/7', 4, 3, 'WORKER', 4, 1, 0);