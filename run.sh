#!/bin/bash

# TasteTest Application - Start everything in Docker
# Usage: ./run.sh [up|down|logs|rebuild]

set -e

COMMAND=${1:-up}

case "$COMMAND" in
  up)
    echo "🚀 Starting TasteTest (DB + Backend + Frontend)..."
    docker compose up -d
    echo ""
    echo "✓ All services started!"
    echo ""
    echo "📍 Frontend:  http://localhost:4200"
    echo "📍 Backend:   http://localhost:8090/api/v1"
    echo "📍 Database:  localhost:5432"
    echo ""
    echo "🔐 Test Credentials:"
    echo "   Email: andrei.popescu@gmail.com"
    echo "   Password: parola123"
    ;;

  down)
    echo "🛑 Stopping all services..."
    docker compose down
    echo "✓ Services stopped"
    ;;

  logs)
    echo "📋 Following logs (Ctrl+C to exit)..."
    docker compose logs -f
    ;;

  rebuild)
    echo "🔨 Rebuilding and starting..."
    docker compose up --build -d
    echo "✓ Rebuild complete!"
    ;;

  *)
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  up       Start all services (default)"
    echo "  down     Stop all services"
    echo "  logs     View live logs"
    echo "  rebuild  Rebuild images and restart"
    exit 1
    ;;
esac

