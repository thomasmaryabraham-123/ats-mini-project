CREATE TABLE jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    required_skills TEXT,
    min_experience INTEGER
);

CREATE TABLE candidates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    email TEXT,
    skills TEXT,
    experience INTEGER,
    job_id INTEGER,
    match_score REAL,
    status TEXT DEFAULT 'Applied'
);