# ============================================================
# Eureka Server — Multi-stage Dockerfile
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Install Maven (Alpine package manager)
RUN apk add --no-cache maven

# Copy POM first for layer caching — dependencies are downloaded
# before source is copied, so a source-only change doesn't re-download deps.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S smartcharge && adduser -S smartcharge -G smartcharge
USER smartcharge

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
