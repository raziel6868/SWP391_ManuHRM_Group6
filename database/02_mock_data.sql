-- =========================================================
-- ManuHRM Iter 1 - Mock data
-- Run after 01_schema_init.sql.
-- =========================================================

USE manufacturing_hrm;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE password_resets;
TRUNCATE TABLE users;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE permissions;
TRUNCATE TABLE contract_types;
TRUNCATE TABLE shifts;
TRUNCATE TABLE leave_types;
TRUNCATE TABLE job_titles;
TRUNCATE TABLE roles;
TRUNCATE TABLE departments;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- Organization master data
-- =========================================================

INSERT INTO departments (id, name, department_type, parent_id, is_active) VALUES
(1, 'Ban Giam Doc', 'OFFICE', NULL, TRUE),
(2, 'Phong Nhan Su (HR)', 'OFFICE', 1, TRUE),
(3, 'Phong Ke Toan', 'OFFICE', 1, TRUE),
(4, 'Xuong Lap Rap A', 'FACTORY', NULL, TRUE),
(5, 'To Cat Han (Xuong A)', 'FACTORY', 4, TRUE),
(6, 'To Lap Rap (Xuong A)', 'FACTORY', 4, TRUE);

INSERT INTO job_titles (id, name, description, is_active) VALUES
(1, 'IT System Admin', 'Quan tri he thong noi bo', TRUE),
(2, 'Truong phong Nhan su', 'Quan ly nhan su va chinh sach HR', TRUE),
(3, 'Quan doc Xuong', 'Quan ly van hanh xuong san xuat', TRUE),
(4, 'Nhan vien ke toan', 'Nhan vien van phong phong ke toan', TRUE),
(5, 'Cong nhan han', 'Cong nhan san xuat thuoc to cat han', TRUE),
(6, 'Cong nhan lap rap', 'Cong nhan san xuat thuoc to lap rap', TRUE);

-- =========================================================
-- Roles
-- role = system permission, employee_type = worker/office classification.
-- PRODUCTION_SUPERVISOR replaces old LINE_MANAGER naming.
-- =========================================================

INSERT INTO roles (id, name, display_name, description, is_system, is_active, hierarchy_level) VALUES
(1, 'SYSADMIN', 'Quan tri he thong', 'Quan ly tai khoan, role, permission va cau hinh he thong', TRUE, TRUE, 4),
(2, 'HR_MANAGER', 'Quan ly nhan su', 'Quan ly ho so nhan su, master data, hop dong, payroll va bao cao', TRUE, TRUE, 3),
(3, 'PRODUCTION_SUPERVISOR', 'Quan doc/To truong san xuat', 'Phan ca, lap danh sach OT, duyet nghi va theo doi cong nhan duoi quyen', FALSE, TRUE, 2),
(4, 'EMPLOYEE', 'Nhan vien/Cong nhan', 'Nguoi dung thuong: xem ho so, lich ca, phieu luong va gui don nghi', FALSE, TRUE, 1);

