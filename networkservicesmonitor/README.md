# NSM Application Server

## Requirements


- JAVA ~> 11
- AWS CLI ~> 1.16
- Docker or Maven

## Configuration and Run with Maven

1. Create AWS Secrets Manager 
2. Add to AWS Secrets Manager secrets listed in section below
3. Generate AWS Access Key ID and AWS Secret Access Key
4. Configure AWS with env variables AWS_DEFAULT_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY or using command:  
    ```
    aws configure
    ```
5. Make sure that in application.yml file are set appropriate values for server.ssl keys. 
    ```
   ATTENTION: DO NOT USE INCLUDED SSL CERTIFICATES DUE TO SECURITY REASONS
   ```
6. Use Maven to create jar package
    ```
   mvn package -DskipTests
   ```
6. Run application with proper values for parameters CLIENT_SERVER_ADDRESS (url for client application) and APP_SERVER_ADDRESS (url for application server) 
    ```
   java -jar /app/app.jar -Dclient.server.address=$CLIENT_SERVER_ADDRESS -Dapp.server.address=$APP_SERVER_ADDRESS
   ```

## Configuration and Run with Docker
1. Use commands with proper values for parameters(parameters was described above)
```
docker build -t [tag] -f [Dockerfile path] .

docker run -d --rm -p 443:8443 \
-e CLIENT_SERVER_ADDRESS=[value] \
-e APP_SERVER_ADDRESS=[value] \
-e AWS_ACCESS_KEY_ID=[value] \
-e AWS_SECRET_KEY=[value] \
-e AWS_DEFAULT_REGION=[value] \
[tag]
```

## AWS Secrets Manager - Required Secrets

1. Name: db_credentials
    
    Value: 
    
    | Secret Key     | Secret Value (examples)                              |
    | -------------  | ---                                                  |
    | username       | patryk****                                           |
    | password       | ****                                                 |
    | host           | tmpdbpmi.casda6sadtmn58.eu-west-3.rds.amazonaws.com  |
    | port           | 5432                                                 |
    | dbInstanceName | monitor                                              |

2. Name: jwtSecret

    Value: 
    
    | Secret Key     | Secret Value (examples)                              |
    | -------------  | ---                                                  |
    | jwtSecret      | ****                                                 |
