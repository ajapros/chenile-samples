 
Feature: Tests the Order Query Service using a REST client.

Scenario: Tests out pagination capability
Given that user is "finance"
When I POST a REST request to URL "/q/orders" with payload
"""
{
	"sortCriteria" :[
		{"name":"amount","ascendingOrder": true}
	],
	"pageNum": 2,
	"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true 
And the REST response key "numRowsReturned" is "15"
And the REST response key "currentPage" is "2"
And the REST response key "maxPages" is "2"
And the REST response key "maxRows" is "30"
And the REST response key "list[0].row.id" is "16"
And the REST response key "list[0].row.amount" is "2015"
And the REST response key "list[0].row.stateId" is "PAYMENT_INITIATED"
And the REST response key "list[0].row.orderDate" is "2024-05-16 12:00:00"
And the REST response key "list[0].allowedActions[0].allowedAction" is "confirmPayment"

Scenario: Tests out pagination capability for minimal_user. This user should not get allowed actions
    for row that has PAYMENT_INITIATED
Given that user is "minimal_user"
When I POST a REST request to URL "/q/orders" with payload
"""
{
	"sortCriteria" :[
		{"name":"amount","ascendingOrder": true}
	],
	"pageNum": 2,
	"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "15"
And the REST response key "currentPage" is "2"
And the REST response key "maxPages" is "2"
And the REST response key "maxRows" is "30"
And the REST response key "list[0].row.id" is "16"
And the REST response key "list[0].row.amount" is "2015"
And the REST response key "list[0].row.stateId" is "PAYMENT_INITIATED"
And the REST response key "list[0].row.orderDate" is "2024-05-16 12:00:00"
And the REST response does not contain key "list[0].allowedActions[0].allowedAction"

Scenario: Test Between - Test higher and lower bounds between query
Given that user is "finance"
When I POST a REST request to URL "/q/orders" with payload
"""
{
	"filters" :{
		"amount": [2000,2005]
	},
	"sortCriteria" :[
		{"name":"amount","ascendingOrder": true}
	],
	"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "6"
And the REST response key "currentPage" is "1"
And the REST response key "maxRows" is "6"
And the REST response key "list[0].row.id" is "1"
And the REST response key "list[0].row.amount" is "2000"
And the REST response key "list[0].row.orderDate" is "2024-05-01 12:00:00"
And the REST response key "list[0].row.stateId" is "CREATED"
And the REST response key "list[0].allowedActions[0].allowedAction" is "initiatePayment"
And the REST response key "list[0].allowedActions[1].allowedAction" is "close"
And the REST response key "list[0].allowedActions[1].isCombinable" is "false"
And the REST response key "list[5].row.id" is "6"
And the REST response key "list[5].row.amount" is "2005"
And the REST response key "list[5].row.orderDate" is "2024-05-06 12:00:00"
And the REST response key "list[5].row.stateId" is "CREATED"

Scenario: Test Between - Test higher and lower bounds between query for one bound only (i.e. between query becomes an equals query)
When I POST a REST request to URL "/q/orders" with payload
"""
{
"filters" :{
    "amount": 2000
},
"sortCriteria" :[
    {"name":"amount","ascendingOrder": true}
],
"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "1"
And the REST response key "currentPage" is "1"
And the REST response key "maxRows" is "1"
And the REST response key "list[0].row.id" is "1"
And the REST response key "list[0].row.amount" is "2000"
And the REST response key "list[0].row.orderDate" is "2024-05-01 12:00:00"

Scenario: Test Between - Test higher and lower bounds between query for dates
    When I POST a REST request to URL "/q/orders" with payload
"""
{
	"filters" :{
		"order_date": ["2024-05-01", "2024-05-05"]
	},
	"sortCriteria" :[
		{"name":"amount","ascendingOrder": true}
	],
	"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "4"
And the REST response key "currentPage" is "1"
And the REST response key "maxRows" is "4"
And the REST response key "list[0].row.id" is "1"
And the REST response key "list[0].row.amount" is "2000"
And the REST response key "list[0].row.orderDate" is "2024-05-01 12:00:00"
And the REST response key "list[3].row.id" is "4"
And the REST response key "list[3].row.amount" is "2003"
And the REST response key "list[3].row.orderDate" is "2024-05-04 12:00:00"

Scenario: Test Between - Test higher and lower bounds between query for dates precise to hours
When I POST a REST request to URL "/q/orders" with payload
"""
{
"filters" :{
"order_date": ["2024-05-01 00:00", "2024-05-05 23:59"]
},
"sortCriteria" :[
{"name":"amount","ascendingOrder": true}
],
"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "5"
And the REST response key "currentPage" is "1"
And the REST response key "maxRows" is "5"
And the REST response key "list[0].row.id" is "1"
And the REST response key "list[0].row.amount" is "2000"
And the REST response key "list[0].row.orderDate" is "2024-05-01 12:00:00"
And the REST response key "list[4].row.id" is "5"
And the REST response key "list[4].row.amount" is "2004"
And the REST response key "list[4].row.orderDate" is "2024-05-05 12:00:00"


Scenario: Test Between - Test higher and lower bounds between query for dates with one bound only (i.e. between query becomes an equals query)
When I POST a REST request to URL "/q/orders" with payload
"""
{
"filters" :{
    "order_date": "2024-05-01 12:00:00"
},
"sortCriteria" :[
    {"name":"amount","ascendingOrder": true}
],
"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "1"
And the REST response key "currentPage" is "1"
And the REST response key "maxRows" is "1"
And the REST response key "list[0].row.id" is "1"
And the REST response key "list[0].row.amount" is "2000"
And the REST response key "list[0].row.orderDate" is "2024-05-01 12:00:00"

Scenario: Test the sub query at the collection level - Test higher and lower bounds between query
    When I POST a REST request to URL "/q/orders" with payload
"""
{
	"filters" :{
		"amount": [2000,2005],
		"product": "prod11"
	},
	"sortCriteria" :[
		{"name":"amount","ascendingOrder": true}
	],
	"numRowsInPage": 15
}
"""
    Then the http status code is 200
    And the top level code is 200
    And success is true
    # cos only one order has prod11 only one row will be returned
    And the REST response key "maxRows" is "1"
    And the REST response key "numRowsReturned" is "1"
    And the REST response key "currentPage" is "1"
    And the REST response key "list[0].row.id" is "1"

Scenario: Test sort criteria at the order lines level
    Given that user is "bank"
    When I POST a REST request to URL "/q/orders" with payload
"""
{
"filters" :{
    "amount": [2000,2005]
},
"sortCriteria" :[
    {"name":"amount","ascendingOrder": true},
    {"name":"product","ascendingOrder": false}
],
"numRowsInPage": 15
}
"""
    Then the http status code is 200
    And the top level code is 200
    And success is true
    And the REST response key "maxRows" is "6"
    And the REST response key "numRowsReturned" is "6"
    And the REST response key "currentPage" is "1"
    And the REST response key "list[0].row.id" is "1"
    # since it is descending sorted by product prod12 must appear before prod11
    And the REST response key "list[0].row.orderLines[0].product" is "prod12"

Scenario: Test toDoList functionality - Bank can only confirm payments.
    So it must only show all orders with state Id as "PAYMENT_INITIATED" (rows 16 to 20)
    The expected order is 17,18,19,16,20 (since ID 16 and 20 will have the same sla_red_date
    16 will come before 20 because sla_yellow_date of 16 < sla_yellow_date of 20)
Given that user is "bank"
When I POST a REST request to URL "/q/orders" with payload
"""
{
"toDoList": true,
"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "maxRows" is "5"
And the REST response key "numRowsReturned" is "5"
And the REST response key "currentPage" is "1"
And the REST response key "list[0].row.id" is "17"
And the REST response key "list[0].row.stateId" is "PAYMENT_INITIATED"
And the REST response key "list[0].allowedActions[0].allowedAction" is "confirmPayment"
And the REST response key "list[1].row.id" is "18"
And the REST response key "list[2].row.id" is "19"
And the REST response key "list[3].row.id" is "16"
And the REST response key "list[4].row.id" is "20"
And the REST response key "list[4].row.stateId" is "PAYMENT_INITIATED"
And the REST response key "list[4].allowedActions[0].allowedAction" is "confirmPayment"