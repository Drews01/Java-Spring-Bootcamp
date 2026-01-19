# Branch Feature Documentation

This document describes the Branch feature implementation for the Loan Workflow system. Branches allow loans to be segmented by geographic location, with Marketing and Branch Manager staff only seeing loans from their assigned branch.

---

## Overview

- **Branch Master Data**: Branches (Jakarta, Surabaya, Semarang) are seeded on application startup.
- **Loan Submission**: When a customer submits a loan, they must specify a `branchId`.
- **Staff Assignment**: Marketing and Branch Manager users are assigned to a specific branch.
- **Queue Filtering**: Marketing and Branch Manager queues only show loans for their assigned branch.
- **Back Office**: Back Office users see ALL loans (they are not branch-filtered, acting as HQ).

---

## Database Schema

### `branches` Table

| Column       | Type         | Description                       |
|--------------|--------------|-----------------------------------|
| `id`         | BIGINT (PK)  | Auto-increment primary key        |
| `code`       | VARCHAR(50)  | Unique branch code (e.g., JKT)    |
| `name`       | VARCHAR(100) | Branch name (e.g., Jakarta)       |
| `address`    | VARCHAR(255) | Branch address                    |
| `is_active`  | BOOLEAN      | Whether branch is active          |
| `created_at` | DATETIME     | Record creation timestamp         |
| `updated_at` | DATETIME     | Record update timestamp           |

### `users` Table (Modified)

| Column      | Type        | Description                          |
|-------------|-------------|--------------------------------------|
| `branch_id` | BIGINT (FK) | References `branches.id` (nullable)  |

### `loan_applications` Table (Modified)

| Column      | Type        | Description                          |
|-------------|-------------|--------------------------------------|
| `branch_id` | BIGINT (FK) | References `branches.id` (nullable)  |

---

## RBAC Permission Menus

The following permission menus control access to workflow actions:

| Menu Code      | Description                              | Assigned Roles     |
|----------------|------------------------------------------|-------------------|
| `LOAN_REVIEW`  | Review loans (SUBMITTED, IN_REVIEW)      | MARKETING         |
| `LOAN_APPROVE` | Approve loans (WAITING_APPROVAL)         | BRANCH_MANAGER    |
| `LOAN_REJECT`  | Reject loans (WAITING_APPROVAL)          | BRANCH_MANAGER    |
| `LOAN_DISBURSE`| Disburse loans (APPROVED_WAITING_DISBURSEMENT) | BACK_OFFICE |

---

## Entities

### `Branch.java`
**Location**: `src/main/java/com/example/demo/entity/Branch.java`

```java
@Entity
@Table(name = "branches")
public class Branch {
  private Long id;
  private String code;       // Unique, e.g., "JKT"
  private String name;       // Unique, e.g., "Jakarta"
  private String address;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
```

### `User.java` (Modified)
**Added Field**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "branch_id")
private Branch branch;
```

### `LoanApplication.java` (Modified)
**Added Field**:
```java
@ManyToOne
@JoinColumn(name = "branch_id")
private Branch branch;
```

---

## DTOs

### `LoanSubmitRequest.java` (Modified)
**Added Field**:
```java
private Long branchId; // Required - the branch this loan application belongs to
```

### `LoanQueueItemDTO.java` (Modified)
**Added Fields**:
```java
private Long branchId;
private String branchName;
```

---

## Repository

### `BranchRepository.java`
**Location**: `src/main/java/com/example/demo/repository/BranchRepository.java`

```java
public interface BranchRepository extends JpaRepository<Branch, Long> {
  Optional<Branch> findByCode(String code);
  Optional<Branch> findByName(String name);
  List<Branch> findByIsActiveTrue();
}
```

### `LoanApplicationRepository.java` (Modified)
**Added Method**:
```java
List<LoanApplication> findByCurrentStatusInAndBranch_IdOrderByCreatedAtDesc(
    List<String> statuses, Long branchId);
