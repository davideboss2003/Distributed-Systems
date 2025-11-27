FROM openjdk:21-ea-23-jdk-bullseye

WORKDIR /app

# Copy the already-built jar from target/
COPY target/producer-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseG1GC","-jar","/app/app.jar"]
