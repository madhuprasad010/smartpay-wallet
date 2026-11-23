# SmartPay — Database Schema & Data Dictionary

## 1. Overview
SmartPay uses MySQL 8.4 for local development and TiDB Cloud Starter for zero-cost cloud deployment. All migrations are managed through Flyway (`V1__init_schema.sql`, `V2__add_missing_enterprise_tables.sql`).

All financial monetary amounts are persisted strictly as `DECIMAL(19,4)` to eliminate floating-point rounding errors and meet FinTech compliance standards.

---

## 2. Table Specifications

### 2.1 `users`
Core identity table for customer and staff accounts.
- `id` (BIGINT, PK, Auto Increment)
- `email` (VARCHAR(150), Unique, Indexed) — User's primary login credential
- `password_hash` (VARCHAR(255)) — Salted BCrypt hash (cost factor 10)
- `role` (VARCHAR(30)) — `CUSTOMER`, `ADMIN`, `SUPPORT`, `SYSTEM`
- `status` (VARCHAR(30)) — `ACTIVE`, `PENDING_VERIFICATION`, `SUSPENDED`, `LOCKED`, `FROZEN`
- `failed_login_attempts` (INT) — Consecutive failed authentication attempts counter
- `locked_until` (TIMESTAMP, Nullable) — Timestamp until which login is blocked
- `created_at` (TIMESTAMP) — UTC creation timestamp
- `updated_at` (TIMESTAMP) — UTC last update timestamp

### 2.2 `refresh_tokens`
Stateful tracking for refresh token rotation and reuse detection.
- `id` (BIGINT, PK, Auto Increment)
- `user_id` (BIGINT, Indexed) — Reference to `users.id`
- `token` (VARCHAR(255), Unique, Indexed) — Opaque cryptographic refresh token
- `expiry_date` (TIMESTAMP) — Token expiration timestamp (default 7 days)
- `revoked` (BOOLEAN) — Set to true once rotated or invalidated

### 2.3 `user_profiles`
Customer demographic and KYC identity verification records.
- `id` (BIGINT, PK, Auto Increment)
- `user_id` (BIGINT, Unique, Indexed) — Reference to `users.id`
- `first_name` (VARCHAR(100))
- `last_name` (VARCHAR(100))
- `phone` (VARCHAR(30))
- `address` (VARCHAR(255))
- `date_of_birth` (DATE)
- `kyc_status` (VARCHAR(30)) — `UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED`
- `kyc_document_type` (VARCHAR(50)) — `PAN`, `AADHAAR`, `PASSPORT`, `DRIVING_LICENSE`
- `kyc_document_number` (VARCHAR(100))
- `kyc_verified_at` (TIMESTAMP, Nullable)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2.4 `wallets`
Authoritative digital currency accounts.
- `id` (BIGINT, PK, Auto Increment)
- `user_id` (BIGINT, Indexed) — Owner reference to `users.id`
- `wallet_number` (VARCHAR(36), Unique, Indexed) — Generated external identifier (e.g. `WLT-A1B2C3D4`)
- `currency` (VARCHAR(10)) — Default `INR`, support for `USD`, `EUR`, `GBP`
- `status` (VARCHAR(20)) — `ACTIVE`, `FROZEN`, `CLOSED`
- `balance` (DECIMAL(19,4)) — Materialized balance projection
- `version` (BIGINT) — Optimistic locking revision counter
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2.5 `wallet_limits`
Configurable velocity and risk limits per wallet.
- `id` (BIGINT, PK, Auto Increment)
- `wallet_id` (BIGINT, Unique, Indexed) — Reference to `wallets.id`
- `daily_limit` (DECIMAL(19,4)) — Maximum permissible daily transfer out (default 100,000.0000)
- `per_transaction_limit` (DECIMAL(19,4)) — Maximum per single transfer (default 50,000.0000)
- `daily_used` (DECIMAL(19,4)) — Amount transferred out today
- `last_reset_date` (DATE) — Date when `daily_used` was last reset
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2.6 `wallet_ledger_entries`
Immutable double-entry financial ledger records.
- `id` (BIGINT, PK, Auto Increment)
- `wallet_id` (BIGINT, Indexed) — Reference to `wallets.id`
- `entry_type` (VARCHAR(10)) — `DEBIT` or `CREDIT`
- `amount` (DECIMAL(19,4)) — Value of the entry
- `balance_after` (DECIMAL(19,4)) — Wallet balance immediately following this entry
- `reference_id` (VARCHAR(100), Indexed) — Corresponding transaction or payment reference
- `description` (VARCHAR(255))
- `created_at` (TIMESTAMP)

### 2.7 `transactions`
Lifecycle state for all peer-to-peer transfers, deposits, and reversals.
- `id` (BIGINT, PK, Auto Increment)
- `transaction_ref` (VARCHAR(64), Unique) — Human-readable reference (`TXN-XXXXXXXXXXXX`)
- `idempotency_key` (VARCHAR(128), Unique) — Client-supplied idempotency key
- `source_wallet_id` (BIGINT, Nullable, Indexed) — Originating wallet
- `dest_wallet_id` (BIGINT, Nullable, Indexed) — Destination wallet
- `amount` (DECIMAL(19,4))
- `currency` (VARCHAR(10))
- `status` (VARCHAR(20)) — `PENDING`, `COMPLETED`, `FAILED`, `REVERSED`
- `type` (VARCHAR(20)) — `TRANSFER`, `DEPOSIT`, `WITHDRAWAL`, `PAYMENT`, `REVERSAL`, `REFUND`
- `description` (VARCHAR(255))
- `created_at` (TIMESTAMP, Indexed)
- `updated_at` (TIMESTAMP)
- `completed_at` (TIMESTAMP, Nullable)

