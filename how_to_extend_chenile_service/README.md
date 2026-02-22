# Service + Service Extension: Technical Design and Validation

## 1. Business Requirement Summary

The business requirement was to keep the **core Vehicle workflow** intact and introduce a **tenant/client specific extension** without breaking existing consumers.

### Required outcomes

1. Keep existing base modules and behavior:
   - `vehicle-api`
   - `vehicle-service`
2. Add extension modules:
   - `vehicleextention-api`
   - `vehicleextention-service`
3. Extend the vehicle entity at persistence layer with extension attributes.
4. Keep a **single persistence table** and avoid a separate extension DAO/repository contract.
5. Extend workflow behavior at FSM level (`states.xml`) to add extension event/state transition.
6. Add automated test coverage that proves:
   - REST flow works end-to-end
   - DB contains expected values per flow stage

## 2. Final Module Architecture

Root aggregator (`pom.xml`) now builds all 4 modules:

- `vehicle-api` (base entity + payloads)
- `vehicle-service` (base workflow, controller, repository, actions)
- `vehicleextention-api` (extended entity subtype)
- `vehicleextention-service` (workflow extension config/actions/tests)

## 3. Entity-Level Design (Business Entity Extension)

### Base entity

File: `vehicle-api/src/main/java/com/mycompany/myorg/vehicle/model/Vehicle.java`

Key technical points:

- `@Entity` + `@Table(name = "vehicle")`
- Single-table inheritance enabled:
  - `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`
  - `@DiscriminatorColumn(name = "ext_type")`
- JSON polymorphism for REST payloads:
  - `@JsonTypeInfo(... property = "ext_type", defaultImpl = Vehicle.class)`

This allows one REST endpoint (`/vehicle`) to accept both base and extension payloads.

### Extension entity

File: `vehicleextention-api/src/main/java/com/mycompany/myorg/vehicle/model/VehicleExtension.java`

- `@Entity`
- `@DiscriminatorValue("client_abc_ext")`
- Added extension columns:
  - `insurance_policy_number`
  - `fitness_expiry`
  - `new_column`

## 4. Repository/DAO Decision

Base repository remains in core service:

- `vehicle-service/src/main/java/com/mycompany/myorg/vehicle/configuration/dao/VehicleRepository.java`

No new DAO contract was introduced for extension. Extension behavior is layered through subtype + workflow/action wiring.

## 5. Workflow-Level Design

## 5.1 Base workflow

File: `vehicle-service/src/main/resources/com/mycompany/myorg/vehicle/vehicle-states.xml`

Flow:

- `OPENED --assign--> ASSIGNED --resolve--> RESOLVED --close--> CLOSED`

## 5.2 Extension workflow

File: `vehicleextention-service/src/main/resources/com/mycompany/myorg/vehicle/vehicle-states.xml`

Workflow extension introduced at state-machine level:

- `ASSIGNED --ext--> EXTENSION --close--> CLOSED`

This keeps original flow usable while enabling extension-specific transition path.

## 6. Extension Runtime Wiring

File: `vehicleextention-service/src/main/java/com/mycompany/myorg/vehicle/extension/configuration/VehicleExtentionConfiguration.java`

Implemented responsibilities:

1. Register subtype mapping for JSON payloads:
   - `ext_type = client_abc_ext -> VehicleExtension`
2. Register extension transition action bean:
   - `vehicleExt()` -> `ExtVehicleAction`

File: `vehicleextention-service/src/main/java/com/mycompany/myorg/vehicle/extension/service/cmd/ExtVehicleAction.java`

- Handles `ext` transition
- Writes `newColumn = payload.comment`

## 7. Test Strategy and Coverage

## 7.1 Base flow Cucumber suite

- Feature: `vehicle-service/src/test/resources/features/service.feature`
- Steps: `vehicle-service/src/test/java/com/mycompany/myorg/vehicle/bdd/CukesSteps.java`

Coverage includes:

1. Create base vehicle
2. Retrieve
3. Assign
4. Resolve
5. Close
6. Invalid transition (assign after closed)
7. DB verification step added to validate persisted base row values:
   - `opened_by`, `description`, `assignee`, `assign_comment`, `resolve_comment`, `close_comment`
   - and extension columns are null for base row

## 7.2 Extension flow Cucumber suite

- Feature: `vehicleextention-service/src/test/resources/features/service.feature`
- Steps: `vehicleextention-service/src/test/java/com/mycompany/myorg/vehicle/extension/bdd/CukesSteps.java`

Coverage includes:

1. Create extension vehicle (`ext_type=client_abc_ext`)
2. Assign
3. Extension transition (`/ext`) sets `newColumn`
4. Close
5. DB verification step validates extension row values:
   - `ext_type`, `opened_by`, `description`, `insurance_policy_number`, `fitness_expiry`
   - base action columns (`assignee`, `assign_comment`, `resolve_comment`, `close_comment`) null at create-validation stage

## 8. Proof of Execution

The following commands were executed successfully:

1. `mvn -pl vehicle-service -Dtest=CukesRestTest test`
   - Result: `BUILD SUCCESS`
   - Tests run: `6`, Failures: `0`, Errors: `0`

2. `mvn -pl vehicleextention-service -am -Dtest=CukesRestTest -Dsurefire.failIfNoSpecifiedTests=false test`
   - Result: `BUILD SUCCESS`
   - Extension Cucumber tests run: `4`, Failures: `0`, Errors: `0`

Observed response proofs during execution include:

- Base flow returning state progression up to `CLOSED`
- Extension flow returning:
  - `insurancePolicyNumber = POL-999`
  - `fitnessExpiry = 2030-12-31`
  - `newColumn = CANNOT-DUPLICATE` after `/ext`

## 9. How to Run Locally

## 9.1 Build all modules

```bash
mvn clean install
```


## 10. Notes and Conventions

1. Module name uses existing spelling in repository: `vehicleextention-*`.
2. Discriminator property/column used for subtype routing: `ext_type`.
3. Extension and base currently share same controller endpoint path: `/vehicle`.
4. Workflow extension is implemented using extension `states.xml` + extension action bean.

## 11. Implementation Checklist (What was required)

- [x] Keep base modules intact (`vehicle-api`, `vehicle-service`)
- [x] Add extension modules (`vehicleextention-api`, `vehicleextention-service`)
- [x] Extend entity with additional columns
- [x] Keep single table inheritance
- [x] Keep base repository, avoid extension DAO contract duplication
- [x] Extend state machine transitions in extension `states.xml`
- [x] Add Cucumber coverage for base and extension flows
- [x] Add DB assertions for persisted column values
- [x] Validate via Maven test runs with `BUILD SUCCESS`
