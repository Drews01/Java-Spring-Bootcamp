# Menu & Role-Based Access Control (RBAC) Documentation

This document describes all menu entries and their role mappings in the application. The RBAC system controls access to API endpoints through the `menus` and `role_menus` tables.

## Overview

The application uses a dynamic authorization system where:
1. **Menus** define access to specific URL patterns
2. **Role-Menu mappings** determine which roles can access which menus
3. **DynamicAuthorizationManager** enforces access at runtime

---

## Roles

| Role | Description |
|------|-------------|
| `ADMIN` | Full system access - can manage users, roles, menus, and all data |
| `USER` | Customer role - can submit loans, view products, manage own profile |
| `MARKETING` | Marketing staff - reviews loan applications, manages marketing queue |
| `BRANCH_MANAGER` | Branch manager - approves loans, manages branch queue |
| `BACK_OFFICE` | Back office staff - handles disbursements, manages back office queue |

---

## Menu Categories

### 1. Admin Module

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `ADMIN_DASHBOARD` | Admin Dashboard | `/api/admin/dashboard` | ADMIN |
| `ADMIN_SYSTEM_LOGS` | Admin System Logs | `/api/admin/system-logs` | ADMIN |

---

### 2. User Management

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `USER_LIST` | List All Users | `/api/users` | ADMIN |
| `USER_GET` | Get User by ID | `/api/users/*` | ADMIN |
| `USER_CREATE` | Create User | `/api/users` | ADMIN |
| `USER_UPDATE` | Update User | `/api/users/*` | ADMIN |
| `USER_DELETE` | Delete User | `/api/users/*` | ADMIN |
| `ADMIN_USER_LIST` | Admin User List | `/api/users/admin/list` | ADMIN |
| `ADMIN_USER_CREATE` | Admin Create User | `/api/users/admin/create` | ADMIN |
| `ADMIN_USER_STATUS` | Admin User Status | `/api/users/admin/*/status` | ADMIN |
| `ADMIN_USER_ROLES` | Admin User Roles | `/api/users/admin/*/roles` | ADMIN |

---

### 3. Role Management

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `ROLE_LIST` | List All Roles | `/api/roles` | ADMIN |
| `ROLE_CREATE` | Create Role | `/api/roles` | ADMIN |
| `ROLE_DELETE` | Delete Role | `/api/roles/*` | ADMIN |

---

### 4. Menu Management

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `MENU_LIST` | List All Menus | `/api/menus` | ADMIN |
| `MENU_GET` | Get Menu by ID | `/api/menus/*` | ADMIN |
| `MENU_CREATE` | Create Menu | `/api/menus` | ADMIN |
| `MENU_UPDATE` | Update Menu | `/api/menus/*` | ADMIN |
| `MENU_DELETE` | Delete Menu | `/api/menus/*` | ADMIN |

---

### 5. Role-Menu Management

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `ROLE_MENU_ASSIGN` | Assign Menu to Role | `/api/role-menus` | ADMIN |
| `ROLE_MENU_BY_ROLE` | Get Menus by Role | `/api/role-menus/role/*` | ADMIN |
| `ROLE_MENU_BY_MENU` | Get Roles by Menu | `/api/role-menus/menu/*` | ADMIN |
| `ROLE_MENU_REMOVE` | Remove Menu from Role | `/api/role-menus` | ADMIN |

---

### 6. Loan Workflow

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `LOAN_SUBMIT` | Submit Loan | `/api/loan-workflow/submit` | USER, ADMIN |
| `LOAN_ACTION` | Perform Loan Action | `/api/loan-workflow/action` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `LOAN_ALLOWED_ACTIONS` | Get Allowed Actions | `/api/loan-workflow/*/allowed-actions` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `LOAN_QUEUE_MARKETING` | Marketing Queue | `/api/loan-workflow/queue/marketing` | MARKETING, ADMIN |
| `LOAN_QUEUE_BRANCH_MANAGER` | Branch Manager Queue | `/api/loan-workflow/queue/branch-manager` | BRANCH_MANAGER, ADMIN |
| `LOAN_QUEUE_BACK_OFFICE` | Back Office Queue | `/api/loan-workflow/queue/back-office` | BACK_OFFICE, ADMIN |

