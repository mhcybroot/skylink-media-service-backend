-- Enhanced Status Management Migration
-- Add contractor interaction tracking

-- Create project view logs table
CREATE TABLE IF NOT EXISTS project_view_logs (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    contractor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    first_viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    view_count INTEGER DEFAULT 1,
    UNIQUE(project_id, contractor_id)
);

-- Add contractor interaction fields to projects
ALTER TABLE projects ADD COLUMN IF NOT EXISTS first_opened_at TIMESTAMP;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS completed_by BIGINT REFERENCES users(id);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_project_view_logs_project ON project_view_logs(project_id);
CREATE INDEX IF NOT EXISTS idx_project_view_logs_contractor ON project_view_logs(contractor_id);
CREATE INDEX IF NOT EXISTS idx_projects_first_opened ON projects(first_opened_at);
CREATE INDEX IF NOT EXISTS idx_projects_completed ON projects(completed_at);
CREATE INDEX IF NOT EXISTS idx_projects_completed_by ON projects(completed_by);
