-- Add profile_photo_url column to tutor_profiles
ALTER TABLE tutor_profiles ADD COLUMN IF NOT EXISTS response_time INTEGER;
ALTER TABLE tutor_profiles ADD COLUMN IF NOT EXISTS email_notifications BOOLEAN DEFAULT TRUE;
ALTER TABLE tutor_profiles ADD COLUMN IF NOT EXISTS sms_notifications BOOLEAN DEFAULT TRUE;
ALTER TABLE tutor_profiles ADD COLUMN IF NOT EXISTS lesson_reminders BOOLEAN DEFAULT TRUE;
