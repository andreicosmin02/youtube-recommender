# ========================
# Stage 1: Build the Application
# ========================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy maven wrapper and configuration first to cache dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Grant execution rights to the wrapper
RUN chmod +x mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application (skip tests to speed up container build)
RUN ./mvnw clean package -DskipTests

# ========================
# Stage 2: Create the Runtime Image
# ========================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy the built jar from the builder stage
# The wildcard *.jar handles version changes automatically
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Configure the startup command
ENTRYPOINT ["java", "-jar", "app.jar"]