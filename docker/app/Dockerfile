# Multi-stage Dockerfile for building the Spring Boot application
# Stage 1: build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only what is needed for dependency resolution first (cache layer)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw || true
RUN mvn -B -f pom.xml dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2: runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ARG JWT_SECRET
ENV JWT_SECRET=$JWT_SECRET

# Copy the built jar from the build stage
COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
