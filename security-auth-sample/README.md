# Chenile Security Auth Sample

This sample demonstrates how an application team should consume the Chenile auth/gateway framework from `chenile-security`.

It is intentionally structured like a production application: framework code stays in `chenile-security`, while application-specific persistence, tenant, client, user, UI, and runtime wiring stay in this sample.

## Modules

- `auth-implementation`: sample Postgres/JPA/Liquibase implementation of the auth framework contracts.
- `auth-server-app`: runnable auth-server composition using `chenile-security-auth-server`.
- `gateway-app`: runnable gateway composition using `chenile-security-gateway`.
- `service-a`: protected resource-server sample using `chenile-security-starter-resource-server`.
- `service-b`: second protected resource-server sample.
- `demo-ui`: React UI for the sample login and service flow.
- `runtime`: Docker/runtime assets copied from the prototype and renamed for Chenile sample usage.

## Layering Pattern

- Framework layer: `org.chenile:chenile-security-*` artifacts provide auth contracts, token issuing, resource-server helpers, gateway route/relay logic, and starters.
- Application auth layer: `auth-implementation` owns database schema, seed data, JPA repositories, tenant lookup, client lookup, and provider lookup.
- Composition layer: `auth-server-app` and `gateway-app` are small Spring Boot applications that assemble Chenile framework artifacts with sample config.
- Resource layer: `service-a` and `service-b` show service-local security chains, JWT validation, request context extraction, ACL checks, and downstream calls.
- UI layer: `demo-ui` demonstrates browser login and calling protected APIs through the gateway.

## Build

Install the current `chenile-security` framework artifacts first:

```bash
cd /Users/gauravbhardwaj/work/ajapro/chenile-security
mvn install -DskipTests
```

Then build the sample:

```bash
cd /Users/gauravbhardwaj/work/ajapro/chenile-samples/security-auth-sample
mvn test
```

## Run Locally

```bash
cd /Users/gauravbhardwaj/work/ajapro/chenile-samples/security-auth-sample
./run.sh
```

The launcher builds local `chenile-security` artifacts, builds this sample, and starts the Docker Compose stack.
Postgres is exposed on host port `15432` to avoid colliding with a local Postgres running on `5432`.

## Usage

Use this sample as the reference for teams that want to plug their own user/client/tenant schema into the new framework. Application teams should implement the framework contracts in their own module and depend on Chenile framework artifacts instead of modifying `auth-framework`.

Framework-owned configuration uses the `chenile.security.*` prefix. Sample-owned configuration uses the `sample.security.*` prefix so application teams can clearly separate product knobs from their own runtime knobs.

Runtime configuration is intentionally external to the packaged jars. Do not add `src/main/resources/application.yml` or `src/main/resources/application.yaml` to the auth server, gateway, or resource-service modules. The Docker Compose stack mounts explicit files from `runtime/config` using `SPRING_CONFIG_ADDITIONAL_LOCATION`, and tests provide their own properties through test annotations or `src/test/resources`.

External config files used by the sample:

- `runtime/config/auth-server-config.yml`: auth-server port, issuer, Liquibase/JPA, token audiences, and demo metadata.
- `runtime/config/gateway-config.yml`: gateway port, token validation, relay headers, and route definitions.
- `runtime/config/service-a-config.yml`: service A port, issuer/JWK URLs, and service B URL.
- `runtime/config/service-b-config.yml`: service B port and issuer/JWK URLs.

## Tenant MFA / Two-Factor Auth

The sample demonstrates sequential tenant-level MFA:

- `tenant-alpha`: primary password or Google login succeeds first, then a seeded OTP challenge is required before a token is issued.
- `tenant-beta`: password login issues a token directly; MFA policy is disabled.
- `platform`: admin login requires the seeded admin OTP.

MFA policy and challenge persistence are application-owned in `auth-implementation`; the framework owns only the contracts, orchestration endpoints, and token claim conventions. Third-party MFA should be integrated by implementing the framework `MfaProvider`/challenge contracts in the application module. The sample uses seeded OTP behavior so local tests and demos do not need Twilio, Duo, Okta, or external secrets.

User and provider IDs exposed by the auth framework are opaque strings, not database sequence numbers. The sample keeps numeric surrogate keys internally for JPA joins, but login APIs and token claims use stable application IDs such as `USR-tenant-alpha-alice` and `PROV-tenant-alpha-alice-local-password`.

Successful MFA tokens include:

- `mfa=true`
- `amr`, for example `["pwd","otp"]`
- `mfa_provider`
