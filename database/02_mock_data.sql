-- =========================================================
-- ManuHRM Iter 1 + Iter 2 + Iter 3 - Mock data
-- Run after 01_schema_init.sql.
-- =========================================================

USE manufacturing_hrm;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE password_resets;
TRUNCATE TABLE holidays;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE allowance_rules;
TRUNCATE TABLE employee_dependents;
TRUNCATE TABLE allowance_types;
TRUNCATE TABLE personal_tax_brackets;
TRUNCATE TABLE personal_tax_settings;
TRUNCATE TABLE insurance_rates;
TRUNCATE TABLE payroll_settings;
TRUNCATE TABLE monthly_salary_allowances;
TRUNCATE TABLE monthly_salaries;
TRUNCATE TABLE monthly_sheet_approvals;
TRUNCATE TABLE monthly_sheets;
TRUNCATE TABLE overtime_records;
TRUNCATE TABLE attendance_corrections;
TRUNCATE TABLE attendance_records;
TRUNCATE TABLE leave_requests;
TRUNCATE TABLE leave_balances;
TRUNCATE TABLE contracts;
TRUNCATE TABLE users;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE permissions;
TRUNCATE TABLE contract_types;
TRUNCATE TABLE leave_types;
TRUNCATE TABLE job_titles;
TRUNCATE TABLE roles;
TRUNCATE TABLE departments;
SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE contracts
    MODIFY COLUMN status ENUM('ACTIVE', 'EXPIRING_SOON', 'EXPIRED', 'PENDING_RENEWAL', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE';

-- =========================================================
-- Organization master data
-- =========================================================

INSERT INTO departments (id, name, department_type, parent_id, is_active) VALUES
(1, 'Ban Giám Đốc', 'OFFICE', NULL, TRUE),
(2, 'Phòng IT', 'OFFICE', 1, TRUE),
(3, 'Phòng Nhân Sự (HR)', 'OFFICE', 1, TRUE),
(4, 'Xưởng Lắp Ráp A', 'FACTORY', 1, TRUE),
(5, 'Xưởng Lắp Ráp B', 'FACTORY', 1, TRUE),
(6, 'Xưởng Lắp Ráp C', 'FACTORY', 1, TRUE);

INSERT INTO job_titles (id, name, description, is_active) VALUES
(1, 'Giám đốc', 'Quản lý điều hành công ty', TRUE),
(2, 'IT Manager', 'Quản lý hệ thống và hạ tầng IT', TRUE),
(3, 'IT Staff', 'Nhân viên hỗ trợ hệ thống IT', TRUE),
(4, 'Trưởng phòng Nhân sự', 'Quản lý phòng nhân sự', TRUE),
(5, 'Nhân viên Nhân sự', 'Xử lý nghiệp vụ nhân sự', TRUE),
(6, 'Production Supervisor', 'Quản lý vận hành xưởng sản xuất', TRUE),
(7, 'Công nhân Lắp Ráp', 'Công nhân sản xuất trong xưởng lắp Ráp', TRUE);

-- =========================================================
-- Roles
-- =========================================================

INSERT INTO roles (id, name, display_name, description, is_system, is_active, hierarchy_level) VALUES
(1, 'SYSADMIN', 'Quản trị hệ thống', 'Quản lý tài khoản, vai trò, quyền và cấu hình hệ thống', TRUE, TRUE, 4),
(2, 'HR_MANAGER', 'Quản lý nhân sự', 'Quản lý hồ sơ nhân sự, master data, hợp đồng, payroll và báo cáo', TRUE, TRUE, 3),
(3, 'PRODUCTION_SUPERVISOR', 'Quản đốc/Tổ trưởng sản xuất', 'Lập danh sách OT, duyệt nghỉ và theo dõi công nhân dưới quyền', FALSE, TRUE, 2),
(4, 'EMPLOYEE', 'Nhân viên/Công nhân', 'Người dùng thường: xem hồ sơ, phiếu lương và gửi đơn nghỉ', FALSE, TRUE, 1);

-- =========================================================
-- Iter 1 Permissions (IDs 1-31)
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module) VALUES
(1,  'USER_VIEW',    'Xem danh sách Nhân sự',       '/user-list',       'USER'),
(2,  'USER_CREATE',  'Thêm mới Nhân sự',             '/user-create',     'USER'),
(3,  'USER_UPDATE',  'Cập nhật Hồ sơ',               '/user-update',     'USER'),
(4,  'USER_STATUS',  'Khóa/Mở tài khoản',            '/user-status',     'USER'),
(5,  'USER_DETAIL',  'Xem chi tiết Nhân sự',         '/user-detail',     'USER'),
(6,  'ROLE_VIEW',    'Xem danh sách Vai trò',        '/role-list',       'ROLE'),
(7,  'ROLE_CREATE',  'Tạo vai trò mới',              '/role-create',     'ROLE'),
(8,  'ROLE_UPDATE',  'Cập nhật Vai trò',             '/role-update',     'ROLE'),
(9,  'ROLE_STATUS',  'Kích hoạt/Vô hiệu Vai trò',    '/role-status',     'ROLE'),
(10, 'ROLE_PERM',    'Phân quyền động',              '/role-permission', 'ROLE'),
(11, 'TICKET_VIEW',  'Quản lý Ticket',                '/admin/tickets',   'TICKET'),
(12, 'DEPARTMENT_VIEW',   'Xem danh sách Phòng ban',      '/department-list',   'DEPARTMENT'),
(13, 'DEPARTMENT_CREATE', 'Thêm Phòng ban',               '/department-create', 'DEPARTMENT'),
(14, 'DEPARTMENT_UPDATE', 'Cập nhật Phòng ban',           '/department-update', 'DEPARTMENT'),
(15, 'DEPARTMENT_STATUS', 'Kích hoạt/Vô hiệu Phòng ban',  '/department-status', 'DEPARTMENT'),
(16, 'JOB_TITLE_VIEW',   'Xem danh sách Chức danh',      '/job-title-list',   'JOB_TITLE'),
(17, 'JOB_TITLE_CREATE', 'Thêm Chức danh',               '/job-title-create', 'JOB_TITLE'),
(18, 'JOB_TITLE_UPDATE', 'Cập nhật Chức danh',           '/job-title-update', 'JOB_TITLE'),
(19, 'JOB_TITLE_STATUS', 'Kích hoạt/Vô hiệu Chức danh',  '/job-title-status', 'JOB_TITLE'),
(20, 'LEAVE_TYPE_VIEW',   'Xem danh sách Loại nghỉ',      '/leave-type-list',   'LEAVE_TYPE'),
(22, 'LEAVE_TYPE_UPDATE', 'Cập nhật Loại nghỉ',           '/leave-type-update', 'LEAVE_TYPE'),
(23, 'LEAVE_TYPE_STATUS', 'Kích hoạt/Vô hiệu Loại nghỉ',  '/leave-type-status', 'LEAVE_TYPE'),
(28, 'CONTRACT_TYPE_VIEW',   'Xem danh sách Loại hợp đồng',      '/contract-type-list',   'CONTRACT_TYPE'),
(29, 'CONTRACT_TYPE_CREATE', 'Thêm Loại hợp đồng',               '/contract-type-create', 'CONTRACT_TYPE'),
(30, 'CONTRACT_TYPE_UPDATE', 'Cập nhật Loại hợp đồng',           '/contract-type-update', 'CONTRACT_TYPE'),
(31, 'CONTRACT_TYPE_STATUS', 'Kích hoạt/Vô hiệu Loại hợp đồng',  '/contract-type-status', 'CONTRACT_TYPE');

