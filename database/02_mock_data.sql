-- =========================================================
-- ManuHRM Iter 1 + Iter 2 + Iter 3 - Mock data
-- Run after 01_schema_init.sql.
-- =========================================================

USE manufacturing_hrm;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE password_resets;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE monthly_salaries;
TRUNCATE TABLE monthly_sheets;
TRUNCATE TABLE salary_bases;
TRUNCATE TABLE overtime_records;
TRUNCATE TABLE attendance_corrections;
TRUNCATE TABLE attendance_records;
TRUNCATE TABLE shift_assignments;
TRUNCATE TABLE leave_requests;
TRUNCATE TABLE leave_balances;
TRUNCATE TABLE contracts;
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
-- =========================================================

INSERT INTO roles (id, name, display_name, description, is_system, is_active, hierarchy_level) VALUES
(1, 'SYSADMIN', 'Quan tri he thong', 'Quan ly tai khoan, role, permission va cau hinh he thong', TRUE, TRUE, 4),
(2, 'HR_MANAGER', 'Quan ly nhan su', 'Quan ly ho so nhan su, master data, hop dong, payroll va bao cao', TRUE, TRUE, 3),
(3, 'PRODUCTION_SUPERVISOR', 'Quan doc/To truong san xuat', 'Phan ca, lap danh sach OT, duyet nghi va theo doi cong nhan duoi quyen', FALSE, TRUE, 2),
(4, 'EMPLOYEE', 'Nhan vien/Cong nhan', 'Nguoi dung thuong: xem ho so, lich ca, phieu luong va gui don nghi', FALSE, TRUE, 1);

-- =========================================================
-- Iter 1 Permissions (IDs 1-31)
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module) VALUES
(1,  'USER_VIEW',    'Xem danh sach Nhan su',       '/user-list',       'USER'),
(2,  'USER_CREATE',  'Them moi Nhan su',             '/user-create',     'USER'),
(3,  'USER_UPDATE',  'Cap nhat Ho so',               '/user-update',     'USER'),
(4,  'USER_STATUS',  'Khoa/Mo tai khoan',            '/user-status',     'USER'),
(5,  'USER_DETAIL',  'Xem chi tiet Nhan su',         '/user-detail',     'USER'),
(6,  'ROLE_VIEW',    'Xem danh sach Vai tro',        '/role-list',       'ROLE'),
(7,  'ROLE_CREATE',  'Tao vai tro moi',              '/role-create',     'ROLE'),
(8,  'ROLE_UPDATE',  'Cap nhat Vai tro',             '/role-update',     'ROLE'),
(9,  'ROLE_STATUS',  'Kich hoat/Vo hieu Vai tro',    '/role-status',     'ROLE'),
(10, 'ROLE_PERM',    'Phan quyen dong',              '/role-permission', 'ROLE'),
(11, 'TICKET_VIEW',  'Quan ly Ticket',                '/admin/tickets',   'TICKET'),
(12, 'DEPARTMENT_VIEW',   'Xem danh sach Phong ban',      '/department-list',   'DEPARTMENT'),
(13, 'DEPARTMENT_CREATE', 'Them Phong ban',               '/department-create', 'DEPARTMENT'),
(14, 'DEPARTMENT_UPDATE', 'Cap nhat Phong ban',           '/department-update', 'DEPARTMENT'),
(15, 'DEPARTMENT_STATUS', 'Kich hoat/Vo hieu Phong ban',  '/department-status', 'DEPARTMENT'),
(16, 'JOB_TITLE_VIEW',   'Xem danh sach Chuc danh',      '/job-title-list',   'JOB_TITLE'),
(17, 'JOB_TITLE_CREATE', 'Them Chuc danh',               '/job-title-create', 'JOB_TITLE'),
(18, 'JOB_TITLE_UPDATE', 'Cap nhat Chuc danh',           '/job-title-update', 'JOB_TITLE'),
(19, 'JOB_TITLE_STATUS', 'Kich hoat/Vo hieu Chuc danh',  '/job-title-status', 'JOB_TITLE'),
(20, 'LEAVE_TYPE_VIEW',   'Xem danh sach Loai nghi',      '/leave-type-list',   'LEAVE_TYPE'),
(21, 'LEAVE_TYPE_CREATE', 'Them Loai nghi',               '/leave-type-create', 'LEAVE_TYPE'),
(22, 'LEAVE_TYPE_UPDATE', 'Cap nhat Loai nghi',           '/leave-type-update', 'LEAVE_TYPE'),
(23, 'LEAVE_TYPE_STATUS', 'Kich hoat/Vo hieu Loai nghi',  '/leave-type-status', 'LEAVE_TYPE'),
(24, 'SHIFT_VIEW',   'Xem danh sach Ca lam',      '/shift-list',   'SHIFT'),
(25, 'SHIFT_CREATE', 'Them Ca lam',               '/shift-create', 'SHIFT'),
(26, 'SHIFT_UPDATE', 'Cap nhat Ca lam',           '/shift-update', 'SHIFT'),
(27, 'SHIFT_STATUS', 'Kich hoat/Vo hieu Ca lam',  '/shift-status', 'SHIFT'),
(28, 'CONTRACT_TYPE_VIEW',   'Xem danh sach Loai hop dong',      '/contract-type-list',   'CONTRACT_TYPE'),
(29, 'CONTRACT_TYPE_CREATE', 'Them Loai hop dong',               '/contract-type-create', 'CONTRACT_TYPE'),
(30, 'CONTRACT_TYPE_UPDATE', 'Cap nhat Loai hop dong',           '/contract-type-update', 'CONTRACT_TYPE'),
(31, 'CONTRACT_TYPE_STATUS', 'Kich hoat/Vo hieu Loai hop dong',  '/contract-type-status', 'CONTRACT_TYPE');

