Feature: Tests the employee Service using a REST client.
 
  Scenario: Save the employee first.
    Given that "tenant" equals "t1"
    And that "employee" equals "E1"
    When I construct a REST request with header "x-chenile-tenant-id" and value "${tenant}"
    And I construct a REST request with header "x-chenile-eid" and value "${employee}"
    And I POST a REST request to URL "/employee" with payload
    """
    {
      "lastName": "Smith",
      "firstName" : "Joe",
	  "email": "joesmith@example.com"
	}
	"""
	Then success is true
    And store "$.payload.id" from  response to "id"
    And the REST response key "tenant" is "${tenant}"
    And the REST response key "createdBy" is "${employee}"

  Scenario: Retrieve the saved employee
    Given that "entity" equals "employee"
    When I GET a REST request to URL "/${entity}/${id}"
    Then success is true
    And the REST response key "id" is "${id}"

  Scenario: Save an employee using an ID that already is determined
  When I construct a REST request with header "x-chenile-tenant-id" and value "t1"
  And I construct a REST request with header "x-chenile-eid" and value "E1"
  When I POST a REST request to URL "/employee" with payload
  """
  {
    "id": "123",
    "lastName": "Smith",
    "firstName" : "John",
    "email": "johnsmith@example.com"
  }
  """
  Then success is true
  And the REST response key "id" is "123"

  Scenario: Retrieve the saved employee
  Given that "entity" equals "employee"
  When I GET a REST request to URL "/${entity}/123"
  Then success is true
  And the REST response key "id" is "123"
  And the REST response key "tenant" is "t1"
  And the REST response key "createdBy" is "E1"

    