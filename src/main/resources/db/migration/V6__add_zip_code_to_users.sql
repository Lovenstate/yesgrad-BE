-- Add zip_code column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS zip_code VARCHAR(20);

-- Create index on zip_code for location-based searches
CREATE INDEX IF NOT EXISTS idx_users_zip_code ON users(zip_code);
