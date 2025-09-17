-- Add unique constraints to enforce business rules
-- V2: Adding unique constraints for data integrity

-- 1. Ensure no duplicate user emails (critical for authentication)
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- 2. Ensure no duplicate races with same name and distance 
-- (e.g., can't have two "Boston Marathon" 42.2km races)
ALTER TABLE races ADD CONSTRAINT uk_races_name_distance UNIQUE (name, distance);

-- 3. Ensure one user can only register once per race
-- (prevents duplicate applications for the same race by the same user)
ALTER TABLE applications ADD CONSTRAINT uk_applications_user_race UNIQUE (user_id, race_id);

-- Add comments for documentation
COMMENT ON CONSTRAINT uk_users_email ON users IS 'Ensures unique email addresses for authentication and communication';
COMMENT ON CONSTRAINT uk_races_name_distance ON races IS 'Prevents duplicate races with identical name and distance';
COMMENT ON CONSTRAINT uk_applications_user_race ON applications IS 'Prevents users from registering multiple times for the same race';
