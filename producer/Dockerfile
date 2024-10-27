FROM maven:3.9.9-eclipse-temurin-21 as build

ADD . /app

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f /app/pom.xml clean install -DskipTests


FROM openjdk:21

WORKDIR /app

COPY --from=build /app/target/ .

EXPOSE 8083

ENTRYPOINT ["java", "-javaagent:classes/opentelemetry-javaagent.jar", "-XX:InitialRAMPercentage=75.0", "-XX:MaxRAMPercentage=75.0", "-jar", "producer-0.0.1-SNAPSHOT.jar"]