### 2.8 `idempotency_records`
Persistent store for request payload hashing and response replay.
- `id` (BIGINT, PK, Auto Increment)
- `idempotency_key` (VARCHAR(128), Unique, Indexed)
- `request_hash` (VARCHAR(64)) — SHA-256 hash of normalized request JSON
- `response_body` (TEXT) — Serialized cached response DTO
- `status` (VARCHAR(30)) — `PROCESSING`, `COMPLETED`, `FAILED`
- `created_at` (TIMESTAMP)
- `expires_at` (TIMESTAMP) — Purge timestamp (24-hour TTL)

### 2.9 `outbox_events`
Transactional outbox pattern table for guaranteed event delivery.
- `id` (BIGINT, PK, Auto Increment)
- `aggregate_type` (VARCHAR(50)) — `TRANSACTION`, `WALLET`, `USER`, `PAYMENT`, `NOTIFICATION`
- `aggregate_id` (VARCHAR(64))
- `event_type` (VARCHAR(50)) — e.g. `TRANSACTION_COMPLETED`
- `payload` (TEXT) — JSON event envelope
- `status` (VARCHAR(20), Indexed) — `PENDING`, `PUBLISHED`, `FAILED`
- `created_at` (TIMESTAMP)
- `sent_at` (TIMESTAMP, Nullable)

### 2.10 `processed_events`
Consumer idempotency tracking to prevent duplicate message side-effects.
- `id` (BIGINT, PK, Auto Increment)
- `event_id` (VARCHAR(64), Unique, Indexed)
- `event_type` (VARCHAR(64))
- `processed_at` (TIMESTAMP)

### 2.11 `payments`
Simulation gateway payments (deposits, payouts, refunds).
- `id` (BIGINT, PK, Auto Increment)
- `payment_ref` (VARCHAR(64), Unique, Indexed) — `PAY-XXXXXXXXXX`, `DEP-XXXXXXXXXX`, `WTH-XXXXXXXXXX`
- `user_id` (BIGINT, Indexed)
- `amount` (DECIMAL(19,4))
- `currency` (VARCHAR(10))
- `status` (VARCHAR(20)) — `INITIATED`, `PROCESSING`, `SUCCESS`, `FAILED`, `REFUNDED`
- `type` (VARCHAR(50)) — `DEPOSIT`, `WITHDRAWAL`, `BILL_PAYMENT`, `REFUND`
- `provider` (VARCHAR(50)) — e.g. `UPI_SIMULATED`, `SIMULATED_IMPS`
- `description` (VARCHAR(255))
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2.12 `payment_webhook_events`
Audit log of received provider webhooks.
- `id` (BIGINT, PK, Auto Increment)
- `webhook_id` (VARCHAR(64), Unique, Indexed)
- `provider` (VARCHAR(50))
- `payload` (TEXT)
- `status` (VARCHAR(30)) — `PENDING`, `PROCESSED`, `FAILED`
- `signature` (VARCHAR(255))
- `created_at` (TIMESTAMP)
- `processed_at` (TIMESTAMP, Nullable)

### 2.13 `beneficiaries`
External bank accounts and internal wallet contacts.
- `id` (BIGINT, PK, Auto Increment)
- `user_id` (BIGINT, Indexed)
- `name` (VARCHAR(100))
- `account_number` (VARCHAR(64))
- `bank_code` (VARCHAR(30))
- `bank_name` (VARCHAR(100))
- `email` (VARCHAR(150))
- `status` (VARCHAR(20)) — `VERIFIED`, `ACTIVE`, `INACTIVE`
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2.14 `notifications`
In-app and simulated queued external notifications.
- `id` (BIGINT, PK, Auto Increment)
- `user_id` (BIGINT, Indexed)
- `type` (VARCHAR(50)) — `TRANSACTION_SUCCESS`, `DEPOSIT_SUCCESS`, `SECURITY_ALERT`
- `channel` (VARCHAR(20)) — `IN_APP`, `EMAIL`, `SMS`
- `subject` (VARCHAR(200))
- `body` (TEXT)
- `status` (VARCHAR(20)) — `SENT`, `READ`, `FAILED`
- `retry_count` (INT)
- `created_at` (TIMESTAMP, Indexed)
- `sent_at` (TIMESTAMP, Nullable)

### 2.15 `audit_events`
Immutable compliance and security trail.
- `id` (BIGINT, PK, Auto Increment)
- `event_type` (VARCHAR(64)) — `LOGIN_SUCCESS`, `FAILED_LOGIN`, `USER_REGISTERED`, etc.
- `actor_id` (VARCHAR(64), Indexed)
- `actor_type` (VARCHAR(32)) — `USER`, `ADMIN`, `SYSTEM`
- `resource_type` (VARCHAR(64))
- `resource_id` (VARCHAR(64))
- `action` (VARCHAR(32))
- `details` (TEXT) — Redacted metadata (passwords and tokens omitted)
- `ip_address` (VARCHAR(45))
- `correlation_id` (VARCHAR(64))
- `created_at` (TIMESTAMP, Indexed)
