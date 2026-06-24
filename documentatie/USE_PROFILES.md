# 1. Build + teste cu profilul test (H2)
./mvnw clean test

# 2. Rulează aplicația cu profilul dev (PostgreSQL — necesită docker compose up tastetest-app-db)
docker compose up -d tastetest-app-db
./mvnw spring-boot:run

# 3. Rulează aplicația cu profilul test (H2 in-memory pe :8091)
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
# verifică: http://localhost:8091/h2-console
#   JDBC URL: jdbc:h2:mem:tastetest_test
#   user: sa, fără parolă
