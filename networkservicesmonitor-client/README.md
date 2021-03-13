# NSM Client Appliaction

## Requirements

- Docker or NPM ~> 6.13

## Configuration and Run with NPM

1. Set REACT_APP_API_URL value in .env file
2. Run npm start

## Configuration and Run with Docker
1. Update certificate in certificate dir
2. Use commands with proper values for parameters(parameters was described above)
```
docker build -t [tag] -f [Dockerfile path] .

docker run -d --rm -p 443:443 -p 80:80 -e REACT_APP_API_URL=[value] [tag]
```
