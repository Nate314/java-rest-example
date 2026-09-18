# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Cache dependencies separately from source for faster rebuilds
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src src
RUN mvn -B package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine

# Run as an unprivileged user. Configuration (DB_*, JWT_SECRET) comes from
# environment variables at run time; nothing secret is baked into the image.
RUN addgroup -S app && adduser -S -G app -H -s /sbin/nologin app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/*.jar ./app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=5 \
  CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
