-- Advanced Search Performance Indexes
-- Created: 2026-03-17
-- Purpose: Optimize advanced search queries on admin dashboard
-- Status: OPTIONAL (Recommended for production with 100+ projects)

-- Add indexes for filter fields
CREATE INDEX IF NOT EXISTS idx_projects_status 
ON projects(status);

CREATE INDEX IF NOT EXISTS idx_projects_payment_status 
ON projects(payment_status);

CREATE INDEX IF NOT EXISTS idx_projects_due_date 
ON projects(due_date);

CREATE INDEX IF NOT EXISTS idx_projects_invoice_price 
ON projects(invoice_price);

CREATE INDEX IF NOT EXISTS idx_project_assignments_contractor 
ON project_assignments(contractor_id);

-- Composite index for common filter combinations
CREATE INDEX IF NOT EXISTS idx_projects_status_payment 
ON projects(status, payment_status);

-- Analyze tables for query optimization
ANALYZE projects;
ANALYZE project_assignments;

-- Verify indexes were created
SELECT 
    tablename, 
    indexname, 
    indexdef 
FROM pg_indexes 
WHERE tablename IN ('projects', 'project_assignments')
ORDER BY tablename, indexname;