-- =========================================================
-- Iter 2 + Iter 3 Permissions (IDs 32+)
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module) VALUES
-- Contracts
(32, 'CONTRACT_VIEW',           'Xem hợp đồng',               '/contract-list',           'CONTRACT'),
(33, 'CONTRACT_DETAIL',         'Xem chi tiết hợp đồng',      '/contract-detail',         'CONTRACT'),
(34, 'CONTRACT_CREATE',         'Tạo hợp đồng',              '/contract-create',         'CONTRACT'),
(35, 'CONTRACT_UPDATE',         'Cập nhật hợp đồng',         '/contract-update',         'CONTRACT'),
(36, 'CONTRACT_RENEW',          'Ký hợp đồng mới khi gia hạn', '/contract-renew',         'CONTRACT'),
(37, 'CONTRACT_UPLOAD',         'Tải lên hợp đồng PDF',      '/contract-upload',         'CONTRACT'),
(38, 'CONTRACT_STATUS',         'Thay đổi trạng thái hợp đồng', '/contract-status',       'CONTRACT'),
(87, 'CONTRACT_RENEW_REQUEST',  'Gửi yêu cầu gia hạn hợp đồng', '/contract-renew-request', 'CONTRACT'),
(88, 'CONTRACT_MY_VIEW',        'Xem hợp đồng cá nhân',      '/my-contract',             'CONTRACT'),
-- Leave
(39, 'LEAVE_BALANCE_VIEW',      'Xem số dư phép năm',         '/leave-balance-list',      'LEAVE'),
(40, 'LEAVE_BALANCE_SETUP',     'Cài đặt quota phép năm',     '/leave-balance-setup',     'LEAVE'),
(41, 'LEAVE_REQUEST_VIEW',      'Xem tất cả đơn nghỉ',        '/leave-request-list',      'LEAVE'),
(42, 'LEAVE_MY_VIEW',           'Xem đơn nghỉ của tôi',       '/leave-request-my',        'LEAVE'),
(43, 'LEAVE_MY_CREATE',         'Gửi đơn nghỉ phép',         '/leave-request-create',    'LEAVE'),
(44, 'LEAVE_MY_CANCEL',         'Hủy đơn nghỉ của tôi',       '/leave-request-cancel',    'LEAVE'),
(45, 'LEAVE_REQUEST_APPROVE_L1','Duyệt nghỉ phép cấp 1',    '/leave-request-approve',   'LEAVE'),
(46, 'LEAVE_REQUEST_APPROVE_L2','Duyệt nghỉ phép cấp cuối',  '/leave-request-final-approve', 'LEAVE'),
(47, 'LEAVE_REQUEST_REJECT',    'Từ chối đơn nghỉ phép',     '/leave-request-reject',    'LEAVE'),
-- Attendance
(51, 'ATTENDANCE_VIEW',         'Xem chấm công tất cả',       '/attendance-list',         'ATTENDANCE'),
(52, 'ATTENDANCE_MY_VIEW',      'Xem chấm công của tôi',       '/attendance-my',           'ATTENDANCE'),
(53, 'ATTENDANCE_IMPORT',       'Nhập chấm công Excel',       '/attendance-import',       'ATTENDANCE'),
(54, 'ATTENDANCE_CORRECTION_REQUEST', 'Yêu cầu chỉnh sửa',     '/attendance-correction-request', 'ATTENDANCE'),
(55, 'ATTENDANCE_CORRECTION_APPROVE','Duyệt chỉnh sửa công',   '/attendance-correction-approve', 'ATTENDANCE'),
(56, 'ATTENDANCE_CORRECTION_REJECT', 'Từ chối chỉnh sửa công', '/attendance-correction-reject',  'ATTENDANCE'),
(57, 'ATTENDANCE_CORRECTION_VIEW', 'Xem đơn chỉnh sửa công',   '/attendance-correction-list', 'ATTENDANCE'),
-- Overtime
(58, 'OT_VIEW',                 'Xem tăng ca',                 '/overtime-list',           'OVERTIME'),
(59, 'OT_REQUEST',              'Yêu cầu tăng ca',            '/overtime-request',        'OVERTIME'),
(60, 'OT_APPROVE',              'Duyệt tăng ca',              '/overtime-approve',        'OVERTIME'),
(61, 'OT_CANCEL',               'Hủy tăng ca',                '/overtime-reject',         'OVERTIME'),
-- Salary / Payroll
(64, 'PAYROLL_VIEW',            'Xem bảng lương',             '/payroll-preview',         'PAYROLL'),
(65, 'PAYROLL_GENERATE',        'Tạo bảng lương tháng',      '/payroll-generate',        'PAYROLL'),
(66, 'PAYSLIP_VIEW',            'Xem phiếu lương',            '/payslip-view',            'PAYSLIP'),
-- Monthly Sheet
(67, 'MONTHLY_SHEET_VIEW',      'Xem bảng công tháng',        '/monthly-sheet-list',      'PAYROLL'),
(68, 'MONTHLY_SHEET_CLOSE',     'Đóng bảng công tháng',       '/monthly-sheet-close',     'PAYROLL'),
(69, 'MONTHLY_SHEET_REOPEN',    'Mở lại bảng công tháng',    '/monthly-sheet-reopen',    'PAYROLL'),
-- Reports
(70, 'REPORT_ATTENDANCE',       'Báo cáo chấm công',          '/report-attendance',       'REPORT'),
(71, 'REPORT_LEAVE',            'Báo cáo nghỉ phép',          '/report-leave',            'REPORT'),
(72, 'REPORT_HEADCOUNT',        'Báo cáo nhân sự',            '/report-headcount',        'REPORT'),
(73, 'REPORT_CONTRACT',         'Báo cáo hợp đồng',          '/report-contract',         'REPORT'),
(74, 'REPORT_PAYROLL',          'Báo cáo lương',              '/report-payroll',          'REPORT'),
-- Audit
(76, 'AUDIT_LOG_VIEW',          'Xem lịch sử hệ thống',      '/audit-log-list',          'AUDIT'),
(77, 'APPROVAL_HISTORY_VIEW',   'Xem lịch sử phê duyệt',     '/approval-history-list',   'AUDIT'),
-- Holiday Management
(78, 'HOLIDAY_VIEW',            'Xem ngày nghỉ lễ',          '/holiday-list',           'HOLIDAY'),
(79, 'HOLIDAY_CREATE',          'Tạo ngày nghỉ lễ',          '/holiday-create',         'HOLIDAY'),
(80, 'HOLIDAY_UPDATE',          'Cập nhật ngày nghỉ lễ',    '/holiday-update',         'HOLIDAY'),
(81, 'HOLIDAY_DELETE',          'Xóa ngày nghỉ lễ',          '/holiday-delete',         'HOLIDAY'),
(83, 'CONTRACT_TERMINATE',      'Chấm dứt hợp đồng',         '/contract-terminate',     'CONTRACT'),
(84, 'CONTRACT_EXPIRY',         'Xem hợp đồng hết hạn',      '/contract-expiry',        'CONTRACT'),
-- Payroll close + payroll configuration
(89, 'PAYROLL_CLOSE',           'Chốt bảng lương',           '/payroll-close',                'PAYROLL'),
(90, 'ALLOWANCE_TYPE_VIEW',     'Xem loại phụ cấp',          '/allowance-type-list',          'ALLOWANCE'),
(91, 'ALLOWANCE_TYPE_CREATE',   'Tạo loại phụ cấp',          '/allowance-type-create',        'ALLOWANCE'),
(92, 'ALLOWANCE_TYPE_UPDATE',   'Cập nhật loại phụ cấp',     '/allowance-type-update',        'ALLOWANCE'),
(93, 'ALLOWANCE_TYPE_STATUS',   'Khóa/Mở loại phụ cấp',      '/allowance-type-status',        'ALLOWANCE'),
(97, 'INSURANCE_RATE_VIEW',     'Xem mức đóng bảo hiểm',     '/insurance-rate-list',          'INSURANCE'),
(98, 'INSURANCE_RATE_SETUP',    'Thiết lập mức đóng BH',     '/insurance-rate-setup',         'INSURANCE'),
(99, 'PERSONAL_TAX_SETTING_VIEW', 'Xem giảm trừ gia cảnh',   '/personal-tax-setting-list',    'TAX'),
(100,'PERSONAL_TAX_SETTING_SETUP','Thiết lập giảm trừ GC',   '/personal-tax-setting-setup',   'TAX'),
(101,'PERSONAL_TAX_BRACKET_VIEW', 'Xem biểu thuế lũy tiến',  '/personal-tax-bracket-list',    'TAX'),
(102,'PERSONAL_TAX_BRACKET_SETUP','Thiết lập biểu thuế',     '/personal-tax-bracket-setup',   'TAX'),
(103,'EMPLOYEE_DEPENDENT_VIEW', 'Xem người phụ thuộc',       '/employee-dependent-list',      'DEPENDENT'),
(104,'EMPLOYEE_DEPENDENT_SETUP','Thiết lập người phụ thuộc', '/employee-dependent-setup',     'DEPENDENT'),
(105,'EMPLOYEE_DEPENDENT_STATUS','Khóa/Mở người phụ thuộc',  '/employee-dependent-status',    'DEPENDENT'),
-- Monthly sheet workflow + supervisor correction
(106,'MONTHLY_SHEET_SUBMIT',     'Gửi duyệt bảng công tháng',         '/monthly-sheet-submit',                'PAYROLL'),
(107,'MONTHLY_SHEET_SUPERVISOR_VIEW', 'Xem bảng công cần trưởng phòng duyệt', '/monthly-sheet-supervisor',     'PAYROLL'),
(108,'MONTHLY_SHEET_SUPERVISOR_APPROVE', 'Trưởng phòng xác nhận bảng công',   '/monthly-sheet-supervisor-approve', 'PAYROLL'),
(109,'MONTHLY_SHEET_HR_APPROVE', 'HR chốt bảng công',                 '/monthly-sheet-hr-approve',            'PAYROLL'),
(111,'ATTENDANCE_CORRECTION_SUPERVISOR_APPROVE', 'Quản đốc duyệt điều chỉnh công', '/attendance-correction-supervisor-approve', 'ATTENDANCE'),
(112,'MONTHLY_SHEET_REJECT',     'Từ chối bảng công tháng',           '/monthly-sheet-reject',                'PAYROLL'),
(113,'PAYROLL_SETTING_VIEW',     'Xem cấu hình payroll',              '/payroll-setting-list',                'PAYROLL'),
(114,'PAYROLL_SETTING_SETUP',    'Thiết lập cấu hình payroll',        '/payroll-setting-setup',               'PAYROLL'),
(115,'OT_UPDATE',                'Sửa tăng ca',                       '/overtime-edit',                       'OVERTIME'),
(116,'OT_MY_VIEW',               'Xem tăng ca của tôi',               '/my-overtime',                         'OVERTIME'),
(117,'LEAVE_TYPE_DETAIL',        'Xem chi tiết Loại nghỉ',            '/leave-type-detail',                   'LEAVE_TYPE'),
-- Ticket action URL. AuthFilter matches exact servlet paths, so this child URL
-- needs its own permission row.
(118,'TICKET_SET_PASSWORD',       'Đặt lại mật khẩu từ ticket',        '/admin/tickets/set-password',          'TICKET');

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
(1, 20), (1, 22), (1, 23),
(1, 28), (1, 29), (1, 30), (1, 31);

