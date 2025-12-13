-- Core user accounts for all platform users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,                    -- User's email address (login credential)
    password_hash VARCHAR(255) NOT NULL,                   -- Bcrypt hashed password
    first_name VARCHAR(100) NOT NULL,                      -- User's first name
    last_name VARCHAR(100) NOT NULL,                       -- User's last name
    phone VARCHAR(20),                                     -- Optional phone number
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'TUTOR', 'ADMIN')), -- User role in platform
    status VARCHAR(30) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')), -- Account status
    avatar_url VARCHAR(500),                               -- Profile picture URL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP                                   -- Last login timestamp for analytics
);

-- Subject categories and topics available for tutoring
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,                     -- Subject name (e.g., "Mathematics")
    description TEXT,                                      -- Detailed subject description
    category VARCHAR(50),                                  -- Subject category (e.g., "STEM", "Languages")
    is_active BOOLEAN DEFAULT true,                       -- Whether subject is available for booking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Extended profile information for tutors
CREATE TABLE IF NOT EXISTS tutor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,                                              -- Tutor's biography and introduction
    hourly_rate DECIMAL(10,2),                            -- Tutor's hourly rate in USD
    experience_years INTEGER,                              -- Years of tutoring/teaching experience
    education TEXT,                                        -- Educational background (degrees, schools)
    certifications TEXT,                                   -- Professional certifications and credentials
    languages VARCHAR(255),                                -- Languages spoken (comma-separated)
    teaching_approach TEXT,                                -- Tutor's teaching methodology and style description
    application_status VARCHAR(20) DEFAULT 'PENDING' CHECK (application_status IN ('PENDING', 'APPROVED', 'REJECTED')), -- Admin approval status
    is_verified BOOLEAN DEFAULT false,                    -- Whether tutor has completed verification process
    total_sessions INTEGER DEFAULT 0,                     -- Total completed sessions (for statistics)
    average_rating DECIMAL(3,2) DEFAULT 0.00,            -- Average rating from student reviews (0.00-5.00)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Extended profile information for students
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    grade_level VARCHAR(20),                               -- Student's current grade/education level
    learning_goals TEXT,                                   -- Student's learning objectives and goals
    preferred_learning_style VARCHAR(50),                  -- How student learns best (visual, auditory, etc.)
    timezone VARCHAR(50),                                  -- Student's timezone for scheduling
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Junction table linking tutors to subjects they can teach
CREATE TABLE IF NOT EXISTS tutor_subjects (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    proficiency_level VARCHAR(20) CHECK (proficiency_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')), -- Tutor's skill level in this subject
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tutor_id, subject_id)                          -- Prevent duplicate subject assignments
);

-- Tutor availability schedule (recurring weekly slots)
CREATE TABLE IF NOT EXISTS availability_slots (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0=Sunday, 1=Monday, etc.
    start_time TIME NOT NULL,                             -- Start time of availability slot
    end_time TIME NOT NULL,                               -- End time of availability slot
    timezone VARCHAR(50) DEFAULT 'UTC',                   -- Timezone for the time slots
    is_recurring BOOLEAN DEFAULT true,                    -- Whether this slot repeats weekly
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student booking requests for tutoring sessions
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES student_profiles(id),
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),   -- Subject to be tutored
    scheduled_at TIMESTAMP NOT NULL,                      -- When the session is scheduled
    duration_minutes INTEGER NOT NULL DEFAULT 60,        -- Session length in minutes
    hourly_rate DECIMAL(10,2) NOT NULL,                  -- Tutor's rate at time of booking
    total_amount DECIMAL(10,2) NOT NULL,                 -- Total cost (rate * duration)
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')), -- Booking lifecycle status
    notes TEXT,                                           -- Special requests or notes from student
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Actual tutoring session records (created when session starts)
CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT UNIQUE NOT NULL REFERENCES bookings(id), -- Links to the original booking
    started_at TIMESTAMP,                                 -- When tutor started the session
    ended_at TIMESTAMP,                                   -- When session was marked complete
    actual_duration_minutes INTEGER,                      -- Actual session length (may differ from booked)
    session_notes TEXT,                                   -- Tutor's notes about the session content
    homework_assigned TEXT,                               -- Homework or tasks assigned to student
    student_feedback TEXT,                                -- Student's immediate feedback
    tutor_feedback TEXT,                                  -- Tutor's assessment of student progress
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student and tutor reviews after completed sessions
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT UNIQUE NOT NULL REFERENCES sessions(id), -- Links to completed session
    reviewer_id BIGINT NOT NULL REFERENCES users(id),    -- Who is writing the review
    reviewee_id BIGINT NOT NULL REFERENCES users(id),    -- Who is being reviewed
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5), -- 1-5 star rating
    comment TEXT,                                         -- Written review comment
    is_public BOOLEAN DEFAULT true,                      -- Whether review appears on public profile
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment processing and financial records
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),  -- Links to the booking being paid for
    student_id BIGINT NOT NULL REFERENCES users(id),     -- Student making payment
    tutor_id BIGINT NOT NULL REFERENCES users(id),       -- Tutor receiving payment
    amount DECIMAL(10,2) NOT NULL,                       -- Total amount charged to student
    platform_fee DECIMAL(10,2) NOT NULL,                -- Platform's commission
    tutor_earnings DECIMAL(10,2) NOT NULL,               -- Amount tutor receives (amount - platform_fee)
    payment_method VARCHAR(50),                           -- Payment method used (card, bank, etc.)
    stripe_payment_intent_id VARCHAR(255),                -- Stripe payment reference
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')), -- Payment processing status
    processed_at TIMESTAMP,                               -- When payment was successfully processed
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Direct messaging between users (students, tutors, admins)
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id),      -- User sending the message
    recipient_id BIGINT NOT NULL REFERENCES users(id),   -- User receiving the message
    booking_id BIGINT REFERENCES bookings(id),           -- Optional: message related to specific booking
    content TEXT NOT NULL,                                -- Message content
    is_read BOOLEAN DEFAULT false,                       -- Whether recipient has read the message
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System notifications for users (booking confirmations, reminders, etc.)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),        -- User receiving the notification
    type VARCHAR(50) NOT NULL,                           -- Notification type (BOOKING_CONFIRMED, SESSION_REMINDER, etc.)
    title VARCHAR(255) NOT NULL,                         -- Notification headline
    message TEXT NOT NULL,                               -- Notification body text
    is_read BOOLEAN DEFAULT false,                       -- Whether user has seen the notification
    related_entity_type VARCHAR(50),                     -- Type of related entity (booking, session, etc.)
    related_entity_id BIGINT,                            -- ID of related entity for deep linking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

