-- Migration: Add SUPER_ADMIN to users_user_type_check constraint
-- This migration updates the check constraint to include the SUPER_ADMIN user type

-- Drop the existing check constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_user_type_check;

-- Add the new check constraint with SUPER_ADMIN included
ALTER TABLE users ADD CONSTRAINT users_user_type_check 
    CHECK (user_type IN ('ADMIN', 'CONTRACTOR', 'SUPER_ADMIN'));