-- HR_MANAGER: Iter 1 master data + user management
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(2, 12), (2, 13), (2, 14), (2, 15),
(2, 16), (2, 17), (2, 18), (2, 19),
(2, 20), (2, 22), (2, 23),
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
(1, 46), (1, 47), (1, 51), (1, 52),
(1, 53), (1, 54), (1, 57), (1, 58), (1, 59),
(1, 60), (1, 61), (1, 64), (1, 65), (1, 66),
(1, 67), (1, 68), (1, 69), (1, 70), (1, 71), (1, 72), (1, 73),
(1, 74), (1, 76), (1, 77), (1, 83), (1, 84),
(1, 87), (1, 88), (1, 89), (1, 90), (1, 91), (1, 92), (1, 93),
(1, 97), (1, 98), (1, 99), (1, 100), (1, 101), (1, 102), (1, 103),
(1, 104), (1, 105), (1, 106), (1, 107), (1, 108), (1, 109), (1, 111), (1, 112),
(1, 113), (1, 114), (1, 115), (1, 117), (1, 118), -- Holiday permissions for SYSADMIN
(1, 78), (1, 79), (1, 80), (1, 81);

-- HR_MANAGER: explicit operational scope
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Contracts
(2, 32), (2, 33), (2, 34), (2, 35), (2, 36), (2, 37), (2, 38),
(2, 83), (2, 84),
-- Leave
(2, 39), (2, 40), (2, 41), (2, 45), (2, 46), (2, 47),
-- Attendance
(2, 51), (2, 52), (2, 53), (2, 54), (2, 55), (2, 56), (2, 57), (2, 111),
-- Overtime
(2, 58),
-- Salary / Payroll
(2, 64), (2, 65), (2, 66), (2, 89),
(2, 90), (2, 91), (2, 92), (2, 93),
(2, 97), (2, 98),
(2, 99), (2, 100), (2, 101), (2, 102),
(2, 103), (2, 104), (2, 105), (2, 113), (2, 114), (2, 115), (2, 117),-- Monthly Sheet
(2, 67), (2, 68), (2, 69), (2, 106), (2, 107), (2, 108), (2, 109), (2, 112),
-- Reports
(2, 70), (2, 71), (2, 72), (2, 73), (2, 74),
-- Audit permissions belong to SYSADMIN (IT Manager) only
-- (2, 76), (2, 77),
-- Holiday permissions for HR_MANAGER
(2, 78), (2, 79), (2, 80), (2, 81);

