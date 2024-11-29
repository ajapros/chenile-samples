Feature: Tests the s1 Service using a REST client. 
  Scenario: Trial test. Change it according to the actual service

    When I POST a REST request to URL "/s1/op1" with payload
    """
    {
	 
	}
	"""
	Then the REST response key "id" is "S1ServiceImpl"
  Scenario: Set id to foo. See if it throws an exception

    When I POST a REST request to URL "/s1/op1" with payload
    """
    {
	 "id": "foo"
	}
	"""
    Then the error array size is 1
    And the top level code is 500
    And the top level subErrorCode is 1234
    And the top level description is "ID with value foo is not valid"
	 
    