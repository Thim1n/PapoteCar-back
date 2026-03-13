FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw || true
RUN mvn -B -f pom.xml dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ARG JWT_SECRET
ENV JWT_SECRET=$JWT_SECRET

COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
