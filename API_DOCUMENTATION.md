# SmartPay — REST API Documentation (v1)

Base URL: `/api/v1`
Interactive Swagger UI: `http://localhost:8080/swagger-ui.html` or `https://<backend-url>/swagger-ui.html`
OpenAPI JSON: `/v3/api-docs`

---

## 1. Authentication & Security

### 1.1 Register Customer
`POST /api/v1/auth/register`
```json
{
  "email": "customer@example.com",
  "password": "SecurePassword123!",
  "firstName": "Rahul",
  "lastName": "Sharma"
}
```
**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "7c5e2d...",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "email": "customer@example.com",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    }
  }
}
```

### 1.2 Login
`POST /api/v1/auth/login`
```json
{
  "email": "customer@example.com",
  "password": "SecurePassword123!"
}
```

### 1.3 Refresh Token Rotation
`POST /api/v1/auth/refresh`
```json
{
  "refreshToken": "7c5e2d..."
}
```

### 1.4 Logout
`POST /api/v1/auth/logout`
```json
{
  "refreshToken": "7c5e2d..."
}
```

### 1.5 Change Password
`POST /api/v1/auth/change-password?userId=1`
```json
{
  "currentPassword": "SecurePassword123!",
  "newPassword": "NewSecurePassword456!"
}
```

---

## 2. Wallets & Double-Entry Ledger

### 2.1 Get User Wallets
`GET /api/v1/wallets/user/{userId}`
Header: `Authorization: Bearer <accessToken>`

### 2.2 Create Digital Wallet
`POST /api/v1/wallets`
```json
{
  "userId": 1,
  "currency": "INR"
}
```

### 2.3 Get Wallet Ledger Entries
`GET /api/v1/wallets/{id}/ledger`

### 2.4 Freeze / Unfreeze Wallet
`POST /api/v1/wallets/{id}/freeze`
`POST /api/v1/wallets/{id}/unfreeze`

### 2.5 Get & Update Wallet Limits
`GET /api/v1/wallets/{id}/limits`
`PUT /api/v1/wallets/{id}/limits`
```json
{
  "dailyLimit": 150000.00,
  "perTransactionLimit": 75000.00
}
```

### 2.6 Balance Reconciliation
`GET /api/v1/wallets/{id}/reconcile`
`GET /api/v1/wallets/reconcile-all` (Admin)

---

## 3. Transactions & Transfers

### 3.1 Execute Peer-to-Peer Transfer
`POST /api/v1/transactions/transfer`
```json
{
  "idempotencyKey": "IDEMP-9b2f-410a-8bf8",
  "sourceWalletId": 1,
  "destWalletId": 2,
  "amount": 500.00,
  "currency": "INR",
  "description": "Monthly maintenance share"
}
```

### 3.2 Reverse Transaction (Compensating Ledger Entries)
`POST /api/v1/transactions/{ref}/reverse`
```json
{
  "reason": "Administrative reversal for disputed transfer"
}
```

### 3.3 Query Transaction History
`GET /api/v1/transactions?walletId=1&page=0&size=20`
`GET /api/v1/transactions/ref/{ref}`

---

## 4. Payment Simulation Adapter

### 4.1 Simulate Deposit
`POST /api/v1/payments/deposit`
```json
{
  "walletId": 1,
  "amount": 2500.00,
  "currency": "INR",
  "paymentMethod": "UPI_SIMULATED",
  "idempotencyKey": "IDEMP-DEP-112233",
  "description": "Top-up via UPI"
}
```

### 4.2 Simulate Withdrawal (IMPS / NEFT)
`POST /api/v1/payments/withdrawal`
```json
{
  "walletId": 1,
  "amount": 1000.00,
  "currency": "INR",
  "bankAccountNumber": "987654321098",
  "ifscCode": "HDFC0001234",
  "idempotencyKey": "IDEMP-WTH-445566",
  "description": "Payout to savings account"
}
```

### 4.3 Refund Simulated Payment
`POST /api/v1/payments/{id}/refund`
```json
{
  "reason": "Merchant returned goods"
}
```

---

## 5. Beneficiaries

### 5.1 Add Beneficiary
`POST /api/v1/beneficiaries`
```json
{
  "userId": 1,
  "name": "Priya Sharma",
  "accountNumber": "987654321098",
  "bankCode": "SBIN0001234",
  "bankName": "State Bank of India",
  "email": "priya@example.com"
}
```

### 5.2 Transfer to Beneficiary
`POST /api/v1/beneficiaries/{id}/transfer`
```json
{
  "sourceWalletId": 1,
  "amount": 1500.00,
  "currency": "INR",
  "idempotencyKey": "IDEMP-BEN-889900",
  "description": "Direct vendor payout"
}
```

---

## 6. User Management & KYC

### 6.1 Get & Update Profile
`GET /api/v1/users/{userId}/profile`
`PUT /api/v1/users/{userId}/profile`

### 6.2 Submit KYC Document
`POST /api/v1/users/{userId}/kyc`
```json
{
  "documentType": "PAN",
  "documentNumber": "ABCDE1234F"
}
```

### 6.3 Verify KYC (Admin)
`POST /api/v1/users/{userId}/kyc/verify`

### 6.4 Update User Account Status (Admin)
`PATCH /api/v1/users/{userId}/status`
```json
{
  "status": "SUSPENDED"
}
```

---

## 7. Notifications

### 7.1 Get User Notifications
`GET /api/v1/notifications/user/{userId}`
`GET /api/v1/notifications/user/{userId}/unread-count`

### 7.2 Mark as Read
`PATCH /api/v1/notifications/{id}/read`
`POST /api/v1/notifications/user/{userId}/read-all`

---

## 8. Audit Logs

### 8.1 Query System Audit Trail (Admin)
`GET /api/v1/audit?page=0&size=20`
`GET /api/v1/audit/actor/{actorId}`
