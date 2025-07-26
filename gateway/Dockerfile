FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY build.gradle.kts settings.gradle.kts ./
COPY libs libs
COPY services services

RUN ./gradlew :services:gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/services/gateway/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]