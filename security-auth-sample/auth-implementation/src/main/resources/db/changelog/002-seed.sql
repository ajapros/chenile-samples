--liquibase formatted sql
--changeset codex:002-seed splitStatements:true stripComments:false

insert into auth_realm (realm_key, display_name, enabled)
values
    ('master', 'Master', true),
    ('tenant-alpha', 'Tenant Alpha', true),
    ('tenant-beta', 'Tenant Beta', true),
    ('platform', 'Platform', true)
on conflict (realm_key) do nothing;

insert into auth_user (realm_id, username, email, password_secret, enabled)
select r.id, 'admin', 'admin@platform.local', '$2a$10$GuZen9I/FL/QyrZu9fyH8uJZPP4qsV7BagVyC7eFAsCig0h1FBeYu', true
from auth_realm r where r.realm_key = 'master'
on conflict (realm_id, username) do nothing;

insert into auth_user (realm_id, username, email, password_secret, enabled)
select r.id, 'alice', 'gaurav.bhardwaj@getvymo.com', '$2a$10$sq20soLUcaSfyyKQ1A7gc.vfdfbpHCEftLa5BK.rcF0z0BkoQ/bF6', true
from auth_realm r where r.realm_key = 'tenant-alpha'
on conflict (realm_id, username) do nothing;

insert into auth_user (realm_id, username, email, password_secret, enabled)
select r.id, 'bob', 'bob@tenant-beta.local', '$2a$10$8bqNr3fPX9tXH5JtduXJie9blFXd.GmYwKknb4g/iwsMTuHZUc1ji', true
from auth_realm r where r.realm_key = 'tenant-beta'
on conflict (realm_id, username) do nothing;

insert into auth_user (realm_id, username, email, password_secret, enabled)
select r.id, 'ops-admin', 'gaurav.bhardwaj@getvymo.com', '$2a$10$2QFgLgMMkiKYEBhEkz20hOa4kwfLXBsknbmll8uryhY8LN./Ecu2m', true
from auth_realm r where r.realm_key = 'platform'
on conflict (realm_id, username) do nothing;

update auth_user u
set email = 'gaurav.bhardwaj@getvymo.com'
from auth_realm r
where u.realm_id = r.id
  and r.realm_key = 'tenant-alpha'
  and u.username = 'alice';

update auth_user u
set email = 'gaurav.bhardwaj@getvymo.com'
from auth_realm r
where u.realm_id = r.id
  and r.realm_key = 'platform'
  and u.username = 'ops-admin';

insert into user_acl (user_id, acl_value)
select u.id, acl_value
from auth_user u
cross join lateral (values ('iam:admin')) acl(acl_value)
join auth_realm r on r.id = u.realm_id
where r.realm_key = 'master' and u.username = 'admin'
on conflict do nothing;

insert into user_acl (user_id, acl_value)
select u.id, acl_value
from auth_user u
cross join lateral (values ('bridge:invoke'), ('orders:read')) acl(acl_value)
join auth_realm r on r.id = u.realm_id
where r.realm_key = 'tenant-alpha' and u.username = 'alice'
on conflict do nothing;

insert into user_acl (user_id, acl_value)
select u.id, acl_value
from auth_user u
cross join lateral (values ('customers:read'), ('portfolio:view')) acl(acl_value)
join auth_realm r on r.id = u.realm_id
where r.realm_key = 'tenant-beta' and u.username = 'bob'
on conflict do nothing;

