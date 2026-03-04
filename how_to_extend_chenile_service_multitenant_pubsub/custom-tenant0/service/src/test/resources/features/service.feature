Feature: Tenant0 extension workflow over REST, pub/sub and DB persistence.

  Scenario: Create tenant0 extension and validate tenant0 columns
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "tenant0_ext",
      "openedBy": "USER-T0",
      "description": "Tenant0 vehicle",
      "tenant0Code": "T0-001",
      "tenant0Segment": "FLEET",
      "tenant0Priority": "HIGH"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.tenant" is "tenant0"
    And the REST response key "mutatedEntity.tenant0Code" is "T0-001"
    And the REST response key "mutatedEntity.tenant0Segment" is "FLEET"
    And the REST response key "mutatedEntity.tenant0Priority" is "HIGH"

  Scenario: Tenant0 custom ext command modifies tenant0 data and publishes
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I PATCH a REST request to URL "/vehicle/${id}/assign" with payload
    """
    {
      "assignee": "TENANT0-ASSIGNEE",
      "comment": "TENANT0-ASSIGN"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
    Given I reset pubsub capture for 1 event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I PATCH a REST request to URL "/vehicle/${id}/ext" with payload
    """
    {
      "comment": "TENANT0-EVENT"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.newColumn" is "TENANT0-EVENT"
    And the REST response key "mutatedEntity.tenant0WorkflowNote" is "tenant0-workflow-TENANT0-EVENT"
    And pubsub receives message containing "TENANT0-EVENT" and tenant "tenant0"
    And DB has tenant0 extension row with code "T0-001" segment "FLEET" priority "HIGH" workflowNote "tenant0-workflow-TENANT0-EVENT"

  Scenario: Close tenant0 extension after ext command
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I PATCH a REST request to URL "/vehicle/${id}/close" with payload
    """
    {
      "comment": "CLOSED-BY-T0"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "CLOSED"
    And the REST response key "mutatedEntity.closeComment" is "CLOSED-BY-T0"
