-- ============================================================
-- SmartPay Enterprise Features (Flyway V2 Migration)
-- Supports Wallet Limits, Idempotency Records, Processed Events,
-- Payment Webhooks, Account Protection, and KYC Workflow
-- Compatible with MySQL 8.0+, TiDB Cloud Starter, H2 (MySQL Mode)
-- ============================================================

-- 1. Wallet Limits
CREATE TABLE IF NOT EXISTS wallet_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL UNIQUE,
    daily_limit DECIMAL(19,4) NOT NULL DEFAULT 100000.0000,
    per_transaction_limit DECIMAL(19,4) NOT NULL DEFAULT 50000.0000,
    daily_used DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    last_reset_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_wallet_limits_wallet (wallet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Idempotency Records
CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_idemp_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Processed Events (Consumer Idempotency)
CREATE TABLE IF NOT EXISTS processed_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    event_type VARCHAR(64) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_processed_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Payment Webhook Events
CREATE TABLE IF NOT EXISTS payment_webhook_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    webhook_id VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    signature VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    INDEX idx_webhook_id (webhook_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. User Account Protection Columns
ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP NULL;

-- 6. User Profile KYC Columns
ALTER TABLE user_profiles ADD COLUMN kyc_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED';
ALTER TABLE user_profiles ADD COLUMN kyc_document_type VARCHAR(50) NULL;
ALTER TABLE user_profiles ADD COLUMN kyc_document_number VARCHAR(100) NULL;
ALTER TABLE user_profiles ADD COLUMN kyc_verified_at TIMESTAMP NULL;
