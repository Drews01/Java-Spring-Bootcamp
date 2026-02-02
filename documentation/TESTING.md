# Unit Testing Documentation

This document outlines the unit testing strategy and coverage implemented for the Spring Boot Loan Application.

## Overview

We have implemented comprehensive unit tests for the core service layer of the application. The tests focus on business logic verification, happy path scenarios, and critical validation rules including IDOR (Insecure Direct Object Reference) protection.

**Technology Stack:**
- **JUnit 5**: Testing framework
- **Mockito**: Mocking dependencies (Repositories, External Services)
- **Spring Boot Test**: Integration with Spring context (using `TestConfig` for mocks)

## Test Coverage

We covered **8 Service Classes** with a total of **57 Test Cases**.

### 1. Loan Eligibility Service (`LoanEligibilityServiceTest`)
**Focus:** Credit limit calculations, tier logic, and upgrades.
- `canApplyForLoan`: Verifies logical check against credit limit.
- `getRemainingCreditLimit`: Ensures active loans are subtracted from limit.
- `getCurrentTierProduct`: Verifies highest tier selection.
- `processLoanPayment`: Checks loan status updates and used amount recalculation.
- `checkAndUpgradeTier`: Validates tier upgrade threshold logic.

### 2. Loan Workflow Service (`LoanWorkflowServiceTest`)
**Focus:** State machine transitions and validations.
- `submitLoan`: Validates loan creation with correct initial status.
- `performAction`: Tests all state transitions (SUBMIT -> IN_REVIEW -> WAITING_APPROVAL -> APPROVED -> DISBURSED).
- `validateTransition`: Ensures invalid transitions throw exceptions.
- `validateActorPermission`: Mock tests for role-based action permission.

### 3. Loan Application Service (`LoanApplicationServiceTest`)
**Focus:** CRUD operations.
- `createLoanApplication`: Verifies entity mapping and saving.
- `getLoanApplications`: specific queries by User ID and Status.

### 4. Product Service (`ProductServiceTest`)
**Focus:** Product validation and management.
- `createProduct`: Validates duplicate codes and invalid amount/tenure ranges.
- `getAllProducts`: Ensures only non-deleted products are returned.

### 5. User Service (`UserServiceTest`)
**Focus:** User management and IDOR security.
- `setUserActiveStatus`: Prevents self-deactivation and deactivating the last admin.
- `updateUserRoles`: Prevents removing own ADMIN role.
- `createUserByAdmin`: Validates unique fields.

### 6. Auth Service (`AuthServiceTest`)
**Focus:** Security and Authentication.
- `register`: New user registration logic.
- `login`: Credential validation and token generation.
- `logout`: Token blacklisting.

### 7. RBAC Service (`RbacServiceTest`)
**Focus:** Role-Menu management.
- `getRoleAccess`: Hierarchy of role permissions.
- `updateRoleAccess`: Modifying permissions.

### 8. Access Control Service (`AccessControlServiceTest`)
**Focus:** Permission checks.
- `hasMenu`: Boolean check validation.

## How to Run Tests

You can run the tests using Maven from the command line:

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=LoanEligibilityServiceTest

# Run all tests in a package
mvn test -Dtest=com.example.demo.service.*
```

## detailed Test Scenarios

### Loan Workflow State Machine
The tests verify the following strict lifecycle:
1. **SUBMITTED** (User)
2. **IN_REVIEW** (Marketing) via `COMMENT`
3. **WAITING_APPROVAL** (Marketing) via `FORWARD_TO_MANAGER`
4. **APPROVED_WAITING_DISBURSEMENT** (Branch Manager) via `APPROVE` or **REJECTED** via `REJECT`
5. **DISBURSED** (Back Office) via `DISBURSE`
6. **PAID** (User/System) via Payment

### Security & IDOR Tests
Special attention was paid to `UserService` to ensure:
- An Admin cannot deactivate themselves.
- An Admin cannot remove their own ADMIN role.
- The system must always have at least one active Admin.
