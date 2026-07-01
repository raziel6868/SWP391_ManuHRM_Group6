-- =========================================================
-- ManuHRM Iter 1 + Iter 2 + Iter 3 - Full schema init
-- =========================================================

DROP DATABASE IF EXISTS manufacturing_hrm;
CREATE DATABASE manufacturing_hrm
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE manufacturing_hrm;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS password_resets;
DROP TABLE IF EXISTS holidays;
DROP TABLE IF EXISTS monthly_salaries;
DROP TABLE IF EXISTS monthly_sheet_approvals;
DROP TABLE IF EXISTS monthly_sheets;
DROP TABLE IF EXISTS salary_bases;
DROP TABLE IF EXISTS overtime_records;
DROP TABLE IF EXISTS attendance_corrections;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS shift_assignments;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS contract_types;
DROP TABLE IF EXISTS shifts;
DROP TABLE IF EXISTS leave_types;
DROP TABLE IF EXISTS job_titles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS departments;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. Organization master data
-- =========================================================

CREATE TABLE departments
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)               NOT NULL UNIQUE,
    department_type ENUM ('OFFICE', 'FACTORY') NOT NULL DEFAULT 'OFFICE',
    parent_id       BIGINT                     NULL,
    is_active       BOOLEAN                    NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_departments_parent
        FOREIGN KEY (parent_id) REFERENCES departments (id) ON DELETE SET NULL
);

CREATE TABLE job_titles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT         NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================
-- 2. Dynamic RBAC
-- =========================================================

CREATE TABLE roles
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    description     TEXT         NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_system       BOOLEAN      NOT NULL DEFAULT FALSE,
    hierarchy_level INT          NOT NULL DEFAULT 1,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE permissions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    url_pattern VARCHAR(255) NOT NULL,
    module      VARCHAR(50)  NOT NULL
);

CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- =========================================================
-- 3. User / employee account
-- =========================================================

