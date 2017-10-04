


Authorization Code Flow

curl -c cookie.txt "http://localhost:8180/auth/realms/ds/protocol/openid-connect/auth?response_type=code&client_id=service-user&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapp%2Fcheck&nonce=abcdefg&scope=openid"

Implicit Flow

curl -c cookie.txt "http://localhost:8180/auth/realms/ds/protocol/openid-connect/auth?response_type=token&client_id=service-user&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapp%2Fcheck&nonce=abcdefg&scope=openid"


Resource Owner Password Credentials Grant

TOKEN=`curl -XPOST -s -u service-user:123456 -d grant_type=password -d username=test1 -d password=123456 http://localhost:8180/auth/realms/ds/protocol/openid-connect/token | jq -r .access_token`

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/app/check | jq


Client Credentials Grant

TOKEN=`curl -XPOST -s -u service-user:123456 -d grant_type=client_credentials http://localhost:8180/auth/realms/ds/protocol/openid-connect/token | jq -r .access_token`

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/app/check | jq

