FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml mvnw .mvn/ ./
COPY restaurant-service ./restaurant-service
RUN mvn -f restaurant-service/pom.xml -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/restaurant-service/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java","-jar","/app/app.jar"]
