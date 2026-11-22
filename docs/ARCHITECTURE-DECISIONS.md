# Architecture Decision Records (ADRs)

## ADR-001: Microservices Architecture with Domain Isolation
- **Status:** Accepted
- **Context:** SmartPay needs to support independent scale, isolated financial domains, and high availability.
- **Decision:** Decompose the application into 9 independent Spring Boot services behind a Reactive Spring Cloud Gateway.
- **Consequences:** Increases deployment complexity, but delivers clear domain separation, fault isolation, and independent deployability.

## ADR-002: Database-Per-Service Pattern
- **Status:** Accepted
- **Context:** Monolithic shared databases create tight coupling and allow non-compliant direct queries across domains.
- **Decision:** Every microservice owns a dedicated logical MySQL schema. Cross-service database access is prohibited.
- **Consequences:** Requires event-driven data propagation and outbox patterns; prevents accidental transactional coupling.

## ADR-003: Apache Kafka in KRaft Mode for Business Domain Events
- **Status:** Accepted
- **Context:** Need reliable event streaming without ZooKeeper operational overhead.
- **Decision:** Deploy Apache Kafka 7.9 using KRaft quorum mode.
- **Consequences:** Eliminates ZooKeeper maintenance while providing high-throughput event streaming for domain events.

## ADR-004: RabbitMQ for Background Asynchronous Tasks
- **Status:** Accepted
- **Context:** Notification delivery and document processing require point-to-point queues, worker competing consumers, and dead-letter exchanges.
- **Decision:** Use RabbitMQ for background tasks and notifications, keeping Kafka for domain event streaming.
- **Consequences:** Clear separation between domain event streams and task queues.

## ADR-005: Double-Entry Bookkeeping for Financial Ledger
- **Status:** Accepted
- **Context:** Digital banking requires verifiable financial balance calculation that cannot be corrupted by race conditions or silent update bugs.
- **Decision:** Model balance movements as immutable credit/debit ledger entries (`wallet_ledger_entries`). Current wallet balance is an atomic projection protected by database row locks (`SELECT FOR UPDATE`).
- **Consequences:** Eliminates arbitrary balance overwrites; provides complete audit trail of every cent moved.

## ADR-006: Transactional Outbox Pattern
- **Status:** Accepted
- **Context:** Dual-write problems occur when writing to a database and publishing to Kafka separately.
- **Decision:** Write outbox events in the same database transaction as the business entity, then publish to Kafka.
- **Consequences:** Guarantees at-least-once event delivery across service boundaries.
