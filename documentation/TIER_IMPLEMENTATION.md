# Loan Product Tier System Implementation

## Overview

This document describes the tier-based loan product system that automatically manages user credit limits, tier assignments, and automatic tier upgrades based on payment history.

## Product Tiers

| Tier   | Credit Limit      | Interest Rate | Upgrade Threshold     |
|--------|-------------------|---------------|----------------------|
| Bronze | Rp 10,000,000     | 8%            | Rp 15,000,000 paid   |
| Silver | Rp 25,000,000     | 7%            | Rp 50,000,000 paid   |
| Gold   | Rp 50,000,000     | 6%            | No upgrade (max tier)|

## How It Works

### 1. Automatic Bronze Assignment
When a user first applies for a loan, the system automatically assigns them the Bronze tier product.

### 2. Credit Limit Validation
Before a loan is submitted, the system checks:
- User's current tier credit limit
- Total unpaid (disbursed but not paid) loans
- Remaining available limit = Credit Limit - Total Unpaid

### 3. Payment Tracking
When a loan is marked as paid:
- The loan status changes to `PAID`
- User's credit limit is freed up (available for new loans)
- Total paid amount is tracked for tier upgrades

### 4. Automatic Tier Upgrades
After payment, the system checks if user qualifies for an upgrade:
- Bronze → Silver: After paying Rp 15,000,000 total
- Silver → Gold: After paying Rp 50,000,000 total

---

## API Endpoints

### Submit Loan Application
```
POST /api/loan-workflow/submit
Authorization: Bearer {your-jwt-token}
```
**Request Body:**
```json
{
  "amount": 5000000,
  "tenureMonths": 12
}
```
**Notes:**
- `userId` is automatically extracted from the JWT token (prevents security issues)
- `productId` is optional - uses user's tier product if not specified  
- `interestRateApplied` is optional - uses product's rate if not specified
- System validates credit limit before accepting

### Mark Loan as Paid
```
POST /api/loan-eligibility/pay/{loanApplicationId}
```
- Marks loan as paid
- Frees up credit limit
- Checks for tier upgrade

### Get User Eligibility Details
```
GET /api/loan-eligibility/user/{userId}
```
**Response:**
```json
{
  "userId": 5,
  "currentTier": "Bronze Loan Product",
  "tierOrder": 1,
  "creditLimit": 10000000.0,
  "remainingLimit": 5000000.0,
  "totalPaidLoans": 5000000.0,
  "totalUnpaidLoans": 5000000.0,
  "upgradeThreshold": 15000000.0
}
```

---

## Testing with Postman

### Prerequisites
1. Start the application: `mvn spring-boot:run`
2. Login to get JWT token (see JWT_AUTHENTICATION_GUIDE.md)

### Test Flow

#### Step 1: Login
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "pass123"
}
```
Save the token from response.

#### Step 2: Submit First Loan (should auto-assign Bronze)
```
POST http://localhost:8081/api/loan-workflow/submit
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 5000000,
  "tenureMonths": 12
}
```
Expected: Loan created with Bronze tier product for the authenticated user.

#### Step 3: Check Eligibility  
```
GET http://localhost:8081/api/loan-eligibility/user/{your-user-id}
Authorization: Bearer {token}
```
**Note:** You can also get eligibility for the currently authenticated user.

Expected: Shows remaining limit = 5,000,000 (10M - 5M used)

#### Step 4: Try to Exceed Limit
```
POST http://localhost:8081/api/loan-workflow/submit
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 6000000,
  "tenureMonths": 12
}
```
Expected: Error - "Loan amount exceeds remaining credit limit"

#### Step 5: Process Loan Through Workflow
Complete the workflow (COMMENT → FORWARD → APPROVE → DISBURSE) using the loan-workflow action endpoint.

#### Step 6: Mark Loan as Paid
```
POST http://localhost:8081/api/loan-eligibility/pay/1
Authorization: Bearer {token}
```
Expected: Loan marked as PAID, credit limit reset.

#### Step 7: Verify Tier Upgrade
After paying enough loans (15M for Silver), check eligibility again:
```
GET http://localhost:8081/api/loan-eligibility/user/{your-user-id}
```
Expected: currentTier = "Silver Loan Product"

---

## Build & Run

### Build
```bash
cd "c:\Users\Andrew\Desktop\Bootcamp\Spring Java Bootcamp\Java Spring Bootcamp"
mvn clean compile
```

### Run
```bash
mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

### Format Code
```bash
mvn spotless:apply
```

---

## Database Schema Changes

### products table (new columns)
| Column            | Type    | Description                        |
|-------------------|---------|------------------------------------|
| tier_order        | INT     | 1=Bronze, 2=Silver, 3=Gold         |
| credit_limit      | DOUBLE  | Max outstanding loans for tier     |
| upgrade_threshold | DOUBLE  | Total paid amount to upgrade       |

### user_products table (new columns)
| Column             | Type   | Description                    |
|--------------------|--------|--------------------------------|
| current_used_amount| DOUBLE | Total outstanding loan amounts |
| total_paid_amount  | DOUBLE | Cumulative paid amounts        |

### loan_applications table (new columns)
| Column  | Type     | Description                |
|---------|----------|----------------------------|
| is_paid | BOOLEAN  | Whether loan is paid off   |
| paid_at | DATETIME | When loan was paid         |

### loan_status enum (new value)
- Added: `PAID` - After DISBURSED, when user pays off the loan

---

## Key Files Modified

- `Product.java` - Added tier fields
- `UserProduct.java` - Added tracking fields  
- `LoanApplication.java` - Added payment fields
- `LoanStatus.java` - Added PAID status
- `DataInitializer.java` - Creates Bronze/Silver/Gold products
- `LoanWorkflowService.java` - Integrated eligibility checks
- `LoanEligibilityService.java` - NEW: Tier management logic
- Repository interfaces - Added tier query methods
