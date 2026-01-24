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
  "productId": 1,
  "amount": 5000000.0,
  "tenureMonths": 12,
  "interestRateApplied": 12.0
}
```

> [!IMPORTANT]
> **Security Update:** The `userId` field has been **removed** from the request body. The user ID is now automatically extracted from the JWT token to prevent IDOR vulnerabilities.

> [!NOTE]
> **Automatic Calculations:**
> - `totalAmountToPay` is automatically calculated using the EMI formula: `EMI = [P × r × (1+r)^n] / [(1+r)^n - 1]`
> - Total repayment amount = EMI × number of months
> - Example: ₹5,000,000 loan at 12% annual interest for 12 months = ₹5,330,000 total to repay

**Validation Rules:**
1. ✅ User must have no active pending loans (SUBMITTED, IN_REVIEW, WAITING_APPROVAL, or APPROVED_WAITING_DISBURSEMENT)
2. ✅ Loan amount must be within user's remaining credit limit
3. ✅ Product must be active and not soft-deleted

**Response Example:**
```json
{
  "success": true,
  "data": {
    "loanApplicationId": 1,
    "userId": 7,
    "productId": 1,
    "amount": 5000000.0,
    "tenureMonths": 12,
    "interestRateApplied": 12.0,
    "totalAmountToPay": 5330000.0,
    "currentStatus": "SUBMITTED",
    "createdAt": "2026-01-05T15:00:00",
    "updatedAt": "2026-01-05T15:00:00"
  }
}
```

**Possible Errors:**
- `IllegalStateException`: "Cannot submit new loan. You already have an active loan application that is being processed..."
- `RuntimeException`: "Loan amount exceeds remaining credit limit..."

**📋 Database Tables to Check:**

| Table | Query | What to Verify |
|-------|-------|----------------|
| `loan_applications` | `SELECT * FROM loan_applications WHERE user_id = 7;` | New loan with `current_status = 'SUBMITTED'` and `total_amount_to_pay` calculated |
| `loan_history` | `SELECT * FROM loan_history WHERE loan_application_id = <loan_id>;` | Initial history record with `action = 'SUBMIT'` |
| `notifications` | `SELECT * FROM notifications WHERE user_id = 7;` | Confirmation notification created |

```sql
-- Quick verification queries
SELECT id, user_id, product_id, amount, tenure_months, 
       interest_rate_applied, total_amount_to_pay, current_status, created_at 
FROM loan_applications 
WHERE user_id = 7 ORDER BY created_at DESC;

SELECT * FROM loan_history 
WHERE loan_application_id = (SELECT MAX(loan_application_id) FROM loan_applications WHERE user_id = 7);
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

1.  **Environment Variables**:
    -   `base_url`: `http://localhost:8081`
    -   `marketing_token`: (auto-set after login)
    -   `manager_token`: (auto-set after login)
    -   `backoffice_token`: (auto-set after login)

2.  **Pre-request Script** (for auto-token refresh):
    ```javascript
    // Add to collection pre-request
    if (pm.environment.get("token_expiry") < Date.now()) {
        // Call login and refresh token
    }
    ```

3.  **Test Scripts** (for automatic validation):
    ```javascript
    pm.test("Status code is 200", function() {
        pm.response.to.have.status(200);
    });
    pm.test("Response is success", function() {
        var jsonData = pm.response.json();
        pm.expect(jsonData.success).to.eql(true);
    });
    ```

---

## 7. New Features (January 2026 Update)

### 7.1 Automatic Total Loan Amount Calculation

The system now automatically calculates the **total repayment amount** (principal + interest) when a loan is submitted.

#### How It Works:
- Uses the **EMI formula**: `EMI = [P × r × (1+r)^n] / [(1+r)^n - 1]`
  - `P` = Principal (loan amount)
  - `r` = Monthly interest rate (annual rate ÷ 12 ÷ 100)
  - `n` = Tenure in months
- **Total Amount to Pay** = EMI × number of months

