# Multi-stage Dockerfile for Pets Spring Boot application (Java 21)
# Stage 1: Build the fat jar using Gradle wrapper
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Cache Gradle wrapper and dependencies
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Pre-download dependencies (will run a minimal task)
RUN ./gradlew --version

# Copy source
COPY src ./src

# Build application jar (skip tests for faster Docker builds; run tests in CI)
RUN ./gradlew clean bootJar -x test

# Determine the built jar (Spring Boot BootJar places it under build/libs/*.jar)
# We'll copy the only generated jar to the runtime image.

# Stage 2: Runtime image (JRE only)
FROM eclipse-temurin:21-jre

ENV APP_HOME=/app \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=default

WORKDIR ${APP_HOME}

# Create non-root user
RUN groupadd -r app && useradd -r -g app app

# Copy jar from builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Ensure permissions
RUN chown -R app:app ${APP_HOME}
USER app

# Expose application port (Spring default 8080)
EXPOSE 8080

# Healthcheck (optional; expects actuator if enabled on /actuator/health)
# HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Allow passing extra JVM options through JAVA_OPTS
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
