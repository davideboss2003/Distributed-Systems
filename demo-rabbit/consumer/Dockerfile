FROM openjdk:21-ea-23-jdk-bullseye

WORKDIR /app

# Copy the already-built jar
COPY target/consumer-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-XX:+UseG1GC","-jar","/app/app.jar"]
