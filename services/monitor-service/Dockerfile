FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY ./gradlew ./gradlew
COPY ./gradle ./gradle
COPY ./build.gradle.kts ./settings.gradle.kts ./

COPY ./libs ./libs
COPY ./services/monitor-service ./services/monitor-service

RUN chmod +x ./gradlew && ./gradlew :services:monitor-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/monitor-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]