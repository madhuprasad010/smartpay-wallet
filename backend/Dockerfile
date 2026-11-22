# ============================================================
# SmartPay Consolidated Backend — Production Dockerfile
# Multi-stage build for Render Free Tier (512 MB RAM constraint)
# ============================================================

# Stage 1: Build Application with Maven & JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml ./
COPY src ./src

# Package fat JAR directly without test overhead
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, Secure JRE 21 Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install curl for container health checks
RUN apk add --no-cache curl tzdata

# Create dedicated non-root user and group
RUN addgroup -S smartpay && adduser -S smartpay -G smartpay

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder --chown=smartpay:smartpay /build/target/app.jar /app/app.jar

# Switch to unprivileged runtime user
USER smartpay

# Environment Defaults for Render Deployment
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=35.0 -XX:+ExitOnOutOfMemoryError -XX:MaxMetaspaceSize=128m -Xss512k -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Health check probing Actuator liveness state
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health/liveness || curl -f http://localhost:${PORT:-8080}/health || exit 1

# Execute Spring Boot Application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
