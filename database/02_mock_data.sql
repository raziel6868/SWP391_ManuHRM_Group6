-- =========================================================
-- ManuHRM Iter 1 + Iter 2 + Iter 3 - Mock data
-- Run after 01_schema_init.sql.
-- =========================================================

USE manufacturing_hrm;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE password_resets;
TRUNCATE TABLE holidays;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE monthly_salaries;
TRUNCATE TABLE monthly_sheet_approvals;
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

INSERT INTO departments (id, name, department_type, parent_id, is_active)
VALUES (1, 'Ban Giám Đốc', 'OFFICE', NULL, TRUE),
       (2, 'Phòng IT', 'OFFICE', 1, TRUE),
       (3, 'Phòng Nhân Sự (HR)', 'OFFICE', 1, TRUE),
       (4, 'Xưởng Lắp Ráp A', 'FACTORY', 1, TRUE),
       (5, 'Xưởng Lắp Ráp B', 'FACTORY', 1, TRUE),
       (6, 'Xưởng Lắp Ráp C', 'FACTORY', 1, TRUE);

INSERT INTO job_titles (id, name, description, is_active)
VALUES (1, 'Giám đốc', 'Quản lý điều hành công ty', TRUE),
       (2, 'IT Manager', 'Quản lý hệ thống và hạ tầng IT', TRUE),
       (3, 'IT Staff', 'Nhân viên hỗ trợ hệ thống IT', TRUE),
       (4, 'Trưởng phòng Nhân sự', 'Quản lý phòng nhân sự', TRUE),
       (5, 'Nhân viên Nhân sự', 'Xử lý nghiệp vụ nhân sự', TRUE),
       (6, 'Production Supervisor', 'Quản lý vận hành xưởng sản xuất', TRUE),
       (7, 'Công nhân Lắp Ráp', 'Công nhân sản xuất trong xưởng lắp Ráp', TRUE);

-- =========================================================
-- Roles
-- =========================================================

INSERT INTO roles (id, name, display_name, description, is_system, is_active, hierarchy_level)
VALUES (1, 'SYSADMIN', 'Quản trị hệ thống', 'Quản lý tài khoản, vai trò, quyền và cấu hình hệ thống', TRUE, TRUE, 4),
       (2, 'HR_MANAGER', 'Quản lý nhân sự', 'Quản lý hồ sơ nhân sự, master data, hợp đồng, payroll và báo cáo', TRUE,
        TRUE, 3),
       (3, 'PRODUCTION_SUPERVISOR', 'Quản đốc/Tổ trưởng sản xuất',
        'Phân ca, lập danh sách OT, duyệt nghỉ và theo dõi công nhân dưới quyền', FALSE, TRUE, 2),
       (4, 'EMPLOYEE', 'Nhân viên/Công nhân', 'Người dùng thường: xem hồ sơ, lịch ca, phiếu lương và gửi đơn nghỉ',
        FALSE, TRUE, 1);

