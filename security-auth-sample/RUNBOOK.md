# Chenile Security Auth Sample Runbook

This guide is for developers who want to run and edit the reference stack locally.

## Start

```bash
./security-auth-sample/run.sh
```

Or use Compose directly:

```bash
docker compose -f security-auth-sample/runtime/spring-auth/docker-compose.yml up --build
```

## Local URLs

- Auth server: `http://localhost:9000`
- Gateway: `http://localhost:8080`
- Service A: `http://localhost:8081`
- Service B: `http://localhost:8082`
- Postgres: `localhost:15432`, database `auth_server`, user `auth_user`, password `auth_pass`
- Demo UI: run separately from `security-auth-sample/demo-ui`

## Edit Gateway Routes And Headers

Edit:

```text
security-auth-sample/runtime/config/gateway-config.yml
```

Use this file to change:

- `chenile.security.gateway.auth-server.uri`
- route `paths`, `uri`, `rewrite-regex`, and `rewrite-replacement`
- JWT issuer/JWK URLs
- relay headers under `chenile.security.gateway.relay.headers`

The gateway validates the bearer token and forwards `Authorization` to services by default. Relay headers are generated from trusted JWT claims, not copied from the incoming request.

## Demo Users

Seeded users and clients live in:

```text
security-auth-sample/auth-implementation/src/main/resources/db/changelog/002-seed.sql
```

Useful seeded flows:

- Email `gaurav.bhardwaj@getvymo.com`, password `Alpha#Pass1`, tenant `tenant-alpha`
- Email `bob@tenant-beta.local`, password `Bravo#Pass2`, tenant `tenant-beta`
- User `ops-admin`, password `Admin#Pass3`, tenant `platform`
- Client credentials: `system-client` / `system-client-secret`

## Example Calls

Get a token:

```bash
curl -X POST http://localhost:9000/realms/platform/protocol/openid-connect/token \
  -u system-client:system-client-secret \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials&scope=gateway.access service-a.read service-b.read'
```

Call through the gateway:

```bash
curl http://localhost:8080/api/a/orders/summary \
  -H "Authorization: Bearer $TOKEN"
```

## Reset Data

Stop and remove volumes when changing seed SQL:

```bash
docker compose -f security-auth-sample/runtime/spring-auth/docker-compose.yml down -v
```

Then restart with `./security-auth-sample/run.sh`.