-- =========================================================
-- Permissions
-- URL patterns must match @WebServlet urlPatterns.
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module) VALUES
-- USER management
(1,  'USER_VIEW',    'Xem danh sach Nhan su',       '/user-list',       'USER'),
(2,  'USER_CREATE',  'Them moi Nhan su',             '/user-create',     'USER'),
(3,  'USER_UPDATE',  'Cap nhat Ho so',               '/user-update',     'USER'),
(4,  'USER_STATUS',  'Khoa/Mo tai khoan',            '/user-status',     'USER'),
(5,  'USER_DETAIL',  'Xem chi tiet Nhan su',         '/user-detail',     'USER'),
-- ROLE management
(6,  'ROLE_VIEW',    'Xem danh sach Vai tro',        '/role-list',       'ROLE'),
(7,  'ROLE_CREATE',  'Tao vai tro moi',              '/role-create',     'ROLE'),
(8,  'ROLE_UPDATE',  'Cap nhat Vai tro',             '/role-update',     'ROLE'),
(9,  'ROLE_STATUS',  'Kich hoat/Vo hieu Vai tro',    '/role-status',     'ROLE'),
(10, 'ROLE_PERM',    'Phan quyen dong',              '/role-permission', 'ROLE'),
-- TICKET management
(11, 'TICKET_VIEW',  'Quan ly Ticket',                '/admin/tickets',   'TICKET'),
-- DEPARTMENT master data
(12, 'DEPARTMENT_VIEW',   'Xem danh sach Phong ban',      '/department-list',   'DEPARTMENT'),
(13, 'DEPARTMENT_CREATE', 'Them Phong ban',               '/department-create', 'DEPARTMENT'),
(14, 'DEPARTMENT_UPDATE', 'Cap nhat Phong ban',           '/department-update', 'DEPARTMENT'),
(15, 'DEPARTMENT_STATUS', 'Kich hoat/Vo hieu Phong ban',  '/department-status', 'DEPARTMENT'),
-- JOB TITLE master data
(16, 'JOB_TITLE_VIEW',   'Xem danh sach Chuc danh',      '/job-title-list',   'JOB_TITLE'),
(17, 'JOB_TITLE_CREATE', 'Them Chuc danh',               '/job-title-create', 'JOB_TITLE'),
(18, 'JOB_TITLE_UPDATE', 'Cap nhat Chuc danh',           '/job-title-update', 'JOB_TITLE'),
(19, 'JOB_TITLE_STATUS', 'Kich hoat/Vo hieu Chuc danh',  '/job-title-status', 'JOB_TITLE'),
-- LEAVE TYPE master data
(20, 'LEAVE_TYPE_VIEW',   'Xem danh sach Loai nghi',      '/leave-type-list',   'LEAVE_TYPE'),
(21, 'LEAVE_TYPE_CREATE', 'Them Loai nghi',               '/leave-type-create', 'LEAVE_TYPE'),
(22, 'LEAVE_TYPE_UPDATE', 'Cap nhat Loai nghi',           '/leave-type-update', 'LEAVE_TYPE'),
(23, 'LEAVE_TYPE_STATUS', 'Kich hoat/Vo hieu Loai nghi',  '/leave-type-status', 'LEAVE_TYPE'),
-- SHIFT master data
(24, 'SHIFT_VIEW',   'Xem danh sach Ca lam',      '/shift-list',   'SHIFT'),
(25, 'SHIFT_CREATE', 'Them Ca lam',               '/shift-create', 'SHIFT'),
(26, 'SHIFT_UPDATE', 'Cap nhat Ca lam',           '/shift-update', 'SHIFT'),
(27, 'SHIFT_STATUS', 'Kich hoat/Vo hieu Ca lam',  '/shift-status', 'SHIFT'),
-- CONTRACT TYPE master data
(28, 'CONTRACT_TYPE_VIEW',   'Xem danh sach Loai hop dong',      '/contract-type-list',   'CONTRACT_TYPE'),
(29, 'CONTRACT_TYPE_CREATE', 'Them Loai hop dong',               '/contract-type-create', 'CONTRACT_TYPE'),
(30, 'CONTRACT_TYPE_UPDATE', 'Cap nhat Loai hop dong',           '/contract-type-update', 'CONTRACT_TYPE'),
(31, 'CONTRACT_TYPE_STATUS', 'Kich hoat/Vo hieu Loai hop dong',  '/contract-type-status', 'CONTRACT_TYPE');

-- =========================================================
-- Role Permissions (Dynamic RBAC)
-- =========================================================