-- =========================================================
-- Iter 1 Permissions (IDs 1-31)
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module)
VALUES (1, 'USER_VIEW', 'Xem danh sách Nhân sự', '/user-list', 'USER'),
       (2, 'USER_CREATE', 'Thêm mới Nhân sự', '/user-create', 'USER'),
       (3, 'USER_UPDATE', 'Cập nhật Hồ sơ', '/user-update', 'USER'),
       (4, 'USER_STATUS', 'Khóa/Mở tài khoản', '/user-status', 'USER'),
       (5, 'USER_DETAIL', 'Xem chi tiết Nhân sự', '/user-detail', 'USER'),
       (6, 'ROLE_VIEW', 'Xem danh sách Vai trò', '/role-list', 'ROLE'),
       (7, 'ROLE_CREATE', 'Tạo vai trò mới', '/role-create', 'ROLE'),
       (8, 'ROLE_UPDATE', 'Cập nhật Vai trò', '/role-update', 'ROLE'),
       (9, 'ROLE_STATUS', 'Kích hoạt/Vô hiệu Vai trò', '/role-status', 'ROLE'),
       (10, 'ROLE_PERM', 'Phân quyền động', '/role-permission', 'ROLE'),
       (11, 'TICKET_VIEW', 'Quản lý Ticket', '/admin/tickets', 'TICKET'),
       (12, 'DEPARTMENT_VIEW', 'Xem danh sách Phòng ban', '/department-list', 'DEPARTMENT'),
       (13, 'DEPARTMENT_CREATE', 'Thêm Phòng ban', '/department-create', 'DEPARTMENT'),
       (14, 'DEPARTMENT_UPDATE', 'Cập nhật Phòng ban', '/department-update', 'DEPARTMENT'),
       (15, 'DEPARTMENT_STATUS', 'Kích hoạt/Vô hiệu Phòng ban', '/department-status', 'DEPARTMENT'),
       (16, 'JOB_TITLE_VIEW', 'Xem danh sách Chức danh', '/job-title-list', 'JOB_TITLE'),
       (17, 'JOB_TITLE_CREATE', 'Thêm Chức danh', '/job-title-create', 'JOB_TITLE'),
       (18, 'JOB_TITLE_UPDATE', 'Cập nhật Chức danh', '/job-title-update', 'JOB_TITLE'),
       (19, 'JOB_TITLE_STATUS', 'Kích hoạt/Vô hiệu Chức danh', '/job-title-status', 'JOB_TITLE'),
       (20, 'LEAVE_TYPE_VIEW', 'Xem danh sách Loại nghỉ', '/leave-type-list', 'LEAVE_TYPE'),
       (21, 'LEAVE_TYPE_CREATE', 'Thêm Loại nghỉ', '/leave-type-create', 'LEAVE_TYPE'),
       (22, 'LEAVE_TYPE_UPDATE', 'Cập nhật Loại nghỉ', '/leave-type-update', 'LEAVE_TYPE'),
       (23, 'LEAVE_TYPE_STATUS', 'Kích hoạt/Vô hiệu Loại nghỉ', '/leave-type-status', 'LEAVE_TYPE'),
       (24, 'SHIFT_VIEW', 'Xem danh sách Ca làm', '/shift-list', 'SHIFT'),
       (25, 'SHIFT_CREATE', 'Thêm Ca làm', '/shift-create', 'SHIFT'),
       (26, 'SHIFT_UPDATE', 'Cập nhật Ca làm', '/shift-update', 'SHIFT'),
       (27, 'SHIFT_STATUS', 'Kích hoạt/Vô hiệu Ca làm', '/shift-status', 'SHIFT'),
       (28, 'CONTRACT_TYPE_VIEW', 'Xem danh sách Loại hợp đồng', '/contract-type-list', 'CONTRACT_TYPE'),
       (29, 'CONTRACT_TYPE_CREATE', 'Thêm Loại hợp đồng', '/contract-type-create', 'CONTRACT_TYPE'),
       (30, 'CONTRACT_TYPE_UPDATE', 'Cập nhật Loại hợp đồng', '/contract-type-update', 'CONTRACT_TYPE'),
       (31, 'CONTRACT_TYPE_STATUS', 'Kích hoạt/Vô hiệu Loại hợp đồng', '/contract-type-status', 'CONTRACT_TYPE');

