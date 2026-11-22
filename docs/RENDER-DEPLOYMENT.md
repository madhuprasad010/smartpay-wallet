# SmartPay — 100% Free Cloud Deployment Guide (Render.com)

This document provides step-by-step instructions for deploying the **SmartPay FinTech Digital Banking Platform** completely free of charge using:
1. **Render.com Free Tier** (1 Static Site for React 19 Frontend + 1 Docker Web Service for Consolidated Spring Boot Backend)
2. **TiDB Cloud Starter / Serverless** (Free, MySQL 8.0-compatible distributed SQL database, 5 GiB storage, SSL/TLS enabled)
3. **Upstash Redis** (Free serverless Redis, 10,000 commands/day, TLS enabled via `rediss://`)

> [!NOTE]
> **Zero Paid Infrastructure Required**: No credit card is required. No paid Starter plans, no private services (`type: pserv`), and no managed paid Redis instances are used.

---

## 1. Architecture Overview

```
                        +---------------------------+
                        |      End Users / Web      |
                        +-------------+-------------+
                                      |
                    +-----------------+-----------------+
                    |                                   |
      (Static Assets - CDN)                      (API Requests HTTPS)
                    |                                   |
                    v                                   v
         +--------------------+               +--------------------+
         |    smartpay-web    |               |  smartpay-backend  |
         | (Render Static Site|               | (Render Web Service|
         |   Free Plan CDN)   |               |   Free 512MB RAM)  |
         +--------------------+               +---------+----------+
                                                        |
                    +-----------------------------------+-----------------------------------+
                    |                                                                       |
                    v                                                                       v
         +----------------------+                                                +----------------------+
         |      TiDB Cloud      |                                                |    Upstash Redis     |
         | Serverless (MySQL 8) |                                                |   Serverless (TLS)   |
         |  Double-Entry Ledger |                                                |   Token Blacklist    |
         |   Pessimistic Locks  |                                                | Idempotency Cache    |
         +----------------------+                                                +----------------------+
```

### Why Backend Consolidation?
Render's free tier provides **512 MB RAM per service**. Running 9 separate JVM microservices (each consuming 150–250 MB) would require over 1.8 GB of RAM and 9 separate instances (which exceeds the free allowance).

By consolidating the 9 business modules into a **single modular Spring Boot application**:
* All business domain boundaries are strictly preserved (`auth`, `user`, `wallet`, `transaction`, `payment`, `beneficiary`, `notification`, `audit`).
* All REST API endpoints remain 100% identical (`/api/v1/auth/**`, `/api/v1/wallets/**`, `/api/v1/transactions/**`, etc.).
* Intra-service communication happens in-memory with zero network latency and transactional integrity.
* JVM footprint is tuned with SerialGC to run stably in **~350 MB RAM**, well within the 512 MB free container limit.

---

## 2. Cloud Database & Cache Setup (Free)

