# Vallexia Makefile - Docker Compose Management
.PHONY: help dev dev-up dev-build dev-down dev-stop dev-logs dev-restart dev-db dev-clean prod prod-up prod-build prod-down prod-stop prod-logs prod-restart prod-clean clean

# Default target
.DEFAULT_GOAL := help

# Variables
COMPOSE_DEV := docker compose --project-directory deployment/docker -f deployment/docker/docker-compose.dev.yml --env-file .env
COMPOSE_PROD := docker compose --project-directory deployment/docker -f deployment/docker/docker-compose.prod.yml --env-file .env

##@ Development Environment

dev: dev-up ## Start development environment (alias for dev-up)

dev-up: ## Start all development services
	@echo "Starting development environment..."
	$(COMPOSE_DEV) up -d
	@echo "Development environment started!"
	@echo "  - Backend: http://localhost:8080"
	@echo "  - Frontend: http://localhost:5173"
	@echo "  - PostgreSQL: localhost:5432"
	@echo "  - Redis: localhost:6379"

dev-build: ## Build and start all development services (rebuilds images, clears volumes)
	@echo "Building and starting development environment..."
	@echo "Clearing volumes to ensure clean database state..."
	$(COMPOSE_DEV) down -v
	@echo "Building and starting services..."
	$(COMPOSE_DEV) up --build -d
	@echo "Development environment built and started!"
	@echo "  - Backend: http://localhost:8080"
	@echo "  - Frontend: http://localhost:5173"
	@echo "  - PostgreSQL: localhost:5432"
	@echo "  - Redis: localhost:6379"

dev-down: dev-stop ## Stop development environment (alias for dev-stop)

dev-stop: ## Stop all development services
	@echo "Stopping development environment..."
	$(COMPOSE_DEV) stop

dev-logs: ## View development environment logs (follow mode)
	$(COMPOSE_DEV) logs -f

dev-restart: dev-stop dev-up ## Restart development environment

dev-db: ## Start only database and Redis (for local Spring Boot development)
	@echo "Starting development database and Redis..."
	$(COMPOSE_DEV) up -d postgres redis
	@echo "Database and Redis started!"
	@echo "  - PostgreSQL: localhost:5432"
	@echo "  - Redis: localhost:6379"

dev-clean: ## Stop and remove all development containers and volumes (WARNING: deletes data)
	@echo "WARNING: This will delete all development data!"
	@echo "Press Ctrl+C to cancel, or wait 5 seconds to continue..."
	@timeout /t 5 /nobreak >nul 2>&1 || sleep 5 || true
	$(COMPOSE_DEV) down -v
	@echo "Development environment cleaned!"

##@ Production Environment

prod: prod-up ## Start production environment (alias for prod-up)

prod-up: ## Start all production services
	@echo "Starting production environment..."
	$(COMPOSE_PROD) up -d
	@echo "Production environment started!"
	@echo "  - Application: http://localhost:80"
	@echo "  - Backend API: http://localhost:80/api"

prod-build: ## Build and start all production services (rebuilds images)
	@echo "Building and starting production environment..."
	$(COMPOSE_PROD) up --build -d
	@echo "Production environment built and started!"
	@echo "  - Application: http://localhost:80"
	@echo "  - Backend API: http://localhost:80/api"

prod-down: prod-stop ## Stop production environment (alias for prod-stop)

prod-stop: ## Stop all production services
	@echo "Stopping production environment..."
	$(COMPOSE_PROD) stop

prod-logs: ## View production environment logs (follow mode)
	$(COMPOSE_PROD) logs -f

prod-restart: prod-stop prod-up ## Restart production environment

prod-clean: ## Stop and remove all production containers and volumes (WARNING: deletes data)
	@echo "WARNING: This will delete all production data!"
	@echo "Press Ctrl+C to cancel, or wait 5 seconds to continue..."
	@timeout /t 5 /nobreak >nul 2>&1 || sleep 5 || true
	$(COMPOSE_PROD) down -v
	@echo "Production environment cleaned!"

##@ Utilities

clean: ## Stop and remove all containers (both dev and prod) without volumes
	@echo "Stopping and removing all containers..."
	$(COMPOSE_DEV) down && $(COMPOSE_PROD) down
	@echo "All containers stopped!"

ps: ## Show running containers for both environments
	@echo "=== Development Containers ==="
	$(COMPOSE_DEV) ps
	@echo ""
	@echo "=== Production Containers ==="
	$(COMPOSE_PROD) ps

status: ps ## Show container status (alias for ps)

help: ## Display this help message
	@echo Vallexia Docker Compose Management
	@echo.
	@echo Development Environment:
	@echo   make dev         - Start development environment
	@echo   make dev-build   - Build and start development environment (rebuilds images)
	@echo   make dev-db      - Start only database and Redis
	@echo   make dev-stop    - Stop development services
	@echo   make dev-logs    - View development logs
	@echo   make dev-restart - Restart development environment
	@echo   make dev-clean   - Stop and remove development containers and volumes
	@echo.
	@echo Production Environment:
	@echo   make prod        - Start production environment
	@echo   make prod-build  - Build and start production environment (rebuilds images)
	@echo   make prod-stop   - Stop production services
	@echo   make prod-logs   - View production logs
	@echo   make prod-restart - Restart production environment
	@echo   make prod-clean  - Stop and remove production containers and volumes
	@echo.
	@echo Utilities:
	@echo   make clean       - Stop all containers
	@echo   make ps          - Show running containers
	@echo   make status      - Show container status
	@echo   make help        - Display this help message