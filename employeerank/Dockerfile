# ========================================================
# Stage 1: Build the application using Maven and Java 17
# ========================================================
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the source code and pom.xml into the container
COPY . .

# Compile and package the application into a JAR file, skipping test execution
RUN mvn clean package -DskipTests

# ========================================================
# Stage 2: Create the lightweight runtime environment
# ========================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the generated JAR file from the build stage and rename it to app.jar
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 to the network
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]