-- =========================================================
-- Iter 2 + Iter 3 Permissions (IDs 32-86)
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module)
VALUES
-- Contracts
(32, 'CONTRACT_VIEW', 'Xem hợp đồng', '/contract-list', 'CONTRACT'),
(33, 'CONTRACT_DETAIL', 'Xem chi tiết hợp đồng', '/contract-detail', 'CONTRACT'),
(34, 'CONTRACT_CREATE', 'Tạo hợp đồng', '/contract-create', 'CONTRACT'),
(35, 'CONTRACT_UPDATE', 'Cập nhật hợp đồng', '/contract-update', 'CONTRACT'),
(36, 'CONTRACT_RENEW', 'Gia hạn hợp đồng', '/contract-renew', 'CONTRACT'),
(37, 'CONTRACT_UPLOAD', 'Tải lên hợp đồng PDF', '/contract-upload', 'CONTRACT'),
(38, 'CONTRACT_STATUS', 'Thay đổi trạng thái hợp đồng', '/contract-status', 'CONTRACT'),
-- Leave
(39, 'LEAVE_BALANCE_VIEW', 'Xem số dư phép năm', '/leave-balance-list', 'LEAVE'),
(40, 'LEAVE_BALANCE_SETUP', 'Cài đặt quota phép năm', '/leave-balance-setup', 'LEAVE'),
(41, 'LEAVE_REQUEST_VIEW', 'Xem tất cả đơn nghỉ', '/leave-request-list', 'LEAVE'),
(42, 'LEAVE_MY_VIEW', 'Xem đơn nghỉ của tôi', '/leave-request-my', 'LEAVE'),
(43, 'LEAVE_MY_CREATE', 'Gửi đơn nghỉ phép', '/leave-request-create', 'LEAVE'),
(44, 'LEAVE_MY_CANCEL', 'Hủy đơn nghỉ của tôi', '/leave-request-cancel', 'LEAVE'),
(45, 'LEAVE_REQUEST_APPROVE_L1', 'Duyệt nghỉ phép cấp 1', '/leave-request-approve', 'LEAVE'),
(46, 'LEAVE_REQUEST_APPROVE_L2', 'Duyệt nghỉ phép cấp cuối', '/leave-request-final-approve', 'LEAVE'),
(47, 'LEAVE_REQUEST_REJECT', 'Từ chối đơn nghỉ phép', '/leave-request-reject', 'LEAVE'),
-- Shift Assignment
(48, 'SHIFT_ASSIGNMENT_VIEW', 'Xem phân ca', '/shift-assignment-list', 'SHIFT'),
(49, 'SHIFT_ASSIGNMENT_ASSIGN', 'Phân ca đơn', '/shift-calendar', 'SHIFT'),
(50, 'SHIFT_ASSIGNMENT_BULK', 'Phân ca hàng loạt', '/shift-calendar-import', 'SHIFT'),
(85, 'SHIFT_CALENDAR_VIEW', 'Xem lịch ca', '/shift-calendar', 'SHIFT'),
(86, 'MY_SHIFT_VIEW', 'Xem ca làm của tôi', '/my-shift', 'SHIFT'),
-- Attendance
(51, 'ATTENDANCE_VIEW', 'Xem chấm công tất cả', '/attendance-list', 'ATTENDANCE'),
(52, 'ATTENDANCE_MY_VIEW', 'Xem chấm công của tôi', '/attendance-my', 'ATTENDANCE'),
(53, 'ATTENDANCE_IMPORT', 'Nhập chấm công Excel', '/attendance-import', 'ATTENDANCE'),
(54, 'ATTENDANCE_CORRECTION_REQUEST', 'Yêu cầu chỉnh sửa công', '/attendance-correction-request', 'ATTENDANCE'),
(55, 'ATTENDANCE_CORRECTION_APPROVE', 'Duyệt chỉnh sửa công (HR)', '/attendance-correction-approve', 'ATTENDANCE'),
(56, 'ATTENDANCE_CORRECTION_REJECT', 'Từ chối chỉnh sửa công (HR)', '/attendance-correction-reject', 'ATTENDANCE'),
(57, 'ATTENDANCE_CORRECTION_VIEW', 'Xem đơn chỉnh sửa công', '/attendance-correction-list', 'ATTENDANCE'),
-- Overtime
(58, 'OT_VIEW', 'Xem tăng ca', '/overtime-list', 'OVERTIME'),
(59, 'OT_REQUEST', 'Yêu cầu tăng ca', '/overtime-request', 'OVERTIME'),
(60, 'OT_APPROVE', 'Duyệt tăng ca', '/overtime-approve', 'OVERTIME'),
(61, 'OT_REJECT', 'Từ chối tăng ca', '/overtime-reject', 'OVERTIME'),
-- Salary / Payroll
(62, 'SALARY_BASE_VIEW', 'Xem lương cơ bản', '/salary-base-list', 'SALARY'),
(63, 'SALARY_BASE_SETUP', 'Cài đặt lương cơ bản', '/salary-base-setup', 'SALARY'),
(64, 'PAYROLL_VIEW', 'Xem bảng lương', '/payroll-preview', 'PAYROLL'),
(65, 'PAYROLL_GENERATE', 'Tạo bảng lương tháng', '/payroll-generate', 'PAYROLL'),
(66, 'PAYSLIP_VIEW', 'Xem phiếu lương', '/payslip-view', 'PAYSLIP'),
-- Monthly Sheet (cũ)
(67, 'MONTHLY_SHEET_VIEW', 'Xem bảng công tháng', '/monthly-sheet-list', 'PAYROLL'),
(68, 'MONTHLY_SHEET_CLOSE', 'Đóng bảng công tháng', '/monthly-sheet-close', 'PAYROLL'),
(69, 'MONTHLY_SHEET_REOPEN', 'Mở lại bảng công tháng', '/monthly-sheet-reopen', 'PAYROLL'),
-- Reports
(70, 'REPORT_ATTENDANCE', 'Báo cáo chấm công', '/report-attendance', 'REPORT'),
(71, 'REPORT_LEAVE', 'Báo cáo nghỉ phép', '/report-leave', 'REPORT'),
(72, 'REPORT_HEADCOUNT', 'Báo cáo nhân sự', '/report-headcount', 'REPORT'),
(73, 'REPORT_CONTRACT', 'Báo cáo hợp đồng', '/report-contract', 'REPORT'),
(74, 'REPORT_PAYROLL', 'Báo cáo lương', '/report-payroll', 'REPORT'),
(75, 'REPORT_OT', 'Báo cáo tăng ca', '/report-overtime', 'REPORT'),
-- Audit
(76, 'AUDIT_LOG_VIEW', 'Xem lịch sử hệ thống', '/audit-log-list', 'AUDIT'),
(77, 'APPROVAL_HISTORY_VIEW', 'Xem lịch sử phê duyệt', '/approval-history-list', 'AUDIT'),
-- Holiday
(78, 'HOLIDAY_VIEW', 'Xem ngày nghỉ lễ', '/holiday-list', 'HOLIDAY'),
(79, 'HOLIDAY_CREATE', 'Tạo ngày nghỉ lễ', '/holiday-create', 'HOLIDAY'),
(80, 'HOLIDAY_UPDATE', 'Cập nhật ngày nghỉ lễ', '/holiday-update', 'HOLIDAY'),
(81, 'HOLIDAY_DELETE', 'Xóa ngày nghỉ lễ', '/holiday-delete', 'HOLIDAY'),
(83, 'CONTRACT_TERMINATE', 'Chấm dứt hợp đồng', '/contract-terminate', 'CONTRACT'),
(84, 'CONTRACT_EXPIRY', 'Xem hợp đồng hết hạn', '/contract-expiry', 'CONTRACT');

