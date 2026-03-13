-- Enhanced Project Fields Migration
-- Run this script to add new optional fields to projects table

-- Add new columns to projects table
ALTER TABLE projects ADD COLUMN IF NOT EXISTS ppw_number VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS work_type VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS work_details TEXT;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS client_company VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS customer VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS loan_number VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS loan_type VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS received_date DATE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS due_date DATE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS assigned_to VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS wo_admin VARCHAR(255);

-- Add indexes for commonly searched fields
CREATE INDEX IF NOT EXISTS idx_projects_ppw_number ON projects(ppw_number);
CREATE INDEX IF NOT EXISTS idx_projects_work_type ON projects(work_type);
CREATE INDEX IF NOT EXISTS idx_projects_customer ON projects(customer);
CREATE INDEX IF NOT EXISTS idx_projects_loan_number ON projects(loan_number);
CREATE INDEX IF NOT EXISTS idx_projects_due_date ON projects(due_date);
