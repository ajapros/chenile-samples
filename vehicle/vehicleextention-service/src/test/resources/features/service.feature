Feature: Tests vehicle extension workflow over REST and validates DB persistence.

  Scenario: Create extension vehicle and validate response + DB
    When I POST a REST request to URL "/vehicle" with payload
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
    When I PATCH a REST request to URL "/vehicle/${id}/assign" with payload
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

  Scenario: Resolve the vehicle with comments
    When I PATCH a REST request to URL "/vehicle/${id}/ext" with payload
		"""
		{
			"comment": "CANNOT-DUPLICATE"
		}
		"""
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "EXTENSION"
    And the REST response key "mutatedEntity.newColumn" is "CANNOT-DUPLICATE"

  Scenario: Close the vehicle with comments
    When I PATCH a REST request to URL "/vehicle/${id}/close" with payload
		"""
		{
			"comment": "OK"
		}
		"""
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "CLOSED"
    And the REST response key "mutatedEntity.closeComment" is "OK"