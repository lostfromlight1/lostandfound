# ---------- BUILD STAGE ----------
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app

# Cache dependencies separately
COPY build.gradle settings.gradle ./
RUN gradle build --no-daemon > /dev/null 2>&1 || true

# Copy source code with Gradle ownership
COPY --chown=gradle:gradle . .

# Build the jar
RUN gradle clean build --no-daemon -x test

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S app && adduser -S app -G app

# Create logs AND uploads directories with proper ownership
RUN mkdir -p /app/logs /app/uploads && chown -R app:app /app/logs /app/uploads

# Copy the jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Switch to non-root user
USER app

# Expose port
EXPOSE 8080

# Set environment variables for directories
ENV LOG_DIR=/app/logs
ENV UPLOAD_DIR=/app/uploads

# Run the app
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
