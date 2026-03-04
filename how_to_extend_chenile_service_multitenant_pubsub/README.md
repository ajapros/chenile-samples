# How To Extend Chenile Workflow Service and Make It Multi-Tenant

This sample demonstrates three things together:

1. How to extend an existing Chenile workflow service (`vehicle-service`) using an extension module.
2. How to run the extended service in a multi-tenant setup using `multi-datasource-utils`.
3. How to publish from a workflow command and consume on a pub/sub subscriber (`chenile-jvm-pub-sub`).

## What This Project Contains

Project root modules:

- `vehicle-api`: base workflow entity and payload contracts
- `vehicle-service`: base workflow implementation and controller
- `custom-tenant0-api`: tenant0 extension entity (`VehicleExtensionClientXyz`, `ext_type=client_xyz_ext`)
- `custom-tenant0-service`: shared/tenant0 workflow extension + multi-tenant + pub/sub tests
- `custom-tenant1-api`: tenant1 extension entity (`VehicleExtension`, `ext_type=client_abc_ext`)
- `custom-tenant1-service`: tenant1-specific transition action wiring (`tenant1VehicleExt`)
- `packager`: integration-test module that packages both extensions and validates end-to-end behavior

## How Workflow Extension Is Implemented

Base workflow (in `vehicle-service`) supports the original flow.

Extension workflow (in `custom-tenant0-service`) adds extension transitions in:

- `custom-tenant0-service/src/main/resources/com/mycompany/myorg/vehicle/vehicle-states.xml`

Subtype registration is wired in:

- `custom-tenant0-service/src/main/java/com/mycompany/myorg/vehicle/extension/configuration/VehicleExtentionConfiguration.java`

Tenant1-specific action bean is wired in:

- `custom-tenant1-service/src/main/java/com/mycompany/myorg/vehicle/extension/configuration/Tenant1VehicleConfiguration.java`

Extension transition logic is in:

- `custom-tenant0-service/src/main/java/com/mycompany/myorg/vehicle/extension/service/cmd/ExtVehicleAction.java`

The `ext` command now:

- updates extension field `newColumn`
- publishes pub/sub event to topic `vehicle.events.test`
- propagates tenant header when present

## How Multi-Tenant Support Is Implemented

Multi-tenant datasource routing is enabled in test runtime by importing:

- `org.chenile.configuration.multids.MultiTenantDataSourceConfiguration`

Tenant datasource definitions are in:

- `custom-tenant0-service/src/test/resources/application.yml`
- `packager/src/test/resources/application.yml`

Configured tenants:

- `tenant1` (default)
- `tenant2`

Tenant routing demo endpoint:

- `GET /test/items`

## Pub/Sub Receive Side

Subscriber controller and service (test scope) are used for integration verification:

- `.../pubsub/TenantPubSubController.java`
- `.../pubsub/TenantPubSubService.java`
- `.../pubsub/PubSubSharedData.java`

`TenantPubSubController` subscribes to `vehicle.events.test`.

## Test Coverage

### 1. Extension workflow + publish-on-command (`custom-tenant0-service`)

Feature file:

- `custom-tenant0-service/src/test/resources/features/service.feature`

Includes scenarios for:

- extension lifecycle (`create -> assign -> ext -> close`)
- publish from `ext` command and receive on subscriber
- tenant header propagation in publish/receive path
- second extension subtype flow (`client_xyz_ext`)

### 2. Multi-tenant datasource routing checks (`custom-tenant0-service`)

Feature file:

- `custom-tenant0-service/src/test/resources/features/multi-tenant-routing.feature`

Validates:

- default tenant behavior (no header)
- tenant specific routing (`x-chenile-tenant-id`)

### 3. Packager integration tests (both extensions + tenant header + pub/sub)

JUnit test:

- `packager/src/test/java/com/mycompany/myorg/vehicle/packager/PackagerIntegrationTest.java`

Validates:

- `client_abc_ext` flow end-to-end
- `client_xyz_ext` flow end-to-end
- tenant header propagation on workflow requests
- pub/sub event delivery from `ext` command to subscriber

## Run

From project root, run extension-service tests:

```bash
mvn -pl custom-tenant0-service -am test
```

Run packager integration tests:

```bash
mvn -pl packager -am test
```

## Expected Outcome

You should see:

- workflow extension transitions working (`ext` transition)
- second extension subtype (`client_xyz_ext`) working
- tenant-based routing checks passing
- pub/sub event emitted from workflow command and consumed by subscriber
- all tests passing in `custom-tenant0-service` and `packager`
