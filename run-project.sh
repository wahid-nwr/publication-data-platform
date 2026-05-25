#!/bin/bash

set -e

echo "Compiling backend..."
mvn clean package -DskipTests

echo "Starting backend with Docker Compose..."
docker compose up --build -d

echo "Backend started."

echo "Starting React frontend..."
cd ../publication-frontend

docker rm -f publication-frontend

docker build -t publication-frontend:latest .

docker run -d \
    -p 8080:8080 \
    --name publication-frontend \
    -e VITE_CSV_API_URL="http://localhost:8082" \
    -e VITE_PUBLICATION_API_URL="http://localhost:8081" \
    -e VITE_APP_URL="http://localhost:8080/" \
    publication-frontend:latest

echo "Started React frontend..."