# Deployment & Production Readiness Guide

## 1. Containerization Architecture

All 9 backend microservices and the React frontend utilize multi-stage production Dockerfiles:
- **Backend:** Stage 1 (`maven:3.9.9-eclipse-temurin-21-alpine`) compiles and packages executable JARs; Stage 2 (`eclipse-temurin:21-jre-alpine`) runs the application as a non-privileged `smartpay` user.
- **Frontend:** Stage 1 (`node:22-alpine`) builds optimized static assets with Vite; Stage 2 (`nginx:alpine`) serves them with gzip compression and cache headers.

---

## 2. Production Deployment via Docker Compose

```bash
docker compose -f docker-compose.yml up -d --build
```

---

## 3. Production Hardening Checklist

- [x] Non-root execution in application containers.
- [x] Multi-stage build strip compile-time dependencies.
- [x] Externalized credentials via environment variables (`.env`).
- [x] Cryptographic passwords with BCrypt.
- [x] Spring Boot Actuator endpoints restricted in production.
- [x] Double-entry ledger invariants enforced in ACID database transactions.
