# Tests for tenant 1 with all stores connected except store2
# When store2 is updated with tenant t1 it is expected that
# only store2 will be initially updated since there is no cloud connection from store2
# However, when the cloud connection gets established, we will wait for a few seconds 
# for the connection to MQTT being re-established. After that, the expectation is as follows:
# The cloud, store1 and store2 will reflect this record.
# It should not be reflected in store3 since it belongs to tenant t2

source common-functions.sh

echo ""
echo "$PURPLE #####################Disconnecting Store2 from network###########################"
echo ""
docker network disconnect cloud-net store2
do_all_tests "store2" t1 joe_store2 "issue_with_computer.it_does_not_boot_in_store2"

echo "$PURPLE ############Connecting back store2 to network. I will sleep for 60 seconds after that###"
docker network connect cloud-net store2
sleep 60
test_all_gets $id $taskId
