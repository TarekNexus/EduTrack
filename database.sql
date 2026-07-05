-- EduTrack schema reference.
-- The app creates these automatically on first run (see database/DBConnection.java),
-- so running this file by hand is optional -- useful if you want to inspect/seed
-- the database directly from the Neon SQL editor or psql.

CREATE TABLE IF NOT EXISTS departments (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS students (
    id              SERIAL PRIMARY KEY,
    student_code    VARCHAR(30) NOT NULL UNIQUE,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(30),
    address         VARCHAR(255),
    gender          VARCHAR(10),
    department_id   INTEGER REFERENCES departments(id) ON DELETE SET NULL,
    semester        VARCHAR(10),
    cgpa            NUMERIC(3,2),
    blood_group     VARCHAR(5),
    photo_path      VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS admins (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL
);

INSERT INTO departments (name) VALUES
    ('CST'), ('GDT'), ('PT')
ON CONFLICT (name) DO NOTHING;

-- Default admin login: username "admin", password "admin123"
-- (SHA-256 of "admin123:studenthub-pepper" -- see dao/AdminDAO.java).
-- The app also seeds this automatically on first run if the admins table is empty.
INSERT INTO admins (username, password_hash) VALUES
    ('admin', '2e24eccfaa117f686dc4669e778b15085cebbf27ae09f5787af7384f5d94fcd9')
ON CONFLICT (username) DO NOTHING;
