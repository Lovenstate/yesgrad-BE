-- V3__create_tutor_settings.sql
CREATE TABLE IF NOT EXISTS tutor_settings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    response_time INTEGER,
    email_notifications BOOLEAN DEFAULT true,
    sms_notifications BOOLEAN DEFAULT false,
    lesson_reminders BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- Add profile_photo_url column to tutor_profiles
ALTER TABLE tutor_profiles DROP COLUMN IF EXISTS response_time;
ALTER TABLE tutor_profiles DROP COLUMN IF EXISTS email_notifications;
ALTER TABLE tutor_profiles DROP COLUMN IF EXISTS sms_notifications;
ALTER TABLE tutor_profiles DROP COLUMN IF EXISTS lesson_reminders;