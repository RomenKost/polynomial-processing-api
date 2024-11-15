FROM gradle:7.6.0-jdk17 AS build

COPY . /app
WORKDIR /app

RUN gradle bootJar --no-daemon

FROM openjdk:17-jdk-slim

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
