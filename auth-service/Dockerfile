FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml mvnw .mvn/ ./
COPY auth-service ./auth-service
RUN mvn -f auth-service/pom.xml -B -DskipTests package

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java","-jar","/app/app.jar"]
