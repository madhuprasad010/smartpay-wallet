# SmartPay — Zero-Cost Cloud Deployment Guide (₹0 / Month)

This guide provides step-by-step instructions to deploy the complete SmartPay Digital Banking & Wallet System to production using **100% free cloud services without requiring a credit card or debit card**.

---

## Zero-Cost & No-Card Guarantee
- **Render**: Free Web Service & Free Static Site (No card required on signup).
- **TiDB Cloud Starter**: Free MySQL-compatible serverless cluster with 5 GB storage (No card required).
- **Upstash Redis**: Free tier with 10,000 commands/day and TLS enabled (No card required).
- **GitHub**: Free public/private repository.

---

## Step 1: Push Repository to GitHub

1. Create a free account on [github.com](https://github.com) if you do not already have one.
2. Create a new repository named `smartpay-wallet`.
3. In your local terminal, initialize and push:
   ```bash
   cd /Users/rihaan/.gemini/antigravity/scratch/smartpay-wallet
   git remote set-url origin https://github.com/<your-username>/smartpay-wallet.git
   git add .
   git commit -m "feat: complete SmartPay digital banking platform"
   git push -u origin main
   ```

---

## Step 2: Provision Free MySQL Database (TiDB Cloud Starter)

1. Open [tidbcloud.com](https://tidbcloud.com) and sign up with your email or GitHub account (**No credit card required**).
2. Click **Create Cluster**.
3. Select **Starter** (Free Tier - ₹0).
4. Choose an AWS region closest to you (e.g. `ap-south-1` Mumbai or `us-east-1`).
5. Set cluster name: `smartpay-db` and click **Create**.
6. In the security popup, set a strong root password and record it.
7. Click **Connect**:
   - Choose **Connect with SQL Client** or **General**.
   - Note the Host, Port (4000), User, and Password.
   - Example JDBC URL format:
     ```
     jdbc:mysql://gateway01.ap-south-1.prod.aws.tidbcloud.com:4000/smartpay?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC
     ```
8. In the TiDB Cloud Web SQL Editor (or local MySQL client), run:
   ```sql
   CREATE DATABASE IF NOT EXISTS smartpay;
   ```

---

## Step 3: Provision Free Redis Cache (Upstash Redis)

1. Open [console.upstash.com](https://console.upstash.com) and log in with GitHub (**No credit card required**).
2. Click **Create Database**.
3. Set name: `smartpay-redis`.
4. Select Region: match your database region.
5. Select Type: **Regional / Serverless** (Free tier).
6. Enable **TLS (SSL)**.
7. Click **Create**.
8. Scroll to **Connect to your database** and select the **Java / Spring** or **Node.js** tab.
9. Copy the `rediss://` URL:
   ```
   rediss://default:YOUR_PASSWORD@your-cluster.upstash.io:6379
   ```

---

## Step 4: Deploy Consolidated Backend on Render

1. Sign up on [render.com](https://render.com) using your GitHub account (**No credit card required** for free plans).
2. In the Render Dashboard, click **New +** -> **Web Service**.
3. Connect your GitHub repository `smartpay-wallet`.
4. Configure the Web Service:
   - **Name**: `smartpay-backend`
   - **Region**: Oregon (US West) or Singapore
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `backend/Dockerfile`
   - **Docker Context**: `backend`
   - **Instance Type**: `Free` ($0/mo - 512 MB RAM, 0.1 CPU)
5. Under **Health Check Path**, enter: `/actuator/health/liveness`
6. Scroll down to **Environment Variables** and add:
   | Key | Value | Notes |
   |---|---|---|
   | `PORT` | `8080` | Container port |
   | `SPRING_PROFILES_ACTIVE` | `prod` | Activates lightweight prod profile |
   | `JAVA_TOOL_OPTIONS` | `-XX:+UseSerialGC -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=128m -Xss512k` | Essential for 512MB RAM stability |
   | `JWT_SECRET` | *(Click "Generate" or paste 64-char hex)* | 256-bit signing key |
   | `TIDB_DATABASE_URL` | `jdbc:mysql://<host>:4000/smartpay?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC` | From Step 2 |
   | `TIDB_USER` | `<tidb-user>` | From Step 2 |
   | `TIDB_PASSWORD` | `<tidb-password>` | From Step 2 |
   | `UPSTASH_REDIS_URL` | `rediss://default:<pass>@<host>:6379` | From Step 3 |
   | `SMARTPAY_KAFKA_ENABLED` | `false` | Keep false unless Aiven is connected |
   | `SMARTPAY_RABBITMQ_ENABLED` | `false` | Keep false unless CloudAMQP is connected |

7. Click **Deploy Web Service**.
8. Render will build the Docker container and start the Spring Boot backend.
9. When status turns **Live**, copy your backend public URL (e.g. `https://smartpay-backend.onrender.com`).
10. Verify health:
    ```bash
    curl -s https://smartpay-backend.onrender.com/actuator/health
    ```

---

## Step 5: Deploy Frontend on Render Static Sites

1. In the Render Dashboard, click **New +** -> **Static Site**.
2. Connect the same repository `smartpay-wallet`.
3. Configure the Static Site:
   - **Name**: `smartpay-web`
   - **Branch**: `main`
   - **Build Command**: `cd frontend/smartpay-web && npm ci && npm run build`
   - **Publish Directory**: `frontend/smartpay-web/dist`
4. Under **Environment Variables**, add:
   | Key | Value |
   |---|---|
   | `VITE_API_BASE_URL` | `https://smartpay-backend.onrender.com/api/v1` |
   *(Replace with your actual backend URL from Step 4)*
5. Under **Redirects/Rewrites**, add:
   - **Type**: `Rewrite`
   - **Source**: `/*`
   - **Destination**: `/index.html`
   *(This ensures client-side React Router functions on page refresh)*
6. Click **Create Static Site**.
7. Render will build and deploy the React frontend to global CDN.
8. Once live, open your public URL (e.g. `https://smartpay-web.onrender.com`).

---

## Step 6: Verify Deployment & Live Demo

1. Open your live frontend URL in your browser.
2. Click **Create Account** and register a new customer:
   - Email: `demo.customer@smartpay.io`
   - Password: `Password@123`
   - Name: `Demo Customer`
3. Log in with the credentials:
   - You will be granted an initial welcome wallet balance of ₹1,000.00.
4. Test transfers:
   - Register a second customer in an incognito window.
   - Note their wallet number (e.g. `WLT-XXXXX`).
   - Transfer ₹250.00 and observe real-time balance updates and double-entry ledger entries.
5. Log in as System Administrator:
   - Email: `admin@smartpay.io`
   - Password: `Admin@SmartPay2026!`
   - Access the Administrator Console at `/admin`.
   - Run the **Authoritative Double-Entry Ledger Reconciliation** to verify balanced status.
   - Inspect User Management and Audit Logs.