-- PRODUCTION_SUPERVISOR: exact operational scope
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 41), (3, 45), (3, 47),
(3, 51), (3, 52), (3, 54), (3, 57),
(3, 58), (3, 59), (3, 61), (3, 115),
(3, 87), (3, 107), (3, 108), (3, 111);

-- EMPLOYEE: self-service scope only
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 33), (4, 87), (4, 88),
(4, 42), (4, 43), (4, 44),
(4, 52), (4, 54), (4, 66),
(4, 116);

INSERT INTO permissions (id, code, name, url_pattern, module)
SELECT 87, 'CONTRACT_RENEW_REQUEST', 'Request contract renewal', '/contract-renew-request', 'CONTRACT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'CONTRACT_RENEW_REQUEST');

INSERT INTO permissions (id, code, name, url_pattern, module)
SELECT 88, 'CONTRACT_MY_VIEW', 'View my contract', '/my-contract', 'CONTRACT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'CONTRACT_MY_VIEW');

-- Personal contract detail + renewal request for self-service roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, 33
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 3 AND permission_id = 33);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, 87
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 3 AND permission_id = 87);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, 88
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 3 AND permission_id = 88);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, 33
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 4 AND permission_id = 33);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, 87
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 4 AND permission_id = 87);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, 88
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 4 AND permission_id = 88);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, 116
WHERE NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = 4 AND permission_id = 116);

