drop database ctpdb;
create database ctpdb;
use ctpdb;

create table ctpdb.user_info (
    telephone   numeric(13)     primary key,
    name        varchar(30)     not null,
    email_id    varchar(50)     not null,
    gcm_id      long varchar    not null
);

create table ctpdb.trip_info (
    trip_id     integer         primary key,
    trip_name   varchar(20)     not null,
    start_date  date,
    end_date    date,
    created_by  numeric(13), 
    FOREIGN KEY (created_by) REFERENCES ctpdb.user_info(telephone)
);

create table ctpdb.trip_members (
    trip_id     integer,
    telephone   numeric(13),
    constraint pk_trip_members  primary key (trip_id, telephone),
    FOREIGN KEY (telephone) REFERENCES ctpdb.user_info(telephone),
    FOREIGN KEY (trip_id) REFERENCES ctpdb.trip_info(trip_id)
);

create table ctpdb.expenses (
    item_id     varchar(20),
    trip_id     integer,
    item_name   varchar(20)     not null,
    amount      real            not null,
    added_by    varchar(20),
    added_on    timestamp       default current_timestamp,
    constraint  pk_expenses     primary key (item_id, trip_id),
    FOREIGN KEY (trip_id) REFERENCES ctpdb.trip_info(trip_id)
);

create table ctpdb.checklist (
    item_id     varchar(20),
    trip_id     integer,
    item_name   varchar(20)     not null,
    quantity    integer,
    status      varchar(8)      default 'open',
    type        varchar(6)      default 'shared',
    assigned_to varchar(20),
    added_by    varchar(20),
    added_on    timestamp       default current_timestamp,  
    constraint pk_checklist     primary key (item_id, trip_id),
    FOREIGN KEY (trip_id) REFERENCES ctpdb.trip_info(trip_id)
);

create table ctpdb.user_auth_codes (
    telephone   numeric(13)     not null,
    code        integer         not null
);

create table ctpdb.best_places (
    trip_id     integer,
    item_id     varchar(20)     not null,
    location    long varchar     not null,
    wiki_key    long varchar,
    constraint pk_best_places     primary key (item_id, trip_id),
    FOREIGN KEY (trip_id) REFERENCES ctpdb.trip_info(trip_id)
);

create table ctpdb.updates_tracker (
    trip_id         integer,
    telephone       numeric(13),
    modification_id integer     not null,
    is_sent         varchar(1)  default 'n',
    received_on     timestamp   default current_timestamp,
    sent_on         timestamp,
    constraint pk_updates_tracker  primary key (modification_id, telephone),
    FOREIGN KEY (trip_id) REFERENCES ctpdb.trip_info(trip_id),
    FOREIGN KEY (telephone) REFERENCES ctpdb.user_info(telephone)
);

create table ctpdb.modification (
    trip_id         integer,
    telephone       numeric(13),
    modification_id integer     not null,
    table_name      varchar(30) not null,
    column_name     varchar(30) not null,
    value           long varchar,
    action          varchar(20)  not null,
    FOREIGN KEY (trip_id) REFERENCES ctpdb.trip_info(trip_id),
    FOREIGN KEY (telephone) REFERENCES ctpdb.user_info(telephone)
);
