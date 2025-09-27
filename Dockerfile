# OpenJDK base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the application JAR file
COPY build/libs/service_discovery-0.0.1-SNAPSHOT.jar app.jar

# Expose the gRPC port
EXPOSE 50051

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]