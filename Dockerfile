# ---------- Build Stage ----------
FROM gradle:8.7-jdk17 AS build

WORKDIR /app

# Copy project files
COPY . .

# Build the Spring Boot jar
RUN gradle clean bootJar --no-daemon


# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]