---

### 7. Loan Application

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `LOAN_APP_CREATE` | Create Loan Application | `/api/loan-applications` | ADMIN |
| `LOAN_APP_GET` | Get Loan Application | `/api/loan-applications/*` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `LOAN_APP_BY_USER` | Get Loan Apps by User | `/api/loan-applications/user/*` | ADMIN |
| `LOAN_APP_BY_STATUS` | Get Loan Apps by Status | `/api/loan-applications/status/*` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `LOAN_APP_LIST` | List All Loan Applications | `/api/loan-applications` | ADMIN |
| `LOAN_APP_UPDATE` | Update Loan Application | `/api/loan-applications/*` | ADMIN |
| `LOAN_APP_DELETE` | Delete Loan Application | `/api/loan-applications/*` | ADMIN |

---

### 8. Loan History

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `LOAN_HISTORY_CREATE` | Create Loan History | `/api/loan-history` | ADMIN |
| `LOAN_HISTORY_GET` | Get Loan History | `/api/loan-history/*` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `LOAN_HISTORY_BY_LOAN` | Get History by Loan | `/api/loan-history/loan/*` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `LOAN_HISTORY_LIST` | List All Loan Histories | `/api/loan-history` | ADMIN |
| `LOAN_HISTORY_DELETE` | Delete Loan History | `/api/loan-history/*` | ADMIN |

---

### 9. Product Management

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `PRODUCT_CREATE` | Create Product | `/api/products` | ADMIN |
| `PRODUCT_LIST` | List All Products | `/api/products` | USER, MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `PRODUCT_ACTIVE` | List Active Products | `/api/products/active` | USER, MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `PRODUCT_BY_CODE` | Get Product by Code | `/api/products/code/*` | USER, ADMIN |
| `PRODUCT_UPDATE_STATUS` | Update Product Status | `/api/products/*/status` | ADMIN |
| `PRODUCT_DELETE` | Delete Product | `/api/products/*` | ADMIN |

---

### 10. User Product

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `USER_PRODUCT_CREATE` | Create User Product | `/api/user-products` | ADMIN |
| `USER_PRODUCT_GET` | Get User Product | `/api/user-products/*` | ADMIN |
| `USER_PRODUCT_BY_USER` | Get User Products by User | `/api/user-products/user/*` | USER, ADMIN |
| `USER_PRODUCT_ACTIVE` | Get Active User Products | `/api/user-products/user/*/active` | USER, ADMIN |
| `USER_PRODUCT_LIST` | List All User Products | `/api/user-products` | ADMIN |
| `USER_PRODUCT_UPDATE` | Update User Product | `/api/user-products/*` | ADMIN |
| `USER_PRODUCT_DELETE` | Delete User Product | `/api/user-products/*` | ADMIN |

---

### 11. User Profile

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `PROFILE_CREATE` | Create/Update Profile | `/api/user-profiles` | USER, ADMIN |
| `PROFILE_ME` | Get My Profile | `/api/user-profiles/me` | USER, ADMIN |
| `PROFILE_LIST` | List All Profiles | `/api/user-profiles` | ADMIN |
| `PROFILE_UPDATE` | Update My Profile | `/api/user-profiles` | USER, ADMIN |
| `PROFILE_DELETE` | Delete My Profile | `/api/user-profiles` | USER, ADMIN |

---

### 12. Notification

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `NOTIFICATION_CREATE` | Create Notification | `/api/notifications` | ADMIN |
| `NOTIFICATION_GET` | Get Notification | `/api/notifications/*` | ADMIN |
| `NOTIFICATION_BY_USER` | Get Notifications by User | `/api/notifications/user/*` | USER, ADMIN |
| `NOTIFICATION_UNREAD` | Get Unread Notifications | `/api/notifications/user/*/unread` | USER, ADMIN |
| `NOTIFICATION_UNREAD_COUNT` | Get Unread Count | `/api/notifications/user/*/unread/count` | USER, ADMIN |
| `NOTIFICATION_LIST` | List All Notifications | `/api/notifications` | ADMIN |
| `NOTIFICATION_MARK_READ` | Mark Notification as Read | `/api/notifications/*/read` | USER, ADMIN |
| `NOTIFICATION_DELETE` | Delete Notification | `/api/notifications/*` | ADMIN |

