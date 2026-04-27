# -----------------------------
# Stage 1: Build (Maven + Java 21)
# -----------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first (better caching)
COPY pom.xml .

# Download dependencies
RUN mvn -B dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# -----------------------------
# Stage 2: Runtime (Java 21 lightweight)
# -----------------------------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# JVM tuning (important for EC2 small instances)
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]