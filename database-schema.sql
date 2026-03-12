-- Skylink Media Service Database Schema
-- Run this script to create the database and tables

-- Create database (run as postgres user)
-- CREATE DATABASE skylink_media_service;

-- Connect to the database and run the following:

-- Users table (with inheritance for Admin and Contractor)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_type VARCHAR(31) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    work_order_number VARCHAR(255) UNIQUE NOT NULL,
    location VARCHAR(255) NOT NULL,
    client_code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Project assignments table
CREATE TABLE IF NOT EXISTS project_assignments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    contractor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, contractor_id)
);

-- Photos table
CREATE TABLE IF NOT EXISTS photos (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    original_name VARCHAR(255),
    file_size BIGINT,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    contractor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type);
CREATE INDEX IF NOT EXISTS idx_projects_work_order ON projects(work_order_number);
CREATE INDEX IF NOT EXISTS idx_project_assignments_project ON project_assignments(project_id);
CREATE INDEX IF NOT EXISTS idx_project_assignments_contractor ON project_assignments(contractor_id);
CREATE INDEX IF NOT EXISTS idx_photos_project ON photos(project_id);
CREATE INDEX IF NOT EXISTS idx_photos_contractor ON photos(contractor_id);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, user_type) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN')
ON CONFLICT (username) DO NOTHING;