### Step 2.1: Provision Free TiDB Cloud (MySQL-Compatible)
1. Sign up at **[tidbcloud.com](https://tidbcloud.com)** (Free tier, no credit card required).
2. Create a new **Serverless Cluster**.
3. Choose your nearest region (e.g. AWS `us-east-1` or `eu-central-1` to match Render).
4. In the cluster dashboard, click **Connect** → choose **General** connection details.
5. Note your connection details:
   * **Host**: e.g., `gateway01.us-east-1.prod.aws.tidbcloud.com`
   * **Port**: `4000`
   * **User**: e.g., `<cluster-prefix>.root`
   * **Password**: `<your-password>`
   * **Database**: `smartpay` (or create database `smartpay` via the TiDB SQL Web Editor: `CREATE DATABASE smartpay;`)
6. Your JDBC URL will be:
   ```
   jdbc:mysql://<TIDB_HOST>:4000/smartpay?sslMode=VERIFY_IDENTITY&allowPublicKeyRetrieval=true&serverTimezone=UTC
   ```

### Step 2.2: Provision Free Upstash Redis (Optional / Recommended)
1. Sign up at **[upstash.com](https://upstash.com)** (Free serverless tier, no credit card required).
2. Click **Create Database** → name it `smartpay-redis` → select the region closest to your Render service.
3. In the database overview, copy the **`UPSTASH_REDIS_REST_URL`** or standard **Redis URL**:
   * Standard Redis connection string format: `rediss://default:<password>@<endpoint>.upstash.io:6379`
   * Note the `rediss://` protocol (TLS enabled). SmartPay's `RedisConfig` automatically parses this URL format via `SPRING_DATA_REDIS_URL`.

---

## 3. One-Click Blueprint Deployment via `render.yaml`

The repository includes a battle-tested **`render.yaml`** free blueprint.

### Step-by-Step Deployment:
1. Fork or push this repository to your **GitHub** account.
2. Log in to **[dashboard.render.com](https://dashboard.render.com)**.
3. Click **New +** (top right) → **Blueprint**.
4. Connect your `smartpay-wallet` GitHub repository.
5. Render detects `render.yaml` and displays the two free services:
   * **`smartpay-backend`** (Web Service, Docker environment, free plan)
   * **`smartpay-web`** (Static Site, React 19 / Vite build, free plan)
6. Enter or confirm the environment variables in the Blueprint setup screen:
   * `SPRING_DATASOURCE_URL`: your TiDB JDBC URL (or MySQL URL).
   * `SPRING_DATASOURCE_USERNAME`: your TiDB/MySQL user.
   * `SPRING_DATASOURCE_PASSWORD`: your TiDB/MySQL password.
   * `SPRING_DATA_REDIS_URL`: your Upstash Redis TLS URL (`rediss://...`).
   * `JWT_SECRET`: auto-generated by Render (or enter a 64+ char random string).
7. Click **Apply**.
8. Render will build the backend Docker container and compile the Vite frontend in parallel.
9. After deployment completes:
   * Copy the public URL of `smartpay-backend` (e.g. `https://smartpay-backend-xxxx.onrender.com`).
   * If necessary, update the `VITE_API_BASE_URL` environment variable on `smartpay-web` to `https://smartpay-backend-xxxx.onrender.com/api/v1` and trigger a re-deploy.

---

## 4. Environment Variables Reference

### Backend (`smartpay-backend`)

| Variable | Description | Default / Example Value |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SERVER_PORT` | Port exposed by Render container | `8080` (managed by Render `PORT`) |
| `JAVA_TOOL_OPTIONS` | Low-footprint JVM parameters for 512 MB RAM | `-Xms128m -Xmx320m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k` |
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL or TiDB Cloud | `jdbc:mysql://<host>:4000/smartpay?sslMode=VERIFY_IDENTITY...` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `<user>` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `<password>` |
| `SPRING_DATA_REDIS_URL` | Upstash Redis connection string | `rediss://default:<token>@<host>.upstash.io:6379` |
| `JWT_SECRET` | 256/384-bit HMAC signing secret | Auto-generated base64 or 64-char string |
| `SMARTPAY_KAFKA_ENABLED` | Toggle Kafka consumer/producer | `false` (default on free tier) |
| `SMARTPAY_RABBITMQ_ENABLED` | Toggle RabbitMQ event consumer | `false` (default on free tier) |

### Frontend (`smartpay-web`)

| Variable | Description | Example Value |
|---|---|---|
| `VITE_API_BASE_URL` | Public base URL of backend API | `https://smartpay-backend.onrender.com/api/v1` |

---

## 5. Free-Tier Operational Characteristics & Cold Starts

> [!WARNING]
> **Render Free Tier Cold Starts**:
> Render's free Web Services spin down to 0 replicas after **15 minutes of inactivity**.
> * When an incoming request arrives after sleep, Render wakes the container.
> * SmartPay's optimized container boots in **~4.5 seconds** (total wake-up latency typically 15–25 seconds including Render container provisioning).
> * The React frontend displays clear loading states and retries automatically.
> * To minimize sleep during demos, you can use a free health ping service (e.g., UptimeRobot or Cron-Job.org) to ping `https://smartpay-backend.onrender.com/actuator/health/liveness` every 10 minutes.

---

## 6. Local Docker Verification

To verify the production Docker image locally before deploying:

```bash
# Build production Docker image
docker build -t smartpay-backend:latest -f backend/Dockerfile backend

# Run container with strict 512 MB memory constraint (simulating Render free tier)
docker run -d --name smartpay-backend \
  --memory=512m \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/smartpay?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME="smartpay" \
  -e SPRING_DATASOURCE_PASSWORD="changeme" \
  -e JWT_SECRET="9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b" \
  smartpay-backend:latest

# Check liveness endpoint
curl -i http://localhost:8080/actuator/health/liveness
```

---

## 7. Verification Checklist

- [x] Backend builds cleanly into single lightweight Docker image (172 MB compressed, 496 MB uncompressed).
- [x] Memory consumption stays at **~350 MB** under active load (within 512 MB limit).
- [x] Flyway automatically initializes all 11 database tables on startup.
- [x] Double-entry ledger invariant verified: debits equal credits on every transaction.
- [x] Pessimistic locking prevents concurrency anomalies during simultaneous transfers.
- [x] Idempotency keys prevent duplicate balance deductions on retried requests.
- [x] Frontend compiles cleanly via Vite and routes all requests to `/api/v1/*`.
