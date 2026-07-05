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

## Runtime Configuration

The runnable modules do not package `src/main/resources/application.yml` or `application.yaml`. This prevents sample defaults, local ports, demo credentials, and database URLs from leaking into reusable jars.

Docker Compose supplies runtime config through mounted files:

- `runtime/config/auth-server-config.yml`
- `runtime/config/gateway-config.yml`
- `runtime/config/service-a-config.yml`
- `runtime/config/service-b-config.yml`

Each container sets `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/<name>.yml`. For direct local Java runs, use the same pattern:

```bash
java -jar auth-server-app/target/*.jar \
  --spring.config.additional-location=file:runtime/config/auth-server-config.yml
```

For environment-specific deployments, copy these files into the deployment system as ConfigMaps, mounted files, or externalized Spring config. Keep secrets such as database passwords in environment variables or a secret manager, not in committed YAML.

## Demo Users

Seeded users and clients live in:

```text
security-auth-sample/auth-implementation/src/main/resources/db/changelog/002-seed.sql
```

Useful seeded flows:

- Email `gaurav.bhardwaj@getvymo.com`, password `Alpha#Pass1`, OTP `246810`, tenant `tenant-alpha`
- Email `bob@tenant-beta.local`, password `Bravo#Pass2`, tenant `tenant-beta`, MFA disabled
- User `ops-admin`, password `Admin#Pass3`, admin OTP, tenant `platform`
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

Tenant Alpha browser-style MFA flow:

```bash
PRIMARY_RESPONSE=$(curl -s -X POST http://localhost:9000/api/login/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}')

CHALLENGE_ID=$(echo "$PRIMARY_RESPONSE" | jq -r '.challengeId')

curl -X POST http://localhost:9000/api/login/mfa/verify \
  -H 'Content-Type: application/json' \
  -d "{\"challengeId\":\"$CHALLENGE_ID\",\"code\":\"246810\"}"
```

Tenant Beta password-only flow:

```bash
curl -X POST http://localhost:9000/api/login/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"email":"bob@tenant-beta.local","providerId":"PROV-tenant-beta-bob-local-password","credential":"Bravo#Pass2"}'
```

## MFA Extension Points

Application teams configure tenant MFA in their implementation module, not in framework code:

- Store tenant policy and challenge state in application persistence.
- Implement the framework MFA contracts for the desired provider.
- Use the sample OTP implementation as the local reference.
- Add a real adapter for Twilio, Duo, Okta, or an internal MFA provider behind the same contracts.

## Reset Data

Stop and remove volumes when changing seed SQL:

```bash
docker compose -f security-auth-sample/runtime/spring-auth/docker-compose.yml down -v
```

Then restart with `./security-auth-sample/run.sh`.