-- =========================================================
-- Iter 1 master data
-- =========================================================

INSERT INTO leave_types
    (id, code, name, description, is_paid, salary_paid_by, is_annual_leave,
     requires_balance, base_days, max_days, has_seniority_bonus,
     seniority_interval_years, seniority_bonus_days, day_count_method, is_active)
VALUES
(1, 'ANNUAL_NORMAL', 'Nghỉ phép năm',
 'Nghỉ phép năm hưởng lương, tính hạn mức theo hợp đồng đầu tiên và thâm niên.',
 TRUE, 'COMPANY', TRUE, TRUE, 12.00, NULL, TRUE, 5, 1.00, 'WORKING_DAY', TRUE),
(2, 'SICK_LEAVE', 'Nghỉ ốm hưởng chế độ BHXH',
 'Nghỉ ốm thuộc chế độ bảo hiểm xã hội; công ty không trả lương trực tiếp và không trừ phép năm.',
 FALSE, 'SOCIAL_INSURANCE', FALSE, FALSE, NULL, NULL, FALSE, 5, 1.00, 'WORKING_DAY', TRUE),
(3, 'MARRIAGE', 'Nghỉ kết hôn',
 'Nghỉ việc riêng hưởng lương khi người lao động kết hôn, tối đa 3 ngày mỗi đơn.',
 TRUE, 'COMPANY', FALSE, FALSE, NULL, 3.00, FALSE, 5, 1.00, 'WORKING_DAY', TRUE),
