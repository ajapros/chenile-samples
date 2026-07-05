--liquibase formatted sql
--changeset codex:004-business-ids splitStatements:true stripComments:false

alter table auth_realm add column if not exists external_id varchar(150);
alter table auth_user add column if not exists external_id varchar(150);
alter table auth_provider add column if not exists external_id varchar(150);
alter table mfa_challenge add column if not exists primary_provider_external_id varchar(150);

update auth_realm
set external_id = 'REALM-' || realm_key
where external_id is null;

update auth_user u
set external_id = 'USR-' || r.realm_key || '-' || u.username
from auth_realm r
where u.realm_id = r.id
  and u.external_id is null;

update auth_provider p
set external_id = 'PROV-' || r.realm_key || '-' || u.username || '-' || p.provider_key
from auth_user u
join auth_realm r on r.id = u.realm_id
where p.user_id = u.id
  and p.external_id is null;

update mfa_challenge c
set primary_provider_external_id = p.external_id
from auth_provider p
where c.primary_provider_id = p.id
  and c.primary_provider_external_id is null;

alter table auth_realm alter column external_id set not null;
alter table auth_user alter column external_id set not null;
alter table auth_provider alter column external_id set not null;
alter table mfa_challenge alter column primary_provider_external_id set not null;
alter table mfa_challenge alter column primary_provider_id drop not null;

create unique index if not exists idx_auth_realm_external_id on auth_realm (external_id);
create unique index if not exists idx_auth_user_external_id on auth_user (external_id);
create unique index if not exists idx_auth_provider_external_id on auth_provider (external_id);
create index if not exists idx_mfa_challenge_primary_provider_external_id on mfa_challenge (primary_provider_external_id);