#### Implementation Details:
| Component | Location | Purpose |
|-----------|----------|---------|
| Database Column | `loan_applications.total_amount_to_pay` | Stores calculated amount |
| DTO Field | `LoanApplicationDTO.totalAmountToPay` | Returns in API response |
| Calculation Method | `LoanWorkflowService.calculateTotalAmountToPay()` | Private helper method |
| Integration | `LoanWorkflowService.submitLoan()` | Auto-calculates on submission |

#### Example Calculation:
```
Loan: ₹5,000,000
Interest: 12% per year
Tenure: 12 months

Monthly Interest Rate: 12% ÷ 12 ÷ 100 = 0.01
EMI = [5000000 × 0.01 × (1+0.01)^12] / [(1+0.01)^12 - 1]
EMI ≈ ₹444,244

Total Amount = 444,244 × 12 = ₹5,330,928
```

### 7.2 Duplicate Loan Prevention

Users are **prevented from submitting multiple simultaneous loan applications**. Only one active loan is allowed at a time.

#### Validation Rules:
Users **CANNOT submit a new loan** if they have an existing loan in any of these statuses:
- ❌ `SUBMITTED`
- ❌ `IN_REVIEW`
- ❌ `WAITING_APPROVAL`
- ❌ `APPROVED_WAITING_DISBURSEMENT`

Users **CAN submit a new loan** if all previous loans are in these final statuses:
- ✅ `DISBURSED` (fully disbursed and active)
- ✅ `PAID` (fully repaid)
- ✅ `REJECTED` (application rejected)

#### Implementation Details:
| Component | Location | Purpose |
|-----------|----------|---------|
| Repository Method | `LoanApplicationRepository.hasActiveLoan()` | Checks for active loans using JPQL |
| Validation | `LoanWorkflowService.submitLoan()` | Validates before creating loan |
| Error Type | `IllegalStateException` | Thrown if validation fails |

#### Error Response Example:
```json
{
  "success": false,
  "message": "Cannot submit new loan. You already have an active loan application that is being processed. Please wait for your current loan to be disbursed, paid, or rejected before submitting a new one."
}
```

#### Testing Duplicate Prevention:
```bash
# Step 1: Submit first loan (should succeed)
POST /api/loan-workflow/submit
Body: { "productId": 1, "amount": 5000000, "tenureMonths": 12 }
# Response: 200 OK, loan created with status SUBMITTED

# Step 2: Try to submit second loan (should fail)
POST /api/loan-workflow/submit
Body: { "productId": 1, "amount": 3000000, "tenureMonths": 6 }
# Response: 400 Bad Request with error message

# Step 3: Move first loan to DISBURSED
POST /api/loan-workflow/action
# ... (complete the workflow to DISBURSED status)

# Step 4: Now you can submit a new loan (should succeed)
POST /api/loan-workflow/submit
Body: { "productId": 1, "amount": 3000000, "tenureMonths": 6 }
# Response: 200 OK, new loan created
```

### 7.3 Security Improvements

#### IDOR Prevention
The `userId` field has been **removed** from the loan submission request body:

**❌ Old (Insecure):**
```json
{
  "userId": 7,
  "productId": 1,
  "amount": 5000000
}
```

**✅ New (Secure):**
```json
{
  "productId": 1,
  "amount": 5000000,
  "tenureMonths": 12
}
```

The user ID is now automatically extracted from the JWT token, preventing **Insecure Direct Object Reference (IDOR)** vulnerabilities where users could submit loans on behalf of other users.

### 7.4 Email Notifications for Loan Disbursement

Users now receive **automated email notifications** when their loan is disbursed, in addition to the existing in-app notifications.

#### Features:
- ✅ **Professional HTML email template** with loan details
- ✅ **Automatic delivery** when loan status changes to `DISBURSED`
- ✅ **Error handling** - email failures do not block the workflow
- ✅ **Detailed logging** for troubleshooting

#### Email Content:
When a loan is disbursed, the user receives an email containing:
- User's name
- Loan ID for reference
- Disbursed amount
- Confirmation message
- Next steps information

