#!/bin/bash
# Requires envs ACCESS_TOKEN, APP_SERVER_IP, APP_SERVER_PORT, MONITOR_IP and MONITOR_PORT
agentsEndpoint="http://$APP_SERVER_IP:$APP_SERVER_PORT/api/v1/agent"
AGENT_NUMBER=$(($RANDOM%10000))
AGENT_NAME="Agent_$AGENT_NUMBER"
response=$(curl \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" \
  --request POST \
  --data "{\"name\": \"$AGENT_NAME\",\"description\": \"Agent dla serwera numer K\",\"allowedOrigins\": \"\",\"isProxyAgent\": $AGENT_PROXY}" \
  "$agentsEndpoint")
echo $response
if [ $? -ne 0 ] ; then
  echo $response
  exit 1
fi

export AGENT_ID=$(echo $response | jq -r '.agentId')
if [ $? -ne 0 ] ; then
  exit 1
fi

export AGENT_ENCRYPTION_KEY=$(echo $response | jq -r '.agentEncryptionKey')
if [ $? -ne 0 ] ; then
  exit 1
fi

java -jar -Dagent.monitor.address=$MONITOR_IP -Dagent.monitor.port=$MONITOR_PORT -Dagent.id=$AGENT_ID -Dagent.encryptionKey=$AGENT_ENCRYPTION_KEY /app/app.jar