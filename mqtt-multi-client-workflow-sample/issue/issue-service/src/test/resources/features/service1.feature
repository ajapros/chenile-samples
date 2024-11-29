Feature: Tests the Chenile Workflow Service using a REST client with an existing ID.
  Issue service exists and is under test. This is required to spread the same ID to multiple stores
  using MQTT.
  It helps to create a issue and manages the state of the issue as follows:
  OPENED -(assign) -> ASSIGNED -(resolve) -> RESOLVED -(close) -> CLOSED

  Scenario: Test create issue
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I POST a REST request to URL "/issue" with payload
    """
    {
    	"id": "my-own-unique-id",
	    "openedBy": "USER1",
	    "description": "Description",
	    "currentState": {
	    	"stateId": "OPENED",
	    	"flowId": "test-flow"
	    }
	}
	"""
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from  response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.openedBy" is "USER1"
    And the REST response key "mutatedEntity.description" is "Description"

  Scenario: Retrieve the issue that just got created
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I GET a REST request to URL "/issue/${id}"
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.openedBy" is "USER1"

  Scenario: Assign the issue to an assignee with comments\
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I PUT a REST request to URL "/issue/${id}/assign" with payload
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

  Scenario: Add a Task to the issue
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I PUT a REST request to URL "/issue/${id}/addTask" with payload
		"""
		{
			"name": "investigate",
			"description": "Find out what is happening"
		}
		"""
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
    And the REST response key "mutatedEntity.tasks[0].name" is "investigate"
    And the REST response key "mutatedEntity.tasks[0].id" contains string "task-REQUEST-ID"
    And the REST response key "mutatedEntity.tasks[0].stateId" is "OPENED"
    And store "$.payload.mutatedEntity.tasks[0].id" from  response to "taskId"

  Scenario: Resolve the issue with comments
    When I PUT a REST request to URL "/issue/${id}/resolve" with payload
		"""
		{
			"comment": "CANNOT-DUPLICATE"
		}
		"""
    Then the REST response does not contain key "mutatedEntity"
    And the http status code is 422

  Scenario: Complete the task with comments
    When I PUT a REST request to URL "/issue/${id}/completeTask" with payload
		"""
		{
			"taskId":"${taskId}",
			"comment": "Nothing found. cannot duplicate"
		}
		"""
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
    And the REST response key "mutatedEntity.tasks[0].stateId" is "COMPLETED"

  Scenario: Resolve the issue with comments
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I PUT a REST request to URL "/issue/${id}/resolve" with payload
		"""
		{
			"comment": "CANNOT-DUPLICATE"
		}
		"""
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "RESOLVED"
    And the REST response key "mutatedEntity.resolveComment" is "CANNOT-DUPLICATE"

  Scenario: Close the issue with comments
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I PUT a REST request to URL "/issue/${id}/close" with payload
		"""
		{
			"comment": "OK"
		}
		"""
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "CLOSED"
    And the REST response key "mutatedEntity.closeComment" is "OK"

  Scenario: Assign a closed issue to someone. This will err out.
    When I construct a REST request with header "x-request-id" and value "REQUEST-ID"
    And I PUT a REST request to URL "/issue/${id}/assign" with payload
		"""
		{
			"assignee": "MY-ASSIGNEE",
			"comment": "MY-ASSIGNEE-CAN-FIX-THIS"
		}
		"""
    Then the REST response does not contain key "mutatedEntity"
    And the http status code is 422

	  