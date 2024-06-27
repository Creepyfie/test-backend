create table budget
(
    id     serial primary key,
    year   int  not null,
    month  int  not null,
    amount int  not null,
    type   text not null
);

update table budget set type = "Расход" WHERE type = Комиссия

create table author
(
    id     serial primary key,
    full_name text not null,
    created timestamp with time zone
);
alter table budget add column author_id references author(id) on delete SET NULL