# Chenile Service Registry Sample

This sample runs a central Chenile service registry with the Chenile Admin UI and two mock services that register themselves remotely at startup.

It is intended to validate that repeated service restarts do not multiply rows in `service_definition` for the same `serviceId + serviceVersion`.

## Runtime

- Registry app: `http://localhost:8090`
- Admin UI: `http://localhost:8090/chenile/admin`
- Orders mock service: `http://localhost:8091`
- Payments mock service: `http://localhost:8092`
- Postgres: `localhost:15433`

## Build

From this directory:

```bash
mvn package -DskipTests
```

If local Chenile artifacts are not installed yet, build the framework modules first from the workspace root:

```bash
mvn -f chenile-parent/pom.xml -DskipTests install
mvn -f chenile-core/pom.xml -DskipTests install
mvn -f chenile-service-registry/pom.xml -DskipTests install
```

## Run

From this directory:

```bash
docker compose -f runtime/service-registry/docker-compose.yml up --build -d
```

Open:

```text
http://localhost:8090/chenile/admin
```

Useful checks:

```bash
curl -fsS http://localhost:8090/actuator/health
curl -fsS http://localhost:8090/serviceregistry
curl -fsS http://localhost:8090/serviceregistry/diagnostics
curl -fsS http://localhost:8091/orders/order-1001
curl -fsS http://localhost:8092/payments/payment-9001
```

## Restart Idempotency Validation

From this directory:

```bash
runtime/scripts/validate-restart-idempotency.sh
```

The script verifies:

- the registry and both mock services are healthy
- service registry diagnostics report no duplicate service/version groups
- operation and parameter link duplicates are zero
- `service_definition` row count is unchanged after repeated mock-service restarts

To run more restart cycles:

```bash
RESTARTS=10 runtime/scripts/validate-restart-idempotency.sh
```

## Stop

```bash
docker compose -f runtime/service-registry/docker-compose.yml down
```

To reset persisted registry data:

```bash
docker compose -f runtime/service-registry/docker-compose.yml down -v
```
