-- Add onboarding fields to student_profiles table
ALTER TABLE student_profiles 
ADD COLUMN IF NOT EXISTS budget_min DECIMAL(10, 2),
ADD COLUMN IF NOT EXISTS budget_max DECIMAL(10, 2),
ADD COLUMN IF NOT EXISTS lesson_format VARCHAR(20),
ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN DEFAULT false;

-- Create student_subjects table for subject preferences
CREATE TABLE IF NOT EXISTS student_subjects (
    id BIGSERIAL PRIMARY KEY,
    student_profile_id BIGINT NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_profile_id, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_student_subjects_student ON student_subjects(student_profile_id);
CREATE INDEX IF NOT EXISTS idx_student_subjects_subject ON student_subjects(subject_id);
