# Stage 1: Build the Application
# Using Eclipse Temurin 17 JDK for the build process
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and configuration files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Copy all source code modules
COPY batch-domain/ batch-domain/
COPY job-import-users/ job-import-users/
COPY job-process-data/ job-process-data/
COPY job-report-gen/ job-report-gen/
COPY batch-launcher/ batch-launcher/

# Give execution rights to the gradlew script
RUN chmod +x ./gradlew

# Build the executable JAR
RUN ./gradlew :batch-launcher:bootJar --no-daemon

# Stage 2: Create the Final Production Image
# Using Eclipse Temurin 17 JRE for a smaller runtime image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /usr/app

# Copy the final executable JAR from the builder stage
COPY --from=builder /app/batch-launcher/build/libs/*.jar app.jar

# Define the entry point for the container.
# This runs your long-running scheduled application.
ENTRYPOINT ["java", "-jar", "app.jar"]