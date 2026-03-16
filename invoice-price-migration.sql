-- Invoice Price Enhancement Migration
-- Add invoice price tracking to projects

-- Add invoice price column with proper financial precision
ALTER TABLE projects ADD COLUMN IF NOT EXISTS invoice_price DECIMAL(10,2);

-- Add constraint for non-negative values (optional but good practice)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_invoice_price_positive') THEN
        ALTER TABLE projects ADD CONSTRAINT chk_invoice_price_positive 
            CHECK (invoice_price IS NULL OR invoice_price >= 0);
    END IF;
END $$;

-- Add index for financial reporting queries
CREATE INDEX IF NOT EXISTS idx_projects_invoice_price ON projects(invoice_price);

-- Update existing projects to have NULL invoice price (explicit)
UPDATE projects SET invoice_price = NULL WHERE invoice_price IS NULL;
