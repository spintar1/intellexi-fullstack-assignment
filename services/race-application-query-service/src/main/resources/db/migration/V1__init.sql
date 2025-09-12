CREATE TABLE IF NOT EXISTS races (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  distance VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS applications (
  id UUID PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  club VARCHAR(255),
  race_id UUID NOT NULL REFERENCES races(id) ON DELETE CASCADE,
  applicant_email VARCHAR(320),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_applications_race_id ON applications(race_id);
CREATE INDEX IF NOT EXISTS idx_applications_applicant_email ON applications(applicant_email);



