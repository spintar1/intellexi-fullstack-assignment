FROM eclipse-temurin:21-jre as runtime
WORKDIR /app

# Build stage (use Maven image so mvn is available)
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -q -DskipTests package spring-boot:repackage

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/race-application-command-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"] 