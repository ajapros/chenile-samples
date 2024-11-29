# Tests for tenant t1 with all stores connected to the internet
# When the cloud is updated with tenant t1 it is expected that
# the cloud, store1 and store2 will reflect this record.
# It should not be reflected in store3  since it belongs to tenant t2
source common-functions.sh

id=$(do_post cloud t1 jane doe)
test_all_gets $id
