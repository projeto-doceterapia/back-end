# syntax=docker/dockerfile:1.7

# ---- Builder: resolve dependencies, package and extract Spring Boot layers ----
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /build

# Resolve dependencies first so this layer is cached while only sources change.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

# Build the application jar.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests

# Extract the Boot layers so the runtime copies dependencies and application
# as separate, cache-friendly layers.
RUN cp target/*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ---- Runtime: lean JRE, only what runs, non-root ----
FROM eclipse-temurin:21.0.5_11-jre-alpine AS runtime

LABEL org.opencontainers.image.source="https://github.com/projeto-doceterapia/back-end" \
      org.opencontainers.image.title="doceterapia-back-end" \
      org.opencontainers.image.description="Doceterapia REST API"

# wget (busybox) is used by the HEALTHCHECK. Run as an unprivileged user.
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Copy extracted layers in dependency-to-application order: the lower layers
# change rarely, so a code-only rebuild only replaces the application layer.
COPY --from=builder --chown=spring:spring /build/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /build/extracted/application/ ./

USER spring:spring

# Size the JVM heap from the container memory limit instead of the host.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Default to the production profile so a plain run reads its config from the
# environment instead of falling back to in-memory H2 and the dev secret.
# Select the local profile explicitly for development.
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
