# Branch Management Feature Documentation

This document describes the Branch Management API for assigning users (Marketing, Branch Manager) to branches. **All endpoints require ADMIN role access.**

---

## Overview

- **Branch CRUD**: Create, read, update, and deactivate branches
- **User Assignment**: Assign/unassign MARKETING and BRANCH_MANAGER users to branches
- **Access Control**: All endpoints are admin-only (`@PreAuthorize("hasRole('ADMIN')")`)

---

## API Endpoints

### Base URL
```
http://localhost:8081/api/admin/branches
```

### Branch CRUD Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/branches` | List all branches (including inactive) |
| GET | `/api/admin/branches/active` | List only active branches |
| GET | `/api/admin/branches/{id}` | Get branch by ID |
| POST | `/api/admin/branches` | Create new branch |
| PUT | `/api/admin/branches/{id}` | Update branch |
| DELETE | `/api/admin/branches/{id}` | Deactivate branch (soft delete) |

### User Assignment Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/branches/{branchId}/users` | List users in a branch |
| POST | `/api/admin/branches/{branchId}/users/{userId}` | Assign user to branch |
| DELETE | `/api/admin/branches/{branchId}/users/{userId}` | Unassign user from branch |
| GET | `/api/admin/branches/users/assignable` | List users that can be assigned (MARKETING/BRANCH_MANAGER) |

---

## DTOs

### BranchDTO (Response)
```json
{
  "id": 1,
  "code": "JKT",
  "name": "Jakarta",
  "address": "Jl. Sudirman No. 1, Jakarta",
  "isActive": true,
  "createdAt": "2026-01-08T10:00:00",
  "updatedAt": "2026-01-08T10:00:00"
}
```

### CreateBranchRequest
```json
{
  "code": "BDG",
  "name": "Bandung",
  "address": "Jl. Asia Afrika No. 1, Bandung"
}
```
> **Note**: `code` and `name` are required. `address` is optional.

### UpdateBranchRequest
```json
{
  "name": "Updated Name",
  "address": "Updated Address",
  "isActive": false
}
```
> **Note**: All fields are optional. Only provided fields will be updated.

### UserBranchDTO (Response)
```json
{
  "userId": 2,
  "username": "marketing",
  "email": "marketing@example.com",
  "branchId": 1,
  "branchName": "Jakarta",
  "roles": ["MARKETING"]
}
```

---

## Postman Testing Guide

### 1. Login as Admin

**Request:**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

> **Save the token** and use it in the `Authorization` header for all subsequent requests:
> `Authorization: Bearer <token>`

---

### 2. List All Branches

**Request:**
```
GET http://localhost:8081/api/admin/branches
Authorization: Bearer <admin_token>
```

**Expected Response (200 OK):**
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
      "createdAt": "...",
      "updatedAt": "..."
    },
    {
      "id": 2,
      "code": "SBY",
      "name": "Surabaya",
      "address": "Jl. Basuki Rahmat No. 10, Surabaya",
      "isActive": true,
      "createdAt": "...",
      "updatedAt": "..."
    },
    {
      "id": 3,
      "code": "SMG",
      "name": "Semarang",
      "address": "Jl. Pemuda No. 5, Semarang",
      "isActive": true,
      "createdAt": "...",
      "updatedAt": "..."
    }
  ]
}
```

---

### 3. Create New Branch

**Request:**
```
POST http://localhost:8081/api/admin/branches
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "code": "BDG",
  "name": "Bandung",
  "address": "Jl. Asia Afrika No. 1, Bandung"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Branch created successfully",
  "data": {
    "id": 4,
    "code": "BDG",
    "name": "Bandung",
    "address": "Jl. Asia Afrika No. 1, Bandung",
    "isActive": true,
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

**Error Cases:**

Code already exists:
```json
{
  "success": false,
  "message": "Branch code already exists: BDG"
}
```

Name already exists:
```json
{
  "success": false,
  "message": "Branch name already exists: Bandung"
}
```

---

### 4. Update Branch

**Request:**
```
PUT http://localhost:8081/api/admin/branches/4
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Bandung Barat",
  "address": "Jl. Setiabudhi No. 99, Bandung"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Branch updated successfully",
  "data": {
    "id": 4,
    "code": "BDG",
    "name": "Bandung Barat",
    "address": "Jl. Setiabudhi No. 99, Bandung",
    "isActive": true,
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

---

### 5. Deactivate Branch

**Request:**
```
DELETE http://localhost:8081/api/admin/branches/4
Authorization: Bearer <admin_token>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Branch deactivated successfully"
}
```

---

### 6. Get Assignable Users

**Request:**
```
GET http://localhost:8081/api/admin/branches/users/assignable
Authorization: Bearer <admin_token>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Assignable users fetched successfully",
  "data": [
    {
      "userId": 2,
      "username": "marketing",
      "email": "marketing@example.com",
      "branchId": 1,
      "branchName": "Jakarta",
      "roles": ["MARKETING"]
    },
    {
      "userId": 3,
      "username": "manager",
      "email": "manager@example.com",
      "branchId": 1,
      "branchName": "Jakarta",
      "roles": ["BRANCH_MANAGER"]
    }
  ]
}
```

---

### 7. Assign User to Branch

**Request:**
```
POST http://localhost:8081/api/admin/branches/2/users/2
Authorization: Bearer <admin_token>
```
> This assigns user ID 2 (marketing) to branch ID 2 (Surabaya)

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "User assigned to branch successfully",
  "data": {
    "userId": 2,
    "username": "marketing",
    "email": "marketing@example.com",
    "branchId": 2,
    "branchName": "Surabaya",
    "roles": ["MARKETING"]
  }
}
```