-- =========================================================
-- Monthly sheet workflow permissions (IDs 87-91)
-- Ghi chú: Giám đốc dùng role HR_MANAGER + job_title_id=1
-- Code phân biệt GĐ vs HR thuần bằng authUser.getJobTitleId()
-- =========================================================

INSERT INTO permissions (id, code, name, url_pattern, module)
VALUES
-- HR gửi bảng công chờ quản đốc duyệt
(87, 'MONTHLY_SHEET_SUBMIT',
 'Gửi bảng công chờ duyệt',
 '/monthly-sheet-submit',
 'PAYROLL'),
-- Quản đốc xem bảng công cấp dưới + chốt
(88, 'MONTHLY_SHEET_SUPERVISOR_VIEW',
 'Xem bảng công (quản đốc)',
 '/monthly-sheet-supervisor',
 'PAYROLL'),
(89, 'MONTHLY_SHEET_SUPERVISOR_APPROVE',
 'Chốt bảng công (quản đốc)',
 '/monthly-sheet-supervisor-approve',
 'PAYROLL'),
-- HR chốt (sau khi tất cả quản đốc đã chốt)
(90, 'MONTHLY_SHEET_HR_APPROVE',
 'Chốt bảng công (HR)',
 '/monthly-sheet-hr-approve',
 'PAYROLL'),
-- Giám đốc chốt + đóng sổ — check thêm job_title_id=1 trong code
(91, 'MONTHLY_SHEET_DIRECTOR_APPROVE',
 'Chốt & đóng sổ bảng công (Giám đốc)',
 '/monthly-sheet-director-approve',
 'PAYROLL'),
-- Quản đốc duyệt correction bước 1
(92, 'ATTENDANCE_CORRECTION_SUPERVISOR_APPROVE',
 'Duyệt chỉnh sửa công (quản đốc)',
 '/attendance-correction-supervisor-approve',
 'ATTENDANCE');

-- =========================================================
-- Iter 1 Role Permissions
-- =========================================================

-- SYSADMIN: all Iter 1 permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),
       (1, 15),
       (1, 16),
       (1, 17),
       (1, 18),
       (1, 19),
       (1, 20),
       (1, 21),
       (1, 22),
       (1, 23),
       (1, 24),
       (1, 25),
       (1, 26),
       (1, 27),
       (1, 28),
       (1, 29),
       (1, 30),
       (1, 31);

