CREATE DATABASE IF NOT EXISTS manufacturing_hrm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE manufacturing_hrm;

DROP TABLE IF EXISTS password_resets;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS departments;

-- ==========================================
-- 1. Tổ chức công ty
-- ==========================================
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_type ENUM('OFFICE', 'FACTORY') NOT NULL DEFAULT 'OFFICE', 
    parent_id BIGINT NULL, 
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- ==========================================
-- 2. Phân quyền động (Dynamic RBAC)
-- ==========================================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,       
    display_name VARCHAR(100) NOT NULL,     
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_system BOOLEAN DEFAULT FALSE         
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,      
    name VARCHAR(100) NOT NULL,             
    url_pattern VARCHAR(255) NOT NULL,      
    module VARCHAR(50) NOT NULL             
);

CREATE TABLE role_permissions (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ==========================================
-- 3. Nhân sự (Users)
-- ==========================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Định danh
    employee_code VARCHAR(20) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Thông tin cơ bản
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    dob DATE NULL,                   
    
    -- Thông tin nhân sự & tổ chức
    job_title VARCHAR(100) NULL,
    department_id BIGINT NULL,
    manager_id BIGINT NULL,
    employee_type ENUM('OFFICE', 'WORKER') NOT NULL DEFAULT 'OFFICE', 
    role_id BIGINT NOT NULL,
    
    -- Trạng thái
    is_active BOOLEAN DEFAULT TRUE,             
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- ==========================================
-- 4. Quên mật khẩu
-- ==========================================
CREATE TABLE password_resets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    status ENUM('PENDING', 'RESOLVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_by BIGINT NULL,  
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
);