# Loan Approval Workflow Documentation

This document explains how the Loan Approval Workflow works and how to test it.

## 1. Workflow Overview

The system uses a state machine to manage loan applications. Each status belongs to a specific "Queue" which is accessible only by specific roles.

### Roles & Queues (Managed via RBAC)
The system uses a dynamic Role-Based Access Control (RBAC) system. Each queue is mapped to a specific **Menu Permission**:

*   **MARKETING**: Accesses the **Marketing Queue** via `LOAN_REVIEW` permission (`SUBMITTED`, `IN_REVIEW`).
*   **BRANCH_MANAGER**: Accesses the **Branch Manager Queue** via `LOAN_APPROVE` permission (`WAITING_APPROVAL`).
*   **BACK_OFFICE**: Accesses the **Back Office Queue** via `LOAN_DISBURSE` permission (`APPROVED_WAITING_DISBURSEMENT`).

> [!NOTE]
> Access is controlled by the `RoleMenu` table. If you want a role to have access to a queue, you must link that role to the corresponding Menu in the database.

### Status Transitions
1.  **SUBMITTED**: Initial status after submission.
2.  **IN_REVIEW**: Set after Marketing adds a `COMMENT`.
3.  **WAITING_APPROVAL**: Set after Marketing performs `FORWARD_TO_MANAGER`.
4.  **APPROVED_WAITING_DISBURSEMENT**: Set after Branch Manager performs `APPROVE`.
5.  **DISBURSED**: Final status after Back Office performs `DISBURSE`.
6.  **REJECTED**: Final status if Branch Manager performs `REJECT`.

---

## 2. API Endpoints

### Workflow Actions
*   **Submit Loan**: `POST /api/loan-workflow/submit`
*   **Perform Action**: `POST /api/loan-workflow/action` (Actions: `COMMENT`, `FORWARD_TO_MANAGER`, `APPROVE`, `REJECT`, `DISBURSE`)

### Queues (Role-based)
*   **Marketing Queue**: `GET /api/loan-workflow/queue/marketing`
*   **Branch Manager Queue**: `GET /api/loan-workflow/queue/branch-manager`
*   **Back Office Queue**: `GET /api/loan-workflow/queue/back-office`

### Utilities
*   **Allowed Actions**: `GET /api/loan-workflow/{loanId}/allowed-actions` (Returns what the current user can do with the loan).

---

## 3. How to Test (Step-by-Step)

> [!NOTE]
> Base URL: `http://localhost:8081`
> All endpoints require JWT authentication. Get your token from `POST /auth/login` first.

### Step 1: Submit a Loan

**Postman Setup:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/api/loan-workflow/submit` |
| Headers | `Content-Type: application/json` |
| | `Authorization: Bearer <your_jwt_token>` |

**Body (raw JSON):**
```json
{
  "userId": 7,
  "productId": 1,
  "amount": 10000000.0,
  "tenureMonths": 12,
  "interestRateApplied": 0.1
}
```
*Result:* Status becomes `SUBMITTED`. A history record is created.

**📋 Database Tables to Check:**

| Table | Query | What to Verify |
|-------|-------|----------------|
| `loan_applications` | `SELECT * FROM loan_applications WHERE user_id = 7;` | New loan with `current_status = 'SUBMITTED'` |
| `loan_history` | `SELECT * FROM loan_history WHERE loan_application_id = <loan_id>;` | Initial history record with `action = 'SUBMIT'` |
| `notifications` | `SELECT * FROM notifications WHERE user_id = 7;` | Confirmation notification created |

```sql
-- Quick verification queries
SELECT id, user_id, product_id, amount, current_status, created_at 
FROM loan_applications 
WHERE user_id = 7 ORDER BY created_at DESC;

SELECT * FROM loan_history 
WHERE loan_application_id = (SELECT MAX(id) FROM loan_applications WHERE user_id = 7);
```

---

### Step 2: Marketing Review (Marketing Queue)

**2.1 Login as Marketing:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/auth/login` |
| Body | `{"usernameOrEmail": "marketing", "password": "password123"}` |

**2.2 Get Marketing Queue:**
| Field | Value |
|-------|-------|
| Method | `GET` |
| URL | `http://localhost:8081/api/loan-workflow/queue/marketing` |
| Headers | `Authorization: Bearer <marketing_token>` |

