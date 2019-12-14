FROM maven:latest AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -P prod -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
RUN mvn -P prod -Dclient.server.address=$clientServerAddress -Dapp.server.address=$appServerAddress -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM openjdk:11 as prod
WORKDIR /app
ENV CLIENT_SERVER_ADDRESS http://localhost:3000
ENV APP_SERVER_ADDRESS http://localhost:5000
ENV AWS_ACCESS_KEY_ID secret
ENV AWS_SECRET_KEY secret
COPY --from=build /app/target/network-services-monitor-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "/app/app.jar", "-Dclient.server.address=$CLIENT_SERVER_ADDRESS -Dapp.server.address=$APP_SERVER_ADDRESS"]
