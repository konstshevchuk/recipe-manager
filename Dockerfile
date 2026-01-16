# Builder
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY src .

RUN mvn clean package -DskipTests

# Application
FROM gcr.io/distroless/java21-debian12

WORKDIR /app
COPY --from=build /app/target/*.jar application.jar

ENTRYPOINT ["java","-jar","/app/application.jar"]
