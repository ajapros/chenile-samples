curl -X POST http://localhost:8180/realms/tenant0/protocol/openid-connect/token \
-H 'content-type: application/x-www-form-urlencoded' \
-d 'client_id=authz-servlet&client_secret=secret' \
-d 'grant_type=client_credentials' 2> /dev/null | jq --raw-output '.access_token' 2> /dev/null

