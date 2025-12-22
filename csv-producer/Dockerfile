FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/csv-producer-1.0.0.jar app.jar
COPY target/lib ./lib

#ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["java", "-Xms64m", "-Xmx256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", \
"-cp", "app.jar:lib/*", "io.wahid.publication.CsvMain"]
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