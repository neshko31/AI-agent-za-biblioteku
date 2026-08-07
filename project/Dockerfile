# syntax=docker/dockerfile:1.4
# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -q

COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/src/main/resources/knjige ./src/main/resources/knjige

RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
