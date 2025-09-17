@echo off
echo 🏁 Race Application - Build ^& Deploy Script
echo ==========================================

REM Stop existing services
echo 🛑 Stopping existing services...
docker compose down

REM Clean up old images (optional)
echo 🧹 Cleaning up...
docker system prune -f

REM Build and start all services
echo 🏗️ Building and starting all services...
docker compose up -d --build

REM Wait for services to start
echo ⏳ Waiting for services to initialize...
timeout /t 30 /nobreak > nul

REM Check service status
echo 📊 Service Status:
docker compose ps

REM Show logs
echo 📝 Recent logs:
docker compose logs --tail=10

echo.
echo ✅ Deployment Complete!
echo 🌐 Application: http://localhost:5173
echo 🐰 RabbitMQ Management: http://localhost:15672 (guest/guest)
echo 🗄️ PostgreSQL: localhost:5434 (query/query)
echo.
echo 📋 Useful commands:
echo   docker compose logs -f [service_name]  # Follow logs
echo   docker compose down                    # Stop services  
echo   docker compose ps                      # View status
