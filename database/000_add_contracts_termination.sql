-- =========================================================
-- ManuHRM Iter 2 - Add contracts termination tracking columns
-- Run after 01_schema_init.sql and 02_mock_data.sql
-- =========================================================

USE manufacturing_hrm;

SET FOREIGN_KEY_CHECKS = 0;

-- Add termination tracking columns to contracts table
ALTER TABLE contracts
    ADD COLUMN terminated_at DATE NULL AFTER file_path,
    ADD COLUMN terminated_by BIGINT NULL AFTER terminated_at,
    ADD COLUMN terminate_reason TEXT NULL AFTER terminated_by;

-- Add FK for terminated_by (references users)
ALTER TABLE contracts
    ADD CONSTRAINT fk_contracts_terminated_by
        FOREIGN KEY (terminated_by) REFERENCES users(id) ON DELETE SET NULL;

SET FOREIGN_KEY_CHECKS = 1;