---

### 13. Role-Specific Dashboards

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `MARKETING_DASHBOARD` | Marketing Dashboard | `/api/marketing/dashboard` | MARKETING |
| `MARKETING_STATS` | Marketing Stats | `/api/marketing/stats` | MARKETING |
| `BRANCH_MANAGER_DASHBOARD` | Branch Manager Dashboard | `/api/branch-manager/dashboard` | BRANCH_MANAGER |
| `BRANCH_MANAGER_REPORTS` | Branch Manager Reports | `/api/branch-manager/reports` | BRANCH_MANAGER |
| `BACK_OFFICE_DASHBOARD` | Back Office Dashboard | `/api/back-office/dashboard` | BACK_OFFICE |
| `BACK_OFFICE_DISBURSEMENTS` | Back Office Disbursements | `/api/back-office/disbursements` | BACK_OFFICE |

---

### 14. RBAC Test Endpoints

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `RBAC_TEST_MARKETING` | RBAC Test Marketing | `/api/test-rbac/marketing` | MARKETING, ADMIN |
| `RBAC_TEST_BRANCH_MANAGER` | RBAC Test Branch Manager | `/api/test-rbac/branch-manager` | BRANCH_MANAGER, ADMIN |
| `RBAC_TEST_BACK_OFFICE` | RBAC Test Back Office | `/api/test-rbac/back-office` | BACK_OFFICE, ADMIN |
| `RBAC_TEST_ADMIN` | RBAC Test Admin | `/api/test-rbac/admin-only` | ADMIN |

---

### 15. RBAC Management API

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `RBAC_ROLES_LIST` | List Roles Summary | `/api/rbac/roles` | ADMIN |
| `RBAC_ROLE_ACCESS` | Get/Update Role Access | `/api/rbac/roles/*/access` | ADMIN |
| `RBAC_CATEGORIES` | Get Menu Categories | `/api/rbac/categories` | ADMIN |

---

### 16. Unified Staff Dashboard API

| Menu Code | Name | URL Pattern | Roles |
|-----------|------|-------------|-------|
| `STAFF_DASHBOARD` | Staff Dashboard | `/api/staff/dashboard` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |
| `STAFF_QUEUE` | Staff Queue | `/api/staff/queue` | MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN |

**Purpose:** Unified dashboard endpoints for all workflow staff roles. The frontend displays one common page for Marketing, Branch Manager, and Back Office, with role-specific data returned dynamically.

**Response DTO:**
```java
public record StaffDashboardDTO(
    String username,
    String primaryRole,          // MARKETING | BRANCH_MANAGER | BACK_OFFICE
    List<String> allRoles,
    String queueName,
    List<String> allowedActions, // Role-specific allowed actions
    String welcomeMessage
) {}
```

---

## Public Endpoints (No Authentication Required)

The following endpoints are public and do not require authentication:

| Endpoint | Description |
|----------|-------------|
| `POST /auth/register` | User registration |
| `POST /auth/login` | User login |
| `POST /auth/logout` | User logout |
| `POST /auth/forgot-password` | Initiate password reset |
| `POST /auth/reset-password` | Reset password with token |
| `POST /auth/refresh` | Refresh access token |

---

## Role Access Summary

### USER Role
- Submit loan applications
- View products (active and by code)
- Manage own profile (CRUD)
- View and manage own notifications
- View own user products

### MARKETING Role
- Access marketing queue
- Perform loan actions (REVIEW, RECOMMEND)
- View loan applications and history
- Access marketing dashboard and stats
- View products

### BRANCH_MANAGER Role
- Access branch manager queue
- Perform loan actions (APPROVE, REJECT)
- View loan applications and history
- Access branch manager dashboard and reports
- View products

### BACK_OFFICE Role
- Access back office queue
- Perform loan actions (DISBURSE)
- View loan applications and history
- Access back office dashboard and disbursements
- View products