insert into user_acl (user_id, acl_value)
select u.id, acl_value
from auth_user u
cross join lateral (values ('admin:all'), ('customers:read'), ('orders:read')) acl(acl_value)
join auth_realm r on r.id = u.realm_id
where r.realm_key = 'platform' and u.username = 'ops-admin'
on conflict do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'local-password', 'Password', 'PASSWORD', '$2a$10$sq20soLUcaSfyyKQ1A7gc.vfdfbpHCEftLa5BK.rcF0z0BkoQ/bF6', 10, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'tenant-alpha' and u.username = 'alice'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'email-otp', 'Email OTP', 'OTP', '$2a$10$B2N9OaelziBwRs9N544wfe1COALqIeFb4IeoylR1us0yf6Nu5WCo.', 20, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'tenant-alpha' and u.username = 'alice'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'google-oauth', 'Google', 'GOOGLE', '$2a$10$7P8uL80b3YwPcM6w6n5j7.2mg0iVw2LOjxw5m3z0HIm6Uj0z3abui', 15, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'tenant-alpha' and u.username = 'alice'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'local-password', 'Password', 'PASSWORD', '$2a$10$8bqNr3fPX9tXH5JtduXJie9blFXd.GmYwKknb4g/iwsMTuHZUc1ji', 10, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'tenant-beta' and u.username = 'bob'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'local-password', 'Password', 'PASSWORD', '$2a$10$2QFgLgMMkiKYEBhEkz20hOa4kwfLXBsknbmll8uryhY8LN./Ecu2m', 10, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'platform' and u.username = 'ops-admin'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'admin-otp', 'Admin OTP', 'OTP', '$2a$10$DXz7/dBsttuklsCsnyf1zOyBjgzAbKTGrzB5SM8moc6Y3ZgO11dt.', 20, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'platform' and u.username = 'ops-admin'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider (user_id, provider_key, provider_label, provider_type, provider_secret, provider_order, enabled)
select u.id, 'google-workspace', 'Google Workspace', 'GOOGLE', '$2a$10$7P8uL80b3YwPcM6w6n5j7.2mg0iVw2LOjxw5m3z0HIm6Uj0z3abui', 15, true
from auth_user u join auth_realm r on r.id = u.realm_id
where r.realm_key = 'platform' and u.username = 'ops-admin'
on conflict (user_id, provider_key) do nothing;

insert into auth_provider_config (
    realm_id, provider_key, provider_type, client_id, client_secret, authorization_uri, token_uri, user_info_uri, scopes, enabled
)
select
    r.id,
    'google-oauth',
    'GOOGLE',
    'replace-with-google-oauth-client-id',
    'replace-with-google-oauth-client-secret',
    'https://accounts.google.com/o/oauth2/v2/auth',
    'https://oauth2.googleapis.com/token',
    'https://openidconnect.googleapis.com/v1/userinfo',
    'openid profile email',
    true
from auth_realm r
where r.realm_key = 'tenant-alpha'
on conflict (realm_id, provider_key) do nothing;

insert into auth_provider_config (
    realm_id, provider_key, provider_type, client_id, client_secret, authorization_uri, token_uri, user_info_uri, scopes, enabled
)
select
    r.id,
    'google-workspace',
    'GOOGLE',
    'replace-with-google-workspace-client-id',
    'replace-with-google-workspace-client-secret',
    'https://accounts.google.com/o/oauth2/v2/auth',
    'https://oauth2.googleapis.com/token',
    'https://openidconnect.googleapis.com/v1/userinfo',
    'openid profile email',
    true
from auth_realm r
where r.realm_key = 'platform'
on conflict (realm_id, provider_key) do nothing;

insert into oauth_client (realm_id, client_id, client_secret, client_credentials_enabled, password_grant_enabled, allowed_scopes)
select r.id, 'admin-cli', null, true, true, ''
from auth_realm r
where r.realm_key = 'master'
on conflict (realm_id, client_id) do nothing;

insert into oauth_client (realm_id, client_id, client_secret, client_credentials_enabled, password_grant_enabled, allowed_scopes)
select r.id, 'system-client', '$2a$10$bHyflsnCjvZ.rTGQeFfGDePP1rwa.3utqQ8IhirRCi3DIdyRyCp0q', true, false, 'gateway.access,service-a.read,service-b.read'
from auth_realm r
where r.realm_key = 'platform'
on conflict (realm_id, client_id) do nothing;

insert into oauth_client (realm_id, client_id, client_secret, client_credentials_enabled, password_grant_enabled, allowed_scopes)
select r.id, 'user-test-client', '$2a$10$KcYlLKJ4tVAAK8W90E/sne.ANRbpHH5NFFqks.nQZarXitwXFOx6a', false, true, 'gateway.access,service-a.read,service-b.read'
from auth_realm r
where r.realm_key in ('tenant-alpha', 'tenant-beta', 'platform')
on conflict (realm_id, client_id) do nothing;

insert into oauth_client (realm_id, client_id, client_secret, client_credentials_enabled, password_grant_enabled, allowed_scopes)
select r.id, 'browser-login', null, false, false, 'gateway.access,service-a.read,service-b.read'
from auth_realm r
where r.realm_key in ('tenant-alpha', 'tenant-beta', 'platform')
on conflict (realm_id, client_id) do nothing;
