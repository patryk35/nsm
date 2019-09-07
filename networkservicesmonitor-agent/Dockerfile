FROM maven:latest AS appserver
WORKDIR /app
COPY pom.xml .
RUN mvn -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
ARG monitoIp=127.0.0.1
ARG monitorAddress=5000
ARG agentId=02931ff7-d2ec-42da-8e0f-f712346d573d
ARG agentEncryptionKey=0d7fc933-4fec-49c0-8e7a-0a0fbd0f8b9d
RUN mvn -Dmonitor.ip=$monitoIp -Dmonitor.address=$monitorAddress -Dagent.id=agentId -Dagent.encryptionKey=$agentEncryptionKey -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM openjdk:11 as prod
WORKDIR /app
COPY --from=appserver /app/target/network-services-monitor-agent-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=default"]
