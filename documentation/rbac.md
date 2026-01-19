# Role-Based Access Control (RBAC) Documentation

This document describes the RBAC implementation in the Loan Bootstrap application.

## 1. Core Concepts

The system uses a **Dynamic RBAC** model where permissions are tied to **Menus**. Each menu has a URL pattern, and access is enforced based on whether a user's role is mapped to that menu.

- **User**: A system user who can have multiple roles.
- **Role**: A collection of permissions (e.g., ADMIN, MARKETING).
- **Menu/Permission**: A specific functional area or API endpoint pattern (e.g., LOAN_SUBMIT, USER_READ).
- **Access Control Mapping**: The bridge between Roles and Menus.
- **Soft Delete**: All entities (User, Role, Access Mapping) support soft deletion. Deleted records remain in the database with `is_deleted = true` but are ignored by the application's read operations.

## 2. Master Data Configuration

The following menus and roles are initialized by default:

### 👑 Roles
- `ADMIN`: Full system access.
- `MARKETING`: Handles initial loan review and customer interaction.
- `BRANCH_MANAGER`: Handles loan approval and rejection.
- `BACK_OFFICE`: Handles loan disbursement.
- `USER`: Standard customer access.

### 📋 Menus & Permissions

| Menu Code | Description | URL Pattern |
| :--- | :--- | :--- |
| **User Management** | | |
| `USER_READ` | View user list/details | `/api/users/**` |
| `USER_CREATE` | Create new users | `/api/users` |
| `USER_UPDATE` | Update user information | `/api/users/**` |
| `USER_DELETE` | Remove users | `/api/users/**` |
| **Role Management** | | |
| `ROLE_READ` | View roles | `/api/roles/**` |
| `ROLE_ASSIGN` | Map roles to menus | `/api/role-menus/**` |
| `ROLE_MANAGE` | Create/Delete roles | `/api/roles/**` |
| **Product Management** | | |
| `PRODUCT_READ` | View loan products | `/api/products/**` |
| `PRODUCT_MANAGE` | Create/Update products | `/api/products/**` |
| **Loan Workflow** | | |
| `LOAN_CREATE` | Submit loan requests | `/api/loan-workflow/submit` |
| `LOAN_REVIEW` | Review submitted loans | `/api/loan-workflow/queue/marketing` |
| `LOAN_APPROVE` | Approve loans | `/api/loan-workflow/queue/branch-manager` |
| `LOAN_REJECT` | Reject loans | `/api/loan-workflow/action` |
| `LOAN_DISBURSE` | Disburse funds | `/api/loan-workflow/queue/back-office` |
| **Others** | | |
| `PROFILE_READ` | View personal profile | `/api/user-profiles/**` |
| `PROFILE_UPDATE` | Update personal profile | `/api/user-profiles/**` |
| `BRANCH_READ` | Branch reports | `/api/loan-workflow/queue/branch-manager` |

## 3. Enforcement Logic

Access is enforced via the `DynamicAuthorizationManager`. 

1. **Request Interception**: Every request to `/api/**` is intercepted.
2. **Identity Check**: The system identifies the user from the JWT token.
3. **Role-Menu Lookup**: It finds all menus associated with the user's roles.
4. **Pattern Matching**: Using `AntPathMatcher`, it checks if the current request URL matches any of the patterns defined in the user's allowed menus.
5. **Decision**: If a match is found, access is granted. If no patterns match, it defaults to requiring authentication.

## 4. API Reference (Master Data)

### User Management
- `GET /api/users`: List all users (ADMIN).
- `POST /api/users`: Create user (ADMIN).
- `PUT /api/users/{id}`: Update user (ADMIN).
- `DELETE /api/users/{id}`: Delete user (ADMIN).

### Role Management
- `GET /api/roles`: List all roles (ADMIN).
- `POST /api/roles`: Create role (ADMIN).
- `DELETE /api/roles/{id}`: Delete role (ADMIN).

### Access Mapping
- `POST /api/role-menus?roleId=X&menuId=Y`: Assign menu to role.
- `DELETE /api/role-menus?roleId=X&menuId=Y`: Remove menu from role.

## 5. Postman Testing Guide

This section explains how to verify the RBAC and Soft Delete functionalities.

### A. Authentication (Get Token)
**Endpoint:** `POST /auth/login`
**Body:**
```json
{
    "usernameOrEmail": "admin",
    "password": "admin123"
}
```
> [!TIP]
> Use `admin`, `manager`, or `user` to test different permission levels. Copy the `token` and use it as a **Bearer Token** in the Authorization tab for all other requests.

### B. User Management (ADMIN)
1. **List Users:** `GET /api/users` (Verifies `USER_READ`)
2. **Create User:** `POST /api/users` (Verifies `USER_CREATE`)
3. **Soft Delete User:** `DELETE /api/users/{id}` (Verifies `USER_DELETE`)
   - **Verification:** After deletion, the user should no longer appear in `GET /api/users`.
   - **DB Check:** `SELECT * FROM users WHERE id = {id}` should show `is_deleted = 1` and `is_active = 0`.

### C. Role & Permission Management (ADMIN)
1. **List Roles:** `GET /api/roles` (Verifies `ROLE_READ`)
2. **Assign Menu to Role:** 
   - **Endpoint:** `POST /api/role-menus?roleId=X&menuId=Y` (Verifies `ROLE_ASSIGN`)
3. **Verify Access Update:**
   - Log in with a user who has Role X.
   - Attempt to access the endpoint defined in Menu Y.

### D. Loan Workflow (Role-Based)
1. **Submit Loan (USER):** `POST /api/loan-workflow/submit`
2. **Check Queue (BRANCH_MANAGER):** `GET /api/loan-workflow/queue/branch-manager`
3. **Perform Action (REJECT):**
   - **Endpoint:** `POST /api/loan-workflow/action`
   - **Body:** `{"loanId": 1, "action": "REJECT", "comments": "Rejected by manager"}`
   - **Permission Test:** Try this with a standard `USER` token; it should return `403 Forbidden`.

### E. Soft Delete Verification
1. **Delete an item** (Role, User, or Product).
2. **Immediate Check:** `GET /api/{module}` should not return the deleted item.
3. **Database Check:** Verify `is_deleted` column is `1`.

---
*Documentation generated on 2025-12-29*
