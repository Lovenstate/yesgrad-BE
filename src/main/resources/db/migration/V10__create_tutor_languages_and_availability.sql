CREATE TABLE IF NOT EXISTS tutor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    profile_photo_url VARCHAR(500),
    school VARCHAR(255),
    degree VARCHAR(255),
    field_of_study VARCHAR(255),
    graduation_year INTEGER,
    hourly_rate DECIMAL(10, 2),
    cancellation_policy VARCHAR(100),
    travel_policy TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- Tutor Subjects Table
CREATE TABLE IF NOT EXISTS tutor_subjects (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    subject VARCHAR(100) NOT NULL
);

-- Tutor Languages Table
CREATE TABLE IF NOT EXISTS tutor_languages (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    language VARCHAR(100) NOT NULL,
    proficiency VARCHAR(50) NOT NULL
);

-- Tutor Availability Table
CREATE TABLE IF NOT EXISTS tutor_availability (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_tutor_profiles_user_id ON tutor_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_tutor_subjects_tutor_id ON tutor_subjects(tutor_id);
CREATE INDEX IF NOT EXISTS idx_tutor_languages_tutor_id ON tutor_languages(tutor_id);
CREATE INDEX IF NOT EXISTS idx_tutor_availability_tutor_id ON tutor_availability(tutor_id);
