SHELL := /bin/sh

PROJECT_NAME := intellexi-fullstack-assignment

DOCKER_COMPOSE := docker compose

.PHONY: dev up down logs build docker-build test clean seed

help:
	@echo "Targets:"
	@echo "  dev           - Run full stack (RabbitMQ, DBs, services, client)"
	@echo "  up            - Start containers in background"
	@echo "  down          - Stop containers and remove"
	@echo "  logs          - Tail docker compose logs"
	@echo "  build         - Build backend services (Gradle)"
	@echo "  docker-build  - Build docker images for services and client"
	@echo "  test          - Run unit/integration tests"
	@echo "  clean         - Clean build artifacts"

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