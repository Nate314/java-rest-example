# --- Build stage ---
# The app is pinned to Java 1.8 / Spring Boot 1.3.1 (out of scope to upgrade),
# so we build with a Maven image bundling JDK 8, not a newer JDK.
FROM maven:3.6-jdk-8 AS build

WORKDIR /build

# Cache dependencies separately from source for faster rebuilds
COPY pom.xml .
COPY .mvn .mvn
RUN mvn -B dependency:go-offline || true

COPY src src
RUN mvn -B package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

# Default config.json used when the container is run without docker-compose
# (docker-compose overrides this with config.docker.json so the app talks
# to the "mysql" compose service instead of localhost).
COPY config.json ./config.json
COPY --from=build /build/target/*.war ./app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.war"]