(4, 'MATERNITY', 'Nghỉ thai sản',
 'Nghỉ thai sản do BHXH chi trả, tối đa 180 ngày lịch mỗi đơn.',
 FALSE, 'SOCIAL_INSURANCE', FALSE, FALSE, NULL, 180.00, FALSE, 5, 1.00, 'CALENDAR_DAY', TRUE),
(5, 'UNPAID_AGREEMENT', 'Nghỉ không lương thỏa thuận',
 'Nghỉ không hưởng lương theo thỏa thuận với quản lý, không giới hạn ngày trong hệ thống.',
 FALSE, 'NONE', FALSE, FALSE, NULL, NULL, FALSE, 5, 1.00, 'WORKING_DAY', TRUE),
(6, 'BEREAVEMENT_CLOSE', 'Nghỉ tang chế người thân gần',
 'Nghỉ việc riêng hưởng lương khi người thân gần mất, tối đa 3 ngày mỗi đơn.',
 TRUE, 'COMPANY', FALSE, FALSE, NULL, 3.00, FALSE, 5, 1.00, 'WORKING_DAY', TRUE);

INSERT INTO contract_types (id, code, name, description, is_active) VALUES
(1, 'INDEFINITE', 'Hợp đồng lao động không xác định thời hạn',
 'Hai bên không xác định thời hạn, thời điểm chấm dứt hiệu lực của hợp đồng.', TRUE),
(2, 'FIXED_TERM', 'Hợp đồng lao động xác định thời hạn',
 'Hai bên xác định thời hạn, thời điểm chấm dứt hiệu lực của hợp đồng trong khoảng từ đủ 12 tháng đến 36 tháng.', TRUE);

-- =========================================================
-- Demo users
-- Default demo password is 123456.
-- =========================================================

INSERT INTO users
(id, employee_code, username, password_hash, full_name, phone, dob, job_title_id,
 department_id, manager_id, employee_type, role_id, is_active, must_change_password)
