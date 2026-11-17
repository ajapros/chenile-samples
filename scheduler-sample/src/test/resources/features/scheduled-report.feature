Feature: A schedule fires an event; the report service subscribes and enqueues idempotent work

  Scenario: The daily-report schedule enqueues one work item, however often it fires
    Then within 5 seconds a work item with key "scheduled-report:daily-sales:2026-06-23" is pending
    And after 2 more seconds there is still 1 pending work item

  Scenario: A worker processes the enqueued work item
    Then within 5 seconds a work item with key "scheduled-report:daily-sales:2026-06-23" is pending
    When a worker processes one work item
    Then the work item with key "scheduled-report:daily-sales:2026-06-23" has status "SUCCESS"