**2.3 Add Comment:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/api/loan-workflow/action` |
| Headers | `Authorization: Bearer <marketing_token>` |

**Body:**
```json
{
  "loanApplicationId": 1,
  "action": "COMMENT",
  "comment": "Documents verified, customer eligible"
}
```
*Result:* Status transitions `SUBMITTED -> IN_REVIEW`. Customer receives notification.

**2.4 Forward to Manager:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/api/loan-workflow/action` |
| Headers | `Authorization: Bearer <marketing_token>` |

**Body:**
```json
{
  "loanApplicationId": 1,
  "action": "FORWARD_TO_MANAGER"
}
```
*Result:* Status transitions `IN_REVIEW -> WAITING_APPROVAL`. Branch Manager(s) receive notifications.

---

### Step 3: Branch Manager Approval (Manager Queue)

**3.1 Login as Branch Manager:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/auth/login` |
| Body | `{"usernameOrEmail": "branch_manager", "password": "password123"}` |

**3.2 Get Manager Queue:**
| Field | Value |
|-------|-------|
| Method | `GET` |
| URL | `http://localhost:8081/api/loan-workflow/queue/branch-manager` |
| Headers | `Authorization: Bearer <manager_token>` |

**3.3 Approve Loan:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/api/loan-workflow/action` |
| Headers | `Authorization: Bearer <manager_token>` |

**Body:**
```json
{
  "loanApplicationId": 1,
  "action": "APPROVE",
  "comment": "Approved for disbursement"
}
```
*Result:* Status transitions `WAITING_APPROVAL -> APPROVED_WAITING_DISBURSEMENT`. Customer and Back Office receive notifications.

---

### Step 4: Back Office Disbursement (Disbursement Queue)

**4.1 Login as Back Office:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/auth/login` |
| Body | `{"usernameOrEmail": "back_office", "password": "password123"}` |

**4.2 Get Back Office Queue:**
| Field | Value |
|-------|-------|
| Method | `GET` |
| URL | `http://localhost:8081/api/loan-workflow/queue/back-office` |
| Headers | `Authorization: Bearer <backoffice_token>` |

**4.3 Disburse Loan:**
| Field | Value |
|-------|-------|
| Method | `POST` |
| URL | `http://localhost:8081/api/loan-workflow/action` |
| Headers | `Authorization: Bearer <backoffice_token>` |

**Body:**
```json
{
  "loanApplicationId": 1,
  "action": "DISBURSE",
  "comment": "Funds transferred to customer account"
}
```
*Result:* Status transitions `APPROVED_WAITING_DISBURSEMENT -> DISBURSED`. Customer receives final notification.

---

## 4. Database Verification

You can verify the data in your SQL database:
*   **`loan_applications`**: Check the `current_status` column.
*   **`loan_history`**: Verify that every action has a corresponding row with `actor_user_id`, `from_status`, `to_status`, and `comment`.
*   **`notifications`**: Check if notifications were generated for the recipient `user_id`.

## 5. Error Cases to Test
*   **Invalid Role**: Try to `APPROVE` as a `MARKETING` user. (Should fail).
*   **Invalid Transition**: Try to `DISBURSE` a loan that is still `IN_REVIEW`. (Should throw `IllegalStateException`).
*   **Missing Comment**: Ensure comments are optional but tracked when provided.

---

## 6. Testing with Latest Project Updates (2025-01-05)

This section covers how to test the loan workflow with all the latest project features.

### 6.1 Authentication Setup

Before testing any workflow, you must authenticate using JWT:

```bash
# 1. Login to get JWT token
POST /auth/login
Content-Type: application/json

{
  "usernameOrEmail": "marketing@example.com",
  "password": "password123"
}

# Response contains JWT token
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}

# 2. Use token in all subsequent requests
Authorization: Bearer <your_jwt_token>
```

### 6.2 RBAC Permission Testing

Test that role-based access is enforced correctly:

| Test Case | User Role | Endpoint | Expected Result |
|-----------|-----------|----------|-----------------|
| Marketing access | MARKETING | `/api/loan-workflow/queue/marketing` | ✅ 200 OK |
| Marketing forbidden | MARKETING | `/api/loan-workflow/queue/branch-manager` | ❌ 403 Forbidden |
| Manager access | BRANCH_MANAGER | `/api/loan-workflow/queue/branch-manager` | ✅ 200 OK |
| Back Office access | BACK_OFFICE | `/api/loan-workflow/queue/back-office` | ✅ 200 OK |

> [!IMPORTANT]
> Ensure the `RoleMenu` table has correct mappings:
> - MARKETING → LOAN_REVIEW
> - BRANCH_MANAGER → LOAN_APPROVE
> - BACK_OFFICE → LOAN_DISBURSE

