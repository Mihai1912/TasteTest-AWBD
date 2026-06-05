FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# copy full project so multi-module build can run
COPY . ./
# build only the original backend module to produce its jar
RUN mvn -B -DskipTests -pl backend-original -am package

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/backend-original/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

