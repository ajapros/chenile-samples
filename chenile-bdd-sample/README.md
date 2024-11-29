
## Test case that demonstrates chenile-bdd
The test case that demonstrates the BDD requires two things to be running:
1. The Keycloak server in port 8180
2. The price server in port 8080

To run keycloak, use keycloak-start.sh script.
To run the price server, compile the price and priced modules under the "service-with-persistence" project. 
Run the priced server using "make run" in folder service-with-persistence/priced.

