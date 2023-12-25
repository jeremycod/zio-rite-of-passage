CREATE DATABASE reviewboard;
\c reviewboard;



create table if not exists companies
(
    id BIGSERIAL PRIMARY KEY,
    slug TEXT UNIQUE NOT NULL,
    name TEXT UNIQUE NOT NULL,
    url TEXT UNIQUE NOT NULL,
    location TEXT,
    country TEXT,
    industry TEXT,
    image TEXT,
    tags TEXT[]
);

create table if not exists jobs
(
    id          text not null
        primary key,
    title       text not null,
    url         text not null,
    company     text not null,
    related_job text
);

