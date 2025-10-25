# Dockerfile pentru Auth Microservice
# Multi-stage build pentru optimizare

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiază fișierele Maven
COPY pom.xml .
COPY src ./src

# Build aplicația (skip tests pentru build mai rapid)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiază JAR-ul din stage-ul de build
COPY --from=build /app/target/*.jar app.jar

# Expune portul aplicației
EXPOSE 8083

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Rulează aplicația
ENTRYPOINT ["java", "-jar", "app.jar"]
