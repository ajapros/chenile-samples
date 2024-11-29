Feature: Tests the Chenile Workflow Service using a REST client. Issue service exists and is under test.
It helps to create a issue and manages the state of the issue as follows:
OPENED -(assign) -> ASSIGNED -(resolve) -> RESOLVED -(close) -> CLOSED
 
  Scenario: Test create issue
    When I POST a REST request to URL "/issue" with payload
    """
    {
	    "openedBy": "USER1",
	    "description": "Description"
	}
	"""
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from  response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
    And the REST response key "mutatedEntity.openedBy" is "USER1"
    And the REST response key "mutatedEntity.description" is "Description"
	  
	Scenario: Retrieve the issue that just got created
	  When I GET a REST request to URL "/issue/${id}"
	  Then the REST response contains key "mutatedEntity"
	  And the REST response key "mutatedEntity.id" is "${id}"
	  And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
	  And the REST response key "mutatedEntity.openedBy" is "USER1"
	  
	Scenario: Assign the issue to an assignee with comments
		When I PUT a REST request to URL "/issue/${id}/assign" with payload
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
		When I PUT a REST request to URL "/issue/${id}/addTask" with payload
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
		And the REST response key "mutatedEntity.tasks[0].name" is "investigate"
		And the REST response key "mutatedEntity.tasks[0].id" is "${taskId}"

	Scenario: Resolve the issue with comments
		When I PUT a REST request to URL "/issue/${id}/resolve" with payload
		"""
		{
			"comment": "CANNOT-DUPLICATE"
		}
		"""
		And the REST response key "mutatedEntity.id" is "${id}"
		And the REST response key "mutatedEntity.currentState.stateId" is "RESOLVED"
	  
	 Scenario: Close the issue with comments
		When I PUT a REST request to URL "/issue/${id}/close" with payload
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
		When I PUT a REST request to URL "/issue/${id}/assign" with payload
		"""
		{
			"assignee": "MY-ASSIGNEE",
			"comment": "MY-ASSIGNEE-CAN-FIX-THIS"
		}
		"""
		Then the REST response does not contain key "mutatedEntity"
		And the http status code is 422

	  