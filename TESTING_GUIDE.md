# SmartPay — Testing & Quality Assurance Guide

## 1. Overview
SmartPay maintains an automated test suite verifying business logic, financial invariants, idempotency, security, and React user interface components.

---

## 2. Running Backend Tests

Ensure Java 21 is active:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd backend
mvn test
```

### Test Suite Structure
- `AuthServiceTests.java`: Customer registration, duplicate email rejection, BCrypt hashing, JWT issuance, and login verification.
- `WalletServiceTests.java`: Wallet provisioning, credit posting, balance calculations, and insufficient balance rejection.
- `TransactionServiceTests.java`: Atomic peer-to-peer transfers, double-entry ledger balance conservation, and idempotency key replays.
- `FinancialCorrectnessTests.java`:
  - Rejection of duplicate idempotency key with conflicting payload (`409 Conflict`).
  - Rejection of zero and negative transfer amounts.
  - Rejection of transfers from frozen wallets.
  - Compensating reversals with balanced debit/credit posting.
  - Global double-entry balance reconciliation ($Calculated = Projected$).
- `PaymentServiceTests.java`: Deposit simulation, payout withdrawal simulation, and wallet ledger integration.
- `BeneficiaryServiceTests.java`: Beneficiary CRUD lifecycle and payout execution.
- `SmartPayApplicationTests.java`: Spring Boot ApplicationContext sanity check.

---

## 3. Running Frontend Tests

```bash
cd frontend/smartpay-web
npm test -- --run
```
To run tests with watch mode:
```bash
npm test
```
To verify production bundle build:
```bash
npm run build
```

---

## 4. End-to-End & Integration Testing with Docker Compose

To verify the entire environment (Backend, Frontend, MySQL 8.4, Redis 7, Kafka KRaft, RabbitMQ, Prometheus, Grafana, Nginx):

```bash
# 1. Start all infrastructure and application services
docker compose up -d --build

# 2. Inspect running service status
docker compose ps

# 3. Verify backend health
curl -s http://localhost:8080/actuator/health | jq .

# 4. Run automated E2E script
./scripts/test-e2e.sh
```
