FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY libs libs
COPY gateway gateway
RUN chmod +x ./gradlew && ./gradlew :gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/gateway/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]