#### Implementation Details:

| Component | Location | Purpose |
|-----------|----------|---------|
| Interface | `EmailService.sendLoanDisbursementEmail()` | Method signature |
| Implementation | `EmailServiceImpl.sendLoanDisbursementEmail()` | Sends HTML email using JavaMailSender |
| Integration | `LoanWorkflowService.sendNotifications()` | Triggers email when status changes to DISBURSED |
| Email Configuration | `application.yml` | SMTP settings (see `forgot_password.md`) |

#### Email Template Features:
```
- Responsive design
- Professional styling with gradient header
- Clear loan information display
- Branding consistent with loan platform
- Call-to-action section
```

#### Error Handling:
The email sending is wrapped in a try-catch block to ensure:
- ❌ Email failures **do not prevent** loan disbursement
- ✅ Errors are **logged** for troubleshooting
- ✅ In-app notifications are **always created** regardless of email status

#### Testing Email Notifications:

> [!NOTE]
> Email configuration requires SMTP settings. See `forgot_password.md` for detailed setup instructions using Mailtrap.

**Step 1: Configure Email Settings**
Add these to `application.yml`:
```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: your-mailtrap-username
    password: your-mailtrap-password
```

**Step 2: Test the Flow**
```bash
# 1. Submit a loan
POST /api/loan-workflow/submit
Body: { "productId": 1, "amount": 5000000, "tenureMonths": 12 }

# 2. Progress through workflow to DISBURSED
# (COMMENT → FORWARD_TO_MANAGER → APPROVE → DISBURSE)

# 3. When DISBURSE action is performed:
POST /api/loan-workflow/action
Body: {
  "loanApplicationId": 1,
  "action": "DISBURSE"
}

# 4. Check your email inbox (Mailtrap) for the disbursement notification
```

**Expected Email Response:**
- Subject: `Your Loan Has Been Disbursed - Loan #[ID]`
- To: User's email from JWT token
- Content: Professional HTML email with loan details

**Log Output:**
```
INFO: Disbursement email sent to user@example.com for loan 1
```

**Error Scenario (email service unavailable):**
```
ERROR: Failed to send disbursement email for loan 1: Connection refused
INFO: In-app notification created successfully
```

#### Notification Comparison:

| Notification Type | Always Sent | User Action Required | Persisted |
|------------------|-------------|----------------------|-----------|
| **In-App** | ✅ Yes | View in notifications API | Yes, in database |
| **Email** | ⚠️ Best effort | Check email inbox | No, sent once |

> [!IMPORTANT]
> Both notification types are triggered on disbursement, but only in-app notifications are guaranteed. Email delivery depends on SMTP configuration and network availability.

### 7.5 Role-Based Action Validation (Bucket Ownership)

The system now enforces **strict role-based validation** when performing actions on loans. Each role can **only act on loans that are currently in their bucket (status)**.

#### How It Works:

All workflow actions go through a single endpoint:
```
POST /api/loan-workflow/action
```

Before any action is performed, the system validates:
1. ✅ **Status-Action Validity**: Is this action valid for the current status?
2. ✅ **Role Permission**: Does the user have permission to act on loans in this status?

#### Bucket Ownership Matrix:

| Loan Status | Bucket Owner | Required Permission | Role |
|-------------|--------------|---------------------|------|
| `SUBMITTED` | Marketing | `LOAN_REVIEW` | MARKETING |
| `IN_REVIEW` | Marketing | `LOAN_REVIEW` | MARKETING |
| `WAITING_APPROVAL` | Branch Manager | `LOAN_APPROVE` | BRANCH_MANAGER |
| `APPROVED_WAITING_DISBURSEMENT` | Back Office | `LOAN_DISBURSE` | BACK_OFFICE |
| `DISBURSED`, `REJECTED`, `PAID` | None | N/A | No actions allowed |

#### Implementation Details:

| Component | Location | Purpose |
|-----------|----------|---------|
| Validation Method | `LoanWorkflowService.validateActorPermission()` | Checks user has menu permission for loan status |
| Integration | `LoanWorkflowService.performAction()` | Called before status transition validation |
| Exception | `AccessDeniedException` | Thrown when user lacks permission |

