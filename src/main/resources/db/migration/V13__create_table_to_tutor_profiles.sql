-- Tutor Profile Table
CREATE TABLE tutor_profiles (
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