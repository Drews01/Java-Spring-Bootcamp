# Loan Approval Workflow Documentation

This document explains how the Loan Approval Workflow works and how to test it.

## 1. Workflow Overview

The system uses a state machine to manage loan applications. Each status belongs to a specific "Queue" which is accessible only by specific roles.

### Roles & Queues
*   **MARKETING**: Accesses the **Marketing Queue** (`SUBMITTED`, `IN_REVIEW`).
*   **BRANCH_MANAGER**: Accesses the **Branch Manager Queue** (`WAITING_APPROVAL`).
*   **BACK_OFFICE**: Accesses the **Back Office Queue** (`APPROVED_WAITING_DISBURSEMENT`).

### Status Transitions
1.  **SUBMITTED**: Initial status after submission.
2.  **IN_REVIEW**: Set after Marketing adds a `COMMENT`.
3.  **WAITING_APPROVAL**: Set after Marketing performs `FORWARD_TO_MANAGER`.
4.  **APPROVED_WAITING_DISBURSEMENT**: Set after Branch Manager performs `APPROVE`.
5.  **DISBURSED**: Final status after Back Office performs `DISBURSE`.
6.  **REJECTED**: Final status if Branch Manager performs `REJECT`.

---

## 2. API Endpoints

### Workflow Actions
*   **Submit Loan**: `POST /api/loan-workflow/submit`
*   **Perform Action**: `POST /api/loan-workflow/action` (Actions: `COMMENT`, `FORWARD_TO_MANAGER`, `APPROVE`, `REJECT`, `DISBURSE`)

### Queues (Role-based)
*   **Marketing Queue**: `GET /api/loan-workflow/queue/marketing`
*   **Branch Manager Queue**: `GET /api/loan-workflow/queue/branch-manager`
*   **Back Office Queue**: `GET /api/loan-workflow/queue/back-office`

### Utilities
*   **Allowed Actions**: `GET /api/loan-workflow/{loanId}/allowed-actions` (Returns what the current user can do with the loan).

---

## 3. How to Test (Step-by-Step)

### Step 1: Submit a Loan
Submit a new loan application. You can do this as any authenticated user.
```bash
# Example JSON Payload
{
  "userId": 1,
  "productId": 1,
  "amount": 10000000.0,
  "tenureMonths": 12,
  "interestRateApplied": 0.1
}
```
*Result:* Status becomes `SUBMITTED`. A history record is created.

### Step 2: Marketing Review (Marketing Queue)
1.  Login as a user with the **MARKETING** role.
2.  Get the queue: `GET /api/loan-workflow/queue/marketing`.
3.  Add a comment: `POST /api/loan-workflow/action` with action `COMMENT`.
    *Result:* Status transitions `SUBMITTED -> IN_REVIEW`. Customer receives an In-App notification.
4.  Forward to Manager: `POST /api/loan-workflow/action` with action `FORWARD_TO_MANAGER`.
    *Result:* Status transitions `IN_REVIEW -> WAITING_APPROVAL`. Branch Manager(s) receive notifications.

### Step 3: Branch Manager Approval (Manager Queue)
1.  Login as a user with the **BRANCH_MANAGER** role.
2.  Get the queue: `GET /api/loan-workflow/queue/branch-manager`.
3.  Approve: `POST /api/loan-workflow/action` with action `APPROVE`.
    *Result:* Status transitions `WAITING_APPROVAL -> APPROVED_WAITING_DISBURSEMENT`. Customer and Back Office receive notifications.

### Step 4: Back Office Disbursement (Disbursement Queue)
1.  Login as a user with the **BACK_OFFICE** role.
2.  Get the queue: `GET /api/loan-workflow/queue/back-office`.
3.  Disburse: `POST /api/loan-workflow/action` with action `DISBURSE`.
    *Result:* Status transitions `APPROVED_WAITING_DISBURSEMENT -> DISBURSED`. Customer receives final notification.

---

## 4. Database Verification

You can verify the data in your SQL database:
*   **`loan_applications`**: Check the `current_status` column.
*   **`loan_history`**: Verify that every action has a corresponding row with `actor_user_id`, `from_status`, `to_status`, and `comment`.
*   **`notifications`**: Check if notifications were generated for the recipient `user_id`.

## 5. Error Cases to Test
*   **Invalid Role**: Try to `APPROVE` as a `MARKETING` user. (Should fail).
*   **Invalid Transition**: Try to `DISBURSE` a loan that is still `IN_REVIEW`. (Should throw `IllegalStateException`).
*   **Missing Comment**: Ensure comments are optional but tracked when provided.
