# syntax=docker/dockerfile:1.4
# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# cache dependencies before copying source
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -q

# copy source and build — skip tests (ES not available at build time)
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-all \
    && rm -rf /var/lib/apt/lists/*

# resolve the real tessdata directory installed by apt (version-numbered subdir varies by release)
RUN TESSDATA_DIR=$(dirname "$(find / -name 'eng.traineddata' -print -quit 2>/dev/null)") \
    && echo "OCR_TESSDATA_PATH=${TESSDATA_DIR}" > /etc/tessdata.env \
    && test -n "${TESSDATA_DIR}" && test -d "${TESSDATA_DIR}"

# non-root user
RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder /app/target/*.jar app.jar

# bake the discovered tessdata path into a startup script (env files aren't auto-sourced by `java -jar`)
RUN echo '#!/bin/sh' > /app/entrypoint.sh \
    && cat /etc/tessdata.env | sed 's/^/export /' >> /app/entrypoint.sh \
    && echo 'exec java -jar app.jar' >> /app/entrypoint.sh \
    && chmod +x /app/entrypoint.sh \
    && chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
