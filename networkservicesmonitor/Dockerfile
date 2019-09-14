FROM maven:latest AS appserver
WORKDIR /app
COPY pom.xml .
RUN mvn -P prod -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
#e. g. 00000000-0000-0000-0000-000000000000
ARG vaultToken
#e. g. http://localhost:8200
ARG vaultUri
RUN mvn -P prod -Dvault.token=$vaultToken -Dvault.uri=$vaultUri -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM openjdk:11 as prod
WORKDIR /app
COPY --from=appserver /app/target/network-services-monitor-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=postgres"]
