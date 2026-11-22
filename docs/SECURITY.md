# SmartPay Security Architecture

## 1. Authentication & Token Architecture

```mermaid
sequenceDiagram
    autonumber
    actor Client as User / Frontend
    participant Gateway as API Gateway (:8080)
    participant Auth as Auth Service (:8081)
    participant Service as Downstream Service

    Client->>Gateway: POST /api/v1/auth/login {email, password}
    Gateway->>Auth: Forward Credentials
    Auth->>Auth: Validate password with BCrypt
    Auth-->>Gateway: Return {accessToken (15m), refreshToken (7d)}
    Gateway-->>Client: Return Tokens

    Note over Client,Gateway: Subsequent Requests with JWT
    Client->>Gateway: GET /api/v1/wallets (Bearer JWT)
    Gateway->>Gateway: Validate Correlation ID (attach X-Correlation-ID)
    Gateway->>Service: Forward request with User Identity headers
    Service-->>Gateway: 200 OK Response
    Gateway-->>Client: 200 OK Response
```

## 2. Security Defense-in-Depth

1. **Stateless JWT:** Signed using HMAC-SHA256 (`jjwt` 0.12.6) with short expiration times (15 minutes).
2. **Refresh Token Rotation:** Refresh tokens are single-use. Once redeemed, the existing token is immediately revoked and a new pair is issued.
3. **Password Security:** Hashes are generated via BCrypt with standard cost factor 10. Raw passwords are never persisted or logged.
4. **Correlation & Request Tracing:** The API Gateway inspects and attaches a unique `X-Correlation-ID` (UUID) to every incoming request.
5. **Least-Privilege Database Access:** Service users only have permissions on their respective database. The audit user is granted `SELECT` and `INSERT` only (no `UPDATE` or `DELETE`).
6. **Input Sanitization:** Strict Zod schema validation on the frontend and Spring `@Valid` with JSR-380 annotations on all backend DTOs.