#### Error Responses:

**Marketing trying to act on Branch Manager's bucket:**
```json
{
  "success": false,
  "message": "Only Branch Managers can perform actions on loans awaiting approval"
}
```

**Back Office trying to act on Marketing's bucket:**
```json
{
  "success": false,
  "message": "Only Marketing users can perform actions on loans in SUBMITTED status"
}
```

#### Testing Role Validation:

| Test Case | User | Loan Status | Action | Expected Result |
|-----------|------|-------------|--------|-----------------|
| ✅ Valid Marketing action | MARKETING | `SUBMITTED` | `COMMENT` | 200 OK, status → `IN_REVIEW` |
| ❌ Invalid Marketing action | MARKETING | `WAITING_APPROVAL` | `APPROVE` | 403 AccessDeniedException |
| ✅ Valid Manager action | BRANCH_MANAGER | `WAITING_APPROVAL` | `APPROVE` | 200 OK, status → `APPROVED_WAITING_DISBURSEMENT` |
| ❌ Invalid Manager action | BRANCH_MANAGER | `IN_REVIEW` | `COMMENT` | 403 AccessDeniedException |
| ✅ Valid Back Office action | BACK_OFFICE | `APPROVED_WAITING_DISBURSEMENT` | `DISBURSE` | 200 OK, status → `DISBURSED` |
| ❌ Invalid Back Office action | BACK_OFFICE | `SUBMITTED` | `COMMENT` | 403 AccessDeniedException |

#### Postman Test Example (Negative Test):

```bash
# Step 1: Login as Back Office
POST /auth/login
Body: { "usernameOrEmail": "back_office", "password": "password123" }
# Save the token

# Step 2: Try to comment on a SUBMITTED loan (should fail)
POST /api/loan-workflow/action
Authorization: Bearer <backoffice_token>
Body: {
  "loanApplicationId": 1,
  "action": "COMMENT",
  "comment": "This should not work"
}

# Expected Response: 403 Forbidden
{
  "success": false,
  "message": "Only Marketing users can perform actions on loans in SUBMITTED status"
}
```

> [!NOTE]
> This validation ensures that even if a user has a valid JWT token, they cannot interfere with another role's workflow. The loan must be in the correct status (bucket) for the user's role before any action is allowed.

### 7.6 Action History API

The system provides **role-specific endpoints** to view action history with **pagination and filtering**. Users can filter their action history by month and year.

#### Endpoints:
| Role | Endpoint | Permission |
|------|----------|------------|
| Marketing | `/api/loan-workflow/history/marketing` | `LOAN_REVIEW` |
| Branch Manager | `/api/loan-workflow/history/branch-manager` | `LOAN_APPROVE` |
| Back Office | `/api/loan-workflow/history/back-office` | `LOAN_DISBURSE` |

#### Query Parameters:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `month` | Integer | No | - | Filter by month (1-12) |
| `year` | Integer | No | - | Filter by year (e.g., 2026) |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Items per page |

#### Testing Action History:

**Step 1: Perform some actions**
Make sure you have processed some loans (Submitted, Commented, Approved, Disbursed) so there is history to view.

**Step 2: Fetch Marketing History (Filtered)**
```bash
GET /api/loan-workflow/history/marketing?year=2026&page=0&size=10
Authorization: Bearer <marketing_token>
```

**Step 3: Response Example**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "loanApplicationId": 1,
        "action": "COMMENT_FORWARD",
        "actionDisplayName": "Forwarded with Comment",
        "actionDate": "2026-01-15T10:30:00",
        "productName": "Personal Loan",
        "amount": 5000000.0,
        "username": "john_doe",
        "resultingStatus": "WAITING_APPROVAL",
        "comment": "Docs verified"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

> [!NOTE]
> The history API only returns actions that are relevant to the specific role (e.g., Marketing sees submissions and comments, Branch Manager sees approvals).


