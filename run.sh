#!/bin/bash

# TasteTest Application - Microservices Architecture
# Start, stop, and manage all services with Docker Compose
# Usage: ./run.sh [command]

set -e

COMMAND=${1:-up}

case "$COMMAND" in
  up)
    echo "🚀 Starting TasteTest Microservices..."
    docker compose up -d
    echo ""
    sleep 2
    echo "✓ All services started!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📍 FRONTEND & API GATEWAY:"
    echo "   Frontend:     http://localhost:4200"
    echo "   API Gateway:  http://localhost:8090"
    echo ""
    echo "📍 MICROSERVICES (internal):"
    echo "   Auth Service:       http://localhost:8091/actuator/health"
    echo "   User Service:       http://localhost:8092/actuator/health"
    echo "   Restaurant Service: http://localhost:8093/actuator/health"
    echo ""
    echo "📍 INFRASTRUCTURE:"
    echo "   Service Discovery (Eureka): http://localhost:8761"
    echo "   Config Server:              http://localhost:8888"
    echo "   Database:                   localhost:5432"
    echo "   Prometheus:                 http://localhost:9090"
    echo "   Grafana:                    http://localhost:3000 (admin/admin)"
    echo "   Zipkin Tracing:             http://localhost:9411"
    echo ""
    echo "🔐 TEST CREDENTIALS:"
    echo "   Email (Admin):           admin@admin.com"
    echo "   Password:                admin"
    echo ""
    echo "   Email (Restaurant Owner): marius.stan@outlook.com"
    echo "   Password:                 mariuspass"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "💡 TIP: Run 'docker compose ps' to see service status"
    echo "💡 TIP: Run './run.sh logs' to view live logs"
    ;;

  down)
    echo "🛑 Stopping all services..."
    docker compose down
    echo "✓ Services stopped"
    ;;

  clean)
    echo "🧹 Removing all services and volumes (WARNING: data will be deleted)..."
    read -p "Are you sure? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      docker compose down -v
      echo "✓ All services and volumes removed"
    else
      echo "✗ Cancelled"
    fi
    ;;

  logs)
    SERVICE=${2:-}
    if [ -z "$SERVICE" ]; then
      echo "📋 Following all logs (Ctrl+C to exit)..."
      docker compose logs -f
    else
      echo "📋 Following $SERVICE logs (Ctrl+C to exit)..."
      docker compose logs -f "$SERVICE"
    fi
    ;;

  status)
    echo "📊 Service Status:"
    docker compose ps
    ;;

  rebuild)
    echo "🔨 Rebuilding all images and starting..."
    docker compose up --build -d
    echo "✓ Rebuild complete!"
    sleep 2
    echo ""
    docker compose ps
    ;;

  shell)
    SERVICE=${2:-api-gateway}
    echo "🐚 Opening shell in $SERVICE container..."
    docker compose exec "$SERVICE" /bin/sh
    ;;

  scale)
    # Run one service with N instances and watch the gateway load-balance across them.
    # Spring Cloud Gateway resolves `lb://<app>` via Eureka and round-robins requests.
    SERVICE=${2:-restaurants}
    COUNT=${3:-3}
    REQUESTS=${4:-12}

    # Map docker-compose service name -> Eureka app name + a gateway path to probe.
    case "$SERVICE" in
      restaurants)         APP=tastetest-restaurant;           TEST_PATH=/api/v1/restaurant/paged ;;
      auth)                APP=tastetest-auth;                 TEST_PATH=/api/v1/auth/health ;;
      users)               APP=tastetest-user;                 TEST_PATH=/api/v1/user/getAll ;;
      notification-service) APP=tastetest-notification-service; TEST_PATH=/api/internal/notifications ;;
      agent-service)       APP=tastetest-agent;                TEST_PATH=/api/v1/agent/health ;;
      backend-original)    APP=tastetest-awdb;                 TEST_PATH=/api/restaurants ;;
      *) echo "✗ Unsupported service '$SERVICE'. Use one of: restaurants auth users notification-service agent-service backend-original"; exit 1 ;;
    esac

    echo "⚖️  Scaling '$SERVICE' to $COUNT instances and testing gateway routing..."
    echo ""

    # Dynamic override: drop the fixed host-port mapping (so replicas don't collide)
    # and stream each instance's Tomcat access log to stdout (so we can see which
    # replica handled each request, prefixed by the container name in compose logs).
    OVERRIDE=$(mktemp /tmp/tastetest-scale-XXXXXX.yml)
    trap 'rm -f "$OVERRIDE"' EXIT
    # `!override []` forces Compose to REPLACE the host-port mapping with an empty
    # list. A plain `ports: []` does not work: Compose concatenates `ports` across
    # files, so the base `8093:8093` would survive and every replica would collide.
    cat > "$OVERRIDE" <<EOF
