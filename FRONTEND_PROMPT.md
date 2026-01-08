# Frontend Prompt: Branch Management Feature

Use this prompt to create branch management features in your Angular frontend.

---

## Features to Implement

### 1. New Page: Branch Management (`/admin/branches`)
**Access:** ADMIN role only

**Features:**
- Display list of branches (table)
- Create new branch (button + modal)
- Delete/deactivate branch

### 2. Modify User Component
**In `user.component.html`:**
- Add "Branch" column to user list table
- Add branch dropdown in "Add User" modal
- Add branch dropdown in "Edit User" modal for reassignment

---

## API Endpoints Reference

**Base URL:** `http://localhost:8081`  
**Authorization:** All endpoints require `Bearer <token>` header (ADMIN role)

---

### GET `/api/admin/branches`
**Description:** List all branches

**Response:**
```json
{
  "success": true,
  "message": "Branches fetched successfully",
  "data": [
    {
      "id": 1,
      "code": "JKT",
      "name": "Jakarta",
      "address": "Jl. Sudirman No. 1, Jakarta",
      "isActive": true,
      "createdAt": "2026-01-08T10:00:00",
      "updatedAt": "2026-01-08T10:00:00"
    }
  ]
}
```

---

### GET `/api/admin/branches/active`
**Description:** List only active branches (for dropdowns)

**Response:** Same format as above, filtered to `isActive: true`

---

### POST `/api/admin/branches`
**Description:** Create new branch

**Request:**
```json
{
  "code": "BDG",
  "name": "Bandung",
  "address": "Jl. Asia Afrika No. 1"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Branch created successfully",
  "data": {
    "id": 4,
    "code": "BDG",
    "name": "Bandung",
    "address": "Jl. Asia Afrika No. 1",
    "isActive": true,
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

**Error (400):**
```json
{
  "success": false,
  "message": "Branch code already exists: BDG"
}
```

---

### DELETE `/api/admin/branches/{id}`
**Description:** Deactivate branch (soft delete)

**Response (200):**
```json
{
  "success": true,
  "message": "Branch deactivated successfully"
}
```

---

### POST `/api/admin/branches/{branchId}/users/{userId}`
**Description:** Assign user to branch

**Response (200):**
```json
{
  "success": true,
  "message": "User assigned to branch successfully",
  "data": {
    "userId": 2,
    "username": "marketing",
    "email": "marketing@example.com",
    "branchId": 1,
    "branchName": "Jakarta",
    "roles": ["MARKETING"]
  }
}
```

**Error (400):**
```json
{
  "success": false,
  "message": "User must have MARKETING or BRANCH_MANAGER role to be assigned to a branch"
}
```

---

### DELETE `/api/admin/branches/{branchId}/users/{userId}`
**Description:** Unassign user from branch

**Response (200):**
```json
{
  "success": true,
  "message": "User unassigned from branch successfully",
  "data": {
    "userId": 2,
    "username": "marketing",
    "email": "marketing@example.com",
    "branchId": null,
    "branchName": null,
    "roles": ["MARKETING"]
  }
}
```

---

## User List - Branch Info Needed

**Important:** To show branch name in user list, the `GET /api/users/admin/list` endpoint needs to return branch info in each user object:

```json
{
  "id": 2,
  "username": "marketing",
  "email": "marketing@example.com",
  "isActive": true,
  "roles": ["MARKETING"],
  "branchId": 1,
  "branchName": "Jakarta"
}
```

---

## Summary Table

| Endpoint | Method | Use Case |
|----------|--------|----------|
| `/api/admin/branches` | GET | List branches for table |
| `/api/admin/branches/active` | GET | Populate branch dropdown |
| `/api/admin/branches` | POST | Create new branch |
| `/api/admin/branches/{id}` | DELETE | Deactivate branch |
| `/api/admin/branches/{branchId}/users/{userId}` | POST | Assign user to branch |
| `/api/admin/branches/{branchId}/users/{userId}` | DELETE | Unassign user from branch |
