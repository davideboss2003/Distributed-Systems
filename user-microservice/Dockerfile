# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# copiaza fișierele pom.xml pentru a descarca dependentele
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

# Copiază JAR-ul din stage-ul de build
COPY --from=build /app/target/*.jar app.jar

# Expune portul aplicației
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Variabile de mediu (pot fi suprascrise în docker-compose)
ENV SPRING_PROFILES_ACTIVE=docker

# run app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