```

---

## Service

### `LoanWorkflowService.java` (Modified)

**`submitLoan` method changes**:
1. Validates that `branchId` is provided.
2. Fetches the `Branch` entity.
3. Sets the `branch` on the `LoanApplication`.

---

## Controller

### `LoanWorkflowController.java` (Modified)

**Queue Endpoints**:
- `GET /api/loan-workflow/queue/marketing` - Filtered by user's branch
- `GET /api/loan-workflow/queue/branch-manager` - Filtered by user's branch
- `GET /api/loan-workflow/queue/back-office` - NOT filtered (sees all branches)

---

## Seeded Data

### Branches (via `DataInitializer.java`)

| Code | Name      | Address                              |
|------|-----------|--------------------------------------|
| JKT  | Jakarta   | Jl. Sudirman No. 1, Jakarta          |
| SBY  | Surabaya  | Jl. Basuki Rahmat No. 10, Surabaya   |
| SMG  | Semarang  | Jl. Pemuda No. 5, Semarang           |

### Test Users

| Username       | Email                      | Branch    | Roles          |
|----------------|----------------------------|-----------|----------------|
| admin          | admin@example.com          | -         | ADMIN, USER    |
| marketing      | marketing@example.com      | Jakarta   | MARKETING      |
| manager        | manager@example.com        | Jakarta   | BRANCH_MANAGER |
| backoffice     | backoffice@example.com     | -         | BACK_OFFICE    |
| user           | user@example.com           | -         | USER           |
| marketing_sby  | marketing_sby@example.com  | Surabaya  | MARKETING      |
| manager_sby    | manager_sby@example.com    | Surabaya  | BRANCH_MANAGER |
| marketing_smg  | marketing_smg@example.com  | Semarang  | MARKETING      |
| manager_smg    | manager_smg@example.com    | Semarang  | BRANCH_MANAGER |

---

## API Testing with Postman

### 1. Login to Get Token

**Request**:
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "pass123"
}
```

**Response**: Save the `token` for subsequent requests.

---

### 2. Get Available Branches

> **Note**: There is no direct API to list branches. Use the seeded data above. Branch IDs are typically:
> - Jakarta: `1`
> - Surabaya: `2`
> - Semarang: `3`

---

### 3. Submit a Loan (Customer)

**Request**:
```
POST http://localhost:8081/api/loan-workflow/submit
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "amount": 5000000,
  "tenureMonths": 12,
  "branchId": 1
}
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Loan application submitted successfully",
  "data": {
    "loanApplicationId": 1,
    "userId": 5,
    "productId": 1,
    "amount": 5000000.0,
    "tenureMonths": 12,
    "interestRateApplied": 8.0,
    "totalAmountToPay": 5218452.12,
    "currentStatus": "SUBMITTED",
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

**Error Case (No branchId)**:
```json
{
  "success": false,
  "message": "Branch ID is required for loan submission"
}
```

**Error Case (Invalid branchId)**:
```json
{
  "success": false,
  "message": "Branch not found with id: 999"
}
```

---

### 4. View Marketing Queue (Jakarta Marketing)

**Login as Jakarta Marketing**:
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "marketing",
  "password": "pass123"
}
```

**Request**:
```
GET http://localhost:8081/api/loan-workflow/queue/marketing
Authorization: Bearer <jakarta_marketing_token>
```

**Expected Response**: Only loans with `branchId: 1` (Jakarta) are returned.

---

### 5. View Marketing Queue (Surabaya Marketing)

**Login as Surabaya Marketing**:
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "marketing_sby",
  "password": "pass123"
}
```

**Request**:
```
GET http://localhost:8081/api/loan-workflow/queue/marketing
Authorization: Bearer <surabaya_marketing_token>
```

**Expected Response**: Only loans with `branchId: 2` (Surabaya) are returned. If the customer submitted a loan to Jakarta, this user will NOT see it.

---

### 6. View Back Office Queue (All Branches)

**Login as Back Office**:
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "backoffice",
  "password": "pass123"
}
```

**Request**:
```
GET http://localhost:8081/api/loan-workflow/queue/back-office
Authorization: Bearer <backoffice_token>
```

**Expected Response**: ALL loans in `APPROVED_WAITING_DISBURSEMENT` status, regardless of branch.

---

### 7. Perform Action (Marketing Comment)

**Request**:
```
POST http://localhost:8081/api/loan-workflow/action
Authorization: Bearer <jakarta_marketing_token>
Content-Type: application/json

{
  "loanApplicationId": 1,
  "action": "COMMENT",
  "comment": "Customer documents verified. Forwarding to manager."
}
```

---

## Summary of Files Changed

| File                                | Change Type | Description                                      |
|-------------------------------------|-------------|--------------------------------------------------|
| `Branch.java`                       | NEW         | Branch entity                                    |
| `BranchRepository.java`             | NEW         | Branch repository                                |
| `User.java`                         | MODIFIED    | Added `branch` field                             |
| `LoanApplication.java`              | MODIFIED    | Added `branch` field                             |
| `LoanSubmitRequest.java`            | MODIFIED    | Added `branchId` field                           |
| `LoanQueueItemDTO.java`             | MODIFIED    | Added `branchId`, `branchName` fields            |
| `LoanApplicationRepository.java`    | MODIFIED    | Added branch-filtered query                      |
| `LoanWorkflowService.java`          | MODIFIED    | Branch validation in `submitLoan`                |
| `LoanWorkflowController.java`       | MODIFIED    | Branch filtering in queue endpoints              |
| `DataInitializer.java`              | MODIFIED    | Seed branches and assign to test users           |
