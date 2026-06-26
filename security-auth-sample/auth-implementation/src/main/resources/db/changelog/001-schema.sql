--liquibase formatted sql
--changeset codex:001-schema splitStatements:true stripComments:false

create table if not exists auth_realm (
    id bigserial primary key,
    realm_key varchar(100) not null unique,
    display_name varchar(150) not null,
    enabled boolean not null default true
);

create table if not exists auth_user (
    id bigserial primary key,
    realm_id bigint not null references auth_realm(id),
    username varchar(150) not null,
    email varchar(255) not null,
    password_secret varchar(255) not null,
    enabled boolean not null default true,
    unique (realm_id, username)
);

create index if not exists idx_auth_user_email on auth_user (lower(email));

create table if not exists user_acl (
    id bigserial primary key,
    user_id bigint not null references auth_user(id) on delete cascade,
    acl_value varchar(150) not null
);

create unique index if not exists idx_user_acl_unique on user_acl (user_id, acl_value);

create table if not exists auth_provider (
    id bigserial primary key,
    user_id bigint not null references auth_user(id) on delete cascade,
    provider_key varchar(150) not null,
    provider_label varchar(150) not null,
    provider_type varchar(50) not null,
    provider_secret varchar(255) not null,
    provider_order integer not null default 10,
    enabled boolean not null default true,
    unique (user_id, provider_key)
);

create index if not exists idx_auth_provider_user_order on auth_provider (user_id, provider_order);

create table if not exists auth_provider_config (
    id bigserial primary key,
    realm_id bigint not null references auth_realm(id) on delete cascade,
    provider_key varchar(150) not null,
    provider_type varchar(50) not null,
    client_id varchar(255) not null,
    client_secret varchar(500) not null,
    authorization_uri varchar(500) not null,
    token_uri varchar(500) not null,
    user_info_uri varchar(500) not null,
    scopes varchar(500) not null,
    enabled boolean not null default true,
    unique (realm_id, provider_key)
);

create table if not exists oauth_client (
    id bigserial primary key,
    realm_id bigint not null references auth_realm(id),
    client_id varchar(150) not null,
    client_secret varchar(255),
    client_credentials_enabled boolean not null default false,
    password_grant_enabled boolean not null default false,
    allowed_scopes text,
    unique (realm_id, client_id)
);
