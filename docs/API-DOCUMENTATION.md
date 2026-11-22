# SmartPay API Documentation

All client traffic is routed through the **API Gateway** on port `8080`.

**Base URL:** `http://localhost:8080/api/v1`

---

## Standard Response Format

All successful responses adhere to the standard envelope:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-11-23T01:00:00Z"
}
```

Standard error response:
```json
{
  "timestamp": "2026-11-23T01:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: {amount=Amount must be at least 0.01}",
  "path": "/api/v1/transactions/transfer",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## Endpoints Summary

### Authentication (`/api/v1/auth`)
- `POST /auth/register` — Register new user account.
- `POST /auth/login` — Authenticate and obtain JWT access & refresh tokens.
- `POST /auth/refresh` — Rotate refresh token and acquire fresh access token.

### Digital Wallets (`/api/v1/wallets`)
- `POST /wallets` — Create a multicurrency wallet.
- `GET /wallets/user/{userId}` — Retrieve all wallets for a user.
- `GET /wallets/{id}` — Retrieve detailed wallet balance and status.
- `GET /wallets/{id}/ledger` — Retrieve immutable ledger entries for audit.

### Transactions (`/api/v1/transactions`)
- `POST /transactions/transfer` — Execute money transfer with idempotency key.
- `GET /transactions` — Paginated list of transactions.
- `GET /transactions/{id}` — Retrieve transaction details by ID.
- `GET /transactions/ref/{ref}` — Retrieve transaction by reference number.

### Payments (`/api/v1/payments`)
- `POST /payments` — Initiate a bill or merchant payment.
- `GET /payments/user/{userId}` — Retrieve payments initiated by user.

### Beneficiaries (`/api/v1/beneficiaries`)
- `POST /beneficiaries` — Save a new payee beneficiary.
- `GET /beneficiaries/user/{userId}` — List all beneficiaries for user.
- `DELETE /beneficiaries/{id}` — Delete a saved beneficiary.

### Notifications (`/api/v1/notifications`)
- `POST /notifications` — Queue an outbound notification.
- `GET /notifications/user/{userId}` — Get in-app notification list.
- `PUT /notifications/{id}/read` — Mark notification as read.

### Audit (`/api/v1/audit`)
- `POST /audit` — Record an immutable audit log entry.
- `GET /audit` — Query audit logs with pagination.
