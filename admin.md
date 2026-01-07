# Admin User Management Documentation

## Overview

This document describes the admin-only endpoints for managing users in the system. Only users with the **ADMIN** role can access these endpoints.

---

## Admin Capabilities

| Capability | Description |
|------------|-------------|
| **View All Users** | See complete list of users with username, email, roles, and status |
| **Create Users** | Create new users with specific roles (password set via email) |
| **Deactivate/Activate Users** | Toggle user active status to enable/disable login |
| **Assign Roles** | Update user's roles (e.g., promote to ADMIN, add MARKETING role) |

---

## Security Protections (IDOR Prevention)

The following safeguards are implemented to prevent security issues:

| Protection | Description |
|------------|-------------|
| **Prevent Self-Deactivation** | Admin cannot deactivate their own account |
| **Prevent Self-Role-Removal** | Admin cannot remove ADMIN role from themselves |
| **Protect Last Admin** | System must always have at least one active admin |

### Error Messages

| Scenario | Error Message |
|----------|---------------|
| Trying to deactivate yourself | `Cannot deactivate your own account` |
| Trying to remove ADMIN from yourself | `Cannot remove ADMIN role from your own account` |
| Trying to deactivate last admin | `Cannot deactivate the last active admin. System must have at least one admin.` |
| Trying to remove ADMIN from last admin | `Cannot remove ADMIN role from the last admin. System must have at least one admin.` |

---

## Available Roles

| Role | Description |
|------|-------------|
| `USER` | Standard user (default) |
| `ADMIN` | Full system access |
| `MARKETING` | Marketing department access |
| `BRANCH_MANAGER` | Branch manager access |
| `BACK_OFFICE` | Back office operations access |

---

## API Endpoints

### Authentication Required
All endpoints require a valid JWT token with **ADMIN** role.

**Header:** `Authorization: Bearer <your_token>`

---

### 1. List All Users

```
GET /api/users/admin/list
```

**Response:**
```json
{
  "success": true,
  "message": "Users list fetched successfully for admin",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "isActive": true,
      "roles": ["ADMIN", "USER"]
    }
  ]
}
```

---

### 2. Create User

Creates a new user. Password is NOT set - user receives email to set their own password.

```
POST /api/users/admin/create
```

**Request Body:**
```json
{
  "username": "newemployee",
  "email": "newemployee@example.com",
  "roleNames": ["USER", "MARKETING"]
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "User created successfully. Password reset email sent.",
  "data": {
    "id": 6,
    "username": "newemployee",
    "email": "newemployee@example.com",
    "isActive": true,
    "roles": ["USER", "MARKETING"]
  }
}
```

> **Note:** If `roleNames` is not provided, user defaults to `USER` role.

---

### 3. Update User Status (Activate/Deactivate)

```
PATCH /api/users/admin/{userId}/status
```

**Request Body:**
```json
{
  "isActive": false
}
```

**Response:**
```json
{
  "success": true,
  "message": "User status updated successfully",
  "data": {
    "id": 3,
    "username": "marketing",
    "email": "marketing@example.com",
    "isActive": false,
    "roles": ["MARKETING"]
  }
}
```

**⚠️ Restrictions:**
- Cannot deactivate yourself
- Cannot deactivate the last active admin

---

### 4. Update User Roles

Replaces all existing roles with the new set of roles.

```
PUT /api/users/admin/{userId}/roles
```

**Request Body:**
```json
{
  "roleNames": ["USER", "ADMIN"]
}
```

**Response:**
```json
{
  "success": true,
  "message": "User roles updated successfully",
  "data": {
    "id": 2,
    "username": "john",
    "email": "john@example.com",
    "isActive": true,
    "roles": ["USER", "ADMIN"]
  }
}
```

**⚠️ Restrictions:**
- Cannot remove ADMIN role from yourself
- Cannot remove ADMIN role from the last admin

---

## Error Responses

| Status Code | Scenario |
|-------------|----------|
| `400` | Validation error, IDOR protection triggered |
| `401` | Missing or invalid JWT token |
| `403` | User does not have ADMIN role |
| `404` | User or Role not found |
| `409` | Username or email already exists |

---

## Testing with Postman

### Step 1: Login as Admin
```
POST http://localhost:8081/auth/login
Body: {"usernameOrEmail": "admin", "password": "admin123"}
```

### Step 2: Copy Token
Copy `accessToken` from response.

### Step 3: Set Authorization
- Go to **Authorization** tab
- Select **Bearer Token**
- Paste your token

### Step 4: Test Endpoints
Use the endpoints above with proper request bodies.

### Step 5: Test IDOR Protections
Try:
- Deactivating your own account → Should fail
- Removing ADMIN role from yourself → Should fail
- Deactivating the only admin → Should fail

---

## User Flow: New User Creation

1. Admin creates user via `/api/users/admin/create`
2. System generates random temp password
3. System sends welcome email with password reset link
4. User clicks link and sets their own password
5. User can now login with their credentials
