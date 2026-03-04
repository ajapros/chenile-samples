Feature: Tenant1 extension workflow over REST, pub/sub and DB persistence.

  Scenario: Create tenant1 extension and validate tenant1 columns
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "tenant1_ext",
      "openedBy": "USER-T1",
      "description": "Tenant1 vehicle",
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

  Scenario: Tenant1 ext command modifies tenant1 data and publishes
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/assign" with payload
    """
    {
      "assignee": "TENANT1-ASSIGNEE",
      "comment": "TENANT1-ASSIGN"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
    Given I reset pubsub capture for 1 event

    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/ext" with payload
    """
    {
      "comment": "TENANT1-EVENT"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.tenant1Code" is "BLR-01"
    And the REST response key "mutatedEntity.tenant1WorkflowNote" is "tenant1-workflow-TENANT1-EVENT"
    And pubsub receives message containing "TENANT1-EVENT" and tenant "tenant1"
    And DB has tenant1 extension row with code "BLR-01" segment "CORP" priority "P1" workflowNote "tenant1-workflow-TENANT1-EVENT"

  Scenario: Close tenant1 extension after ext command
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/close" with payload
    """
    {
      "comment": "CLOSED-BY-T1"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "CLOSED"
    And the REST response key "mutatedEntity.closeComment" is "CLOSED-BY-T1"