CREATE TABLE users
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code        VARCHAR(20)               NOT NULL UNIQUE,
    username             VARCHAR(50)               NOT NULL UNIQUE,
    password_hash        VARCHAR(255)              NOT NULL,

    full_name            VARCHAR(100)              NOT NULL,
    phone                VARCHAR(20)               NULL,
    dob                  DATE                      NULL,

    job_title_id         BIGINT                    NULL,
    department_id        BIGINT                    NULL,
    manager_id           BIGINT                    NULL,
    employee_type        ENUM ('OFFICE', 'WORKER') NOT NULL DEFAULT 'OFFICE',
    role_id              BIGINT                    NOT NULL,

    is_active            BOOLEAN                   NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN                   NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_job_title
        FOREIGN KEY (job_title_id) REFERENCES job_titles (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_manager
        FOREIGN KEY (manager_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
);

-- =========================================================
-- 4. Password reset ticket
-- =========================================================

CREATE TABLE password_resets
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT                                   NOT NULL,
    status       ENUM ('PENDING', 'RESOLVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    new_password VARCHAR(255)                             NULL,
    created_at   TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_by  BIGINT                                   NULL,
    resolved_at  TIMESTAMP                                NULL,
    CONSTRAINT fk_password_resets_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_password_resets_resolver
        FOREIGN KEY (resolved_by) REFERENCES users (id) ON DELETE SET NULL
);

-- =========================================================
-- 5. Iter 1 master data modules
-- =========================================================

CREATE TABLE leave_types
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    is_paid     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE shifts
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(30)  NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    start_time     TIME         NOT NULL,
    end_time       TIME         NOT NULL,
    break_minutes  INT          NOT NULL DEFAULT 0,
    is_night_shift BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE contract_types
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================
-- 6. Holiday management
-- =========================================================

CREATE TABLE holidays
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    date         DATE         NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    is_recurring BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    description  TEXT         NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================
-- 7. Iter 2 + Iter 3 transaction tables
-- =========================================================

CREATE TABLE contracts
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT                                                      NOT NULL,
    contract_type_id BIGINT                                                      NOT NULL,
    start_date       DATE                                                        NOT NULL,
    end_date         DATE                                                        NULL,
    salary           DECIMAL(15, 2)                                              NULL,
    file_path        VARCHAR(500)                                                NULL,
    terminated_at    DATE                                                        NULL,
    terminated_by    BIGINT                                                      NULL,
    terminate_reason TEXT                                                        NULL,
    status           ENUM ('ACTIVE', 'EXPIRED', 'PENDING_RENEWAL', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    renewal_of_id    BIGINT                                                      NULL,
    created_at       TIMESTAMP                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_contracts_renewal_of FOREIGN KEY (renewal_of_id) REFERENCES contracts (id) ON DELETE SET NULL,
    CONSTRAINT fk_contracts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_contracts_contract_type
        FOREIGN KEY (contract_type_id) REFERENCES contract_types (id) ON DELETE RESTRICT,
    CONSTRAINT fk_contracts_terminated_by
        FOREIGN KEY (terminated_by) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE leave_balances
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    leave_type_id BIGINT        NOT NULL,
    year          INT           NOT NULL,
    total_days    DECIMAL(5, 2) NOT NULL DEFAULT 0,
    used_days     DECIMAL(5, 2) NOT NULL DEFAULT 0,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_balances_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_balances_leave_type
        FOREIGN KEY (leave_type_id) REFERENCES leave_types (id) ON DELETE RESTRICT,
    CONSTRAINT uq_leave_balances_user_type_year
        UNIQUE (user_id, leave_type_id, year)
);

CREATE TABLE leave_requests
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT                                                                    NOT NULL,
    leave_type_id       BIGINT                                                                    NOT NULL,
    start_date          DATE                                                                      NOT NULL,
    end_date            DATE                                                                      NOT NULL,
    days                DECIMAL(5, 2)                                                             NOT NULL,
    reason              TEXT                                                                      NULL,
    status              ENUM ('PENDING', 'APPROVED_LEVEL_1', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    level_1_approver_id BIGINT                                                                    NULL,
    level_1_approved_at TIMESTAMP                                                                 NULL,
    approver_id         BIGINT                                                                    NULL,
    approved_at         TIMESTAMP                                                                 NULL,
    created_at          TIMESTAMP                                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP                                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_requests_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_leave_requests_leave_type
        FOREIGN KEY (leave_type_id) REFERENCES leave_types (id) ON DELETE RESTRICT,
    CONSTRAINT fk_leave_requests_level_1_approver
        FOREIGN KEY (level_1_approver_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_leave_requests_approver
        FOREIGN KEY (approver_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE shift_assignments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    shift_id   BIGINT    NOT NULL,
    date       DATE      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shift_assignments_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_shift_assignments_shift
        FOREIGN KEY (shift_id) REFERENCES shifts (id) ON DELETE RESTRICT,
    CONSTRAINT uq_shift_assignments_user_date
        UNIQUE (user_id, date)
);

CREATE TABLE attendance_records
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT                                  NOT NULL,
    date            DATE                                    NOT NULL,
    shift_id        BIGINT                                  NULL,
    check_in        TIME                                    NULL,
    check_out       TIME                                    NULL,
    working_hours   DECIMAL(5, 2)                           NULL,
    status          ENUM ('NORMAL', 'LATE', 'ABSENT', 'OT') NOT NULL DEFAULT 'NORMAL',
    import_batch_id VARCHAR(100)                            NULL,
    created_at      TIMESTAMP                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_records_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_records_shift
        FOREIGN KEY (shift_id) REFERENCES shifts (id) ON DELETE SET NULL,
    CONSTRAINT uq_attendance_records_user_date
        UNIQUE (user_id, date)
);

-- =========================================================
-- attendance_corrections — 2 bước duyệt: quản đốc → HR
-- =========================================================
CREATE TABLE attendance_corrections
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_record_id     BIGINT                                   NOT NULL,
    requested_by             BIGINT                                   NOT NULL, -- employee hoặc quản đốc tạo request

    new_check_in             TIME                                     NULL,
    new_check_out            TIME                                     NULL,
    reason                   TEXT                                     NULL,

    -- Bước 1: quản đốc duyệt
    supervisor_id            BIGINT                                   NULL,     -- quản đốc phụ trách duyệt bước 1
    supervisor_status        ENUM ('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    supervisor_approved_at   TIMESTAMP                                NULL,
    supervisor_reject_reason TEXT                                     NULL,

    -- Bước 2: HR duyệt (chỉ sau khi supervisor APPROVED)
    status                   ENUM ('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    approver_id              BIGINT                                   NULL,     -- HR duyệt bước 2
    hr_reject_reason         TEXT                                     NULL,

    created_at               TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_corrections_record
        FOREIGN KEY (attendance_record_id) REFERENCES attendance_records (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_corrections_requested_by
        FOREIGN KEY (requested_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_attendance_corrections_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_attendance_corrections_approver
        FOREIGN KEY (approver_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE overtime_records
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT                                   NOT NULL,
    date            DATE                                     NOT NULL,
    requested_hours DECIMAL(5, 2)                            NOT NULL,
    approved_hours  DECIMAL(5, 2)                            NULL,
    reason          TEXT                                     NULL,
    status          ENUM ('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    approver_id     BIGINT                                   NULL,
    approved_at     TIMESTAMP                                NULL,
    created_at      TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_overtime_records_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_overtime_records_approver
        FOREIGN KEY (approver_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE salary_bases
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    base_salary    DECIMAL(15, 2) NOT NULL,
    effective_from DATE           NOT NULL,
    effective_to   DATE           NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_salary_bases_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_salary_bases_user_effective_from
        UNIQUE (user_id, effective_from)
);

-- =========================================================
-- monthly_sheets — workflow duyệt nhiều cấp
-- =========================================================
CREATE TABLE monthly_sheets
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    year           INT       NOT NULL,
    month          INT       NOT NULL,
    status         ENUM (
        'OPEN',               -- đang mở, HR import attendance
        'PENDING_SUPERVISOR', -- HR gửi duyệt, chờ tất cả quản đốc chốt
        'PENDING_HR',         -- tất cả quản đốc đã chốt, chờ HR chốt
        'PENDING_DIRECTOR',   -- HR đã chốt, chờ giám đốc chốt
        'CLOSED'              -- giám đốc đã chốt, đóng sổ, payroll chạy được
        )                    NOT NULL DEFAULT 'OPEN',

    -- HR gửi duyệt
    submitted_by   BIGINT    NULL,
    submitted_at   TIMESTAMP NULL,

    -- HR chốt
    hr_approved_by BIGINT    NULL,
    hr_approved_at TIMESTAMP NULL,

    -- Giám đốc chốt + đóng sổ
    closed_by      BIGINT    NULL,
    closed_at      TIMESTAMP NULL,

    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_monthly_sheets_submitted_by
        FOREIGN KEY (submitted_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_monthly_sheets_hr_approved_by
        FOREIGN KEY (hr_approved_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_monthly_sheets_closed_by
        FOREIGN KEY (closed_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT uq_monthly_sheets_year_month
        UNIQUE (year, month)
);

-- =========================================================
-- monthly_sheet_approvals — trạng thái chốt của từng quản đốc
-- =========================================================
CREATE TABLE monthly_sheet_approvals
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    monthly_sheet_id BIGINT                       NOT NULL,
    supervisor_id    BIGINT                       NOT NULL, -- quản đốc được yêu cầu chốt
    status           ENUM ('PENDING', 'APPROVED') NOT NULL DEFAULT 'PENDING',
    approved_at      TIMESTAMP                    NULL,
    created_at       TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_msa_sheet
        FOREIGN KEY (monthly_sheet_id) REFERENCES monthly_sheets (id) ON DELETE CASCADE,
    CONSTRAINT fk_msa_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uq_msa_sheet_supervisor
        UNIQUE (monthly_sheet_id, supervisor_id)
);

CREATE TABLE monthly_salaries
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    monthly_sheet_id BIGINT                          NOT NULL,
    user_id          BIGINT                          NOT NULL,
    actual_work_days DECIMAL(5, 2)                   NOT NULL DEFAULT 0,
    ot_hours         DECIMAL(5, 2)                   NOT NULL DEFAULT 0,
    gross_salary     DECIMAL(15, 2)                  NOT NULL DEFAULT 0,
    deductions       DECIMAL(15, 2)                  NOT NULL DEFAULT 0,
    net_salary       DECIMAL(15, 2)                  NOT NULL DEFAULT 0,
    status           ENUM ('DRAFT', 'FINAL', 'PAID') NOT NULL DEFAULT 'DRAFT',
    created_at       TIMESTAMP                       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP                       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_monthly_salaries_sheet
        FOREIGN KEY (monthly_sheet_id) REFERENCES monthly_sheets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_monthly_salaries_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE audit_logs
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_code     VARCHAR(100) NOT NULL,
    entity_type    VARCHAR(100) NOT NULL,
    entity_id      BIGINT       NULL,
    actor_id       BIGINT       NOT NULL,
    actor_name     VARCHAR(100) NOT NULL,
    changed_fields TEXT         NULL,
    ip_address     VARCHAR(45)  NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE RESTRICT
);