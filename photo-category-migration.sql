-- Image Category Migration
-- Add image category column to photos table for categorizing uploads
-- Categories: BEFORE, DURING, AFTER, UNCATEGORIZED

-- Add image category column to photos table
ALTER TABLE photos ADD COLUMN IF NOT EXISTS image_category VARCHAR(20) DEFAULT 'UNCATEGORIZED';

-- Add constraint for valid categories
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_image_category') THEN
        ALTER TABLE photos ADD CONSTRAINT chk_image_category 
            CHECK (image_category IN ('BEFORE', 'DURING', 'AFTER', 'UNCATEGORIZED'));
    END IF;
END $$;

-- Create index for filtering by category
CREATE INDEX IF NOT EXISTS idx_photos_image_category ON photos(image_category);

-- Update existing photos to have default category if null
UPDATE photos SET image_category = 'UNCATEGORIZED' WHERE image_category IS NULL;
