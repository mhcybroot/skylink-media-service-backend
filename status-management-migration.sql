-- Project Status Management Migration
-- Add status tracking fields to projects table

-- Add status columns
ALTER TABLE projects ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'UNASSIGNED';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS status_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS status_updated_by BIGINT REFERENCES users(id);

-- Create status history table for audit trail
CREATE TABLE IF NOT EXISTS project_status_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL REFERENCES users(id),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_payment_status ON projects(payment_status);
CREATE INDEX IF NOT EXISTS idx_project_status_history_project ON project_status_history(project_id);
CREATE INDEX IF NOT EXISTS idx_project_status_history_changed_at ON project_status_history(changed_at);

-- Update existing projects to have default status
UPDATE projects SET status = 'UNASSIGNED' WHERE status IS NULL;
UPDATE projects SET payment_status = 'UNPAID' WHERE payment_status IS NULL;
UPDATE projects SET status_updated_at = CURRENT_TIMESTAMP WHERE status_updated_at IS NULL;
