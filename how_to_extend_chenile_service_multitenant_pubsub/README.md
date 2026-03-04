# Developer How-To: Extend `vehicle` Core for Tenant0/Tenant1 and Run Both in One JVM

This repo is a technical sample for **TDD-first multi-tenant extension** of a Chenile workflow service.

It shows how to:
- keep a reusable core module (`vehicle`)
- add independent client extensions (`custom-tenant0`, `custom-tenant1`)
- package both in one runtime (`packager`) with multi-datasource routing
- verify everything through cucumber tests

## 1. Module Layout

Root modules:
- `vehicle`
  - `vehicle/api`: core model contracts
  - `vehicle/service`: core workflow + REST + central HTTP subtype registration
- `custom-tenant0`
  - `api`: tenant0 extension entity (`tenant0_ext`)
  - `service`: tenant0-specific `ext` transition action and tests
- `custom-tenant1`
  - `api`: tenant1 extension entity (`tenant1_ext`)
  - `service`: tenant1-specific `ext` transition action and tests
- `packager`
  - `service`: imports both tenant modules and validates both workflows in one JVM

## 2. Core Design: Register Extension Subtypes in `vehicle` Module

Subtype registration is centralized in:
- `vehicle/service/src/main/java/com/mycompany/myorg/vehicle/configuration/VehicleConfiguration.java`

`VehicleConfiguration` implements `WebMvcConfigurer` and registers extension subtypes dynamically from:
- `chenile.http.extension-subtypes`

This allows tenant modules to stay independent: they only contribute config values (YAML), not core wiring changes.

## 2.1 Base Workflow Was Also Modified for Tenant-Aware Action Discovery

The base workflow config in `VehicleConfiguration` also wires transition action discovery with tenant awareness:

```java
@Bean
STMTransitionActionResolver vehicleTransitionActionResolver(
@Qualifier("defaultvehicleSTMTransitionAction") STMTransitionAction<Vehicle> defaultSTMTransitionAction){
    return new STMTransitionActionResolver("vehicle",defaultSTMTransitionAction, HeaderUtils.TENANT_ID_KEY.replace("x-",""));
}
```

Why this matters:
- Prefix `"vehicle"` tells Chenile to discover actions using the standard naming convention.
- `HeaderUtils.TENANT_ID_KEY.replace("x-","")` maps tenant context from `x-chenile-tenant-id`.
- With this setup, Chenile auto-discovers tenant-specific transition beans without manual per-request wiring.

In this repo, tenant-specific `ext` actions are exposed as beans:
- tenant0: `@Bean("tenant0VehicleExt")`
- tenant1: `@Bean("tenant1VehicleExt")`

So the same workflow event (`ext`) can resolve to tenant-specific implementations based on header context.

## 2.2 Initial Workflow vs Extended Workflow

Initial workflow (core `vehicle` module):
- Defined in `vehicle/service/src/main/resources/com/mycompany/myorg/vehicle/vehicle-states.xml`
- Core lifecycle covers base transitions like:
  - `OPENED -> ASSIGNED` (`assign`)
  - `ASSIGNED -> RESOLVED` (`resolve`)
  - `ASSIGNED -> CLOSED` (`close`) and standard closure flow

What extension modules changed:
- Extension state model is added in tenant extension workflow XML:
  - `custom-tenant0/service/src/main/resources/com/mycompany/myorg/vehicle/vehicle-states.xml`
- Adds new event path:
  - `ASSIGNED -> EXTENSION` (`ext`)
  - `EXTENSION -> CLOSED` (`close`)

What this enables:
- Core workflow remains reusable and unchanged for non-extension tenants.
- Tenant modules plug additional behavior only for `ext` transition.
- `tenant0` and `tenant1` implement different `ext` mutation logic while sharing the same base lifecycle.

Expected YAML shape:

```yaml
chenile:
  http:
    extension-subtypes:
      - name: tenant0_ext
        className: com.mycompany.myorg.vehicle.model.VehicleExtensionTenant0
      - name: tenant1_ext
        className: com.mycompany.myorg.vehicle.model.VehicleExtensionTenant1
```

## 3. Tenant Extension Modules (Independent)

### Tenant0 module
- Entity type: `tenant0_ext`
- Action bean config:
  - `custom-tenant0/service/src/main/java/com/mycompany/myorg/vehicle/extension/configuration/VehicleExtentionConfiguration.java`