**Error Case (Non-assignable role):**
```json
{
  "success": false,
  "message": "User must have MARKETING or BRANCH_MANAGER role to be assigned to a branch"
}
```

---

### 8. Unassign User from Branch

**Request:**
```
DELETE http://localhost:8081/api/admin/branches/2/users/2
Authorization: Bearer <admin_token>
```

**Expected Response (200 OK):**
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

### 9. Get Users in a Branch

**Request:**
```
GET http://localhost:8081/api/admin/branches/1/users
Authorization: Bearer <admin_token>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Users fetched successfully for branch",
  "data": [
    {
      "userId": 2,
      "username": "marketing",
      "email": "marketing@example.com",
      "branchId": 1,
      "branchName": "Jakarta",
      "roles": ["MARKETING"]
    },
    {
      "userId": 3,
      "username": "manager",
      "email": "manager@example.com",
      "branchId": 1,
      "branchName": "Jakarta",
      "roles": ["BRANCH_MANAGER"]
    }
  ]
}
```

---

### 10. Test RBAC (Non-Admin Denied)

**Login as non-admin:**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "marketing",
  "password": "pass123"
}
```

**Request:**
```
GET http://localhost:8081/api/admin/branches
Authorization: Bearer <marketing_token>
```

**Expected Response (403 Forbidden):**
```json
{
  "error": "Access Denied"
}
```

---

## Frontend Implementation Prompt

Copy and use the following prompt to implement branch management features in your Angular frontend:

---

### PROMPT: Branch Management Feature Implementation

Implement the following features for branch management in the Angular frontend. All features require **ADMIN role** access.

---

#### PART 1: Create `BranchService` (Angular Service)

Create a new Angular service `src/app/services/branch.service.ts`:

```typescript
// branch.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Branch {
  id: number;
  code: string;
  name: string;
  address: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBranchRequest {
  code: string;
  name: string;
  address?: string;
}

export interface UpdateBranchRequest {
  name?: string;
  address?: string;
  isActive?: boolean;
}

