# API Changes Summary

## Overview

This document summarizes the API response format changes made during the code improvement implementation.

**Key Change:** API endpoints now return **DTOs (Data Transfer Objects)** instead of full Entity objects. This improves security by not exposing sensitive/internal fields.

> 📖 **Looking for removed fields?** See [API_FIELD_ACCESS_GUIDE.md](./API_FIELD_ACCESS_GUIDE.md) for how to access fields that were removed from responses.

---

## Table of Contents

1. [Changed Endpoints](#changed-endpoints)
2. [Unchanged Endpoints](#unchanged-endpoints)
3. [Benefits](#benefits-of-these-changes)
4. [Migration Guide](#migration-guide-for-frontend-developers)
5. [Accessing Removed Fields](#accessing-removed-fields)

---

## Changed Endpoints

### 1. Role Endpoints (`/api/roles`)

#### GET `/api/roles` - List All Roles

**Before (Entity):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "ADMIN",
      "users": [
        {"id": 1, "username": "admin", ...}
      ],
      "isActive": true,
      "deleted": false
    }
  ]
}
```

**After (DTO):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "ADMIN",
      "isActive": true
    }
  ]
}
```

**Fields Removed:**
- `users` (user collection - security risk)
- `deleted` (internal field)

---

#### POST `/api/roles` - Create Role

**Response changed from `Role` to `RoleDTO`**

Same field changes as above.

---

### 2. Product Endpoints (`/api/products`)

#### GET `/api/products` - List All Products

**Before (Entity):** (had many fields including internal ones)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "BRONZE",
      "name": "Bronze Loan",
      "interestRate": 12.0,
      "interestRateType": "FLAT",
      "minAmount": 1000000,
      "maxAmount": 5000000,
      "minTenureMonths": 3,
      "maxTenureMonths": 12,
      "tierOrder": 1,
      "creditLimit": 5000000,
      "upgradeThreshold": 5000000,
      "isActive": true,
      "deleted": false,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ]
}
```

**After (DTO):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "BRONZE",
      "name": "Bronze Loan",
      "minAmount": 1000000,
      "maxAmount": 5000000,
      "minTenureMonths": 3,
      "maxTenureMonths": 12,
      "interestRate": 12.0,
      "creditLimit": 5000000,
      "tierOrder": 1,
      "upgradeThreshold": 5000000,
      "isActive": true
    }
  ]
}
```

**Fields Removed:**
- `interestRateType` (not commonly needed in list view)
- `deleted` (internal field)
- `createdAt` (internal timestamp)
- `updatedAt` (internal timestamp)

---

#### GET `/api/products/active` - List Active Products

Same changes as List All Products.

---

#### GET `/api/products/code/{code}` - Get Product by Code

Same changes as List All Products.

---

#### POST `/api/products` - Create Product

Same response format changes as above.

---

#### PATCH `/api/products/{id}/status` - Update Product Status

Same response format changes as above.

---

### 3. User Endpoints (`/api/users`)

#### GET `/api/users` - List All Users

**Before (Entity):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "password": "$2a$10$...",
      "authProvider": "LOCAL",
      "isActive": true,
      "roles": [
        {"id": 1, "name": "USER", ...}
      ],
      "userProfile": {...},
      "branch": {...},
      "deleted": false,
      "lastPasswordResetDate": "2026-01-01T00:00:00"
    }
  ]
}
```

**After (UserListDTO):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "isActive": true,
      "roles": ["USER", "ADMIN"],
      "branchName": "Main Branch"
    }
  ]
}
```

**Fields Removed:**
- `password` (security risk)
- `authProvider` (internal detail)
- `userProfile` (separate endpoint available)
- `branch` (simplified to `branchName` string)
- `deleted` (internal field)
- `lastPasswordResetDate` (internal field)

**Fields Changed:**
- `roles` - Now array of role name strings instead of role objects

---

#### GET `/api/users/{id}` - Get User by ID

Same changes as List All Users.

---

#### POST `/api/users` - Create User

**Response changed from `User` to `UserListDTO`**

Same field changes as above.

---

#### PUT `/api/users/{id}` - Update User

**Response changed from `User` to `UserListDTO`**

Same field changes as above.

---

### 4. Role-Menu Endpoints (`/api/role-menus`)

#### POST `/api/role-menus` - Assign Menu to Role

**Before (Entity):**
```json
{
  "success": true,
  "data": {
    "roleId": 1,
    "menuId": 5,
    "role": {"id": 1, "name": "ADMIN", ...},
    "menu": {"id": 5, "code": "USER_LIST", ...},
    "isActive": true,
    "deleted": false
  }
}
```

**After (RoleMenuDTO):**
```json
{
  "success": true,
  "data": {
    "roleId": 1,
    "roleName": "ADMIN",
    "menuId": 5,
    "menuName": "User List",
    "menuCode": "USER_LIST",
    "isActive": true
  }
}
```

**Fields Changed:**
- `role` (object) → `roleName` (string)
- `menu` (object) → `menuName`, `menuCode` (strings)

**Fields Removed:**
- `deleted` (internal field)

---

#### GET `/api/role-menus/role/{roleId}` - Get Menus by Role

