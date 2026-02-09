# API Field Access Guide

## Overview

This guide documents fields that were removed from API responses for security/cleanliness reasons, and explains **how to access them through alternative endpoints** when needed.

---

## Removed Fields & How to Access Them

### 1. User Password

**Why Removed:** Security risk - passwords should never be exposed in API responses.

**How to Verify/Change Password:**

| Operation | Endpoint | Method | Request Body |
|-----------|----------|--------|--------------|
| Verify password | `/auth/login` | POST | `{"username": "john", "password": "current"}` |
| Change password | `/auth/reset-password` | POST | `{"token": "...", "newPassword": "..."}` |
| Admin create user | `/api/users/admin/create` | POST | Creates user without password (email sent) |

**Note:** There is **NO endpoint** to retrieve a user's plain-text password (this is by design for security).

---

### 2. User Profile ID / User Profile Data

**Why Removed:** User profile has its own dedicated endpoint. Including it in user list creates unnecessary payload bloat.

**How to Access:**

#### Get My Profile (Current User)
```http
GET /api/user-profiles/me
```

**Response:**
```json
{
  "success": true,
  "data": {
    "username": "johndoe",
    "email": "john@example.com",
    "address": "123 Main St",
    "nik": "1234567890123456",
    "ktpPath": "/uploads/ktp_123.jpg",
    "phoneNumber": "08123456789",
    "accountNumber": "1234567890",
    "bankName": "BCA",
    "updatedAt": "2026-02-09T10:00:00"
  }
}
```

#### Get Profile by User ID
```http
GET /api/user-profiles/{userId}
```

**Same response format as above.**

#### Check if Profile is Complete
```http
GET /api/user-profiles/{userId}/complete
```

**Response:**
```json
{
  "success": true,
  "data": true
}
```

---

### 3. Last Password Reset Date

**Why Removed:** Internal security detail, not usually needed by frontend.

**How to Access (Admin Only):**

Currently, this field is **not exposed** through any public API endpoint. It is used internally for:
- JWT token validation (tokens issued before password reset are invalidated)
- Security audit logs

**If needed for audit purposes,** it can be accessed through:

#### Option 1: Admin User Detail Endpoint (If Available)
```http
GET /api/admin/users/{userId}/security-info
```

*Note: This endpoint may need to be created if audit access is required.*

#### Option 2: Database Query (Direct Backend Access)
```sql
SELECT last_password_reset_date FROM users WHERE id = ?
```

---

### 4. Auth Provider (LOCAL, GOOGLE, etc.)

**Why Removed:** Internal authentication detail, rarely needed by frontend.

**How to Access:**

#### Through Current User Endpoint
```http
GET /auth/me
```

