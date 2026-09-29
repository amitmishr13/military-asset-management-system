# Stage 1: Build stage using official Maven with JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Package the Spring Boot application as an executable fat JAR
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage using official Eclipse Temurin JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the compiled JAR artifact from the build stage
COPY --from=build /app/target/military-asset-management-backend-1.0.0-SNAPSHOT.jar app.jar

# Inform Docker that the container listens on port 8080 by default (overridden by Render via $PORT)
EXPOSE 8080

# Launch the executable JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
