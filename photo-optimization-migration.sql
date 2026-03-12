-- Migration script for photo optimization fields
-- Run this after the initial schema setup

-- Add optimization columns if they don't exist
ALTER TABLE photos 
ADD COLUMN IF NOT EXISTS webp_path VARCHAR(500),
ADD COLUMN IF NOT EXISTS thumbnail_path VARCHAR(500),
ADD COLUMN IF NOT EXISTS original_path VARCHAR(500),
ADD COLUMN IF NOT EXISTS is_optimized BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS optimized_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS optimization_status VARCHAR(20) DEFAULT 'PENDING';

-- Create index for cleanup queries
CREATE INDEX IF NOT EXISTS idx_photos_uploaded_at ON photos(uploaded_at);
CREATE INDEX IF NOT EXISTS idx_photos_optimization_status ON photos(optimization_status);