### 6.3 Soft Delete Verification

When testing with soft-deleted records:

1. **Deleted Users**: Soft-deleted users should NOT appear in workflow queues
2. **Deleted Products**: Loans with soft-deleted products should still be visible but cannot be edited
3. **Query Behavior**: All repository queries automatically filter `is_deleted = false`

```sql
-- Verify soft delete filtering
SELECT * FROM users WHERE is_deleted = 0;
SELECT * FROM roles WHERE is_deleted = 0;
```

### 6.4 Redis Integration Testing

The workflow leverages Redis for:

1. **Token Blacklisting**: Logged-out tokens are blacklisted
   ```bash
   # After logout, trying to use the same token should fail
   POST /auth/logout
   # Then retry any endpoint with same token → 401 Unauthorized
   ```

2. **Session Validation**: Each request validates token against Redis blacklist

3. **Password Reset Impact**: After password reset, all old tokens are invalidated
   ```bash
   # User A logs in on Device 1 and Device 2
   # User A resets password
   # Both Device 1 and Device 2 tokens become invalid
   ```

### 6.5 Complete Workflow Test with Authentication

```bash
# Step 1: Login as Marketing
POST /auth/login
Body: { "usernameOrEmail": "marketing", "password": "password123" }
# Save the token

# Step 2: Submit a loan
POST /api/loan-workflow/submit
Authorization: Bearer <marketing_token>
Body: {
  "userId": 1,
  "productId": 1,
  "amount": 15000000.0,
  "tenureMonths": 24,
  "interestRateApplied": 0.12
}

# Step 3: Add comment and forward
POST /api/loan-workflow/action
Authorization: Bearer <marketing_token>
Body: { "loanId": 1, "action": "COMMENT", "comment": "Documents verified" }

POST /api/loan-workflow/action
Authorization: Bearer <marketing_token>
Body: { "loanId": 1, "action": "FORWARD_TO_MANAGER" }

# Step 4: Login as Branch Manager
POST /auth/login
Body: { "usernameOrEmail": "branch_manager", "password": "password123" }
# Save the new token

# Step 5: Approve the loan
POST /api/loan-workflow/action
Authorization: Bearer <manager_token>
Body: { "loanId": 1, "action": "APPROVE", "comment": "Approved for disbursement" }

# Step 6: Login as Back Office
POST /auth/login
Body: { "usernameOrEmail": "back_office", "password": "password123" }

# Step 7: Disburse
POST /api/loan-workflow/action
Authorization: Bearer <backoffice_token>
Body: { "loanId": 1, "action": "DISBURSE" }
```

### 6.6 Docker Environment Testing

If running in Docker (`docker-compose`), ensure:

1. **Services Running**: App, SQL Server, Redis, Nginx all healthy
2. **Port Mapping**: Access via `http://localhost:8081`
3. **Redis Connection**: Verify Redis is accessible at `redis:6379`

```bash
# Check container status
docker-compose ps

# View application logs
docker-compose logs app

# Test endpoint through Nginx
curl http://localhost:8081/api/loan-workflow/queue/marketing \
  -H "Authorization: Bearer <token>"
```

### 6.7 Troubleshooting Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Invalid/expired token | Re-login to get new token |
| 403 Forbidden | Missing role permission | Check `RoleMenu` mapping in DB |
| 500 Internal Server Error | Database connection | Verify SQL Server is running |
| Empty queue | No loans in correct status | Submit new loan or check `loan_applications` table |
| Redis connection refused | Redis not running | Start Redis: `docker-compose up redis` |

### 6.8 Postman Collection Tips

For efficient testing, set up Postman:

1. **Environment Variables**:
   - `base_url`: `http://localhost:8081`
   - `marketing_token`: (auto-set after login)
   - `manager_token`: (auto-set after login)
   - `backoffice_token`: (auto-set after login)

2. **Pre-request Script** (for auto-token refresh):
   ```javascript
   // Add to collection pre-request
   if (pm.environment.get("token_expiry") < Date.now()) {
       // Call login and refresh token
   }
   ```

3. **Test Scripts** (for automatic validation):
   ```javascript
   pm.test("Status code is 200", function() {
       pm.response.to.have.status(200);
   });
   pm.test("Response is success", function() {
       var jsonData = pm.response.json();
       pm.expect(jsonData.success).to.eql(true);
   });
   ```
