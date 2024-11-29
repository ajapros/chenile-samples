# Demonstration of Multi-Tenant-Edge Architecture with Chenile

This demo serves a few purposes:
1. It demonstrates how to use MQ-TT in practice in a Chenile application.
2. It shows how to configure MQ-TT for multi-tenancy.
3. It illustrates how Chenile seamlessly integrates with Spring repositories and how it leverages what they bring. It also shows how the ID generation can be made optional for a new record. It only creates an ID if it is not already supplied. This is important if we need to propagate the same record across multiple edges and the cloud.
4. It shows the Chenile Hub-Spoke model for propagating data across the cloud and edges for MQ-TT apps. It demonstrates the power of the cloud-edge-switch in chenile.
5. It shows how to use the cloud switch to switch between edge and cloud behavior and how we can use the hub and spoke model to automatically propagate data from the cloud to the edge and vice-versa.
6. It illustrates the use of Chenile HTTP proxy in the cloud-edge switch.
7. It shows dockerization and how to make different containers for the cloud and edge and how to start these containers in different edges.
8. Finally, we have a docker compose file that demonstrates how to start 5 containers (1 cloud, 3 store and 1 emqx) 

## How does the Hub & Spoke Model work in a multi tenant installation.
We have store1 and store2 who belong to tenant t1. We have store3 which belongs to tenant2. The cloud
of course, serves all tenants. 

Test 001 -> When the cloud is updated for tenant t1 all the stores that belong to tenant t1 will get updated for the same record.
Test 002 -> When store2 is updated for tenant t1 the record will instantly get updated in the cloud and the changes will then get propagated to all the stores that belong to tenant t1. (store1 and store2)
Test 003 -> When store2 is disconnected and store2 is updated with a record then store2 will get upgraded first. However, the system waits for the connection to be re-established. Once the connection gets re-established, then the record gets propagated first to the cloud and from the cloud down back to all the stores that belong to tenant t1 (store1 and store2). Since store2 already has the record, it will not update itself again.
Test 004 -> When store3 is updated with tenant t2 then both the cloud and store3 will get the updates. Store1 and store2 will not see the updates since they belong to tenant t1.


## How to run the demo

### Requirements
* Make sure that Java and maven are installed. (Download and install)
* 'make' must be installed (Enable the developer utilities in MAC)
* docker and docker compose must be installed. (goto docker web site and download and install)
* jq for testing the stuff (brew install jq in MAC) 
* curl for sending requests to the server. Postman is an alternative

### Steps
$ # Clone the samples repository.
$ git clone https://github.com/rajakolluru/chenile-samples.git
$ cd mqtt-multi-client-sample
$ cd employee
$ mvn install # to build the employee service and its contract
$ cd ../employeed
$ mvn install # to build the mini monolith 
$ make store-image cloud-image 
$ # the above will make the images.
$ cd .. # goto the root folder
$ docker compose up  # starts the five containers

### Test the program
We have enabled the h2-console to verify the results. You can start the following four URLs in a browser:
1. localhost:8000/h2-console - for the cloud
2. localhost:8001/h2-console - for store1
3. localhost:8002/h2-console - for store2
4. localhost:8003/h2-console - for store3

### Curl Scripts
Some curl scripts are bundled to test out the functionality. They reside in demo-script folder and are quite self explanatory.

You can look at the comments in each one of them and run them one after the other. They have been
numbered accordingly. 




