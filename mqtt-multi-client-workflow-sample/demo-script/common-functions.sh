# Common functions for testing
tmpfile=/tmp/$$.${RANDOM}
source ansi-color-codes.sh

## Does a post and extracts the ID. echoes it back
## Usage: do_post port tenant openedBy description
function do_post {
	port=$(find_port $1)
	tenant=$2
	openedBy=$3
	description=$4

	curl -X POST http://localhost:${port}/issue \
		-H 'Content-Type: application/json' \
		-H "x-chenile-tenant-id: $tenant" \
		-H 'x-chenile-eid: User1' \
		-d '{
		   "openedBy" : "'${openedBy}'",
		    "description" : "'${description}'"
		}'  > $tmpfile 2> /dev/null

	id=$(cat $tmpfile | jq '.payload.mutatedEntity.id' |tr -d '"')
	if [[ $id != "" && $id != null ]]
	then
		echo "Successfully created the record with ID $id in $1 for tenant $tenant ($openedBy '$description')" >&2
	fi
	rm $tmpfile
	echo $id
}

## Does a put. The payload varies according to the event passed
## Usage: do_put port id tenant eventId
function do_put {
	port=$(find_port $1)
	id=$2
	tenant=$3
	eventId=$4
	if [[ $eventId == "completeTask" ]]
	then
	  taskId=$5
	fi

	curl -X PUT http://localhost:${port}/issue/${id}/${eventId} \
		-H 'Content-Type: application/json' \
		-H "x-chenile-tenant-id: $tenant" \
		-H 'x-chenile-eid: User1' \
		-d "$(get_payload $eventId $taskId)"  > $tmpfile 2> /dev/null

	success=$(cat $tmpfile | jq '.success' |tr -d '"')
	taskId=$(cat $tmpfile | jq '.payload.mutatedEntity.id' |tr -d '"')
  if [[ $success == "true" ]]
  then
    stateId=$(cat $tmpfile | jq '.payload.mutatedEntity.currentState.stateId' |tr -d '"' 2> /dev/null)
    taskId=$(cat $tmpfile | jq '.payload.mutatedEntity.tasks[0].id' |tr -d '"')
    echo "${GREEN}ID $id that was created has been sent an event $eventId. It processed successfully.$NC" >&2
    if [[ $stateId == $(valid_state $eventId) ]]
    then
      echo "${LIGHT_GREEN}New state is $stateId which is as expected.$NC" >&2
    else
      echo "${LIGHT_RED}New state is $stateId which is not as expected ($(valid_state $eventId)).$NC" >&2
    fi
    echo "New task ID is $taskId" >&2
    echo $taskId
    rm $tmpfile
    return 0
  else
    echo  "${RED}ID $id that was created could not be processed in $1. Ret value is $(cat $tmpfile)$NC" >&2
    rm $tmpfile
    return 1
  fi
}

# get_payload eventId [taskId]
# pass taskId only if eventId = completeTask
function get_payload {
  case $1 in
  "assign") echo '{
                  			"assignee": "MY-ASSIGNEE",
                  			"comment": "MY-ASSIGNEE-CAN-FIX-THIS"
                  }';;
  "addTask") echo '{
                   			"name": "investigate",
                   			"description": "Find out what is happening"
                   	}';;
  "completeTask") echo '{
                        			"taskId":"'${2}'",
                        			"comment": "Nothing found. cannot duplicate"
                        }';;
  "close") echo '{
                 			"comment": "OK"
                }';;
  "resolve") echo '{
                   			"comment": "CANNOT-DUPLICATE"
                   	}';;
  esac
}

function valid_state {
  case $1 in
  "assign") echo "ASSIGNED";;
  "addTask") echo "ASSIGNED";;
  "completeTask")echo "ASSIGNED";;
  "resolve") echo "RESOLVED";;
  "close")echo "CLOSED";;
  esac
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

## Usage do_get port id task-id
function do_get {
	curl -X GET http://localhost:$(find_port $1)/issue/$2 > $tmpfile 2> /dev/null
	success=$(cat $tmpfile | jq '.success' |tr -d '"')

	if [[ $success == "true" ]]
	then
	  stateId=$(cat $tmpfile | jq '.payload.mutatedEntity.currentState.stateId' |tr -d '"' 2> /dev/null)
	  assignComment=$(cat $tmpfile | jq '.payload.mutatedEntity.assignComment' |tr -d '"' 2> /dev/null)
	  resolveComment=$(cat $tmpfile | jq '.payload.mutatedEntity.resolveComment' |tr -d '"' 2> /dev/null)
	  closeComment=$(cat $tmpfile | jq '.payload.mutatedEntity.closeComment' |tr -d '"' 2> /dev/null)
		echo  "${GREEN}ID $id that was created is found successfully in $1. State ID is $stateId${NC}"
		echo "assignComment: $assignComment. ResolveComment: $resolveComment. closeComment: $closeComment"
		taskId=$(cat $tmpfile | jq '.payload.mutatedEntity.tasks[0].id' |tr -d '"')
		if [[ $taskId != "" && $taskId != null ]]
		then
		  echo "A new task was found with value $taskId"
		  if [[ -n $3 && $taskId != $3 ]]
		  then
		    echo  "${YELLOW}Task ID $taskId is not matching the passed taskID ($3)${NC}"
		  else
		    echo  "${GREEN}Task ID matches the passed Task ID ${NC}"
		  fi
		fi
	else
		echo  "${RED}ID $id that was created is not found in $1${NC}"
	fi
	rm $tmpfile
}

# Usage test_all_gets ID
function test_all_gets {
	id=$1
	taskId=$2
	do_get cloud $id $taskId
	do_get store1 $id $taskId
	do_get store2 $id $taskId
	do_get store3 $id $taskId
}

function do_all_tests {
  location=$1
  tenant=$2
  employee=$3
  description=$4
  id=$(do_post $location $tenant $employee "$description")
  test_all_gets $id
  echo ""
  echo "$PURPLE#####################Doing a put on assign in $location ###############################$NC"
  echo ""
  do_put $location $id $tenant assign
  test_all_gets $id
  echo ""
  echo "$PURPLE#######################Doing a put on addTask in $location #############################$NC"
  echo ""
  taskId=$(do_put $location $id $tenant addTask)
  test_all_gets $id
  echo ""
  echo "$PURPLE#######################Doing a put on completeTask in $location #############################$NC"
  echo ""
  do_put $location $id $tenant completeTask $taskId
  test_all_gets $id $taskId
  echo ""
  echo "$PURPLE#######################Doing a put on resolve in $location #############################$NC"
  echo ""
  do_put $location $id $tenant resolve
  test_all_gets $id $taskId
  echo ""
  echo "$PURPLE######################Doing a put on close in $location ##############################$NC"
  echo ""
  do_put $location $id $tenant close
  test_all_gets $id $taskId
}