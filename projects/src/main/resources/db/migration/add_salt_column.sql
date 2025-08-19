-- Database Migration Script
-- Add SALT column to USER_INFO table for password hashing

-- Add SALT column to USER_INFO table
ALTER TABLE USER_INFO ADD SALT VARCHAR2(100);

-- Add comments for new column
COMMENT ON COLUMN USER_INFO.SALT IS 'Salt for password hashing - SHA-256 with 32-byte salt';

-- Update existing records with dummy salt (existing passwords will need to be reset)
-- Note: This is for development only - in production, force password reset for all users
UPDATE USER_INFO SET SALT = 'LEGACY_MIGRATION_SALT_NEEDS_PASSWORD_RESET';

-- Commit changes
COMMIT;

-- Create backup table before migration (optional, for safety)
CREATE TABLE USER_INFO_BACKUP AS SELECT * FROM USER_INFO;

-- Verify the migration
SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE 
FROM USER_TAB_COLUMNS 
WHERE TABLE_NAME = 'USER_INFO'
ORDER BY COLUMN_ID;