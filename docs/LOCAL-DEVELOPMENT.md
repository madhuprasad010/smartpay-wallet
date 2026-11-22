# Local Development Guide

## Prerequisites

- **Java:** OpenJDK 21 LTS (`brew install openjdk@21`)
- **Node.js:** Node 20+ and npm 10+
- **Maven:** Apache Maven 3.9+ (`brew install maven`)
- **Docker:** Docker Desktop with Compose support

---

## 1. Quick Start Commands

### Start All Infrastructure Containers
```bash
make infra-up
```
This boots MySQL, Redis, Kafka (KRaft), RabbitMQ, Prometheus, and Grafana.

### Build and Verify Backend
```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.11/libexec/openjdk.jdk/Contents/Home
make backend-build
make backend-test
```

### Install and Run Frontend
```bash
make frontend-install
make frontend-dev
```
The React 19 application will launch at `http://localhost:5173`.

---

## 2. Accessing Developer Dashboards

- **API Gateway:** `http://localhost:8080/actuator/health`
- **React Frontend:** `http://localhost:5173`
- **RabbitMQ Management:** `http://localhost:15672` (Username: `smartpay_rabbit`, Password: `smartpay_rabbit_pass`)
- **Prometheus UI:** `http://localhost:9090`
- **Grafana Dashboards:** `http://localhost:3001` (Username: `admin`, Password: `smartpay_admin_pass`)
- **MySQL Direct:** `localhost:3306` (User: `root`, Password: `smartpay_root_pass`)
