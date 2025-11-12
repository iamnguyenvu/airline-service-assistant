-- Flyway Repair Script for V10 migration checksum and description mismatch
-- Run this script in your PostgreSQL database to fix the error

-- The issue: Database has V10 with description "update vector dimensions"
-- But the file is now "create conversation tables"
-- We need to update both description and checksum

-- Step 1: Check current state
SELECT version, description, checksum, installed_on 
FROM flyway_schema_history 
WHERE version = '10';

-- Step 2: Update V10 to match the new file "create conversation tables"
-- New checksum: 287909867 (from error message: "Resolved locally")
UPDATE flyway_schema_history 
SET 
    description = 'create conversation tables',
    checksum = 287909867,
    type = 'SQL'
WHERE version = '10';

-- Step 3: Verify the update
SELECT version, description, checksum, installed_on 
FROM flyway_schema_history 
WHERE version IN ('10', '11')
ORDER BY version;

-- Note: If V11 doesn't exist yet, Flyway will create it when you run the app
-- V11 should be "update vector dimensions" (moved from old V10)

