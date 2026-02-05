# API Documentation

Base URL: `/api` (unless otherwise noted, e.g., `/auth`)

## Table of Contents
1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [User Profiles](#user-profiles)
4. [User Products](#user-products)
5. [Loans](#loans)
6. [Loan Workflow](#loan-workflow)
7. [Branches](#branches)
8. [Products](#products)
9. [Staff Dashboard](#staff-dashboard)
10. [System & Utilities](#system--utilities)
11. [Admin](#admin)

---

## Authentication
Base URL: `/auth`

### Register
**POST** `/auth/register`
Creates a new user account.
- **Request Body:**
  ```json
  {
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securePassword123",
    "role": "USER"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "User registered successfully",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "username": "johndoe",
      "email": "john@example.com",
      "roles": ["USER"]
    }
  }
  ```

### Login
**POST** `/auth/login`
Authenticates a user and returns a JWT token.
- **Request Body:**
  ```json
  {
    "username": "johndoe",
    "password": "securePassword123"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "Login successful",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "username": "johndoe",
      "email": "john@example.com",
      "roles": ["USER"]
    }
  }
  ```

### Google Login
**POST** `/auth/google`
Authenticates using a Google ID token.
- **Request Body:**
  ```json
  {
    "idToken": "eyJhbGciOiJSUzI1NiIsIm..."
  }
  ```

### Logout
**POST** `/auth/logout`
Logs out the user and invalidates the session/token.

### Get Current User
**GET** `/auth/me`
Retrieves details of the currently authenticated user.
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "roles": ["USER"],
      "branch": "Main Branch"
    }
  }
  ```

### Refresh Token
**POST** `/auth/refresh`
Refreshes the access token using a refresh token.
- **Request Body:**
  ```json
  {
    "refreshToken": "d7e8..."
  }
  ```

### Forgot Password
**POST** `/auth/forgot-password`
Initiates password reset process.
- **Request Body:** `{"email": "john@example.com"}`

### Reset Password
**POST** `/auth/reset-password`
Completes password reset.
- **Request Body:**
  ```json
  {
    "token": "reset-token-uuid",
    "newPassword": "newSecurePassword123"
  }
  ```

---

## User Management
Base URL: `/api/users`

### Get All Users
**GET** `/api/users`
Retrieves a list of all users.

### Get User by ID
**GET** `/api/users/{id}`

### Create User
**POST** `/api/users`
- **Request Body:** (User entity)

### Update User
**PUT** `/api/users/{id}`

### Delete User
**DELETE** `/api/users/{id}`

### Admin: List Users
**GET** `/api/users/admin/list`
Paginated list of users for admin view.

### Admin: Create User
**POST** `/api/users/admin/create`
Admin creates a user (sends email).
- **Request Body:** `AdminCreateUserRequest`

### Admin: Update Status
**PATCH** `/api/users/admin/{id}/status`
Enable/Disable user.
- **Request Body:** `{"active": true}`

### Admin: Update Roles
**PUT** `/api/users/admin/{id}/roles`
- **Request Body:** `{"roleNames": ["USER", "ADMIN"]}`

---

## User Profiles
Base URL: `/api/user-profiles`

### Get My Profile
**GET** `/api/user-profiles/me`
Retrieves the profile of the logged-in user.
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "fullName": "John Doe",
      "nik": "1234567890",
      "phoneNumber": "08123456789",
      "address": "123 Street",
      "ktpPath": "http://.../uploads/ktp.jpg"
    }
  }
  ```

### Create/Update Profile
**POST** `/api/user-profiles`
- **Request Body:**
  ```json
  {
    "fullName": "John Doe",
    "nik": "1234567890",
    "phoneNumber": "08123456789",
    "address": "123 Street",
    "dateOfBirth": "1990-01-01"
  }
  ```

### Upload KTP
**POST** `/api/user-profiles/upload-ktp`
Uploads KTP image.
- **Request:** Multipart file (`file`)

---

## User Products
Base URL: `/api/user-products`

### Get My Tier & Limits
**GET** `/api/user-products/my-tier`
Retrieves current tier, credit limit, and progress.
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "tierName": "Silver",
      "maxLoanLimit": 10000000,
      "currentLoanBalance": 2000000,
      "remainingLimit": 8000000
    }
  }
  ```

---

## Loans
Base URL: `/api/loan-applications`

### Create Loan Application
**POST** `/api/loan-applications`
- **Request Body:**
  ```json
  {
    "amount": 5000000,
    "tenureMonths": 12,
    "productId": 1
  }
  ```

### Get My Loan History
**GET** `/api/loan-applications/my-history`

### Get Loan by ID
**GET** `/api/loan-applications/{id}`

---

## Loan Workflow
Base URL: `/api/loan-workflow`

### Submit Loan
**POST** `/api/loan-workflow/submit`
Submits a loan for processing.
- **Request Body:** (latitude/longitude are optional but recommended)
  ```json
  {
    "amount": 5000000,
    "tenureMonths": 12,
    "productId": 1,
    "productId": 1,
    "branchId": 5,
    "latitude": -6.2088,
    "longitude": 106.8456
  }
  ```

### Perform Action (Approve/Reject/etc)
**POST** `/api/loan-workflow/action`
- **Request Body:**
  ```json
  {
    "loanApplicationId": 101,
    "action": "APPROVE",
    "comment": "Documents verified."
  }
  ```

### Get Queues (Staff)
- **GET** `/api/loan-workflow/queue/marketing` (Submitted/In Review)
- **GET** `/api/loan-workflow/queue/branch-manager` (Waiting Approval)
- **GET** `/api/loan-workflow/queue/back-office` (Approved/Waiting Disbursement)

### Get Action History
- **GET** `/api/loan-workflow/history/marketing`
- **GET** `/api/loan-workflow/history/branch-manager`
- **GET** `/api/loan-workflow/history/back-office`

### Get Allowed Actions
**GET** `/api/loan-workflow/{loanId}/allowed-actions`
Returns valid actions for current user/status (e.g., `["APPROVE", "REJECT", "RETURN"]`).

---

## Branches
Base URL: `/api/admin/branches` (Admin) | `/api/branches` (Public)

### Public: Dropdown List
**GET** `/api/branches/dropdown`
- **Response:** `[{"id": 1, "name": "Central Branch"}, ...]`

### Admin: CRUD
- **GET** `/api/admin/branches` (List all)
- **POST** `/api/admin/branches` (Create)
- **PUT** `/api/admin/branches/{id}` (Update)
- **DELETE** `/api/admin/branches/{id}` (Deactivate)

### Branch Users
- **GET** `/api/admin/branches/{id}/users`
- **POST** `/api/admin/branches/{id}/users/{userId}` (Assign)
- **DELETE** `/api/admin/branches/{id}/users/{userId}` (Unassign)

---

## Products
Base URL: `/api/products`

### List Products
**GET** `/api/products`

### List Active Products
**GET** `/api/products/active`

### Create Product
**POST** `/api/products`
- **Request Body:**
  ```json
  {
    "name": "Micro Loan",
    "code": "MICRO001",
    "interestRate": 1.5,
    "maxTenure": 12
  }
  ```

---

## Staff Dashboard
Base URL: `/api/staff`

### Get Dashboard
**GET** `/api/staff/dashboard`
Returns unified dashboard data based on role.
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "username": "marketing_staff",
      "role": "MARKETING",
      "queueName": "Marketing Queue",
      "allowedActions": ["REVIEW", "RECOMMEND"]
    }
  }
  ```

### Get Queue
**GET** `/api/staff/queue`
Returns the work queue for the logged-in staff member.

### Analytics (Admin/Exec)
**GET** `/api/staff/dashboard/analytics`

---

## System & Utilities

### RBAC (Role Based Access Control)
Base URL: `/api/rbac`
- **GET** `/api/rbac/roles` (List roles with summary)
- **GET** `/api/rbac/roles/{id}/access` (Get specific access)
- **PUT** `/api/rbac/roles/{id}/access` (Update access)

### Menus
Base URL: `/api/menus`
- **GET** `/api/menus` (List all menus)

### File Access
**GET** `/uploads/{fileName}`
Download/View uploaded files (e.g., KTP images).

### Push Notifications (FCM)
Base URL: `/api/fcm`
- **POST** `/api/fcm/register`: Register device token.
  - Body: `fcmToken`, `deviceName`
- **POST** `/api/fcm/send`: Send notification (Admin).
- **POST** `/api/fcm/test`: Send test notification to self.

### Notifications (Internal)
Base URL: `/api/notifications`
- **GET** `/api/notifications`: List all.
- **GET** `/api/notifications/user/{userId}/unread`: Get unread.
- **PATCH** `/api/notifications/{id}/read`: Mark as read.

### CSRF
**GET** `/api/csrf-token`
Get CSRF token for state-changing requests in Single Page Apps.

---

## Admin
Base URL: `/api/admin`

> [!NOTE]
> All admin endpoints require `ADMIN_MODULE` menu permission.

### Dashboard
**GET** `/api/admin/dashboard`
Returns admin dashboard welcome message.

### System Logs
**GET** `/api/admin/system-logs`
Returns system logs placeholder.

### Get All Loan Applications
**GET** `/api/admin/loan-applications`
Retrieves all loan applications with profile information and current bucket status.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 20 | Number of items per page |

**Response:**
```json
{
  "success": true,
  "message": "Loan applications retrieved successfully",
  "data": [
    {
      "loanApplicationId": 1,
      "amount": 5000000.0,
      "tenureMonths": 12,
      "interestRateApplied": 12.0,
      "totalAmountToPay": 5661360.0,
      "productName": "Bronze Tier",
      "createdAt": "2026-02-01T10:30:00",
      "updatedAt": "2026-02-02T14:20:00",
      "userId": 5,
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "profileId": 5,
      "nik": "1234567890123456",
      "phoneNumber": "081234567890",
      "bankName": "BCA",
      "accountNumber": "1234567890",
      "currentStatus": "IN_REVIEW",
      "displayStatus": "In Review",
      "currentBucket": "MARKETING",
      "branchId": 1,
      "branchName": "Jakarta Branch",
      "latitude": -6.2088,
      "longitude": 106.8456
    }
  ],
  "statusCode": 200,
  "timestamp": "2026-02-05T10:14:56"
}
```

**Bucket Values:**
| Bucket | Status Values |
|--------|---------------|
| `MARKETING` | SUBMITTED, IN_REVIEW |
| `BRANCH_MANAGER` | WAITING_APPROVAL |
| `BACK_OFFICE` | APPROVED_WAITING_DISBURSEMENT |
| `COMPLETED` | DISBURSED, PAID, REJECTED |