- Action implementation:
  - `custom-tenant0/service/src/main/java/com/mycompany/myorg/vehicle/extension/service/cmd/Tenant0ExtVehicleAction.java`

Behavior on `ext`:
- sets `newColumn`
- sets `tenant0WorkflowNote`
- publishes event to `vehicle.events.test` with tenant header propagation

### Tenant1 module
- Entity type: `tenant1_ext`
- Action bean config:
  - `custom-tenant1/service/src/main/java/com/mycompany/myorg/vehicle/extension/configuration/Tenant1VehicleConfiguration.java`
- Action implementation:
  - `custom-tenant1/service/src/main/java/com/mycompany/myorg/vehicle/extension/service/cmd/Tenant1ExtVehicleAction.java`

Behavior on `ext`:
- sets `tenant1WorkflowNote`
- publishes event to `vehicle.events.test` with tenant header propagation

## 4. Packager: Both Tenants in Same JVM

Packager test runtime uses:
- `packager/service/src/test/java/com/mycompany/myorg/vehicle/packager/PackagerSpringTestConfig.java`
- imports `MultiTenantDataSourceConfiguration`
- scans `org.chenile` and `com.mycompany`

Packager datasource + subtype config:
- `packager/service/src/test/resources/application.yml`
- defines multiple datasource entries under `chenile.multids.datasources`
- defines both subtype registrations under `chenile.http.extension-subtypes`

This lets tenant0 and tenant1 workflows run in a single process while tenant context is driven by request header:
- `x-chenile-tenant-id`

## 5. TDD Flow (Read Tests First)

### Tenant0 TDD specs
- Feature: `custom-tenant0/service/src/test/resources/features/service.feature`

Covers:
- create tenant0 entity and assert tenant0 columns
- assign -> ext -> close transitions
- pub/sub message and tenant propagation
- DB assertion for `tenant0_*` columns

### Tenant1 TDD specs
- Feature: `custom-tenant1/service/src/test/resources/features/service.feature`

Covers:
- create tenant1 entity and assert tenant1 columns
- assign -> ext -> close transitions
- pub/sub message and tenant propagation
- DB assertion for `tenant1_*` columns

### Combined packager TDD specs
- Feature: `packager/service/src/test/resources/features/multitenant-service.feature`

Covers in one JVM:
- tenant1 workflow with `tenant1_ext`
- tenant0 workflow with `tenant0_ext`
- extension-column assertions for each tenant type
- pub/sub assertions with tenant context for both

## 6. How to Develop a New Tenant Extension (Pattern)

1. Add tenant entity in new tenant `api` module with new `ext_type`.
2. Add tenant-specific transition action in tenant `service` module.
3. Add tenant bean configuration class in tenant `service` module.
4. Add subtype mapping entry in that runtime's YAML:
   - `chenile.http.extension-subtypes`
5. Add cucumber feature scenarios first (create, assign, ext, close, pubsub, DB).
6. Run tenant module tests.
7. Add/extend packager feature so multiple tenants are verified together in same JVM.

## 7. Commands

Run tenant0 tests:

```bash
mvn -pl custom-tenant0/service -am test
```

Run tenant1 tests:

```bash
mvn -pl custom-tenant1/service -am test
```

Run packager multi-tenant tests:

```bash
mvn -pl packager/service -am test
```

Run only packager cucumber suite:

```bash
mvn -pl packager/service -am -Dtest=com.mycompany.myorg.vehicle.packager.bdd.CukesRestTest -Dsurefire.failIfNoSpecifiedTests=false test
```

## 8. Key Technical Rules in This Sample

- Core module owns mapper wiring (`VehicleConfiguration`).
- Tenant modules are independent of each other for extension logic.
- Packager is the integration point that loads all tenant modules together.
- Tenant routing is header-driven (`x-chenile-tenant-id`).
- Tests are the source of truth for behavior (TDD).

## 9. Current Limitation (Same JVM / Packager)

When both tenant modules are loaded in the same JVM (for example in `packager`), the extension workflow definitions must currently be identical in structure.

Practical meaning:
- tenant-specific Java action logic can differ (`Tenant0ExtVehicleAction` vs `Tenant1ExtVehicleAction`)
- but workflow XML extension shape must remain the same across tenants for this setup

This is a known limitation in the current approach and is planned to be improved in a future release.
