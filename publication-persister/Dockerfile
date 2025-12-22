FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/publication-persister-1.0.0.jar app.jar
COPY target/lib ./lib

COPY /scripts/wait-for-db.sh /usr/local/bin/wait-for-db.sh
RUN chmod +x /usr/local/bin/wait-for-db.sh

#ENTRYPOINT ["/usr/local/bin/wait-for-db.sh", "pubdb", "5432"]
# Cloud Run uses this PORT; Jetty must bind to it.
ARG MY_DB_HOST
ARG MY_DB_PORT
ENV DB_HOST=$MY_DB_HOST
ENV DB_PORT=$MY_DB_PORT
ENV PORT=8081

# Declare metadata only; Cloud Run ignores EXPOSE but good practice
EXPOSE 8081

# ENTRYPOINT: pass DB host + port from Cloud Run env variables
ENTRYPOINT ["/usr/local/bin/wait-for-db.sh"]

# CMD passes Cloud Run env variables to entrypoint
#CMD ["${DB_HOST}", "${DB_PORT}"]

# ============
#  Build Stage
# ============
#FROM maven:3.9.6-eclipse-temurin-21 AS builder
#WORKDIR /workspace
#
#COPY pom.xml .
#COPY src ./src
#RUN mvn -q -e -DskipTests package

# ====================
#  Runtime (Cloud Run)
# ====================
#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
## Copy application
#COPY --from=builder /workspace/target/*.jar /app/app.jar
#COPY --from=builder /workspace/target/lib /app/lib
#
## Copy entrypoint script
#COPY /scripts/entrypoint.sh /app/entrypoint.sh
#RUN chmod +x /app/entrypoint.sh
#
## Cloud Run uses this PORT; Jetty must bind to it.
#ENV PORT=8080
#
## Declare metadata only; Cloud Run ignores EXPOSE but good practice
#EXPOSE 8080
#
## ENTRYPOINT: pass DB host + port from Cloud Run env variables
#ENTRYPOINT ["/app/entrypoint.sh"]
#
## CMD passes Cloud Run env variables to entrypoint
#CMD ["${DB_HOST}", "${DB_PORT}"]






#ENTRYPOINT ["java", "-Xms256m", "-Xmx256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-cp", "app.jar:lib/*", "io.wahid.publication.PublicationManagementApplication"]
#COPY target/publication-common-1.0.0.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]

# ---- Stage 1: Build ----
#FROM maven:3.9.6-eclipse-temurin-17 AS builder
#WORKDIR /build

# copy all modules
#COPY . .

# ARG for module name (passed during build)
#ARG MODULE

# build only the selected module and its dependencies
#RUN mvn -B -pl ${MODULE} -am clean package -DskipTests

# ---- Stage 2: Runtime ----
#FROM openjdk:17-jdk-slim
#WORKDIR /app
#ARG MODULE
#COPY --from=builder /build/${MODULE}/target/*-jar-with-dependencies.jar app.jar

#ENTRYPOINT ["java", "-jar", "app.jar"]