-- ============================================
-- Fix SALT column size issue
-- ============================================
-- Problem: SALT column is VARCHAR2(32) but generated salt is 44 characters
-- Solution: Increase SALT column size to VARCHAR2(64)
-- Error: ORA-12899: value too large for column "SALT" (actual: 44, maximum: 32)

-- Check current column definition (for verification)
-- SELECT column_name, data_type, data_length FROM user_tab_columns 
-- WHERE table_name = 'USER_INFO' AND column_name = 'SALT';

-- Modify SALT column to accommodate Base64 encoded salt (44 characters)
ALTER TABLE USER_INFO MODIFY SALT VARCHAR2(64);

-- Verify the change
-- SELECT column_name, data_type, data_length FROM user_tab_columns 
-- WHERE table_name = 'USER_INFO' AND column_name = 'SALT';

-- Optional: Add comment to document the change
COMMENT ON COLUMN USER_INFO.SALT IS 'Base64 encoded salt for password hashing (up to 64 characters)';

-- Note: This change is backward compatible
-- - Existing shorter salt values will still work
-- - New 44-character salts will now fit properly
-- - No data migration needed as existing data remains valid