CREATE TABLE IF NOT EXISTS audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    actor_id VARCHAR(64),
    actor_type VARCHAR(32),
    resource_type VARCHAR(64),
    resource_id VARCHAR(64),
    action VARCHAR(32) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    correlation_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_actor_id (actor_id),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
