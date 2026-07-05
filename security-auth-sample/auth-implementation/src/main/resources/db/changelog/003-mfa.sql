--liquibase formatted sql
--changeset codex:003-mfa splitStatements:true stripComments:false

create table if not exists tenant_mfa_policy (
    id bigserial primary key,
    realm_id bigint not null references auth_realm(id) on delete cascade,
    enabled boolean not null default false,
    provider_key varchar(150) not null,
    provider_type varchar(50) not null,
    display_name varchar(150) not null,
    destination_hint varchar(255),
    ttl_seconds bigint not null default 300,
    unique (realm_id)
);

create table if not exists mfa_challenge (
    challenge_id varchar(80) primary key,
    user_id bigint not null references auth_user(id) on delete cascade,
    primary_provider_id bigint not null references auth_provider(id),
    client_id varchar(150) not null,
    primary_provider_type varchar(50) not null,
    mfa_provider_key varchar(150) not null,
    mfa_provider_type varchar(50) not null,
    status varchar(30) not null,
    attempts integer not null default 0,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null
);

create index if not exists idx_mfa_challenge_user_status on mfa_challenge (user_id, status);
create index if not exists idx_mfa_challenge_expires_at on mfa_challenge (expires_at);

insert into tenant_mfa_policy (
    realm_id, enabled, provider_key, provider_type, display_name, destination_hint, ttl_seconds
)
select r.id, true, 'email-otp', 'OTP', 'Email OTP', 'Seeded OTP for Tenant Alpha', 300
from auth_realm r
where r.realm_key = 'tenant-alpha'
on conflict (realm_id) do update
set enabled = excluded.enabled,
    provider_key = excluded.provider_key,
    provider_type = excluded.provider_type,
    display_name = excluded.display_name,
    destination_hint = excluded.destination_hint,
    ttl_seconds = excluded.ttl_seconds;

insert into tenant_mfa_policy (
    realm_id, enabled, provider_key, provider_type, display_name, destination_hint, ttl_seconds
)
select r.id, false, 'local-password', 'PASSWORD', 'Disabled', 'Tenant Beta does not require MFA', 300
from auth_realm r
where r.realm_key = 'tenant-beta'
on conflict (realm_id) do update
set enabled = excluded.enabled,
    provider_key = excluded.provider_key,
    provider_type = excluded.provider_type,
    display_name = excluded.display_name,
    destination_hint = excluded.destination_hint,
    ttl_seconds = excluded.ttl_seconds;

insert into tenant_mfa_policy (
    realm_id, enabled, provider_key, provider_type, display_name, destination_hint, ttl_seconds
)
select r.id, true, 'admin-otp', 'OTP', 'Admin OTP', 'Seeded admin OTP for Platform', 300
from auth_realm r
where r.realm_key = 'platform'
on conflict (realm_id) do update
set enabled = excluded.enabled,
    provider_key = excluded.provider_key,
    provider_type = excluded.provider_type,
    display_name = excluded.display_name,
    destination_hint = excluded.destination_hint,
    ttl_seconds = excluded.ttl_seconds;
