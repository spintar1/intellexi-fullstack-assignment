# Trail Race Registration System - Makefile
# CQRS Microservice Architecture with Spring Boot and React

.PHONY: help dev build test clean start stop restart logs docker-clean install-deps check-deps

# Default target
help: ## Show this help message
	@echo "Trail Race Registration System - Available Commands:"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*##/ {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "Examples:"
	@echo "  make dev              # Start development environment"
	@echo "  make test             # Run all tests"
	@echo "  make build            # Build all services"
	@echo "  make clean            # Clean up everything"

# Check Dependencies
check-deps: ## Check if required tools are installed
	@echo "Checking dependencies..."
	@command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is required but not installed."; exit 1; }
	@command -v docker compose >/dev/null 2>&1 || command -v docker-compose >/dev/null 2>&1 || { echo "ERROR: Docker Compose is required but not installed."; exit 1; }
	@command -v java >/dev/null 2>&1 || { echo "WARNING: Java is not installed. Docker will handle Java for containers."; }
	@command -v node >/dev/null 2>&1 || { echo "WARNING: Node.js is not installed. Docker will handle Node for containers."; }
	@command -v mvn >/dev/null 2>&1 || { echo "WARNING: Maven is not installed. Docker will handle Maven for containers."; }
	@echo "✅ Dependencies check completed"

# Development Environment
dev: check-deps ## Start the complete development environment
	@echo "🚀 Starting development environment..."
	@docker compose down --remove-orphans || true
	@docker compose up -d --build
	@echo "🎉 Development environment is running!"
	@echo ""
	@echo "Available services:"
	@echo "  🌐 Web Application:     http://localhost:5173"
	@echo "  🔧 Command API:         http://localhost:8081"  
	@echo "  📊 Query API:           http://localhost:8082"
	@echo "  🐰 RabbitMQ Management: http://localhost:15672 (guest/guest)"
	@echo "  🗄️  PostgreSQL:          localhost:5434 (query/query/query_db)"
	@echo ""
	@echo "📝 View logs: make logs"
	@echo "🛑 Stop: make stop"

dev-logs: ## Follow development logs
	@docker compose logs -f

# Build Commands
build: check-deps ## Build all services (Java + Docker)
	@echo "🏗️  Building all services..."
	@$(MAKE) build-backend
	@$(MAKE) build-frontend
	@$(MAKE) build-docker
	@echo "✅ All services built successfully!"

build-backend: ## Build Java backend services
	@echo "🏗️  Building Command Service..."
	@cd services/race-application-command-service && mvn clean package -DskipTests
	@echo "🏗️  Building Query Service..."
	@cd services/race-application-query-service && mvn clean package -DskipTests
	@echo "✅ Backend services built!"

build-frontend: ## Build React frontend
	@echo "🏗️  Building React frontend..."
	@cd client && npm ci && npm run build
	@echo "✅ Frontend built!"

build-docker: ## Build Docker images
	@echo "🏗️  Building Docker images..."
	@docker compose build --no-cache
	@echo "✅ Docker images built!"

# Test Commands  
test: check-deps ## Run all tests (unit + integration)
	@echo "🧪 Running all tests..."
	@$(MAKE) test-backend
	@$(MAKE) test-frontend
	@echo "✅ All tests completed!"

test-backend: ## Run backend tests (unit + integration)
	@echo "🧪 Running Command Service tests..."
	@cd services/race-application-command-service && mvn test
	@echo "🧪 Running Query Service tests..."
	@cd services/race-application-query-service && mvn test
	@echo "✅ Backend tests completed!"

test-frontend: ## Run frontend tests
	@echo "🧪 Running frontend tests..."
	@cd client && npm test -- --coverage --watchAll=false
	@echo "✅ Frontend tests completed!"

test-integration: ## Run integration tests only
	@echo "🧪 Running integration tests..."
	@cd services/race-application-command-service && mvn test -Dtest="*IntegrationTest"
	@cd services/race-application-query-service && mvn test -Dtest="*IntegrationTest"
	@echo "✅ Integration tests completed!"

test-unit: ## Run unit tests only  
	@echo "🧪 Running unit tests..."
	@cd services/race-application-command-service && mvn test -Dtest="!*IntegrationTest"
	@cd services/race-application-query-service && mvn test -Dtest="!*IntegrationTest"
	@cd client && npm test -- --coverage --watchAll=false --testPathIgnorePatterns="integration"
	@echo "✅ Unit tests completed!"

# Service Management
start: ## Start all services
	@echo "▶️  Starting services..."
	@docker compose up -d
	@echo "✅ Services started!"

stop: ## Stop all services
	@echo "⏹️  Stopping services..."  
	@docker compose down
	@echo "✅ Services stopped!"

restart: ## Restart all services
	@echo "🔄 Restarting services..."
	@docker compose restart
	@echo "✅ Services restarted!"

