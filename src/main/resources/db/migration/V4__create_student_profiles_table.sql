-- Create student_profiles table
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    grade_level VARCHAR(50),
    school_name VARCHAR(255),
    learning_goals TEXT,
    preferred_learning_style VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on user_id
CREATE INDEX idx_student_profiles_user_id ON student_profiles(user_id);
