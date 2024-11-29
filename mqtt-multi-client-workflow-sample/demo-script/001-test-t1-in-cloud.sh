# Tests for tenant t1 with all stores connected to the internet
# When the cloud is updated with tenant t1 it is expected that
# the cloud, store1 and store2 will reflect this record.
# It should not be reflected in store3  since it belongs to tenant t2
source common-functions.sh

do_all_tests "cloud" t1 jane_cloud "issue_with_computer.it_does_not_boot_in_the_cloud"
