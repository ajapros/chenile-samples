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
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.openedBy" is "USER-EXT"
    And the REST response key "mutatedEntity.description" is "Extension vehicle"
    And the REST response key "mutatedEntity.insurancePolicyNumber" is "POL-999"
    And the REST response key "mutatedEntity.fitnessExpiry" is "2030-12-31"
    And DB has vehicle extension row with policy "POL-999" fitness "2030-12-31" and type "client_abc_ext"
