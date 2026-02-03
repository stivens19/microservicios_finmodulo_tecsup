#!/bin/bash

echo "Starting Infrastructure..."
docker-compose up -d

echo "Waiting for DBs and Kafka..."
sleep 30

echo "Starting Microservices..."

# Function to start a service in a new Git Bash window or background depending on environment
# For Git Bash on Windows, 'start' command works to open new windows.
start "" cmd //c "cd user-service && ./mvnw.cmd spring-boot:run"
start "" cmd //c "cd course-service && ./mvnw.cmd spring-boot:run"
start "" cmd //c "cd enrollment-service && ./mvnw.cmd spring-boot:run"
start "" cmd //c "cd payment-service && ./mvnw.cmd spring-boot:run"
start "" cmd //c "cd notification-service && ./mvnw.cmd spring-boot:run"

echo "All services started."
read -p "Press any key to close..."
