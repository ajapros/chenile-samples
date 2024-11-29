Feature: Tests the secservice Service using a REST client. 
 
  Scenario: In Tenant0 Test Premium User for Premium Resource
    When I construct a REST request with authorization header in realm "tenant0" for user "t0-premium" and password "t0-premium"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I POST a REST request to URL "/sec/premium" with payload
    """
    {
	}
	"""
	Then the REST response key "id" is "SecServiceImpl"

  Scenario: In Tenant0 Test Normal User for Premium Resource
    When I construct a REST request with authorization header in realm "tenant0" for user "t0-normal" and password "t0-normal"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I POST a REST request to URL "/sec/premium" with payload
    """
    {
	}
	"""
    Then the http status code is 403

  Scenario: In Tenant1 Test Premium User for Premium Resource
    When I construct a REST request with authorization header in realm "tenant1" for user "t1-premium" and password "t1-premium"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/sec/premium" with payload
    """
    {
	}
	"""
    Then the REST response key "id" is "SecServiceImpl"

  Scenario: In Tenant1 Test Normal User for Premium Resource
    When I construct a REST request with authorization header in realm "tenant1" for user "t1-normal" and password "t1-normal"
    And I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/sec/premium" with payload
    """
    {
	}
	"""
    Then the http status code is 403

  Scenario: In Tenant1 Test with no authorization for Premium Resource
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/sec/premium" with payload
    """
    {
	}
	"""
    Then the http status code is 401
    