export interface UserBranch {
  userId: number;
  username: string;
  email: string;
  branchId: number | null;
  branchName: string | null;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class BranchService {
  private baseUrl = '/api/admin/branches';

  constructor(private http: HttpClient) {}

  getAllBranches(): Observable<any> {
    return this.http.get(`${this.baseUrl}`);
  }

  getActiveBranches(): Observable<any> {
    return this.http.get(`${this.baseUrl}/active`);
  }

  getBranchById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${id}`);
  }

  createBranch(request: CreateBranchRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}`, request);
  }

  updateBranch(id: number, request: UpdateBranchRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, request);
  }

  deactivateBranch(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getUsersByBranch(branchId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${branchId}/users`);
  }

  assignUserToBranch(branchId: number, userId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${branchId}/users/${userId}`, {});
  }

  unassignUserFromBranch(branchId: number, userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${branchId}/users/${userId}`);
  }

  getAssignableUsers(): Observable<any> {
    return this.http.get(`${this.baseUrl}/users/assignable`);
  }
}
```

---

#### PART 2: Create `BranchManagementComponent` (New Page)

Create a new component for `/admin/branches` route with:

**Features:**
1. **Branch List Table**
   - Columns: Code, Name, Address, Status (Active/Inactive badge), Actions
   - Actions: Delete button (calls `DELETE /api/admin/branches/{id}`)

2. **Create Branch Button**
   - Opens a modal/form with fields:
     - Code (required, uppercase)
     - Name (required)
     - Address (optional)
   - Submit calls `POST /api/admin/branches`

3. **Delete Branch**
   - Confirmation dialog before deleting
   - Calls `DELETE /api/admin/branches/{id}` (soft delete/deactivate)

**Route Configuration:**
```typescript
// In app.routes.ts
{
  path: 'admin/branches',
  component: BranchManagementComponent,
  canActivate: [authGuard],
  data: { roles: ['ADMIN'] }
}
```

**Sidebar Menu:**
Add "Branch Management" menu item visible only to ADMIN users.

---

#### PART 3: Modify `user.component.html` - Add Branch Column to User List

In the user list table, add a new column to display the user's assigned branch:

```html
<!-- In the table header -->
<th>Branch</th>

<!-- In the table body for each user row -->
<td>
  @if (user.branchName) {
    <span class="badge badge-info">{{ user.branchName }}</span>
  } @else {
    <span class="text-gray-400">Not Assigned</span>
  }
</td>
```

**Backend API Update Required:**
The `GET /api/users/admin/list` endpoint response must include `branchId` and `branchName` for each user. If not present, update `UserListDTO` in the backend to include branch info.

---

#### PART 4: Modify Add User Modal - Include Branch Selection

In the "Add User" modal form (`user.component.html`), add a branch dropdown:

```html
<!-- Add after role selection in the add user form -->
<div class="form-group">
  <label for="branch">Assign to Branch (Optional)</label>
  <select id="branch" formControlName="branchId" class="form-control">
    <option [value]="null">-- No Branch --</option>
    @for (branch of branches; track branch.id) {
      <option [value]="branch.id">{{ branch.name }} ({{ branch.code }})</option>
    }
  </select>
  <small class="text-gray-500">Only required for Marketing or Branch Manager roles</small>
</div>
```

**In component.ts:**
```typescript
// Add to component class
branches: Branch[] = [];

ngOnInit() {
  this.loadBranches();
}

loadBranches() {
  this.branchService.getActiveBranches().subscribe({
    next: (response) => {
      if (response.success) {
        this.branches = response.data;
      }
    }
  });
}

// When creating user, include branchId in the request
onAddUser() {
  const formData = this.addUserForm.value;
  // After user is created via POST /api/users/admin/create,
  // if branchId is selected, call:
  // POST /api/admin/branches/{branchId}/users/{newUserId}
}
```

---

#### PART 5: Modify Edit User Modal - Branch Reassignment

In the "Edit User" modal, add ability to change branch assignment:

```html
<!-- Add in edit user modal -->
<div class="form-group">
  <label for="editBranch">Branch Assignment</label>
  <select id="editBranch" formControlName="branchId" class="form-control">
    <option [value]="null">-- No Branch --</option>
    @for (branch of branches; track branch.id) {
      <option [value]="branch.id">{{ branch.name }} ({{ branch.code }})</option>
    }
  </select>
</div>
```

**In component.ts:**
```typescript
// When saving edit, handle branch changes:
onSaveEditUser() {
  const userId = this.editingUser.id;
  const newBranchId = this.editUserForm.value.branchId;
  const oldBranchId = this.editingUser.branchId;

  // If branch changed
  if (newBranchId !== oldBranchId) {
    if (newBranchId) {
      // Assign to new branch
      this.branchService.assignUserToBranch(newBranchId, userId).subscribe();
    } else if (oldBranchId) {
      // Unassign from branch
      this.branchService.unassignUserFromBranch(oldBranchId, userId).subscribe();
    }
  }
}
```

---

#### API Endpoints Summary

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/admin/branches` | GET | List all branches |
| `/api/admin/branches/active` | GET | List active branches only |
| `/api/admin/branches` | POST | Create new branch |
| `/api/admin/branches/{id}` | PUT | Update branch |
| `/api/admin/branches/{id}` | DELETE | Deactivate branch |
| `/api/admin/branches/{branchId}/users/{userId}` | POST | Assign user to branch |
| `/api/admin/branches/{branchId}/users/{userId}` | DELETE | Unassign user from branch |
| `/api/admin/branches/users/assignable` | GET | List assignable users |

---

#### Styling Notes
- Use TailwindCSS consistent with existing dashboard design
- Status badges: green for Active, gray for Inactive
- Confirmation dialogs for destructive actions (delete)
- Toast notifications for success/error messages
- Loading spinners during API calls

---

## Summary of Files

| File | Type | Description |
|------|------|-------------|
| `BranchDTO.java` | DTO | Response DTO for branch data |
| `CreateBranchRequest.java` | DTO | Request for creating branches |
| `UpdateBranchRequest.java` | DTO | Request for updating branches |
| `UserBranchDTO.java` | DTO | Response for user-branch mapping |
| `BranchService.java` | Service | Business logic for branch management |
| `BranchController.java` | Controller | Admin-only REST endpoints |
| `BranchRepository.java` | Repository | Added `existsByCode`, `existsByName` |

