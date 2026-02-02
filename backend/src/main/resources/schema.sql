create sequence if not exists ticket_seq start with 1 increment by 1;

create table if not exists support_ticket (
                                              id uuid primary key,
                                              ticket_number varchar(32) not null,
    subject varchar(120) not null,
    description varchar(2000) not null,
    priority varchar(16) not null,
    status varchar(16) not null,
    created_at timestamp not null,
    constraint uk_ticket_number unique (ticket_number)
    );