-- =========================================================
-- Iter 2 + Iter 3 Permissions (IDs 32-77)
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module) VALUES
-- Contracts
(32, 'CONTRACT_VIEW',           'View contracts',           '/contract-list',           'CONTRACT'),
(33, 'CONTRACT_DETAIL',         'View contract detail',     '/contract-detail',         'CONTRACT'),
(34, 'CONTRACT_CREATE',         'Create contracts',         '/contract-create',         'CONTRACT'),
(35, 'CONTRACT_UPDATE',         'Edit contracts',           '/contract-update',         'CONTRACT'),
(36, 'CONTRACT_RENEW',          'Renew contracts',          '/contract-renew',          'CONTRACT'),
(37, 'CONTRACT_UPLOAD',         'Upload contract PDF',      '/contract-upload',         'CONTRACT'),
(38, 'CONTRACT_STATUS',         'Toggle contract status',   '/contract-status',         'CONTRACT'),
-- Leave
(39, 'LEAVE_BALANCE_VIEW',      'View leave balances',      '/leave-balance-list',      'LEAVE'),
(40, 'LEAVE_BALANCE_SETUP',     'Set/update leave quota',   '/leave-balance-setup',     'LEAVE'),
(41, 'LEAVE_REQUEST_VIEW',      'View all leave requests',  '/leave-request-list',      'LEAVE'),
(42, 'LEAVE_MY_VIEW',           'View own leave requests',  '/leave-request-my',        'LEAVE'),
(43, 'LEAVE_MY_CREATE',         'Submit leave request',     '/leave-request-create',    'LEAVE'),
(44, 'LEAVE_MY_CANCEL',         'Cancel own leave request', '/leave-request-cancel',    'LEAVE'),
(45, 'LEAVE_REQUEST_APPROVE_L1','Approve leave (Level 1)',  '/leave-request-approve',   'LEAVE'),
(46, 'LEAVE_REQUEST_APPROVE_L2','Final approve leave',      '/leave-request-final-approve', 'LEAVE'),
(47, 'LEAVE_REQUEST_REJECT',    'Reject leave request',     '/leave-request-reject',    'LEAVE'),
-- Shift Assignment
(48, 'SHIFT_ASSIGNMENT_VIEW',   'View shift assignments',   '/shift-assignment-list',   'SHIFT'),
(49, 'SHIFT_ASSIGNMENT_ASSIGN', 'Assign single shift',     '/shift-assignment-assign', 'SHIFT'),
(50, 'SHIFT_ASSIGNMENT_BULK',   'Bulk assign shifts',      '/shift-assignment-bulk',   'SHIFT'),
-- Attendance
(51, 'ATTENDANCE_VIEW',         'View all attendance',      '/attendance-list',         'ATTENDANCE'),
(52, 'ATTENDANCE_MY_VIEW',      'View own attendance',      '/attendance-my',           'ATTENDANCE'),
(53, 'ATTENDANCE_IMPORT',       'Import attendance Excel',  '/attendance-import',       'ATTENDANCE'),
(54, 'ATTENDANCE_CORRECTION_REQUEST', 'Request correction',  '/attendance-correction-request', 'ATTENDANCE'),
(55, 'ATTENDANCE_CORRECTION_APPROVE','Approve correction',   '/attendance-correction-approve', 'ATTENDANCE'),
(56, 'ATTENDANCE_CORRECTION_REJECT', 'Reject correction',   '/attendance-correction-reject',  'ATTENDANCE'),
(57, 'ATTENDANCE_CORRECTION_VIEW', 'View attendance corrections', '/attendance-correction-list', 'ATTENDANCE'),
-- Overtime
(58, 'OT_VIEW',                 'View overtime records',    '/overtime-list',           'OVERTIME'),
(59, 'OT_REQUEST',              'Request overtime',         '/overtime-request',        'OVERTIME'),
(60, 'OT_APPROVE',              'Approve overtime',         '/overtime-approve',        'OVERTIME'),
(61, 'OT_REJECT',               'Reject overtime',          '/overtime-reject',         'OVERTIME'),
-- Salary / Payroll
(62, 'SALARY_BASE_VIEW',        'View salary bases',        '/salary-base-list',        'SALARY'),
(63, 'SALARY_BASE_SETUP',       'Set/update salary base',   '/salary-base-setup',       'SALARY'),
(64, 'PAYROLL_VIEW',            'View payroll preview',     '/payroll-preview',         'PAYROLL'),
(65, 'PAYROLL_GENERATE',        'Generate monthly payroll', '/payroll-generate',        'PAYROLL'),
(66, 'PAYSLIP_VIEW',            'View payslip',             '/payslip-view',            'PAYSLIP'),
-- Monthly Sheet
(67, 'MONTHLY_SHEET_VIEW',      'View monthly sheets',      '/monthly-sheet-list',      'PAYROLL'),
(68, 'MONTHLY_SHEET_CLOSE',     'Close monthly sheet',      '/monthly-sheet-close',     'PAYROLL'),
(69, 'MONTHLY_SHEET_REOPEN',    'Reopen monthly sheet',     '/monthly-sheet-reopen',    'PAYROLL'),
-- Reports
(70, 'REPORT_ATTENDANCE',       'Attendance report',        '/report-attendance',       'REPORT'),
(71, 'REPORT_LEAVE',            'Leave report',             '/report-leave',            'REPORT'),
(72, 'REPORT_HEADCOUNT',        'Headcount report',         '/report-headcount',        'REPORT'),
(73, 'REPORT_CONTRACT',         'Contract report',          '/report-contract',         'REPORT'),
(74, 'REPORT_PAYROLL',          'Payroll report',           '/report-payroll',          'REPORT'),
(75, 'REPORT_OT',               'Overtime report',          '/report-overtime',         'REPORT'),
-- Audit
(76, 'AUDIT_LOG_VIEW',          'View audit log',           '/audit-log-list',          'AUDIT'),
(77, 'APPROVAL_HISTORY_VIEW',   'View approval history',    '/approval-history-list',   'AUDIT');

