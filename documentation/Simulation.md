# Loan Onboarding Simulation Guide (Postman)

This guide provides a step-by-step simulation of the **Onboarding Customer Online** flow.

## Prerequisites
- **Base URL**: `http://localhost:8081`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{token}}` (Required for all steps except Register/Login)

---

## Phase 1: Customer (Android/User)
### 1. Register
**Endpoint**: `POST /auth/register`

**Payload**:
```json
{
  "username": "customer01",
  "password": "password123",
  "email": "customer01@example.com",
  "fullName": "Customer One"
}
```

**Expected Response (201 Created)**:
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbG...",
    "username": "customer01",
    "roles": ["USER"]
  }
}
```

### 2. Login
**Endpoint**: `POST /auth/login`

**Payload**:
```json
{
  "username": "customer01",
  "password": "password123"
}
```

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "token": "eyJhbG...",
    "username": "customer01",
    "roles": ["USER"]
  }
}
```
> [!TIP]
> Copy the `token` from the response and set it as a Postman variable `{{token}}`.

### 3. Lihat Plafond (Get Active Products)
**Endpoint**: `GET /api/products/active`

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Active products retrieved successfully",
  "data": [
    {
      "id": 1,
      "code": "LOAN-PERSONAL-001",
      "name": "Personal Loan - Standard",
      "interestRate": 8.5,
      "minAmount": 5000000.0,
      "maxAmount": 50000000.0,
      "minTenureMonths": 6,
      "maxTenureMonths": 36
    }
  ]
}
```

### 4. Ajukan Pinjaman (Submit Loan)
**Endpoint**: `POST /api/loan-workflow/submit`

**Payload**:
```json
{
  "productId": 1,
  "amount": 10000000.0,
  "tenureMonths": 12,
  "interestRateApplied": 8.5
}
```

**Expected Response (201 Created)**:
```json
{
  "status": "SUCCESS",
  "message": "Loan application submitted successfully",
  "data": {
    "loanApplicationId": 123,
    "userId": 1,
    "productId": 1,
    "amount": 10000000.0,
    "tenureMonths": 12,
    "interestRateApplied": 8.5,
    "currentStatus": "SUBMITTED",
    "createdAt": "2025-12-29T...",
    "updatedAt": "2025-12-29T..."
  }
}
```

---

## Phase 2: Marketing (Web)
### 1. Login as Marketing
**Payload**: `{"username": "marketing", "password": "pass123"}`
**Response**: Get new `{{token}}`.

### 2. Review Pinjaman (Get Queue)
**Endpoint**: `GET /api/loan-workflow/queue/marketing`

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Marketing queue retrieved successfully",
  "data": [
    {
      "loanApplicationId": 123,
      "userId": 1,
      "username": "customer01",
      "userEmail": "customer01@example.com",
      "productId": 1,
      "productName": "Personal Loan - Standard",
      "amount": 10000000.0,
      "tenureMonths": 12,
      "interestRateApplied": 8.5,
      "currentStatus": "SUBMITTED",
      "createdAt": "2025-12-29T...",
      "updatedAt": "2025-12-29T...",
      "allowedActions": ["COMMENT", "FORWARD_TO_MANAGER"]
    }
  ]
}
```

### 3. Action: Comment & Forward
**Endpoint**: `POST /api/loan-workflow/action`

**Payload (Forward to Manager)**:
```json
{
  "loanId": 123,
  "action": "FORWARD_TO_MANAGER",
  "comment": "Data verified, looking good."
}
```

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Action performed successfully",
  "data": {
    "loanApplicationId": 123,
    "currentStatus": "WAITING_APPROVAL"
  }
}
```

---

## Phase 3: Branch Manager (Web)
### 1. Login as Manager
**Payload**: `{"username": "manager", "password": "pass123"}`
**Response**: Get new `{{token}}`.

### 2. Approval Pinjaman
**Endpoint**: `POST /api/loan-workflow/action`

**Option A: Approve**
**Payload**:
```json
{
  "loanId": 123,
  "action": "APPROVE",
  "comment": "Approved for disbursement."
}
```

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Action performed successfully",
  "data": {
    "loanApplicationId": 123,
    "currentStatus": "APPROVED_WAITING_DISBURSEMENT"
  }
}
```

**Option B: Reject**
**Payload**:
```json
{
  "loanId": 123,
  "action": "REJECT",
  "comment": "Credit score too low."
}
```

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Action performed successfully",
  "data": {
    "loanApplicationId": 123,
    "currentStatus": "REJECTED"
  }
}
```

---

## Phase 4: Back Office (Web)
### 1. Login as Back Office
**Payload**: `{"username": "backoffice", "password": "pass123"}`
**Response**: Get new `{{token}}`.

### 2. Pencairan Pinjaman (Disburse)
**Endpoint**: `POST /api/loan-workflow/action`

**Payload (Disburse)**:
```json
{
  "loanId": 123,
  "action": "DISBURSE",
  "comment": "Funds transferred."
}
```

**Expected Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "message": "Action performed successfully",
  "data": {
    "loanApplicationId": 123,
    "currentStatus": "DISBURSED"
  }
}
```

---

## Final Verification: Notifications
**Endpoint**: `GET /api/notifications/user/{customerId}`
> Use customer01's user ID (from Phase 1 response) and their login `token` to check notifications.

**Expected Response**:
```json
{
  "status": "SUCCESS",
  "message": "Notifications retrieved successfully",
  "data": [
    {
       "notificationId": 10,
       "userId": 1,
       "relatedLoanApplicationId": 123,
       "notifType": "LOAN_STATUS_UPDATE",
       "channel": "PUSH",
       "message": "Your loan 123 has been DISBURSED",
       "isRead": false,
       "createdAt": "2025-12-29T..."
    }
  ]
}
```
