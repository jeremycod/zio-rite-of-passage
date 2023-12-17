CREATE TABLE IF NOT EXISTS jobs (
    id TEXT primary key,
    title TEXT not null ,
    url TEXT NOT NULL,
    company TEXT NOT NULL,
    relatedJob TEXT
);