# NSM Agent Application

## Requirements

- Linux OS 
- JAVA ~> 11
- Maven and installed netstat and jq (or Docker) 

## Configuration and Run with Maven

1. Make sure that in application.yml file are set appropriate values for server.ssl keys. 
    ```
   ATTENTION: DO NOT USE INCLUDED SSL CERTIFICATES DUE TO SECURITY REASONS
   ```
2. For production environment make sure that in applications.yml file value of agent.ssl.validation.enabled and 
appdynamics.force.default.ssl.certificate.validation are set to true
3. Use Maven to create jar package
    ```
   mvn package -DskipTests
   ```
4. Run application with proper values for connections parameters 
    ```
   java -jar /app/app.jar \
   -Dmonitor.ip=[value] \
   -Dmonitor.port=[value] \
   -Dagent.id=[value] \
   -Dagent.encryptionKey=[value]
   ```

##Configuration and Run with Docker
You can use 2 types of Dockerfile. First one is prepared for already created agents, second one include registering new agent with random name.
1. Use commands with proper values for env parameters

a) Dockerfile with entering all connection params
```
docker build -t [tag] -f [Dockerfile path] 

docker run -d --rm -p 9999:9999 \
-e MONITOR_IP=[value] \
-e MONITOR_PORT=[value] \
-e AGENT_ID=[value] \
-e AGENT_ENCRYPTION_KEY=[value] \
[tag]
```

b) Dockerfile with creating and registering new agent. 
For connection without agent proxy values pairs MONITOR_IP, MONITOR_PORT and APP_SERVER_IP, APP_SERVER_PORT
are equal.

```
docker build -t [tag] -f [Dockerfile.withRegistration path] .

docker run -d --rm -p 9999:9999 \
-e MONITOR_IP=[value] \
-e MONITOR_PORT=[value] \
-e ACCESS_TOKEN=[value] \
-e AGENT_PROXY=[value] \
-e APP_SERVER_IP=[value] \
-e APP_SERVER_PORT=[value] \
[tag]
```