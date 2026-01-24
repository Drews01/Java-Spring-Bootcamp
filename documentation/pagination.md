# Pagination Standard Documentation

## Overview
This document outlines the standard pagination approach implemented across the application using Spring Data's `Pageable` and `Page<T>` interfaces. This standardization ensures consistent API responses and simplifies frontend integration.

## Standard Pagination Pattern

### Request
Endpoints accepting pagination should typically support the following query parameters:
- `page`: Zero-based page index (default: 0)
- `size`: The size of the page to be returned (default: 10 or 20 depending on context)
- `sort` (optional): Sorting criteria in the format `property(,asc|desc)`.

### Response
Responses are wrapped in the standard `ApiResponse` and contain a Spring `Page` object.

**Example JSON Response Structure:**
```json
{
  "data": {
    "content": [ ... ],          // List of items
    "pageable": { ... },         // Pagination details requested
    "last": false,               // Is this the last page?
    "totalElements": 100,        // Total items across all pages
    "totalPages": 10,            // Total number of pages
    "size": 10,                  // Page size
    "number": 0,                 // Current page number
    "sort": { ... },             // Sorting details
    "numberOfElements": 10,      // Number of items in current page
    "first": true,               // Is this the first page?
    "empty": false               // Is the page empty?
  },
  "message": "Success message",
  "status": 200
}
```

## Implemented Endpoints

The following endpoints have been updated to support standard pagination:

### 1. User Management
- **GET** `/api/users/admin/list`
- **Parameters:** `page` (int), `size` (int)
- **Returns:** `Page<UserListDTO>`
- **Description:** Returns a paginated list of active (non-deleted) users for admin view.

### 2. Branch Management
- **GET** `/api/admin/branches`
- **Parameters:** `page` (int), `size` (int)
- **Returns:** `Page<BranchDTO>`
- **Description:** Returns a paginated list of all branches.

### 3. Loan Workflow Queues
- **GET** `/queue/marketing`
    - **Returns:** `Page<LoanQueueItemDTO>`
    - **Filter:** `SUBMITTED`, `IN_REVIEW`
- **GET** `/queue/branch-manager`
    - **Returns:** `Page<LoanQueueItemDTO>`
    - **Filter:** `WAITING_APPROVAL` (Filtered by user's branch)
- **GET** `/queue/back-office`
    - **Returns:** `Page<LoanQueueItemDTO>`
    - **Filter:** `APPROVED_WAITING_DISBURSEMENT`

### 4. Loan Action History
- **GET** `/history/marketing`
- **GET** `/history/branch-manager`
- **GET** `/history/back-office`
- **Parameters:** `page`, `size`, `month` (optional), `year` (optional)
- **Returns:** `Page<ActionHistoryDTO>`
- **Note:** The deprecated `ActionHistoryPageDTO` has been successfully removed and replaced with standard `Page<T>`.

### 5. Role Management (RBAC)
- **GET** `/api/rbac/roles`
- **Parameters:** `page` (int), `size` (int)
- **Returns:** `Page<RoleAccessSummaryDTO>`
- **Description:** Returns a paginated list of roles with their menu access summary.

## Key Code Changes

- **Repositories:** Added methods accepting `Pageable` (e.g., `findByDeletedFalse(Pageable pageable)`).
- **Services:** Updated service methods to accept `Pageable` and return `Page<DTO>`.
- **Controllers:** Updated endpoints to use `PageRequest.of(page, size)` and return `ResponseEntity<ApiResponse<Page<DTO>>>`.
- **Removed:** `ActionHistoryPageDTO.java` (Redundant custom pagination class).

## Frontend Integration Guide
Frontend applications consuming these APIs should be updated to:
1.  Pass `page` and `size` parameters in API calls.
2.  Read the `content` array from the response `data` object for the list of items.
3.  Use `totalElements` and `totalPages` from the response `data` object for pagination controls.
