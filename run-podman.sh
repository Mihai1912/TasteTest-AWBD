#!/bin/bash

# TasteTest Application - Microservices Architecture (Podman variant)
# Usage: ./run-podman.sh [command]
#
# Notes for WSL + Podman Desktop:
#   The podman socket lives at:
#     /mnt/wsl/podman-sockets/podman-machine-default/podman-root.sock
#   and is owned by root:uucp (mode 0660). If your user is not in the uucp
#   group, run this script with sudo, or add your user to the uucp group:
#     sudo usermod -aG uucp $USER  # then re-login

set -e

COMMAND=${1:-up}

# Pick the available compose front-end. `podman compose` is the modern wrapper
# (delegates to docker-compose if installed). `podman-compose` is the python tool.
if podman compose version >/dev/null 2>&1; then
  PC="podman compose"
elif command -v podman-compose >/dev/null 2>&1; then
  PC="podman-compose"
else
  echo "✗ Neither 'podman compose' nor 'podman-compose' is available."
  echo "  Install one of them, e.g.:  sudo apt install podman-compose"
  exit 1
fi

case "$COMMAND" in
  up)
    echo "🚀 Starting TasteTest Microservices (podman)..."
    $PC up -d
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
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    ;;

  down)
    echo "🛑 Stopping all services..."
    $PC down
    ;;

  clean)
    echo "🧹 Removing all services and volumes (WARNING: data will be deleted)..."
    read -p "Are you sure? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      $PC down -v
    else
      echo "✗ Cancelled"
    fi
    ;;

  logs)
    SERVICE=${2:-}
    if [ -z "$SERVICE" ]; then
      $PC logs -f
    else
      $PC logs -f "$SERVICE"
    fi
    ;;

  status)
    $PC ps
    ;;

  rebuild)
    echo "🔨 Rebuilding all images and starting..."
    $PC up --build -d
    sleep 2
    $PC ps
    ;;

  shell)
    SERVICE=${2:-api-gateway}
    $PC exec "$SERVICE" /bin/sh
    ;;

  test-health)
    echo "🏥 Testing service health..."
    for svc in "API Gateway:8090" "Auth Service:8091" "User Service:8092" "Restaurant Service:8093"; do
      name=${svc%:*}; port=${svc#*:}
      printf "%-22s " "$name"
      curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:$port/actuator/health" || true
    done
    echo ""
    echo "Discovery (Eureka registered apps):"
    curl -s http://localhost:8761/eureka/apps 2>/dev/null | grep -o '<name>[^<]*</name>' | head -10
    ;;

  test-login)
    RESPONSE=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"admin@admin.com","password":"admin"}')
    if echo "$RESPONSE" | grep -q '"token"'; then
      echo "✓ Login successful!"
      echo "$RESPONSE" | grep -o '"token":"[^"]*"'
    else
      echo "✗ Login failed!"
      echo "$RESPONSE"
    fi
    ;;

  *)
    echo "TasteTest - Microservices Control Script (podman)"
    echo "Using: $PC"
    echo ""
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands: up | down | clean | logs [svc] | status | rebuild | shell [svc] | test-health | test-login"
    exit 1
    ;;
esac
