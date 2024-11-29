# Tests for tenant t2 with all stores connected to the internet
# When store3 is updated with tenant t2 it is expected that
# both the cloud and store3 will reflect this record.
# It should not be reflected in store1 and store2 since they belong to tenant t1
source common-functions.sh
do_all_tests "store3" t2 james_store3 "issue_with_computer.it_does_not_boot_in_store3"

