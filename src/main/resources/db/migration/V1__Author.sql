create table author
(
    id     serial primary key,
    full_name text not null,
    created timestamp with time zone
);