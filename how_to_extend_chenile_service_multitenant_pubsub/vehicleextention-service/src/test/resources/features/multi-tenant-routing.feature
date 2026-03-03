Feature: Multi-tenant datasource routing in extension sample

  Scenario: Default tenant datasource is used when no tenant header is set
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I GET a REST request to URL "/test/items"
    Then the http status code is 200
    And success is true
    And the REST response key "items" collection has an item with keys and values:
      | key    | value     |
      | name   | t1-item-a |
      | tenant | tenant1   |
    And the REST response key "items" collection has an item with keys and values:
      | key    | value     |
      | name   | t1-item-b |
      | tenant | tenant1   |

  Scenario: Tenant2 header routes to tenant2 datasource
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant2"
    And I GET a REST request to URL "/test/items"
    Then the http status code is 200
    And success is true
    And the REST response key "items" collection has an item with keys and values:
      | key    | value     |
      | name   | t2-item-a |
      | tenant | tenant2   |
    And the REST response key "items" collection has an item with keys and values:
      | key    | value     |
      | name   | t2-item-b |
      | tenant | tenant2   |
