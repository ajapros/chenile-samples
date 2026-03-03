Feature: Tests vehicle extension workflow over REST, pub/sub and DB persistence.

  Scenario: Create extension vehicle and validate response + DB
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "client_abc_ext",
      "openedBy": "USER-EXT",
      "description": "Extension vehicle",
      "insurancePolicyNumber": "POL-999",
      "fitnessExpiry": "2030-12-31"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.openedBy" is "USER-EXT"
    And the REST response key "mutatedEntity.description" is "Extension vehicle"
    And the REST response key "mutatedEntity.insurancePolicyNumber" is "POL-999"
    And the REST response key "mutatedEntity.fitnessExpiry" is "2030-12-31"
    And DB has extension vehicle row with extType "client_abc_ext" openedBy "USER-EXT" description "Extension vehicle" policy "POL-999" fitness "2030-12-31"

  Scenario: Assign the vehicle to an assignee with comments
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/assign" with payload
    """
    {
      "assignee": "MY-ASSIGNEE",
      "comment": "MY-ASSIGNEE-CAN-FIX-THIS"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
    And the REST response key "mutatedEntity.assignee" is "MY-ASSIGNEE"
    And the REST response key "mutatedEntity.assignComment" is "MY-ASSIGNEE-CAN-FIX-THIS"

  Scenario: Ext command publishes pubsub event and subscriber receives it
    Given I reset pubsub capture for 1 event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/ext" with payload
    """
    {
      "comment": "CANNOT-DUPLICATE"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.newColumn" is "CANNOT-DUPLICATE"
    And pubsub receives message containing "CANNOT-DUPLICATE" and tenant "tenant1"

  Scenario: Close the vehicle with comments
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${id}/close" with payload
    """
    {
      "comment": "OK"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "CLOSED"
    And the REST response key "mutatedEntity.closeComment" is "OK"

  Scenario: Publish from ext command with explicit tenant header and receive with same tenant
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "client_abc_ext",
      "openedBy": "USER-T1",
      "description": "Tenant1 extension vehicle",
      "insurancePolicyNumber": "POL-T1",
      "fitnessExpiry": "2031-01-01"
    }
    """
    Then store "$.payload.mutatedEntity.id" from response to "tenant1Id"
    Given I reset pubsub capture for 1 event
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${tenant1Id}/assign" with payload
    """
    {
      "assignee": "TENANT1-ASSIGNEE",
      "comment": "TENANT1-ASSIGN"
    }
    """
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${tenant1Id}/ext" with payload
    """
    {
      "comment": "TENANT1-EVENT"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And pubsub receives message containing "TENANT1-EVENT" and tenant "tenant1"

  Scenario: Multi-tenant check - default tenant data when header is absent
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I GET a REST request to URL "/test/items"
    Then the http status code is 200
    And success is true
    And the REST response key "items" collection has an item with keys and values:
      | key    | value     |
      | name   | t1-item-a |
      | tenant | tenant1   |

  Scenario: Multi-tenant check - tenant2 data when tenant header is passed
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant2"
    And I GET a REST request to URL "/test/items"
    Then the http status code is 200
    And success is true
    And the REST response key "items" collection has an item with keys and values:
      | key    | value     |
      | name   | t2-item-a |
      | tenant | tenant2   |

  Scenario: Create second tenant extension client_xyz and process ext transition
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/vehicle" with payload
    """
    {
      "ext_type": "client_xyz_ext",
      "openedBy": "USER-XYZ",
      "description": "XYZ Extension vehicle",
      "insurancePolicyNumber": "POL-XYZ",
      "fitnessExpiry": "2033-01-01",
      "xyzBranchCode": "BLR-01",
      "xyzSegment": "CORP",
      "xyzPriority": "P1"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "xyzId"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.insurancePolicyNumber" is "POL-XYZ"
    And the REST response key "mutatedEntity.xyzBranchCode" is "BLR-01"
    And the REST response key "mutatedEntity.xyzSegment" is "CORP"
    And the REST response key "mutatedEntity.xyzPriority" is "P1"
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${xyzId}/assign" with payload
    """
    {
      "assignee": "XYZ-ASSIGNEE",
      "comment": "XYZ-ASSIGN"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I PATCH a REST request to URL "/vehicle/${xyzId}/ext" with payload
    """
    {
      "comment": "XYZ-EVENT"
    }
    """
    Then the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.newColumn" is "XYZ-EVENT"