-- =========================================================
-- Iter 1 Role Permissions (Explicit)
-- =========================================================

-- SYSADMIN: all Iter 1 permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11),
(1, 12), (1, 13), (1, 14), (1, 15),
(1, 16), (1, 17), (1, 18), (1, 19),
(1, 20), (1, 21), (1, 22), (1, 23),
(1, 24), (1, 25), (1, 26), (1, 27),
(1, 28), (1, 29), (1, 30), (1, 31);

-- HR_MANAGER: Iter 1 master data + user management
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(2, 12), (2, 13), (2, 14), (2, 15),
(2, 16), (2, 17), (2, 18), (2, 19),
(2, 20), (2, 21), (2, 22), (2, 23),
(2, 24), (2, 25), (2, 26), (2, 27),
(2, 28), (2, 29), (2, 30), (2, 31);

-- PRODUCTION_SUPERVISOR: Iter 1 user view only
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 1), (3, 5);

-- EMPLOYEE: no Iter 1 permission grants

-- =========================================================
-- Iter 2 + Iter 3 Role Permissions (Explicit per-code)
-- =========================================================

-- SYSADMIN: all Iter 2/3 permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 32), (1, 33), (1, 34), (1, 35), (1, 36), (1, 37), (1, 38),
(1, 39), (1, 40), (1, 41), (1, 42), (1, 43), (1, 44), (1, 45),
(1, 46), (1, 47), (1, 48), (1, 49), (1, 50), (1, 51), (1, 52),
(1, 53), (1, 54), (1, 55), (1, 56), (1, 57), (1, 58), (1, 59),
(1, 60), (1, 61), (1, 62), (1, 63), (1, 64), (1, 65), (1, 66),
(1, 67), (1, 68), (1, 69), (1, 70), (1, 71), (1, 72), (1, 73),
(1, 74), (1, 75), (1, 76), (1, 77);

