# SmartPay — Digital Banking & Wallet Management Platform

> A production-grade FinTech digital banking and wallet platform built with Java 21, Spring Boot 3.5, React 19, and a consolidated modular architecture engineered for financial consistency, immutable double-entry ledger bookkeeping, and 100% free cloud deployment (₹0/month without credit card requirements).

[![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.3-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38B2AC?logo=tailwind-css)](https://tailwindcss.com/)
[![Render Free Tier](https://img.shields.io/badge/Render-Free%20Tier%20(₹0)-46E3B7?logo=render)](https://render.com)
[![MySQL 8.4](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql)](https://mysql.com)

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Prerequisites](#4-prerequisites)
5. [Local Installation](#5-local-installation)
6. [Docker Installation](#6-docker-installation)
7. [Environment Configuration](#7-environment-configuration)
8. [Database Migration Instructions](#8-database-migration-instructions)
9. [Backend Startup](#9-backend-startup)
10. [Frontend Startup](#10-frontend-startup)
11. [Test Commands](#11-test-commands)
12. [GitHub Setup](#12-github-setup)
13. [Render Deployment](#13-render-deployment)
14. [TiDB Setup](#14-tidb-setup)
15. [Upstash Redis Setup](#15-upstash-redis-setup)
16. [Aiven Kafka Setup](#16-aiven-kafka-setup)
17. [CloudAMQP Setup](#17-cloudamqp-setup)
18. [Environment Variable Configuration](#18-environment-variable-configuration)
19. [Deployment Troubleshooting](#19-deployment-troubleshooting)
20. [Free-Tier Limitations](#20-free-tier-limitations)
21. [Security Considerations](#21-security-considerations)
22. [Demo Instructions](#22-demo-instructions)

---

## 1. Project Overview
SmartPay is a comprehensive digital banking and wallet management system. It provides real business logic, database persistence, authentication and authorization, atomic transaction processing, event-driven messaging, automated tests, Docker support, and cloud deployment guides.

### Core Capabilities
- **Authentication & Security**: BCrypt password hashing, JWT stateless access tokens, refresh token rotation, refresh token reuse detection, login failure lockout.
- **Wallet & Account Management**: Creation of multi-currency wallets (INR default, USD, EUR, GBP), freeze/unfreeze controls, velocity limits (daily & per-transaction).
- **Double-Entry Ledger**: Atomic debit & credit ledger entries for every balance movement. Reversals create compensating entries without altering historical records.
- **Payment Simulation Adapter**: Top-up deposits, payouts, webhooks, and refunds without paid subscriptions or real card collection.
- **Beneficiaries**: Payee management with direct wallet-to-wallet and simulated bank transfers.
- **Administration & Audit**: System audit logs, KYC approvals, account freeze controls, and authoritative balance reconciliation.

---

## 2. Architecture
SmartPay is organized as a **modular monolith designed with microservice boundaries**. It packages eight distinct business modules into a single executable Spring Boot deployment:
1. **Auth Module (`com.smartpay.auth`)**
2. **User Module (`com.smartpay.user`)**
3. **Wallet Module (`com.smartpay.wallet`)**
4. **Transaction Module (`com.smartpay.transaction`)**
5. **Payment Module (`com.smartpay.payment`)**
6. **Beneficiary Module (`com.smartpay.beneficiary`)**
7. **Notification Module (`com.smartpay.notification`)**
8. **Audit Module (`com.smartpay.audit`)**

All modules interact through typed service contracts and the **Transactional Outbox Pattern** to ensure financial integrity even during network or broker outages.

Detailed documentation: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 3. Technology Stack

### Backend
- **Java 21** & **Spring Boot 3.5.3**
- **Spring Web**, **Spring Data JPA**, **Hibernate** (`ddl-auto: validate`)
- **Spring Security** & **JJWT 0.12.6**
- **Flyway Database Migrations** (MySQL/TiDB compatible)
- **Spring Actuator** & **Micrometer Prometheus**
- **Spring Kafka** & **Spring AMQP (RabbitMQ)** (Conditional & resilient)
- **Spring Data Redis** (Cache-aside & rate limiting)
- **Maven** build automation

### Frontend
- **React 19**, **Vite 6**, **TypeScript/JavaScript**
- **Tailwind CSS** & **Lucide React** icons
- **React Router 7**, **Axios**, **Zustand**
- **Recharts** for interactive financial data visualization
- **Vitest** & **Testing Library**

### Database & Messaging
- **MySQL 8.4** (Local Docker) / **TiDB Cloud Starter** (Cloud Production)
- **Redis 7** (Local Docker) / **Upstash Redis Free** (Cloud Production)
- **Apache Kafka KRaft** (Local Docker) / **Aiven Kafka Free** (Cloud Production)
- **RabbitMQ 3.13** (Local Docker) / **CloudAMQP Free Tier** (Cloud Production)

---

## 4. Prerequisites
- **JDK 21** installed locally.
- **Maven 3.9+** installed locally.
- **Node.js 20+** and **npm 10+**.
- **Docker Desktop** (optional for local full-stack containerization).

---

## 5. Local Installation

Clone the repository:
```bash
git clone https://github.com/<your-username>/smartpay-wallet.git
cd smartpay-wallet
```

Install backend dependencies:
```bash
cd backend
mvn clean install -DskipTests
cd ..
```

Install frontend dependencies:
```bash
cd frontend/smartpay-web
npm install
cd ../..
```

---

## 6. Docker Installation

Start all 10 local services (Backend, Frontend, MySQL 8.4, Redis 7, Kafka KRaft, RabbitMQ, Prometheus, Grafana, Nginx reverse proxy):
```bash
# 1. Copy sample environment file
cp .env.example .env

# 2. Build and start containers
docker compose up -d --build

# 3. Verify running containers
docker compose ps
```

Local access URLs:
- **Frontend App**: `http://localhost:3000` (or `http://localhost` via Nginx)
- **Backend API**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **RabbitMQ Dashboard**: `http://localhost:15672` (user: `smartpay`, pass: `changeme_rabbitmq_password`)
- **Grafana Dashboard**: `http://localhost:3001` (user: `admin`, pass: `changeme_grafana_password`)
- **Prometheus Metrics**: `http://localhost:9090`

---

## 7. Environment Configuration
Configuration is managed via `.env` locally and dashboard environment variables in the cloud. Refer to `.env.example` for all documented keys.

Key variables:
- `SPRING_PROFILES_ACTIVE`: `local` or `prod`
- `PORT`: HTTP port (`8080`)
- `TIDB_DATABASE_URL`: JDBC connection string for TiDB Cloud
- `UPSTASH_REDIS_URL`: `rediss://` connection string for Upstash
- `JWT_SECRET`: 256-bit hexadecimal string

---

## 8. Database Migration Instructions
Database migrations are automatically applied on application boot by Flyway:
- `V1__init_schema.sql`: Core tables (users, wallets, transactions, ledger, payments, outbox).
- `V2__add_missing_enterprise_tables.sql`: Enterprise tables (limits, idempotency records, KYC, processed events, webhooks).

To manually check migration status:
```bash
cd backend
mvn flyway:info
```

---

## 9. Backend Startup
To run the backend independently against a local MySQL instance:
```bash
cd backend
export SPRING_PROFILES_ACTIVE=local
export DB_HOST=localhost
export DB_PORT=3307
export DB_NAME=smartpay
export DB_USER=smartpay
export DB_PASSWORD=changeme
mvn spring-boot:run
```

---

## 10. Frontend Startup
To start the React/Vite development server:
```bash
cd frontend/smartpay-web
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## 11. Test Commands

Run backend automated test suite:
```bash
cd backend
mvn test
```

Run frontend vitest suite:
```bash
cd frontend/smartpay-web
npm test -- --run
```

Run frontend production build verification:
```bash
cd frontend/smartpay-web
npm run build
```

---

## 12. GitHub Setup
1. Create a repository on GitHub named `smartpay-wallet`.
2. Push your codebase:
   ```bash
   git add .
   git commit -m "feat: complete SmartPay platform"
   git push origin main
   ```

---

## 13. Render Deployment
- **Backend**: Deploy as a Render Free Web Service using Docker (`backend/Dockerfile`).
- **Frontend**: Deploy as a Render Static Site (`frontend/smartpay-web/dist`).
- **Cost**: ₹0 / month (No credit card required).

Step-by-step walkthrough: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

---

## 14. TiDB Setup
1. Register on [tidbcloud.com](https://tidbcloud.com) (No credit card needed).
2. Create a free **Starter** cluster.
3. Create the database: `CREATE DATABASE smartpay;`
4. Copy the JDBC connection URL with SSL enabled:
   `jdbc:mysql://<host>:4000/smartpay?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC`

---

## 15. Upstash Redis Setup
1. Register on [console.upstash.com](https://console.upstash.com) (No credit card needed).
2. Create a database with **TLS enabled**.
3. Copy the `rediss://` connection URL.
4. Set as `UPSTASH_REDIS_URL` in the Render dashboard.

---

## 16. Aiven Kafka Setup
1. Register on [aiven.io](https://aiven.io) (Free trial / Free plan).
2. Provision free Kafka cluster with 5 primary topics:
   - `smartpay.user.events.v1`
   - `smartpay.wallet.events.v1`
   - `smartpay.transaction.events.v1`
   - `smartpay.payment.events.v1`
   - `smartpay.notification.events.v1`
3. Configure `KAFKA_BOOTSTRAP_SERVERS` and SASL credentials.

---

## 17. CloudAMQP Setup
1. Register on [cloudamqp.com](https://cloudamqp.com).
2. Provision a free **Little Lemur** instance (₹0 / month).
3. Copy the AMQP connection URL.
4. Set `SMARTPAY_RABBITMQ_ENABLED=true` when active.

---

## 18. Environment Variable Configuration
Refer to [.env.example](.env.example) for the exhaustive list of environment variables and dashboard mappings.

---

## 19. Deployment Troubleshooting
- **Backend sleeping on inactivity**: Render free tier instances sleep after 15 minutes of inactivity. Initial wake-up takes 30-50 seconds.
- **TiDB handshake error**: Verify `sslMode=VERIFY_IDENTITY` and `enabledTLSProtocols=TLSv1.2,TLSv1.3` are present in `TIDB_DATABASE_URL`.
- **Out of Memory (OOM)**: Ensure `JAVA_TOOL_OPTIONS` includes `-XX:+UseSerialGC -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=128m -Xss512k`.

---

## 20. Free-Tier Limitations
- **Render Web Service**: 512 MB RAM, sleeps after inactivity, 750 free instance-hours per workspace per month.
- **TiDB Cloud Starter**: 5 GB storage limit, 50 million Request Units per month.
- **Upstash Redis**: 10,000 commands per day, 256 MB storage.

---

## 21. Security Considerations
- Zero secrets committed to Git repository.
- Sensitive values read strictly through environment variables.
- Passwords protected by BCrypt hashing.
- Idempotency prevents double-spending.
- Deterministic locking prevents concurrency deadlocks.

---

## 22. Demo Instructions
1. Navigate to your deployed application or `http://localhost:3000`.
2. **Register a Customer**:
   - Register at `/register` -> Automatic welcome credit of ₹1,000.00 is provisioned.
3. **Execute a Transfer**:
   - Register a second customer in another tab.
   - Transfer ₹250.00 from Customer 1 to Customer 2 at `/transfer`.
   - Inspect the real-time balance reduction and balanced double-entry ledger entries at `/wallets`.
4. **Deposit & Withdrawal**:
   - Use `/deposit` to simulate adding ₹2,500.00 via simulated UPI.
   - Use `/withdraw` to simulate withdrawing ₹500.00 via IMPS.
5. **Admin Operations**:
   - Log in as `admin@smartpay.io` / `Admin@SmartPay2026!`.
   - Navigate to `/admin`.
   - Click **Reconcile All Wallets** to execute an authoritative balance vs. ledger audit across all customer accounts.
   - Manage users at `/admin/users` and inspect forensic audit logs at `/admin/audit`.
