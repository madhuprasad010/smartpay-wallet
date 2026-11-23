# SmartPay — Security Architecture & Policy

## 1. Authentication & Identity
- **BCrypt Password Hashing**: Passwords hashed with BCrypt (strength 10). Passwords are never logged or returned via API.
- **Stateless JWT**: Short-lived access tokens (15 minutes expiration). Signed with HMAC-SHA256 (`Keys.hmacShaKeyFor`).
- **Refresh Token Rotation**: Refresh tokens are single-use. Generating a new access token rotates the refresh token and invalidates the previous token.
- **Refresh Token Reuse Detection**: If an invalidated refresh token is presented, all active sessions for that user account are immediately revoked.
- **Brute Force Protection**: 5 consecutive failed authentication attempts automatically lock the user account for 15 minutes.

---

## 2. Authorization & RBAC
Roles:
- `CUSTOMER`: Default role. Full access to own wallets, transfers, deposits, withdrawals, beneficiaries, and profile.
- `ADMIN`: Access to user management, ledger reconciliation, transaction reversals, and system audit logs.
- `SUPPORT`: Read-only access to customer status and transactions for dispute resolution.
- `SYSTEM`: Machine-to-machine internal asynchronous process authority.

Access is evaluated in Spring Security via both `SecurityConfig` URL pattern filters and method-level `@PreAuthorize` annotations.

---

## 3. Financial Integrity & Idempotency
- **Idempotency Keys**: All financial operations (`/transactions/transfer`, `/payments/deposit`, `/payments/withdrawal`) require an `idempotencyKey`.
- **Payload Hash Verification**: A SHA-256 hash of the normalized request JSON is stored in `idempotency_records`. Reusing a key with a modified payload returns `409 Conflict`. Reusing with identical payload returns the cached result without executing double deductions.
- **Deterministic Pessimistic Locking**: Concurrent transfers lock database rows in ascending order of wallet IDs (`Math.min(id1, id2)` then `Math.max(id1, id2)`), completely eliminating deadlock vulnerabilities.
- **Ledger Immutability**: Historical ledger entries are never modified or deleted. Errors are corrected exclusively through compensating ledger entries.

---

## 4. Transport & Network Security
- **CORS Policy**: Restricts allowed origins to localhost and authorized deployment domains (`https://*.onrender.com`).
- **TLS/HTTPS**: All production traffic on Render runs under automatic TLS 1.3 encryption.
- **Redis & Database TLS**: TiDB connections require `sslMode=VERIFY_IDENTITY` and Upstash requires `rediss://` scheme.

---

## 5. Audit & Redaction Policy
- Audit events record: Event Type, Actor ID, Resource Type, Action, Details, Client IP, and Correlation ID.
- Strict redaction ensures passwords, tokens, full PAN/card numbers, and secret keys never appear in log output or audit records.
