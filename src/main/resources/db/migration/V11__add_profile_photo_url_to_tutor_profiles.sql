-- Add profile_photo_url column to tutor_profiles
ALTER TABLE tutor_profiles ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(500);