services:
  $SERVICE:
    ports: !override []
    environment:
      SERVER_TOMCAT_ACCESSLOG_ENABLED: "true"
      SERVER_TOMCAT_ACCESSLOG_DIRECTORY: /dev
      SERVER_TOMCAT_ACCESSLOG_PREFIX: stdout
      SERVER_TOMCAT_ACCESSLOG_SUFFIX: ""
      SERVER_TOMCAT_ACCESSLOG_FILE_DATE_FORMAT: ""
      SERVER_TOMCAT_ACCESSLOG_BUFFERED: "false"
EOF

    docker compose -f docker-compose.yml -f "$OVERRIDE" up -d --scale "$SERVICE=$COUNT" "$SERVICE"

    echo ""
    echo "⏳ Waiting for instances to register with Eureka..."
    sleep 15

    echo ""
    echo "📦 Running instances of '$SERVICE':"
    docker compose ps "$SERVICE"

    echo ""
    echo "🗂️  Eureka registrations for '$APP':"
    curl -s "http://localhost:8761/eureka/apps/$APP" \
      | grep -o '<instanceId>[^<]*</instanceId>' || echo "   (none yet — give it a few more seconds)"

    # The gateway enforces JWT auth, so grab a token first — otherwise every
    # request is rejected at the gateway and never reaches the instances.
    echo ""
    echo "🔐 Obtaining a JWT (admin@admin.com) so requests pass the gateway..."
    TOKEN=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"admin@admin.com","password":"admin"}' \
      | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//;s/"//')
    if [ -z "$TOKEN" ]; then
      echo "   ⚠️  Could not get a token; sending unauthenticated (may 401 at the gateway)."
      AUTH=()
    else
      echo "   ✓ Token acquired."
      AUTH=(-H "Authorization: Bearer $TOKEN")
    fi

    echo ""
    echo "📡 Sending $REQUESTS requests to http://localhost:8090$TEST_PATH ..."
    SINCE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
    for i in $(seq 1 "$REQUESTS"); do
      CODE=$(curl -s -o /dev/null -w "%{http_code}" "${AUTH[@]}" "http://localhost:8090$TEST_PATH")
      printf "   request %2d -> HTTP %s\n" "$i" "$CODE"
    done

    sleep 2
    echo ""
    echo "📊 Requests handled per instance (from access logs):"
    docker compose logs --since "$SINCE" "$SERVICE" 2>/dev/null \
      | grep -F "$TEST_PATH" \
      | awk '{print $1}' | sort | uniq -c \
      || echo "   (no access-log lines captured)"

    echo ""
    echo "✓ Done. The counts above show the gateway round-robining across replicas."
    echo "💡 Watch it live in another terminal: ./run.sh logs $SERVICE"
    echo "💡 Scale back down with: docker compose up -d --scale $SERVICE=1 $SERVICE"
    ;;

  test-health)
    echo "🏥 Testing service health..."
    echo ""
    echo "Testing API Gateway..."
    curl -s -i http://localhost:8090/actuator/health | head -1
    
    echo ""
    echo "Testing Auth Service..."
    curl -s -i http://localhost:8091/actuator/health | head -1
    
    echo ""
    echo "Testing User Service..."
    curl -s -i http://localhost:8092/actuator/health | head -1
    
    echo ""
    echo "Testing Restaurant Service..."
    curl -s -i http://localhost:8093/actuator/health | head -1
    
    echo ""
    echo "Testing Service Discovery..."
    curl -s http://localhost:8761/eureka/apps 2>/dev/null | grep -o '<name>[^<]*</name>' | head -5
    ;;

  test-login)
    echo "🔐 Testing login endpoint..."
    RESPONSE=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "email": "admin@admin.com",
        "password": "admin"
      }')
    
    if echo "$RESPONSE" | grep -q '"token"'; then
      echo "✓ Login successful!"
      echo ""
      echo "Token:"
      echo "$RESPONSE" | grep -o '"token":"[^"]*"'
    else
      echo "✗ Login failed!"
      echo "$RESPONSE"
    fi
    ;;

  *)
    echo "TasteTest - Microservices Control Script"
    echo ""
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  up              Start all services (default)"
    echo "  down            Stop all services"
    echo "  clean           Remove all services and volumes (DATA LOSS!)"
    echo "  logs [service]  View live logs (optionally filter by service)"
    echo "  status          Show status of all services"
    echo "  rebuild         Rebuild images and restart all services"
    echo "  shell [service] Open shell in a service container"
    echo "  scale [service] [count] [requests]"
    echo "                  Run a service with N instances and watch the gateway route across them"
    echo "  test-health     Quick health check of all services"
    echo "  test-login      Test login with admin credentials"
    echo ""
    echo "Examples:"
    echo "  ./run.sh up                    # Start everything"
    echo "  ./run.sh logs restaurant       # View restaurant service logs"
    echo "  ./run.sh shell api-gateway     # Shell into api-gateway"
    echo "  ./run.sh scale restaurants 3   # 3 restaurant instances + routing test"
    echo "  ./run.sh test-health           # Check if all services are healthy"
    exit 1
    ;;
esac

