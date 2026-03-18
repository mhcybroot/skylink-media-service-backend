-- Add PARTIAL to payment_status CHECK constraint
-- Date: 2026-03-17
-- Issue: Database constraint only allowed UNPAID and PAID, but Java enum includes PARTIAL

BEGIN;

-- Drop the old constraint
ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_payment_status_check;

-- Add new constraint with PARTIAL included
ALTER TABLE projects ADD CONSTRAINT projects_payment_status_check 
    CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID'));

-- Verify constraint was added
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'projects_payment_status_check';

COMMIT;