-- HR_MANAGER: Iter 1 master data + user management
INSERT INTO role_permissions (role_id, permission_id)
VALUES (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (2, 12),
       (2, 13),
       (2, 14),
       (2, 15),
       (2, 16),
       (2, 17),
       (2, 18),
       (2, 19),
       (2, 20),
       (2, 21),
       (2, 22),
       (2, 23),
       (2, 24),
       (2, 25),
       (2, 26),
       (2, 27),
       (2, 28),
       (2, 29),
       (2, 30),
       (2, 31);

-- PRODUCTION_SUPERVISOR: Iter 1 user view only
INSERT INTO role_permissions (role_id, permission_id)
VALUES (3, 1),
       (3, 5);

-- EMPLOYEE: no Iter 1 permissions

-- =========================================================
-- Iter 2 + Iter 3 Role Permissions
-- =========================================================

-- SYSADMIN: tất cả
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1, 32),
       (1, 33),
       (1, 34),
       (1, 35),
       (1, 36),
       (1, 37),
       (1, 38),
       (1, 39),
       (1, 40),
       (1, 41),
       (1, 42),
       (1, 43),
       (1, 44),
       (1, 45),
       (1, 46),
       (1, 47),
       (1, 48),
       (1, 49),
       (1, 50),
       (1, 51),
       (1, 52),
       (1, 53),
       (1, 54),
       (1, 55),
       (1, 56),
       (1, 57),
       (1, 58),
       (1, 59),
       (1, 60),
       (1, 61),
       (1, 62),
       (1, 63),
       (1, 64),
       (1, 65),
       (1, 66),
       (1, 67),
       (1, 68),
       (1, 69),
       (1, 70),
       (1, 71),
       (1, 72),
       (1, 73),
       (1, 74),
       (1, 75),
       (1, 76),
       (1, 77),
       (1, 78),
       (1, 79),
       (1, 80),
       (1, 81),
       (1, 83),
       (1, 84),
       (1, 85),
       (1, 86),
       (1, 87),
       (1, 88),
       (1, 89),
       (1, 90),
       (1, 91),
       (1, 92);

-- HR_MANAGER (bao gồm cả Giám đốc dùng role này)
-- Giám đốc phân biệt bằng job_title_id=1 trong code
INSERT INTO role_permissions (role_id, permission_id)
VALUES
-- Contracts
(2, 32),
(2, 33),
(2, 34),
(2, 35),
(2, 36),
(2, 37),
(2, 38),
(2, 83),
(2, 84),
-- Leave
(2, 39),
(2, 40),
(2, 41),
(2, 45),
(2, 46),
(2, 47),
-- Shift Assignment
(2, 48),
(2, 49),
(2, 50),
(2, 85),
(2, 86),
-- Attendance
(2, 51),
(2, 53),
(2, 55),
(2, 56),
(2, 57),
-- Overtime
(2, 58),
(2, 60),
(2, 61),
-- Salary / Payroll
(2, 62),
(2, 63),
(2, 64),
(2, 65),
(2, 66),
-- Monthly Sheet — HR có submit + view + hr_approve
-- director_approve (91) check thêm job_title_id=1 trong servlet
(2, 67),
(2, 68),
(2, 69),
(2, 87),
(2, 90),
(2, 91),
-- Reports
(2, 70),
(2, 71),
(2, 72),
(2, 73),
(2, 74),
(2, 75),
-- Holiday
(2, 78),
(2, 79),
(2, 80),
(2, 81);

-- PRODUCTION_SUPERVISOR
INSERT INTO role_permissions (role_id, permission_id)
VALUES
-- Leave
(3, 41),
(3, 45),
(3, 47),
-- Shift
(3, 48),
(3, 85),
(3, 86),
-- Attendance
(3, 51),
(3, 52),
(3, 57),
(3, 54),
(3, 55),
(3, 56), -- correction: request + approve + reject bước 1
(3, 92), -- duyệt correction bước 1 (quản đốc)
-- Overtime
(3, 58),
(3, 59),
-- Monthly Sheet — quản đốc xem + chốt
(3, 88),
(3, 89);

-- EMPLOYEE: self-service only
INSERT INTO role_permissions (role_id, permission_id)
VALUES (4, 42),
       (4, 43),
       (4, 44), -- leave
       (4, 52),
       (4, 54),
       (4, 66), -- attendance my + correction request + payslip
       (4, 86);
-- my shift

-- =========================================================
-- Iter 1 master data
-- =========================================================

