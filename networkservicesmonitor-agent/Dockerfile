FROM maven:latest AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM openjdk:11 as prod
RUN apt update && apt -y install net-tools && apt install -y jq
WORKDIR /app
COPY --from=build /app/target/network-services-monitor-agent-0.0.1-SNAPSHOT.jar app.jar
ENV MONITOR_IP=127.0.0.1
ENV MONITOR_PORT=8443
ENV AGENT_ID=agentId
ENV AGENT_ENCRYPTION_KEY=agentEncryptionKey
EXPOSE 9999
ENTRYPOINT ["java", "-jar", "/app/app.jar", "-Dagent.monitor.address=$MONITOR_IP", "-Dagent.monitor.port=$MONITOR_PORT", "-Dagent.id=$AGENT_ID", "-Dagent.encryptionKey=$AGENT_ENCRYPTION_KEY"]
CMD ["--spring.profiles.active=default"]
