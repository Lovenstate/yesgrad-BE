-- Create subjects table
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on status for filtering active subjects
CREATE INDEX idx_subjects_status ON subjects(status);

-- Insert default subjects
INSERT INTO subjects (name, description, status) VALUES
('Mathematics', 'Algebra, Calculus, Geometry, Statistics', 'ACTIVE'),
('Physics', 'Mechanics, Thermodynamics, Electromagnetism', 'ACTIVE'),
('Chemistry', 'Organic, Inorganic, Physical Chemistry', 'ACTIVE'),
('Biology', 'Cell Biology, Genetics, Ecology', 'ACTIVE'),
('Computer Science', 'Programming, Algorithms, Data Structures', 'ACTIVE'),
('English', 'Literature, Writing, Grammar', 'ACTIVE'),
('History', 'World History, US History, European History', 'ACTIVE'),
('Economics', 'Microeconomics, Macroeconomics', 'ACTIVE');
