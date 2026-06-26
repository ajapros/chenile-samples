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