-- HR_MANAGER: explicit operational scope
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Contracts
(2, 32), (2, 33), (2, 34), (2, 35), (2, 36), (2, 37), (2, 38),
-- Leave
(2, 39), (2, 40), (2, 41), (2, 46), (2, 47),
-- Shift Assignment
(2, 48), (2, 49), (2, 50),
-- Attendance
(2, 51), (2, 53), (2, 55), (2, 56), (2, 57),
-- Overtime
(2, 58), (2, 60), (2, 61),
-- Salary / Payroll
(2, 62), (2, 63), (2, 64), (2, 65), (2, 66),
-- Monthly Sheet
(2, 67), (2, 68), (2, 69),
-- Reports
(2, 70), (2, 71), (2, 72), (2, 73), (2, 74), (2, 75),
-- Audit
(2, 76), (2, 77);

-- PRODUCTION_SUPERVISOR: exact operational scope
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 45), (3, 47),
(3, 48), (3, 51), (3, 57), (3, 55), (3, 56),
(3, 58), (3, 59);

-- EMPLOYEE: self-service scope only
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 42), (4, 43), (4, 44),
(4, 52), (4, 54), (4, 66);

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
-- Default demo password is 123456.
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

-- =========================================================
-- Iter 2 + Iter 3 sample data
-- =========================================================

INSERT INTO contracts (user_id, contract_type_id, start_date, end_date, salary, file_path, status) VALUES
(5, 2, '2024-01-01', '2027-12-31', 8000000, '/contracts/worker_an_full_time.pdf', 'ACTIVE');

INSERT INTO leave_balances (user_id, leave_type_id, year, total_days, used_days) VALUES
(5, 1, 2026, 12, 2);

INSERT INTO leave_requests (user_id, leave_type_id, start_date, end_date, days, reason, status) VALUES
(5, 1, '2026-06-20', '2026-06-22', 3, 'Nghi phep nam dinh ky', 'PENDING');

INSERT INTO shift_assignments (user_id, shift_id, date) VALUES
(5, 2, '2026-06-15');

INSERT INTO attendance_records (user_id, date, shift_id, check_in, check_out, working_hours, status, import_batch_id) VALUES
(5, '2026-06-15', 2, '06:02:00', '14:05:00', 8.0, 'NORMAL', 'BATCH-202606');

INSERT INTO attendance_corrections (attendance_record_id, requested_by, new_check_in, new_check_out, reason, status) VALUES
(1, 5, '06:00:00', '14:00:00', 'Cham cong sai gio', 'PENDING');

INSERT INTO overtime_records (user_id, date, requested_hours, reason, status) VALUES
(5, '2026-06-14', 2, 'San xuat tang cuong don hang', 'PENDING');

INSERT INTO salary_bases (user_id, base_salary, effective_from) VALUES
(5, 8000000, '2024-01-01');

-- For demo user: worker_an (user_id=5) needs salary base for payroll preview calculation.
-- EMPLOYEE role has PAYSLIP_VIEW permission (ID 64) and monthly_salaries row at line 309;
-- salary base enables end-to-end demo of payslip-view flow per Phase 6 success criteria.

INSERT INTO monthly_sheets (year, month, status, closed_at, closed_by) VALUES
(2026, 6, 'CLOSED', '2026-06-30 23:59:59', 1);

INSERT INTO monthly_salaries (monthly_sheet_id, user_id, actual_work_days, ot_hours, gross_salary, deductions, net_salary, status) VALUES
(1, 5, 22, 0, 8000000, 0, 8000000, 'FINAL');

INSERT INTO audit_logs (event_code, entity_type, entity_id, actor_id, actor_name, changed_fields, ip_address) VALUES
('SYSTEM_RESET', 'DATABASE', 1, 1, 'admin', 'Reset to Iter 3 baseline', '127.0.0.1');

