-- Users table (as specified in assignment requirements)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(320) UNIQUE NOT NULL,
  date_of_birth DATE,
  club VARCHAR(255), -- User's running club
  role VARCHAR(50) NOT NULL CHECK (role IN ('Applicant', 'Administrator')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Races table
CREATE TABLE IF NOT EXISTS races (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  distance VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Applications table (clean, normalized design)
CREATE TABLE IF NOT EXISTS applications (
  id UUID PRIMARY KEY,
  race_id UUID NOT NULL REFERENCES races(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_applications_race_id ON applications(race_id);
CREATE INDEX IF NOT EXISTS idx_applications_user_id ON applications(user_id);

-- Insert default users as specified in assignment (can be inserted directly)
INSERT INTO users (id, first_name, last_name, email, date_of_birth, club, role) 
VALUES 
  ('550e8400-e29b-41d4-a716-446655440001', 'Admin', 'User', 'admin@example.com', '1985-01-15', NULL, 'Administrator'),
  ('550e8400-e29b-41d4-a716-446655440002', 'John', 'Runner', 'applicant@example.com', '1990-06-20', 'City Runners Club', 'Applicant'),
  ('550e8400-e29b-41d4-a716-446655440003', 'Jane', 'Smith', 'runner@example.com', '1988-03-10', 'Marathon Masters', 'Applicant'),
  ('550e8400-e29b-41d4-a716-446655440004', 'Mike', 'Johnson', 'mike.johnson@example.com', '1992-11-05', 'Trail Blazers', 'Applicant')
ON CONFLICT (email) DO NOTHING;



