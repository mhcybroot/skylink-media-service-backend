-- Migration to add metadata_json column to photos table
ALTER TABLE photos ADD COLUMN IF NOT EXISTS metadata_json TEXT;
