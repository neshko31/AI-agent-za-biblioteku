# syntax=docker/dockerfile:1.4
# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