Same response format changes as above.

---

#### GET `/api/role-menus/menu/{menuId}` - Get Roles by Menu

Same response format changes as above.

---

## Unchanged Endpoints

The following endpoints were **NOT affected** by these changes:

### Authentication (`/auth`)
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/google`
- `POST /auth/logout`
- `GET /auth/me`

### User Profiles (`/api/user-profiles`)
- Already used `UserProfileDTO`

### Loans (`/api/loan-applications`)
- Already used `LoanApplicationDTO`

### Loan Workflow (`/api/loan-workflow`)
- Response formats unchanged

### Branches (`/api/branches`, `/api/admin/branches`)
- Already used `BranchDTO`

### Staff Dashboard (`/api/staff`)
- Already used custom DTOs

### RBAC (`/api/rbac`)
- `GET /api/rbac/roles` - Returns `RoleAccessSummaryDTO` (unchanged)
- `GET /api/rbac/roles/{id}/access` - Returns `RoleAccessDTO` (unchanged)

### Menus (`/api/menus`)
- Already used `MenuDTO`

---

## Benefits of These Changes

1. **Security Improvement**
   - Passwords no longer exposed in API responses
   - Internal fields (`deleted`, timestamps) hidden
   - Sensitive relationships not exposed

2. **Reduced Payload Size**
   - Responses are smaller and more focused
   - Less data transferred over network

3. **Better API Design**
   - Clear separation between internal entities and public API
   - Consistent response formats
   - Easier to maintain and version

4. **Frontend Compatibility**
   - Frontend receives only needed data
   - Simpler data structures to work with
   - Role names as strings instead of objects

---

## Migration Guide for Frontend Developers

### Role Responses
```javascript
// Before
const roleName = response.data.roles[0].name;

// After (same - roles are now string array)
const roleName = response.data.roles[0];
```

### Product Responses
```javascript
// Before - might have had extra fields
const product = response.data;

// After - same fields except internal ones removed
// No changes needed if you weren't using deleted/createdAt/updatedAt
```

### User Responses
```javascript
// Before
const userRole = response.data.roles[0].name;
const branchName = response.data.branch?.name;

// After
const userRole = response.data.roles[0]; // Direct string
const branchName = response.data.branchName; // Direct string
```

### Role-Menu Responses
```javascript
// Before
const roleName = response.data.role.name;
const menuCode = response.data.menu.code;

// After
const roleName = response.data.roleName;
const menuCode = response.data.menuCode;
```

---

## Error Response Format

Error responses remain the same structure, only the error messages are more specific:

```json
{
  "success": false,
  "message": "User not found with id: '123'",
  "error": {
    "errorCode": "RESOURCE_NOT_FOUND"
  },
  "statusCode": 404,
  "timestamp": "2026-02-09T10:30:00"
}
```

Instead of generic:
```json
{
  "success": false,
  "message": "User not found with id: 123",
  "statusCode": 500
}
```

---

## Testing Checklist

After deployment, verify these endpoints return the new format:

- [ ] `GET /api/roles` - No `users` array in response
- [ ] `GET /api/products` - No `deleted`, `createdAt`, `updatedAt` fields
- [ ] `GET /api/users` - No `password` field, `roles` is string array
- [ ] `POST /api/role-menus` - Returns simplified object with name strings

---

## Questions?

If you encounter any issues with the new response formats, please check:
1. Your frontend is not accessing removed fields
2. Role arrays are now strings, not objects
3. Branch is now `branchName` string, not `branch` object


---

## Accessing Removed Fields

Several fields were removed from API responses for security and cleanliness reasons. **This does not mean the data is lost** - it just needs to be accessed differently.

### Quick Reference

| Field | Removed From | Alternative Access Method |
|-------|--------------|---------------------------|
| `password` | All endpoints | Cannot access (by design) |
| `userProfile` | User list | `GET /api/user-profiles/{userId}` |
| `lastPasswordResetDate` | All endpoints | Internal use only |
| `authProvider` | User list | `GET /auth/me` |
| `branch` (object) | User list | `GET /api/branches/{id}` |
| `deleted` | All endpoints | Query param `?includeDeleted=true` |
| `createdAt`/`updatedAt` | Products | Admin audit endpoint |

### Detailed Guide

For complete instructions on how to access each removed field, see:

📄 **[API_FIELD_ACCESS_GUIDE.md](./API_FIELD_ACCESS_GUIDE.md)**

This guide includes:
- Alternative endpoints for each removed field
- Common use cases and solutions
- Code examples for frontend developers
- How to request new endpoints if needed

### Common Questions

**Q: How do I get the user's profile information?**  
A: Use `GET /api/user-profiles/{userId}` or `GET /api/user-profiles/me` for current user.

**Q: How do I check if a user has a complete profile?**  
A: Use `GET /api/user-profiles/{userId}/complete` which returns a boolean.

**Q: How do I see which branch a user belongs to?**  
A: The `branchName` field is included in user responses. For full branch details, use `GET /api/branches/{id}`.

**Q: Why can't I see the password or password reset date?**  
A: These are security-sensitive fields intentionally restricted. Passwords are never exposed in any API response.

