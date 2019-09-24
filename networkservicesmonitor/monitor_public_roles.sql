create table roles
(
    id   bigserial not null
        constraint roles_pkey
            primary key,
    name varchar(50)
            unique
);

alter table roles
    owner to postgres;

INSERT INTO public.roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO public.roles (id, name) VALUES (2, 'ROLE_ADMINISTRATOR');