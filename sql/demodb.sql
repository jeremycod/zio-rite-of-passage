create table public.jobs
(
    id          text not null
        primary key,
    title       text not null,
    url         text not null,
    company     text not null,
    related_job text
);

alter table public.jobs
    owner to docker;

create unique index if not exists jobs_pkey
    on public.jobs (id);
