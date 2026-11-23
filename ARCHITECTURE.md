# SmartPay — System Architecture Documentation

## 1. Executive Architecture Summary
SmartPay is built as a **Modular Monolith designed with strict Microservice Boundaries**, optimized for deployment on zero-cost cloud infrastructure (Render Free Tier 512 MB RAM, TiDB Cloud Starter, Upstash Redis Free, Aiven Kafka Free, CloudAMQP Free).

Rather than deploying eight isolated Spring Boot processes that would exceed container resource quotas and instance-hour budgets, SmartPay consolidates all business capabilities into a single executable deployment while maintaining absolute domain, persistence, and interface separation.

```
+-----------------------------------------------------------------------------------------+
|                                    React / Vite SPA                                     |
|                                (Render Static Site CDN)                                 |
+-----------------------------------------------------------------------------------------+
                                             |
                                  HTTPS / REST / JSON
                                             v
+-----------------------------------------------------------------------------------------+
|                                SmartPay Spring Boot App                                 |
|                         (Render Free Tier Web Service, 512MB RAM)                       |
|                                                                                         |
|  +----------------+  +----------------+  +----------------+  +-----------------------+  |
|  |  Auth Module   |  |  User Module   |  |  Wallet Module |  |  Transaction Module   |  |
|  +----------------+  +----------------+  +----------------+  +-----------------------+  |
|  +----------------+  +----------------+  +----------------+  +-----------------------+  |
|  | Payment Module |  | Beneficiary Mod|  | Notification M |  |     Audit Module      |  |
|  +----------------+  +----------------+  +----------------+  +-----------------------+  |
|                                                                                         |
|  +-----------------------------------------------------------------------------------+  |
|  |              Cross-Cutting: Security, Transactional Outbox, Caching               |  |
|  +-----------------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------------+
         |                       |                       |                      |
         v                       v                       v                      v
+-----------------+     +-----------------+     +------------------+   +------------------+
|   TiDB Cloud    |     |  Upstash Redis  |     |   Aiven Kafka    |   |     CloudAMQP    |
| (Relational DB) |     |  (Cache & Rate) |     | (Event Stream)   |   | (Async Msg Queue)|
+-----------------+     +-----------------+     +------------------+   +------------------+
```

---

## 2. Domain Modules & Boundaries

1. **Auth Module (`com.smartpay.auth`)**: Customer registration, BCrypt password hashing, JWT generation and validation, refresh token rotation, refresh token reuse detection, and login lockout.
2. **User Module (`com.smartpay.user`)**: Customer profiles, address and contact details, and KYC verification state machine.
3. **Wallet Module (`com.smartpay.wallet`)**: Multi-currency wallet accounts, balance projections, velocity limits (daily & per-transaction), and freeze/unfreeze controls.
4. **Transaction Module (`com.smartpay.transaction`)**: Core transfer processing, idempotency validation, double-entry ledger execution, transaction reference generation, and compensating reversals.
5. **Payment Module (`com.smartpay.payment`)**: Simulated payment adapter for deposits, payouts, webhooks, and refund processing without external paid subscriptions.
6. **Beneficiary Module (`com.smartpay.beneficiary`)**: Management of saved payee bank accounts and internal wallet contacts.
7. **Notification Module (`com.smartpay.notification`)**: In-app notifications and background durable email dispatch via RabbitMQ.
8. **Audit Module (`com.smartpay.audit`)**: Immutable logging of all sensitive actions, actor identities, and correlation IDs without recording secrets.

---

## 3. Financial Correctness Principles

### 3.1 Immutable Double-Entry Ledger
For every peer-to-peer transfer of amount $X$:
1. A **DEBIT** entry of $X$ is written for the sender wallet, updating its projected balance.
2. A **CREDIT** entry of $X$ is written for the destination wallet, updating its projected balance.
3. The sum of all debits strictly equals the sum of all credits.
4. Both entries share the same transaction reference (`TXN-XXXXXXXXXXXX`).

### 3.2 Deterministic Row Locking
To eliminate concurrency deadlocks when two wallets transfer funds to each other concurrently:
```java
Long firstId = Math.min(sourceWalletId, destWalletId);
Long secondId = Math.max(sourceWalletId, destWalletId);

Wallet first = walletRepository.findByIdForUpdate(firstId);
Wallet second = walletRepository.findByIdForUpdate(secondId);
```
Acquiring pessimistic write locks in monotonically ascending ID order guarantees deadlock-free transfers.

### 3.3 Transactional Outbox Pattern
Domain events are not published directly to Kafka inside the relational database transaction. Instead:
1. Business data (wallet balances, ledger entries, transaction record) and an `OutboxEvent` (status `PENDING`) are written in the **same MySQL/TiDB transaction**.
2. A scheduled, bounded background publisher polls pending outbox records and dispatches them to Kafka.
3. If Kafka is temporarily unreachable, the financial transaction is never rolled back; events are safely retried once connectivity restores.

### 3.4 Consumer Idempotency
Event consumers check the `processed_events` table by unique `eventId`. If a duplicate message is received, it is discarded safely.

---

## 4. Free-Tier Optimization Strategy
- **Container Footprint**: Configured with `-XX:+UseSerialGC -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=128m -Xss512k` to run reliably within Render's 512 MB RAM ceiling.
- **Connection Pools**: HikariCP maximum pool size capped at 4-5 connections, preserving TiDB connection limits.
- **Fail-Open Non-Authoritative Services**: If Redis or Kafka are paused by cloud sleep timers, financial transfers continue to execute correctly against the authoritative database.
