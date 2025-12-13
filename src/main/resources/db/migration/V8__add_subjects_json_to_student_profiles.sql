-- Add subjects_json column to store subject preferences as JSON array
ALTER TABLE student_profiles ADD COLUMN IF NOT EXISTS subjects_json TEXT;
