Feature: Packager multi-tenant workflow over REST, pub/sub and extension commands.

  Scenario: Tenant header drives tenant1 extension workflow and publishes tenant-aware event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "tenant1_ext",
      "openedBy": "PACKAGER-T1",
      "description": "Packager tenant1 extension",
      "tenant1Code": "BLR-01",
      "tenant1Segment": "CORP",
      "tenant1Priority": "P1",
      "insurancePolicyNumber": "POL-T1-101",
      "fitnessExpiry": "2030-12-31"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.tenant" is "tenant1"
    And the REST response key "mutatedEntity.tenant1Code" is "BLR-01"
    And the REST response key "mutatedEntity.tenant1Segment" is "CORP"
    And the REST response key "mutatedEntity.tenant1Priority" is "P1"

    Given I reset pubsub capture for 1 event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/assign" with payload
    """
    {
      "assignee": "PACKAGER-ASSIGNEE",
      "comment": "PACKAGER-ASSIGN"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"

    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/ext" with payload
    """
    {
      "comment": "PACKAGER-T1-EVENT"
    }

    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.tenant1Code" is "BLR-01"
    And the REST response key "mutatedEntity.tenant1WorkflowNote" is "tenant1-workflow-PACKAGER-T1-EVENT"
    And pubsub receives message containing "PACKAGER-T1-EVENT" and tenant "tenant1"
    And datasource "tenant1" contains tenant1 extension code "BLR-01"
    And datasource "tenant0" does not contain tenant1 extension code "BLR-01"

  Scenario: Tenant header drives tenant0 extension workflow and publishes tenant-aware event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "tenant0_ext",
      "openedBy": "PACKAGER-T0",
      "description": "Packager tenant0 extension",
      "tenant0Code": "T0-001",
      "tenant0Segment": "FLEET",
      "tenant0Priority": "HIGH",
      "insurancePolicyNumber": "POL-T0-101",
      "fitnessExpiry": "2030-12-31"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.tenant" is "tenant0"
    And the REST response key "mutatedEntity.tenant0Code" is "T0-001"
    And the REST response key "mutatedEntity.tenant0Segment" is "FLEET"
    And the REST response key "mutatedEntity.tenant0Priority" is "HIGH"

    Given I reset pubsub capture for 1 event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I PATCH a REST request to URL "/vehicle/${id}/assign" with payload
    """
    {
      "assignee": "PACKAGER-ASSIGNEE",
      "comment": "PACKAGER-ASSIGN"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"

    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I PATCH a REST request to URL "/vehicle/${id}/ext" with payload
    """
    {
      "comment": "PACKAGER-T0-EVENT"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.newColumn" is "PACKAGER-T0-EVENT"
    And the REST response key "mutatedEntity.tenant0WorkflowNote" is "tenant0-workflow-PACKAGER-T0-EVENT"
    And pubsub receives message containing "PACKAGER-T0-EVENT" and tenant "tenant0"
    And datasource "tenant0" contains tenant0 extension code "T0-001"
    And datasource "tenant1" does not contain tenant0 extension code "T0-001"