INSERT INTO leave_types (id, code, name, description, is_paid, is_active)
VALUES (1, 'ANNUAL', 'Nghỉ phép năm', 'Nghỉ phép hưởng lương theo quota năm', TRUE, TRUE),
       (2, 'SICK', 'Nghỉ bệnh', 'Nghỉ do vấn đề sức khỏe', TRUE, TRUE),
       (3, 'UNPAID', 'Nghỉ không lương', 'Nghỉ không hưởng lương', FALSE, TRUE),
       (4, 'OTHER', 'Nghỉ khác', 'Các loại nghỉ khác', FALSE, TRUE);

INSERT INTO shifts (id, code, name, start_time, end_time, break_minutes, is_night_shift, is_active)
VALUES (1, 'OFFICE', 'Ca hành chính', '08:00:00', '17:00:00', 60, FALSE, TRUE),
       (2, 'MORNING', 'Ca sáng', '06:00:00', '14:00:00', 30, FALSE, TRUE),
       (3, 'AFTERNOON', 'Ca chiều', '14:00:00', '22:00:00', 30, FALSE, TRUE),
       (4, 'NIGHT', 'Ca đêm', '22:00:00', '06:00:00', 30, TRUE, TRUE);

INSERT INTO contract_types (id, code, name, description, is_active)
VALUES (1, 'PROBATION', 'Hợp đồng thử việc', 'Hợp đồng thử việc ban đầu', TRUE),
       (2, 'FULL_TIME', 'Hợp đồng chính thức', 'Hợp đồng lao động chính thức', TRUE),
       (3, 'SEASONAL', 'Hợp đồng thời vụ', 'Hợp đồng theo mùa vụ/sản lượng', TRUE);

-- =========================================================
-- Demo users  |  Password mặc định: 123456
-- Giám đốc (GD001): role HR_MANAGER + job_title_id=1
-- Code phân biệt GĐ vs HR thuần bằng authUser.getJobTitleId() == 1
-- =========================================================

INSERT INTO users
(id, employee_code, username, password_hash, full_name, phone, dob,
 job_title_id, department_id, manager_id, employee_type, role_id, is_active, must_change_password)
VALUES
-- Ban Giám Đốc — role HR_MANAGER, job_title_id=1 → code nhận diện là Giám đốc
(1, 'GD001', 'director_minhanh', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Minh Anh',
 '0901000001', '1980-01-01', 1, 1, NULL, 'OFFICE', 2, TRUE, FALSE),

-- Phòng IT — role SYSADMIN
(2, 'IT001', 'it_manager_khoa', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Trần Đăng Khoa',
 '0902000001', '1988-02-01', 2, 2, 1, 'OFFICE', 1, TRUE, FALSE),
(3, 'IT002', 'it_staff_huy', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Lê Quang Huy',
 '0902000002', '1995-02-02', 3, 2, 2, 'OFFICE', 1, TRUE, FALSE),

-- Phòng Nhân Sự — role HR_MANAGER, job_title_id != 1 → HR thuần
(4, 'HR001', 'hr_manager_lan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Lê Thị Lan',
 '0903000001', '1990-03-01', 4, 3, 1, 'OFFICE', 2, TRUE, FALSE),
(5, 'HR002', 'hr_staff_hoa', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Thị Hoa',
 '0903000002', '1996-03-02', 5, 3, 4, 'OFFICE', 2, TRUE, FALSE),
(6, 'HR003', 'hr_staff_trang', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Phạm Thu Trang',
 '0903000003', '1997-03-03', 5, 3, 4, 'OFFICE', 2, TRUE, FALSE),

-- Xưởng A — 1 quản đốc (role 3) + 2 công nhân (role 4)
(7, 'SX001', 'sup_a_tuan', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Trần Văn Tuấn',
 '0904000001', '1987-04-01', 6, 4, 1, 'OFFICE', 3, TRUE, FALSE),
(8, 'CN001', 'worker_a_an', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Nguyễn Văn An',
 '0904000002', '2000-04-02', 7, 4, 7, 'WORKER', 4, TRUE, FALSE),
(9, 'CN002', 'worker_a_binh', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Phạm Thái Bình',
 '0904000003', '2001-04-03', 7, 4, 7, 'WORKER', 4, TRUE, FALSE),

-- Xưởng B — 1 quản đốc + 2 công nhân
(10, 'SX002', 'sup_b_hung', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Đỗ Mạnh Hùng',
 '0905000001', '1989-05-01', 6, 5, 1, 'OFFICE', 3, TRUE, FALSE),
