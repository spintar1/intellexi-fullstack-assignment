SHELL := /bin/sh

PROJECT_NAME := intellexi-fullstack-assignment

DOCKER_COMPOSE := docker compose

.PHONY: dev up down logs build docker-build test clean seed

help:
	@echo "Targets:"
	@echo "  dev             - Run full stack (RabbitMQ, DBs, services, client)"
	@echo "  up              - Start containers in background"
	@echo "  down            - Stop containers and remove"
	@echo "  logs            - Tail docker compose logs"
	@echo "  build           - Build backend services (Maven)"
	@echo "  docker-build    - Build docker images for services and client"
	@echo "  test            - Run unit/integration tests"
	@echo "  clean           - Clean build artifacts"
	@echo ""
	@echo "Debug Targets:"
	@echo "  debug-dev       - Start infrastructure for debugging (use IDE for services)"
	@echo "  debug-up        - Start only infrastructure (RabbitMQ, PostgreSQL)"
	@echo "  debug-logs      - Show infrastructure logs"
	@echo "  client-debug    - Start React app with debugging enabled"
	@echo "  build-debug     - Build all services with debug information"

dev: docker-build up logs

up:
	$(DOCKER_COMPOSE) up -d --remove-orphans

down:
	$(DOCKER_COMPOSE) down -v

logs:
	$(DOCKER_COMPOSE) logs -f --tail=200 | cat

build:
	cd services/race-application-command-service && mvn -q -DskipTests clean package
	cd services/race-application-query-service && mvn -q -DskipTests clean package

docker-build:
	$(DOCKER_COMPOSE) build --no-cache

test:
	cd services/race-application-command-service && mvn -q test
	cd services/race-application-query-service && mvn -q test

clean:
	cd services/race-application-command-service && mvn -q clean || true
	cd services/race-application-query-service && mvn -q clean || true
	$(DOCKER_COMPOSE) down -v || true

# Debug targets for enhanced debugging
debug-dev: docker-build-debug debug-up debug-logs

debug-up:
	$(DOCKER_COMPOSE) up -d rabbitmq postgres_command postgres_query
	@echo "Infrastructure started. Run Java services in IDE with debug configuration."

docker-build-debug:
	$(DOCKER_COMPOSE) build --no-cache --build-arg BUILD_TYPE=debug

debug-logs:
	$(DOCKER_COMPOSE) logs -f --tail=50 rabbitmq postgres_command postgres_query | cat

client-debug:
	cd client && npm run dev:debug

# Build with debug information
build-debug:
	cd services/race-application-command-service && mvn -q -DskipTests clean compile -Dmaven.compiler.debug=true
	cd services/race-application-query-service && mvn -q -DskipTests clean compile -Dmaven.compiler.debug=true
	cd client && npm run build:debug