### ADMIN Role
- **Full access to all endpoints**
- Manage users, roles, and menus
- Manage all loan applications and history
- Manage products and user products
- Access all queues and dashboards
- Manage notifications

---

## Database Tables

### `menus` Table
```sql
CREATE TABLE menus (
    menu_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    url_pattern VARCHAR(255)
);
```

### `role_menus` Table
```sql
CREATE TABLE role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (menu_id) REFERENCES menus(menu_id)
);
```

---

## How Authorization Works

1. User makes a request to an API endpoint
2. `DynamicAuthorizationManager` intercepts the request
3. It extracts the user's roles from the JWT token
4. For each menu in the database, it checks if the request URL matches the menu's URL pattern
5. If a match is found, it checks if any of the user's roles have access to that menu via `role_menus`
6. Access is granted if a valid role-menu mapping exists

> **Note:** ADMIN role has a bypass in `DynamicAuthorizationManager` - it automatically gets access to all endpoints.

---

## Adding New Menus

To add a new menu:

1. Add the menu entry in `DataInitializer.java`:
   ```java
   Menu newMenu = findOrCreateMenu("MENU_CODE", "Menu Name", "/api/endpoint/pattern");
   ```

2. Map the menu to appropriate roles:
   ```java
   mapRoleToMenu(roleObject, newMenu);
   ```

3. Restart the application to seed the new menu

---

## RBAC Management API

### Controller: `RbacController.java`

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `GET` | `/api/rbac/roles` | List all roles with summary | - | `List<RoleAccessSummaryDTO>` |
| `GET` | `/api/rbac/roles/{roleId}/access` | Get role permissions grouped by category | - | `RoleAccessDTO` |
| `PUT` | `/api/rbac/roles/{roleId}/access` | Bulk update role menus | `BulkRoleMenuUpdateRequest` | `RoleAccessDTO` |
| `GET` | `/api/rbac/categories` | Get all menu categories | - | `List<String>` |

### DTOs

#### `RoleAccessSummaryDTO`
```java
public record RoleAccessSummaryDTO(
    Long roleId,
    String roleName,
    int totalMenus,
    int assignedMenus
) {}
```

#### `RoleAccessDTO`
```java
public record RoleAccessDTO(
    Long roleId,
    String roleName,
    List<MenuGroupDTO> menuGroups
) {}
```

#### `MenuGroupDTO`
```java
public record MenuGroupDTO(
    String category,
    List<MenuItemDTO> menus
) {}
```

#### `MenuItemDTO`
```java
public record MenuItemDTO(
    Long menuId,
    String code,
    String name,
    String urlPattern,
    boolean isAssigned
) {}
```

#### `BulkRoleMenuUpdateRequest`
```java
public record BulkRoleMenuUpdateRequest(
    Long roleId,
    List<Long> menuIds  // List of menu IDs to assign
) {}
```

### Example API Usage

#### Get All Roles Summary
```http
GET /api/rbac/roles
Authorization: Bearer {admin_token}
```

Response:
```json
{
  "success": true,
  "data": [
    { "roleId": 1, "roleName": "ADMIN", "totalMenus": 80, "assignedMenus": 80 },
    { "roleId": 2, "roleName": "USER", "totalMenus": 80, "assignedMenus": 14 },
    { "roleId": 3, "roleName": "MARKETING", "totalMenus": 80, "assignedMenus": 12 }
  ]
}
```

#### Get Role Access Details
```http
GET /api/rbac/roles/3/access
Authorization: Bearer {admin_token}
```

Response:
```json
{
  "success": true,
  "data": {
    "roleId": 3,
    "roleName": "MARKETING",
    "menuGroups": [
      {
        "category": "Loan Workflow",
        "menus": [
          { "menuId": 1, "code": "LOAN_SUBMIT", "name": "Submit Loan", "isAssigned": false },
          { "menuId": 2, "code": "LOAN_QUEUE_MARKETING", "name": "Marketing Queue", "isAssigned": true }
        ]
      }
    ]
  }
}
```

#### Update Role Access
```http
PUT /api/rbac/roles/3/access
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "roleId": 3,
  "menuIds": [2, 5, 8, 12, 15]
}
```

---

*Last Updated: January 2026*