(11, 'CN003', 'worker_b_cuong', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Võ Văn Cường',
 '0905000002', '2000-05-02', 7, 5, 10, 'WORKER', 4, TRUE, FALSE),
(12, 'CN004', 'worker_b_dung', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Bùi Tiến Dũng',
 '0905000003', '2001-05-03', 7, 5, 10, 'WORKER', 4, TRUE, FALSE),

-- Xưởng C — 1 quản đốc + 2 công nhân
(13, 'SX003', 'sup_c_phuc', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Hoàng Minh Phúc',
 '0906000001', '1990-06-01', 6, 6, 1, 'OFFICE', 3, TRUE, FALSE),
(14, 'CN005', 'worker_c_hai', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Đặng Văn Hải',
 '0906000002', '2000-06-02', 7, 6, 13, 'WORKER', 4, TRUE, FALSE),
(15, 'CN006', 'worker_c_kiet', '$2a$12$Tn1q.eN8rBFqEBGS9iqWFus7.8lAbr5dp4oI8WsKH3tpbXzJNM.Ny', 'Mai Tuấn Kiệt',
 '0906000003', '2001-06-03', 7, 6, 13, 'WORKER', 4, TRUE, FALSE);

-- =========================================================
-- Iter 2 + Iter 3 sample data
-- =========================================================

INSERT INTO contracts (user_id, contract_type_id, start_date, end_date, salary, file_path, status)
VALUES (8, 2, '2024-01-01', '2027-12-31', 8000000, '/contracts/worker_a_an_full_time.pdf', 'ACTIVE');

INSERT INTO leave_balances (user_id, leave_type_id, year, total_days, used_days)
VALUES (8, 1, 2026, 12, 2);

INSERT INTO leave_requests (user_id, leave_type_id, start_date, end_date, days, reason, status)
VALUES (8, 1, '2026-06-20', '2026-06-22', 3, 'Nghỉ phép năm định kỳ', 'PENDING');

INSERT INTO shift_assignments (user_id, shift_id, date)
VALUES (8, 2, '2026-06-15');

INSERT INTO attendance_records (user_id, date, shift_id, check_in, check_out, working_hours, status, import_batch_id)
VALUES (8, '2026-06-15', 2, '06:02:00', '14:05:00', 8.0, 'NORMAL', 'BATCH-202606');

-- attendance_corrections: thêm supervisor_status (schema mới)
INSERT INTO attendance_corrections
(attendance_record_id, requested_by, new_check_in, new_check_out, reason,
 supervisor_id, supervisor_status, status)
VALUES (1, 8, '06:00:00', '14:00:00', 'Chấm công sai giờ',
        7, 'PENDING', 'PENDING');

INSERT INTO overtime_records (user_id, date, requested_hours, reason, status)
VALUES (8, '2026-06-14', 2, 'Sản xuất tăng cường đơn hàng', 'PENDING');

INSERT INTO salary_bases (user_id, base_salary, effective_from)
VALUES (8, 8000000, '2024-01-01');

-- monthly_sheets: để OPEN để demo được workflow duyệt
INSERT INTO monthly_sheets (year, month, status)
VALUES (2026, 6, 'OPEN');

INSERT INTO monthly_salaries (monthly_sheet_id, user_id, actual_work_days, ot_hours, gross_salary, deductions,
                              net_salary, status)
VALUES (1, 8, 22, 0, 8000000, 0, 8000000, 'FINAL');

INSERT INTO audit_logs (event_code, entity_type, entity_id, actor_id, actor_name, changed_fields, ip_address)
VALUES ('SYSTEM_RESET', 'DATABASE', 1, 2, 'it_manager_khoa', 'Reset to Iter 3 baseline', '127.0.0.1');

-- =========================================================
-- Holiday sample data
-- =========================================================

INSERT INTO holidays (date, name, is_recurring, is_active, description)
VALUES ('2026-01-01', 'Tết Dương Lịch', TRUE, TRUE, 'Ngày Tết năm mới'),
       ('2026-04-30', 'Ngày Giải Phóng Miền Nam', TRUE, TRUE, 'Kỷ niệm 30/4'),
       ('2026-05-01', 'Ngày Lao Động Quốc Tế', TRUE, TRUE, 'Ngày 1 tháng 5'),
       ('2026-09-02', 'Quốc Khánh', TRUE, TRUE, 'Ngày Quốc khánh 2/9');