Feature: Tests the s1 Service using a REST client. 
  Scenario: Check if the overridden service is called
    When I construct a REST request with header "x-chenile-trajectory-id" and value "t1"
    And I POST a REST request to URL "/s1/op1" with payload
    """
    {

	}
	"""
	Then the REST response key "id" is "S1T1ServiceImpl"
  Scenario: Set id to foo. See if it throws an exception with the appropriate error code for t1
    When I construct a REST request with header "x-chenile-trajectory-id" and value "t1"
    And I POST a REST request to URL "/s1/op1" with payload
    """
    {
	 "id": "foo"
	}
	"""
    Then the error array size is 1
    And the top level code is 500
    And the top level subErrorCode is 1235
    And the top level description is "ID with value foo is illegal"
  Scenario: Set id to foo. See if it throws an exception with the appropriate error code for non t1
    When I construct a REST request with header "x-chenile-trajectory-id" and value "t2"
    And I POST a REST request to URL "/s1/op1" with payload
    """
    {
	 "id": "foo"
	}
	"""
    Then the error array size is 1
    And the top level code is 500
    And the top level subErrorCode is 1234
    And the top level description is "ID with value foo is not valid"