-- SYSADMIN: full access
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- HR_MANAGER: employee management + all Iter 1 master data
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id
FROM permissions
WHERE code IN (
    'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_STATUS', 'USER_DETAIL',
    'DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_STATUS',
    'JOB_TITLE_VIEW', 'JOB_TITLE_CREATE', 'JOB_TITLE_UPDATE', 'JOB_TITLE_STATUS',
    'LEAVE_TYPE_VIEW', 'LEAVE_TYPE_CREATE', 'LEAVE_TYPE_UPDATE', 'LEAVE_TYPE_STATUS',
    'SHIFT_VIEW', 'SHIFT_CREATE', 'SHIFT_UPDATE', 'SHIFT_STATUS',
    'CONTRACT_TYPE_VIEW', 'CONTRACT_TYPE_CREATE', 'CONTRACT_TYPE_UPDATE', 'CONTRACT_TYPE_STATUS'
);

-- PRODUCTION_SUPERVISOR: only view employee list/detail in current Iter 1
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE code IN ('USER_VIEW', 'USER_DETAIL');

-- EMPLOYEE: no admin/master-data permissions in Iter 1

-- =========================================================
-- Iter 1 master data
-- =========================================================

INSERT INTO leave_types (id, code, name, description, is_paid, is_active) VALUES
(1, 'ANNUAL', 'Nghi phep nam', 'Nghi phep huong luong theo quota nam', TRUE, TRUE),
(2, 'SICK', 'Nghi benh', 'Nghi do van de suc khoe', TRUE, TRUE),
(3, 'UNPAID', 'Nghi khong luong', 'Nghi khong huong luong', FALSE, TRUE),
(4, 'OTHER', 'Nghi khac', 'Cac loai nghi khac', FALSE, TRUE);

INSERT INTO shifts (id, code, name, start_time, end_time, break_minutes, is_night_shift, is_active) VALUES
(1, 'OFFICE', 'Ca hanh chinh', '08:00:00', '17:00:00', 60, FALSE, TRUE),
(2, 'MORNING', 'Ca sang', '06:00:00', '14:00:00', 30, FALSE, TRUE),
(3, 'AFTERNOON', 'Ca chieu', '14:00:00', '22:00:00', 30, FALSE, TRUE),
(4, 'NIGHT', 'Ca dem', '22:00:00', '06:00:00', 30, TRUE, TRUE);

INSERT INTO contract_types (id, code, name, description, is_active) VALUES
(1, 'PROBATION', 'Hop dong thu viec', 'Hop dong thu viec ban dau', TRUE),
(2, 'FULL_TIME', 'Hop dong chinh thuc', 'Hop dong lao dong chinh thuc', TRUE),
(3, 'SEASONAL', 'Hop dong thoi vu', 'Hop dong theo mua vu/san luong', TRUE);

-- =========================================================
-- Demo users
-- Default demo password is usually 123456.
-- =========================================================

INSERT INTO users
(id, employee_code, username, password_hash, full_name, phone, dob, job_title_id,
 department_id, manager_id, employee_type, role_id, is_active, must_change_password)
VALUES
(1, 'AD001', 'admin', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyen Quan', '0901111111', '1990-01-01', 1, 1, NULL, 'OFFICE', 1, TRUE, FALSE),
(2, 'HR001', 'hr_lan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Le Thi Lan', '0902222222', '1992-02-02', 2, 2, 1, 'OFFICE', 2, TRUE, FALSE),
(3, 'MNG001', 'supervisor_tuan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Tran Van Tuan', '0903333333', '1988-03-03', 3, 4, 1, 'OFFICE', 3, TRUE, FALSE),
(4, 'KT001', 'accountant_mai', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Pham Thi Mai', '0904444444', '1996-04-04', 4, 3, 2, 'OFFICE', 4, TRUE, FALSE),
(5, 'CN001', 'worker_an', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyen Van An', '0905555555', '2000-05-05', 5, 5, 3, 'WORKER', 4, TRUE, FALSE),
(6, 'CN002', 'worker_binh', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Pham Thai Binh', '0906666666', '2001-06-06', 6, 6, 3, 'WORKER', 4, TRUE, FALSE);
