-- Create tutor_profiles table
CREATE TABLE IF NOT EXISTS tutor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    hourly_rate DECIMAL(10, 2),
    years_of_experience INTEGER,
    education TEXT,
    certifications TEXT,
    languages TEXT,
    availability_status VARCHAR(20) DEFAULT 'AVAILABLE',
    rating DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    total_sessions INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on user_id
CREATE INDEX idx_tutor_profiles_user_id ON tutor_profiles(user_id);

-- Create index on availability_status
CREATE INDEX idx_tutor_profiles_availability ON tutor_profiles(availability_status);

-- Create index on rating for sorting
CREATE INDEX idx_tutor_profiles_rating ON tutor_profiles(rating DESC);
