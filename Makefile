# ============================================================
# SmartPay Wallet — Makefile
# ============================================================

SHELL := /bin/bash
.DEFAULT_GOAL := help

export JAVA_HOME := /opt/homebrew/Cellar/openjdk@21/21.0.11/libexec/openjdk.jdk/Contents/Home
export PATH := $(JAVA_HOME)/bin:$(PATH)

COMPOSE := docker compose
COMPOSE_FILE := docker-compose.yml

# --- Infrastructure ---

.PHONY: infra-up
infra-up: ## Start infrastructure services (MySQL, Redis, Kafka, RabbitMQ, monitoring)
	@if [ ! -f .env ]; then cp .env.example .env && echo "Created .env from .env.example"; fi
	$(COMPOSE) -f $(COMPOSE_FILE) up -d mysql redis kafka rabbitmq prometheus grafana

.PHONY: infra-down
infra-down: ## Stop all infrastructure services
	$(COMPOSE) -f $(COMPOSE_FILE) down

# --- Backend (Java 21 / Maven) ---

.PHONY: backend-build
backend-build: ## Build all backend microservices
	cd backend && mvn clean package -DskipTests

.PHONY: backend-test
backend-test: ## Run backend tests
	cd backend && mvn test

# --- Frontend (Node / React 19) ---

.PHONY: frontend-install
frontend-install: ## Install frontend dependencies
	cd frontend/smartpay-web && npm install

.PHONY: frontend-dev
frontend-dev: ## Start frontend dev server
	cd frontend/smartpay-web && npm run dev

.PHONY: frontend-build
frontend-build: ## Build frontend for production
	cd frontend/smartpay-web && npm run build

.PHONY: frontend-test
frontend-test: ## Run frontend tests
	cd frontend/smartpay-web && npm run test

# --- Full Stack ---

.PHONY: full-build
full-build: backend-build frontend-build ## Build entire project (backend + frontend)
	@echo "Full build completed."

.PHONY: full-up
full-up: ## Start all services (infrastructure + application)
	@if [ ! -f .env ]; then cp .env.example .env && echo "Created .env from .env.example"; fi
	$(COMPOSE) -f $(COMPOSE_FILE) up -d --build

# --- Health & Monitoring ---

.PHONY: health-check
health-check: ## Check health of all services
	@bash scripts/health-check.sh

# --- Logs ---

.PHONY: logs
logs: ## Tail logs from all containers
	$(COMPOSE) -f $(COMPOSE_FILE) logs -f --tail=100

# --- Cleanup ---

.PHONY: clean
clean: ## Stop containers, remove volumes, and clean build artifacts
	$(COMPOSE) -f $(COMPOSE_FILE) down -v --remove-orphans
	-cd backend && mvn clean 2>/dev/null || true
	-rm -rf frontend/smartpay-web/node_modules frontend/smartpay-web/dist 2>/dev/null || true
	@echo "Clean completed."

# --- Help ---

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
