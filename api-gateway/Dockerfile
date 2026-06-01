FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# copy full multi-module project so the parent pom resolves
COPY . ./
# build only the api-gateway module and its prerequisites
RUN mvn -B -DskipTests -pl api-gateway -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
