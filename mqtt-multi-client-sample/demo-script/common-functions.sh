# Common functions for testing
tmpfile=/tmp/$$.${RANDOM}
## Does a post and extracts the ID. echoes it back
## Usage: do_post port tenant firstname lastname
function do_post {
	port=$(find_port $1)
	tenant=$2
	firstname=$3
	lastname=$4

	curl -X POST http://localhost:${port}/employee \
		-H 'Content-Type: application/json' \
		-H "x-chenile-tenant-id: $tenant" \
		-H 'x-chenile-eid: User1' \
		-d '{
		   "lastName" : "'${lastname}'",
		    "firstName" : "'${firstname}'",
		    "email": "'${firstname}_$lastname'@example.com"
		}'  > $tmpfile 2> /dev/null

	id=$(cat $tmpfile | jq '.payload.id' |tr -d '"')
	if [[ id != "" ]]
	then
		echo "Successfully created the record with ID $id in $1 for tenant $tenant ($firstname $lastname)" >&2
	fi
	rm $tmpfile
	echo $id
}

## Usage: find_port cloud|store1|store2|store3
function find_port {
	case $1 in 
		"cloud") echo 8000; return ;;
		"store1") echo 8001; return ;;
		"store2") echo 8002; return ;;
		"store3") echo 8003; return ;;
	esac
}

## Usage do_get port id
function do_get {
	curl -X GET http://localhost:$(find_port $1)/employee/$2 > $tmpfile 2> /dev/null
	success=$(cat $tmpfile | jq '.success' |tr -d '"')
	if [[ $success == "true" ]]
	then
		echo "ID $id that was created is found successfully in $1"
	else
		echo "ID $id that was created is not found in $1"
	fi
	rm $tmpfile
}

# Usage test_all_gets ID
function test_all_gets {
	id=$1
	do_get cloud $id
	do_get store1 $id
	do_get store2 $id
	do_get store3 $id
}