-- Tutor Subjects Table
CREATE TABLE tutor_subjects (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    subject VARCHAR(100) NOT NULL
);

-- Tutor Languages Table
CREATE TABLE tutor_languages (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    language VARCHAR(100) NOT NULL,
    proficiency VARCHAR(50) NOT NULL
);

-- Tutor Availability Table
CREATE TABLE tutor_availability (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
);



-- Insert default subjects
INSERT INTO subjects (name, description, category) VALUES
('Mathematics', 'Basic to advanced mathematics', 'STEM'),
('Physics', 'Physics concepts and problem solving', 'STEM'),
('Chemistry', 'Chemistry fundamentals and applications', 'STEM'),
('English', 'English language and literature', 'Language Arts'),
('Computer Science', 'Programming and computer science concepts', 'STEM'),
('Biology', 'Life sciences and biology', 'STEM'),
('History', 'World and regional history', 'Social Studies'),
('Spanish', 'Spanish language learning', 'Languages'),
('French', 'French language learning', 'Languages'),
('SAT Prep', 'SAT test preparation', 'Test Prep'),
('ACT Prep', 'ACT test preparation', 'Test Prep')
ON CONFLICT (name) DO NOTHING;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_subjects_category ON subjects(category);
CREATE INDEX IF NOT EXISTS idx_tutor_profiles_user_id ON tutor_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_tutor_profiles_status ON tutor_profiles(application_status);
CREATE INDEX IF NOT EXISTS idx_student_profiles_user_id ON student_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_tutor_subjects_tutor_id ON tutor_subjects(tutor_id);
CREATE INDEX IF NOT EXISTS idx_tutor_subjects_subject_id ON tutor_subjects(subject_id);
CREATE INDEX IF NOT EXISTS idx_availability_tutor_id ON availability_slots(tutor_id);
CREATE INDEX IF NOT EXISTS idx_bookings_student_id ON bookings(student_id);
CREATE INDEX IF NOT EXISTS idx_bookings_tutor_id ON bookings(tutor_id);
CREATE INDEX IF NOT EXISTS idx_bookings_scheduled_at ON bookings(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_sessions_booking_id ON sessions(booking_id);
CREATE INDEX IF NOT EXISTS idx_reviews_session_id ON reviews(session_id);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewee_id ON reviews(reviewee_id);
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_tutor_profiles_user_id ON tutor_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_tutor_subjects_tutor_id ON tutor_subjects(tutor_id);
CREATE INDEX IF NOT EXISTS idx_tutor_languages_tutor_id ON tutor_languages(tutor_id);
CREATE INDEX IF NOT EXISTS idx_tutor_availability_tutor_id ON tutor_availability(tutor_id);