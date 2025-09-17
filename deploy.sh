#!/bin/bash

echo "🏁 Race Application - Build & Deploy Script"
echo "=========================================="

# Stop existing services
echo "🛑 Stopping existing services..."
docker compose down

# Clean up old images (optional)
echo "🧹 Cleaning up..."
docker system prune -f

# Build and start all services
echo "🏗️ Building and starting all services..."
docker compose up -d --build

# Wait for services to start
echo "⏳ Waiting for services to initialize..."
sleep 30

# Check service status
echo "📊 Service Status:"
docker compose ps

# Show logs
echo "📝 Recent logs:"
docker compose logs --tail=10

echo ""
echo "✅ Deployment Complete!"
echo "🌐 Application: http://localhost:5173"
echo "🐰 RabbitMQ Management: http://localhost:15672 (guest/guest)"
echo "🗄️ PostgreSQL: localhost:5434 (query/query)"
echo ""
echo "📋 Useful commands:"
echo "  docker compose logs -f [service_name]  # Follow logs"
echo "  docker compose down                    # Stop services"
echo "  docker compose ps                      # View status"