VALUES
-- Ban Giam Doc
(1, 'GD001', 'director_minhanh', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Minh Anh', '0901000001', '1980-01-01', 1, 1, NULL, 'OFFICE', 2, TRUE, FALSE),

-- Phong IT: 1 IT Manager + 1 IT Staff, deu role SYSADMIN
(2, 'IT001', 'it_manager_khoa', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Trần Đăng Khoa', '0902000001', '1988-02-01', 2, 2, 1, 'OFFICE', 1, TRUE, FALSE),
(3, 'IT002', 'it_staff_huy', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Lê Quang Huy', '0902000002', '1995-02-02', 3, 2, 2, 'OFFICE', 1, TRUE, FALSE),

-- Phong Nhan Su: 1 HR Manager + 2 HR Staff, deu role HR_MANAGER
(4, 'HR001', 'hr_manager_lan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Lê Thị Lan', '0903000001', '1990-03-01', 4, 3, 1, 'OFFICE', 2, TRUE, FALSE),
(5, 'HR002', 'hr_staff_hoa', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Thị Hoa', '0903000002', '1996-03-02', 5, 3, 4, 'OFFICE', 2, TRUE, FALSE),
(6, 'HR003', 'hr_staff_trang', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Phạm Thu Trang', '0903000003', '1997-03-03', 5, 3, 4, 'OFFICE', 2, TRUE, FALSE),

-- Xuong Lap Rap A: 1 Production Supervisor + 2 Employee
(7, 'SX001', 'sup_a_tuan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Trần Văn Tuấn', '0904000001', '1987-04-01', 6, 4, 1, 'OFFICE', 3, TRUE, FALSE),
(8, 'CN001', 'worker_a_an', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Văn An', '0904000002', '2000-04-02', 7, 4, 7, 'WORKER', 4, TRUE, FALSE),
(9, 'CN002', 'worker_a_binh', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Phạm Thái Bình', '0904000003', '2001-04-03', 7, 4, 7, 'WORKER', 4, TRUE, FALSE),

-- Xuong Lap Rap B: 1 Production Supervisor + 2 Employee
(10, 'SX002', 'sup_b_hung', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Đỗ Mạnh Hùng', '0905000001', '1989-05-01', 6, 5, 1, 'OFFICE', 3, TRUE, FALSE),
(11, 'CN003', 'worker_b_cuong', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Võ Văn Cường', '0905000002', '2000-05-02', 7, 5, 10, 'WORKER', 4, TRUE, FALSE),
(12, 'CN004', 'worker_b_dung', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Bùi Tiến Dũng', '0905000003', '2001-05-03', 7, 5, 10, 'WORKER', 4, TRUE, FALSE),

-- Xuong Lap Rap C: 1 Production Supervisor + 2 Employee
(13, 'SX003', 'sup_c_phuc', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Hoàng Minh Phúc', '0906000001', '1990-06-01', 6, 6, 1, 'OFFICE', 3, TRUE, FALSE),
(14, 'CN005', 'worker_c_hai', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Đặng Văn Hải', '0906000002', '2000-06-02', 7, 6, 13, 'WORKER', 4, TRUE, FALSE),
(15, 'CN006', 'worker_c_kiet', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Mai Tuấn Kiệt', '0906000003', '2001-06-03', 7, 6, 13, 'WORKER', 4, TRUE, FALSE);

-- =========================================================
-- Iter 2 + Iter 3 sample data
-- =========================================================

INSERT INTO payroll_settings
    (standard_work_days, standard_work_hours_per_day, normal_overtime_rate, attendance_bonus_amount,
     effective_from, effective_to)
VALUES
    (22.00, 8.00, 1.50, 1000000.00, '2024-01-01', NULL);

INSERT INTO insurance_rates
    (social_insurance_employee_rate, health_insurance_employee_rate, unemployment_insurance_employee_rate,
     social_insurance_employer_rate, health_insurance_employer_rate, unemployment_insurance_employer_rate,
     social_health_insurance_cap, unemployment_insurance_cap, effective_from, effective_to)
VALUES
    (0.0800, 0.0150, 0.0100, 0.1750, 0.0300, 0.0100, 46800000.00, 99200000.00, '2024-01-01', NULL);

INSERT INTO personal_tax_settings
    (personal_deduction, dependent_deduction, effective_from, effective_to)
VALUES
    (15500000.00, 6200000.00, '2026-01-01', NULL);

INSERT INTO personal_tax_brackets
    (bracket_order, income_from, income_to, tax_rate, effective_from, effective_to)
VALUES
    (1,         0.00,  10000000.00, 0.0500, '2026-01-01', NULL),
    (2,  10000000.00,  30000000.00, 0.1000, '2026-01-01', NULL),
    (3,  30000000.00,  60000000.00, 0.2000, '2026-01-01', NULL),
    (4,  60000000.00, 100000000.00, 0.3000, '2026-01-01', NULL),
    (5, 100000000.00,         NULL, 0.3500, '2026-01-01', NULL);

INSERT INTO allowance_types
    (id, code, name, description, is_taxable, is_insurance_based, is_active)
VALUES
    (1, 'MEAL', 'Phụ cấp ăn trưa', 'Phụ cấp ăn ca cố định hàng tháng', FALSE, FALSE, TRUE),
    (2, 'POSITION', 'Phụ cấp vị trí', 'Phụ cấp vị trí tính vào thu nhập và căn cứ bảo hiểm', TRUE, TRUE, TRUE),
    (3, 'PHONE', 'Phụ cấp điện thoại', 'Phụ cấp điện thoại khoán cố định hàng tháng', FALSE, FALSE, TRUE),
    (4, 'HAZARD', 'Phụ cấp độc hại', 'Phụ cấp độc hại cố định cho công nhân xưởng sản xuất', FALSE, TRUE, TRUE);

INSERT INTO contracts (contract_code, user_id, contract_type_id, start_date, end_date, salary, file_path, status) VALUES
('HĐLĐ01', 1, 1, '2024-01-01', NULL, 35000000, '/contracts/director_minhanh_indefinite.pdf', 'ACTIVE'),
('HĐLĐ02', 4, 1, '2024-01-01', NULL, 28000000, '/contracts/hr_manager_lan_indefinite.pdf', 'ACTIVE'),
('HĐLĐ03', 5, 2, '2024-01-01', '2027-01-01', 16000000, '/contracts/hr_staff_hoa_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ04', 6, 2, '2024-01-01', '2027-01-01', 15500000, '/contracts/hr_staff_trang_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ05', 7, 1, '2024-01-01', NULL, 18000000, '/contracts/sup_a_tuan_indefinite.pdf', 'ACTIVE'),
('HĐLĐ06', 8, 2, '2024-01-01', '2027-01-01', 8000000, '/contracts/worker_a_an_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ07', 9, 2, '2024-01-01', '2027-01-01', 8200000, '/contracts/worker_a_binh_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ08', 10, 1, '2024-01-01', NULL, 18000000, '/contracts/sup_b_hung_indefinite.pdf', 'ACTIVE'),
('HĐLĐ09', 11, 2, '2024-01-01', '2027-01-01', 8100000, '/contracts/worker_b_cuong_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ10', 12, 2, '2024-01-01', '2027-01-01', 8000000, '/contracts/worker_b_dung_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ11', 13, 1, '2024-01-01', NULL, 18000000, '/contracts/sup_c_phuc_indefinite.pdf', 'ACTIVE'),
('HĐLĐ12', 14, 2, '2024-01-01', '2027-01-01', 8050000, '/contracts/worker_c_hai_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ13', 15, 2, '2024-01-01', '2027-01-01', 8000000, '/contracts/worker_c_kiet_fixed_term.pdf', 'ACTIVE'),
('HĐLĐ14', 2, 1, '2024-01-01', NULL, 30000000, '/contracts/it_manager_khoa_indefinite.pdf', 'ACTIVE'),
('HĐLĐ15', 3, 2, '2024-01-01', '2027-01-01', 18000000, '/contracts/it_staff_huy_fixed_term.pdf', 'ACTIVE');

INSERT INTO leave_balances (user_id, leave_type_id, year, total_days, used_days)
SELECT c.user_id,
       1 AS leave_type_id,
       2026 AS year,
       12.00 + FLOOR(TIMESTAMPDIFF(YEAR, MIN(c.start_date), '2026-12-31') / 5) AS total_days,
       0.00 AS used_days
FROM contracts c
GROUP BY c.user_id;

-- Attendance, correction, OT and payroll rows are intentionally not seeded
-- here so the demo flow can start clean from Excel import.
-- Keep one OPEN monthly sheet for 6/2026 so the HR monthly-sheet screen has
-- a visible baseline right after DB reset.

-- INSERT INTO monthly_sheets (year, month, status) VALUES
-- (2026, 6, 'OPEN');

-- Company-wide allowance rules. Attendance bonus is configured in payroll_settings.
INSERT INTO allowance_rules
    (id, allowance_type_id, apply_scope, employee_type, department_type, department_id, job_title_id,
     amount, effective_from, effective_to, is_active)
VALUES
    (1, 1, 'ALL', NULL, NULL, NULL, NULL, 500000.00, '2024-01-01', NULL, TRUE),
    (2, 3, 'EMPLOYEE_TYPE', 'OFFICE', NULL, NULL, NULL, 300000.00, '2024-01-01', NULL, TRUE),
    (3, 4, 'DEPARTMENT_TYPE', 'WORKER', 'FACTORY', NULL, NULL, 1000000.00, '2026-07-01', NULL, TRUE),
    (4, 2, 'JOB_TITLE', NULL, NULL, NULL, 1, 3000000.00, '2024-01-01', NULL, TRUE),
    (5, 2, 'JOB_TITLE', NULL, NULL, NULL, 2, 1000000.00, '2024-01-01', NULL, TRUE),
    (6, 2, 'JOB_TITLE', NULL, NULL, NULL, 3, 500000.00, '2024-01-01', NULL, TRUE),
    (7, 2, 'JOB_TITLE', NULL, NULL, NULL, 4, 1000000.00, '2024-01-01', NULL, TRUE),
    (8, 2, 'JOB_TITLE', NULL, NULL, NULL, 5, 500000.00, '2024-01-01', NULL, TRUE),
    (9, 2, 'JOB_TITLE', NULL, NULL, NULL, 6, 1000000.00, '2024-01-01', NULL, TRUE),
    (10, 2, 'JOB_TITLE', NULL, NULL, NULL, 7, 300000.00, '2024-01-01', NULL, TRUE);

INSERT INTO audit_logs (event_code, entity_type, entity_id, actor_id, actor_name, changed_fields, ip_address) VALUES
('SYSTEM_RESET', 'DATABASE', 1, 2, 'it_manager_khoa', 'Reset to Iter 3 baseline for manual attendance import and payroll demo', '127.0.0.1');

-- =========================================================
-- Holiday sample data
-- =========================================================

INSERT INTO holidays (date, name, is_recurring, is_active, description) VALUES
('2026-01-01', 'Tết Dương Lịch', TRUE, TRUE, 'Ngày Tết năm mới'),
('2026-04-30', 'Ngày Giải Phóng Miền Nam', TRUE, TRUE, 'Kỷ niệm 30/4'),
('2026-05-01', 'Ngày Lao Động Quốc Tế', TRUE, TRUE, 'Ngày 1 tháng 5'),
('2026-09-02', 'Quốc Khánh', TRUE, TRUE, 'Ngày Quốc khánh 2/9');

