# Stage 1: Build Stage (Uses Maven to compile your code)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy pom.xml and source code to the container
COPY pom.xml .
COPY src ./src
# Compile the code and create the JAR file
# RUN mvn clean package -DskipTests
# Replace -DskipTests with the stronger -Dmaven.test.skip=true
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Production Stage (Creates the final small image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR from the Stage 1 build
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
