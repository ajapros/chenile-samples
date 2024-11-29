# Orders Sample
This project demonstrates the simplicity of writing a typical two level grid query using the chenile
mybatis query controller. 
There is no Java code required. All the logic is in the two mapper files. 
This project also demonstrates the power of BDD to validate the results of the query.

## What is being illustrated?
Imagine a typical two level query system such as orders and order lines. An order exists with multiple 
order lines. When an order is fetched, we should also proactively fetch all the order lines associated 
with it. We should return the orders and the order lines as a nested JSON structure. When a page full of 
data is requested then the pagination must happen at the level of orders (and not a combination of orders
and order lines). For example if page size is 10, then there must be 10 orders retrieved along with all 
the order lines that pertain to the order. 
Query filters can be at the level of orders and order lines as well. There are multiple types of query 
filters that are possible. In this case we show two types of filters - a between filter (for order amounts)
and an IN filter (for products in an order line)

## Mapper File
The Mybatis Mapper file order.xml illustrates the power of Mybatis. It contains two queries:
1. a getAll query that fetches the applicable orders and order lines with appropriate filters, 
pagination and order by that are applied at both the order and order line level.
2. a getAll-count query that fetches the count of distinct order IDs that match the passed criteria. 
Please read the comments inside the file for more information on how these queries are crafted.

# JSON 
The JSON file provides metadata about the query and the relevant information about all the columns
that are returned. 


