-- Create tutor_subjects junction table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS tutor_subjects (
    id BIGSERIAL PRIMARY KEY,
    tutor_profile_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    proficiency_level VARCHAR(20) DEFAULT 'INTERMEDIATE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tutor_profile_id, subject_id)
);

-- Create indexes for faster lookups
CREATE INDEX idx_tutor_subjects_tutor ON tutor_subjects(tutor_profile_id);
CREATE INDEX idx_tutor_subjects_subject ON tutor_subjects(subject_id);
