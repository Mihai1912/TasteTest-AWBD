#!/bin/bash

echo "=== Bringing environment down and up ==="
podman compose down
podman compose up -d

echo "Waiting 180s for full startup..."
sleep 180

echo "=== JAR string check ==="
podman cp tastetest-awbd_users_1:/app/app.jar /tmp/u3.jar
unzip -p /tmp/u3.jar BOOT-INF/classes/com/example/user/config/security/JwtAuthenticationFilter.class \
  | strings | grep "JWT-FILTER" | sort -u

echo "=== Container health ==="
podman ps --format "{{.Names}}\t{{.Status}}" | grep tastetest

echo "=== Probe ==="
ADMIN=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.access_token')
PLAIN=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@user.com","password":"user"}' | jq -r '.access_token')

echo "ADMIN token len: ${#ADMIN}"
echo "PLAIN token len: ${#PLAIN}"
echo "admin direct: $(curl -s -o /dev/null -w '%{http_code}' \
  http://localhost:8092/api/v1/user/all -H "Authorization: Bearer $ADMIN")"
echo "plain direct: $(curl -s -o /dev/null -w '%{http_code}' \
  http://localhost:8092/api/v1/user/all -H "Authorization: Bearer $PLAIN")"

echo "=== JWT-FILTER LOGS ==="
podman logs tastetest-awbd_users_1 2>&1 | grep "JWT-FILTER" | tail -40
