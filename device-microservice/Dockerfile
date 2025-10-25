# Dockerfile pentru Device Microservice
# Multi-stage build pentru optimizare

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiază fișierele pom.xml pentru a descărca dependențele
COPY pom.xml .

# Descarcă dependențele (acest layer va fi cached dacă pom.xml nu se schimbă)
RUN mvn dependency:go-offline -B

# Copiază codul sursă
COPY src ./src

# Build aplicația (skip tests pentru build mai rapid)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Creează un user non-root pentru securitate
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiază jar-ul din stage-ul de build
COPY --from=build /app/target/*.jar app.jar

# Expune portul pe care rulează aplicația
EXPOSE 8082

# Healthcheck pentru a verifica dacă aplicația este healthy
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Variabile de mediu (pot fi suprascrise la runtime)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Rulează aplicația
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
