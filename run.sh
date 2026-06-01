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
    echo "  test-health     Quick health check of all services"
    echo "  test-login      Test login with admin credentials"
    echo ""
    echo "Examples:"
    echo "  ./run.sh up                    # Start everything"
    echo "  ./run.sh logs restaurant       # View restaurant service logs"
    echo "  ./run.sh shell api-gateway     # Shell into api-gateway"
    echo "  ./run.sh test-health           # Check if all services are healthy"
    exit 1
    ;;
esac