**Response includes auth provider:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john",
    "email": "john@example.com",
    "roles": ["USER"],
    "authProvider": "LOCAL"
  }
}
```

*Note: The `/auth/me` endpoint returns more detailed user info than the list endpoints.*

---

### 5. Full Branch Object

**Why Removed:** Simplified to `branchName` string to reduce payload size.

**How to Access Full Branch Details:**

#### List All Branches
```http
GET /api/branches
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "MAIN001",
      "name": "Main Branch",
      "address": "123 Main Street, Jakarta",
      "isActive": true
    }
  ]
}
```

#### Get Branch by ID
```http
GET /api/branches/{branchId}
```

#### Admin: Get Users by Branch
```http
GET /api/admin/branches/{branchId}/users
```

---

### 6. Deleted Flag (Soft Delete Status)

**Why Removed:** Internal field - API only returns non-deleted records by default.

**How to Access (Admin Only):**

#### List All Users (Including Deleted) - If Needed
Currently not available through public API. Can be added as:

```http
GET /api/admin/users?includeDeleted=true
```

*Note: This would need to be implemented if required.*

---

### 7. Created At / Updated At Timestamps

**Why Removed:** Internal audit fields, not usually needed by frontend.

**How to Access:**

#### Through Detail Endpoints (If Needed)

For Products, these fields are available in the entity but not exposed in DTO. If audit timestamps are needed, consider creating an admin-specific endpoint:

```http
GET /api/admin/products/{id}/audit
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-02-01T12:00:00",
    "deleted": false
  }
}
```

---

## Quick Reference Table

| Field | Removed From | How to Access | Endpoint |
|-------|--------------|---------------|----------|
| `password` | All user endpoints | Cannot access (by design) | N/A |
| `userProfile` | User list | Use profile endpoint | `GET /api/user-profiles/{userId}` |
| `lastPasswordResetDate` | All endpoints | Admin only (DB query) | Internal use only |
| `authProvider` | User list | Current user endpoint | `GET /auth/me` |
| `branch` (object) | User list | Branch endpoint | `GET /api/branches/{id}` |
| `deleted` | All endpoints | Admin query param | `GET /api/admin/users?includeDeleted=true` |
| `createdAt`/`updatedAt` | Product list | Admin audit endpoint | `GET /api/admin/products/{id}/audit` |
| `roles` (objects) | User list | Role names are in array | Already available as strings |

---

## Common Use Cases & Solutions

### Use Case 1: "I need to check if user has a complete profile"

**Solution:**
```http
GET /api/user-profiles/{userId}/complete
```

Or get full profile:
```http
GET /api/user-profiles/{userId}
```

---

### Use Case 2: "I need to know which branch a user belongs to"

**Solution:**
User list now includes `branchName`:
```json
{
  "branchName": "Main Branch"
}
```

For full branch details:
```http
GET /api/branches/{branchId}
```

*Note: You may need to map branchName to branchId if you need the ID.*

**Alternative:** Add `branchId` to UserListDTO if frequently needed.

---

### Use Case 3: "I need to see when a user's password was last reset"

**Solution:** This is intentionally restricted. Options:

1. **For user themselves:** Not exposed (security)
2. **For admin audit:** Add new endpoint:
   ```http
   GET /api/admin/users/{userId}/security-audit
   ```

---

### Use Case 4: "I need to see all roles with their permissions"

**Solution:**
```http
GET /api/rbac/roles/{roleId}/access
```

**Response:**
```json
{
  "success": true,
  "data": {
    "roleId": 1,
    "roleName": "ADMIN",
    "menuGroups": [
      {
        "category": "User Management",
        "menus": [
          {"id": 1, "code": "USER_LIST", "name": "List Users", "hasAccess": true}
        ]
      }
    ]
  }
}
```

---

## Recommended Frontend Patterns

### Pattern 1: Lazy Loading Profile Data

```javascript
// Step 1: Get user list (lightweight)
const users = await fetch('/api/users').then(r => r.json());

// Step 2: Load profile only when needed (e.g., clicking on user)
async function loadUserDetails(userId) {
  const [user, profile] = await Promise.all([
    fetch(`/api/users/${userId}`).then(r => r.json()),
    fetch(`/api/user-profiles/${userId}`).then(r => r.json())
  ]);
  return { user, profile };
}
```

### Pattern 2: Caching Branch Data

```javascript
// Load all branches once and cache
const branchesCache = await fetch('/api/branches').then(r => r.json());

// Map branchName to full branch object when needed
function getBranchDetails(branchName) {
  return branchesCache.data.find(b => b.name === branchName);
}
```

---

## Adding New Endpoints (If Needed)

If you find that certain removed fields are needed frequently, consider adding dedicated endpoints:

### Example: User Summary with Extended Info

```http
GET /api/users/{id}/summary
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john",
    "email": "john@example.com",
    "isActive": true,
    "roles": ["USER"],
    "branchName": "Main Branch",
    "hasCompleteProfile": true,
    "profileId": 5,
    "lastLoginAt": "2026-02-09T10:00:00"
  }
}
```

This combines data from multiple sources into one call.

---

## Questions?

If you need access to a removed field for a specific use case:

1. Check this guide for the alternative endpoint
2. If no endpoint exists, consider:
   - Adding the field to UserListDTO (if lightweight and commonly needed)
   - Creating a new dedicated endpoint (for complex/audit data)
   - Using the existing detail endpoint (e.g., `/api/user-profiles/me`)