logs: ## View logs from all services
	@docker compose logs -f

logs-query: ## View Query Service logs
	@docker compose logs -f race_application_query_service

logs-command: ## View Command Service logs  
	@docker compose logs -f race_application_command_service

logs-client: ## View Client logs
	@docker compose logs -f race_application_client

status: ## Check service status
	@echo "📊 Service Status:"
	@docker compose ps

# Database Commands
db-connect: ## Connect to PostgreSQL database
	@echo "🗄️  Connecting to PostgreSQL..."
	@docker compose exec postgres_query psql -U query -d query_db

db-reset: ## Reset database (WARNING: Destroys all data!)
	@echo "⚠️  WARNING: This will destroy all database data!"
	@echo "Are you sure? Press Ctrl+C to cancel, Enter to continue..."
	@read
	@docker compose down -v
	@docker volume rm intellexi-fullstack-assignment_query_db_data || true
	@echo "🗄️  Database reset completed!"

# Clean Commands
clean: ## Clean up everything (containers, volumes, images)
	@echo "🧹 Cleaning up..."
	@$(MAKE) clean-containers
	@$(MAKE) clean-volumes  
	@$(MAKE) clean-images
	@$(MAKE) clean-build
	@echo "✅ Cleanup completed!"

clean-containers: ## Stop and remove containers
	@echo "🧹 Removing containers..."
	@docker compose down --remove-orphans || true

clean-volumes: ## Remove Docker volumes (WARNING: Destroys data!)
	@echo "🧹 Removing volumes..."
	@docker volume rm intellexi-fullstack-assignment_query_db_data || true

clean-images: ## Remove Docker images
	@echo "🧹 Removing images..."
	@docker compose down --rmi all || true

clean-build: ## Clean build artifacts
	@echo "🧹 Cleaning build artifacts..."
	@cd services/race-application-command-service && mvn clean || true
	@cd services/race-application-query-service && mvn clean || true
	@cd client && rm -rf dist node_modules/.cache || true

# Development Utilities
install-deps: ## Install frontend dependencies
	@echo "📦 Installing frontend dependencies..."
	@cd client && npm ci
	@echo "✅ Dependencies installed!"

format: ## Format code (backend + frontend)
	@echo "🎨 Formatting code..."
	@cd services/race-application-command-service && mvn spotless:apply || true
	@cd services/race-application-query-service && mvn spotless:apply || true  
	@cd client && npm run format || true
	@echo "✅ Code formatted!"

lint: ## Lint code (frontend)
	@echo "🔍 Linting code..."
	@cd client && npm run lint || true
	@echo "✅ Linting completed!"

# Health Checks
health: ## Check service health
	@echo "🏥 Checking service health..."
	@echo "Command Service:" && curl -f http://localhost:8081/actuator/health || echo "❌ Command Service unhealthy"
	@echo "Query Service:" && curl -f http://localhost:8082/actuator/health || echo "❌ Query Service unhealthy"  
	@echo "Client Service:" && curl -f http://localhost:5173 || echo "❌ Client Service unhealthy"
	@echo "✅ Health check completed!"

# Production Commands  
prod-build: ## Build for production
	@echo "🏭 Building for production..."
	@$(MAKE) clean
	@$(MAKE) build
	@echo "✅ Production build completed!"

# Documentation
docs: ## Generate API documentation
	@echo "📚 Generating API documentation..."
	@echo "Command Service API docs available at: http://localhost:8081/swagger-ui.html (when running)"
	@echo "Query Service API docs available at: http://localhost:8082/swagger-ui.html (when running)"

# Debug Commands
debug: ## Start in debug mode (for IDE attachment)
	@echo "🐛 Starting in debug mode..."
	@docker compose -f docker-compose.yml -f docker-compose.debug.yml up -d --build || docker compose up -d --build
	@echo "🎯 Debug ports:"
	@echo "  Command Service: localhost:5005"
	@echo "  Query Service: localhost:5006"

# Quick Commands
quick-start: clean dev ## Quick start (clean + dev)

quick-test: build test ## Quick test (build + test)

full-cycle: clean build test dev ## Full development cycle

# Information
info: ## Show system information
	@echo "📋 Trail Race Registration System Information"
	@echo ""
	@echo "Architecture: CQRS Microservice"
	@echo "Backend: Spring Boot (Java)"
	@echo "Frontend: React (TypeScript)"
	@echo "Database: PostgreSQL"
	@echo "Messaging: RabbitMQ"
	@echo "Containerization: Docker + Docker Compose"
	@echo ""
	@echo "Services:"
	@echo "  • Command Service (Port 8081): Handles write operations"
	@echo "  • Query Service (Port 8082): Handles read operations"  
	@echo "  • React Client (Port 5173): User interface"
	@echo ""
	@echo "Repository: https://github.com/spintar1/intellexi-fullstack-assignment"