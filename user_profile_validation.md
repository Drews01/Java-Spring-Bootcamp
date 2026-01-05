# User Profile Validation Documentation

## Overview

This document describes the user profile bank account fields and profile completion validation implemented for the loan application system. Users must complete all required profile fields before they can submit a loan application.

## New User Profile Fields

The `user_profile` table has been enhanced with the following bank account information fields:

| Field Name | Type | Max Length | Required | Description |
|------------|------|------------|----------|-------------|
| `account_number` | VARCHAR | 50 | Yes | User's bank account number for loan disbursement |
| `bank_name` | VARCHAR | 100 | Yes | Name of the bank where the account is held |

### Existing Required Fields

- `address` - User's residential address
- `nik` - National ID Number (NIK/KTP number)
- `ktp_path` - Path to the uploaded KTP document
- `phone_number` - User's contact phone number

## Profile Completion Requirements

### Validation Rules

A user profile is considered **complete** when ALL of the following fields contain valid (non-null, non-empty) values:

1. ✅ `address`
2. ✅ `nik`
3. ✅ `ktp_path`
4. ✅ `phone_number`
5. ✅ `account_number` (NEW)
6. ✅ `bank_name` (NEW)

### Loan Submission Validation

When a user attempts to submit a loan application via the `/api/loan-workflow/submit` endpoint, the system performs the following validation sequence:

1. **User Existence** - Verify user exists
2. **Tier Product Assignment** - Ensure user has a tier product assigned
3. **Profile Completion** ⚠️ **NEW** - Verify all required profile fields are filled
4. **Active Loan Check** - Ensure user doesn't have pending loans
5. **Credit Limit Check** - Verify requested amount doesn't exceed credit limit

If the profile is incomplete, the loan submission will be **rejected** with a clear error message.

## API Documentation

### Security Note

> [!IMPORTANT]
> **IDOR Prevention**: All user profile endpoints extract the `userId` from the JWT Bearer token automatically. You **DO NOT** need to (and **SHOULD NOT**) include `userId` in the URL or request body. This prevents Insecure Direct Object Reference (IDOR) vulnerabilities where users could manipulate the userId to access other users' profiles.

### 1. Create or Update User Profile

Create or update the authenticated user's profile. The endpoint automatically detects if a profile exists and creates or updates accordingly.

**Endpoint:** `POST /api/user-profiles`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "address": "Jl. Sudirman No. 123, Jakarta Pusat",
  "nik": "3174012345678901",
  "ktpPath": "/uploads/ktp/user1_ktp.jpg",
  "phoneNumber": "+628123456789",
  "accountNumber": "1234567890",
  "bankName": "Bank BCA"
}
```

> **Note:** Do NOT include `userId` in the request body. It will be automatically extracted from your JWT token.

**Success Response (200 OK for update, 201 Created for new):**
```json
{
  "success": true,
  "message": "User profile updated successfully",
  "data": {
    "userId": 1,
    "address": "Jl. Sudirman No. 123, Jakarta Pusat",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp/user1_ktp.jpg",
    "phoneNumber": "+628123456789",
    "accountNumber": "1234567890",
    "bankName": "Bank BCA",
    "updatedAt": "2026-01-05T15:45:00"
  }
}
```

### 2. Get My Profile

Retrieve the authenticated user's profile.

**Endpoint:** `GET /api/user-profiles/me`

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "userId": 1,
    "address": "Jl. Sudirman No. 123, Jakarta Pusat",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp/user1_ktp.jpg",
    "phoneNumber": "+628123456789",
    "accountNumber": "1234567890",
    "bankName": "Bank BCA",
    "updatedAt": "2026-01-05T15:45:00"
  }
}
```

**Error Response (500 when profile not found):**
```json
{
  "success": false,
  "message": "UserProfile not found for user id: 1",
  "data": null
}
```

### 3. Update My Profile

Update the authenticated user's profile.

**Endpoint:** `PUT /api/user-profiles`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "address": "Jl. Thamrin No. 456, Jakarta Pusat",
  "nik": "3174012345678901",
  "ktpPath": "/uploads/ktp/user1_ktp.jpg",
  "phoneNumber": "+628987654321",
  "accountNumber": "0987654321",
  "bankName": "Bank Mandiri"
}
```

> **Note:** Do NOT include `userId` in the request body. It will be automatically extracted from your JWT token.

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User profile updated successfully",
  "data": {
    "userId": 1,
    "address": "Jl. Thamrin No. 456, Jakarta Pusat",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp/user1_ktp.jpg",
    "phoneNumber": "+628987654321",
    "accountNumber": "0987654321",
    "bankName": "Bank Mandiri",
    "updatedAt": "2026-01-05T16:00:00"
  }
}
```

### 4. Submit Loan Application

Submit a loan application (requires complete profile).

**Endpoint:** `POST /api/loan-workflow/submit`

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Request Body:**
```json
{
  "productId": 1,
  "amount": 5000000.00,
  "tenureMonths": 12,
  "interestRateApplied": 12.0
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Loan submitted successfully",
  "data": {
    "loanApplicationId": 123,
    "userId": 1,
    "productId": 1,
    "amount": 5000000.00,
    "tenureMonths": 12,
    "interestRateApplied": 12.0,
    "totalAmountToPay": 5334847.50,
    "currentStatus": "SUBMITTED",
    "createdAt": "2026-01-05T15:50:00",
    "updatedAt": "2026-01-05T15:50:00"
  }
}
```

**Error Response - Incomplete Profile (500 Internal Server Error):**
```json
{
  "success": false,
  "message": "Cannot submit loan. Your profile is incomplete. Please complete all required fields: address, NIK, KTP document, phone number, account number, and bank name. Update your profile at /api/user-profiles",
  "data": null
}
```

**Error Response - Profile Not Found (500 Internal Server Error):**
```json
{
  "success": false,
  "message": "Cannot submit loan. Your profile is incomplete. Please complete all required fields: address, NIK, KTP document, phone number, account number, and bank name. Update your profile at /api/user-profiles",
  "data": null
}
```

## Error Handling Scenarios

### Scenario 1: Incomplete Profile

**Situation:** User attempts to submit a loan with missing bank information

**Request:** `POST /api/loan-workflow/submit`
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```
```json
{
  "amount": 5000000.00,
  "tenureMonths": 12
}
```

**Response:** `500 Internal Server Error`
```json
{
  "success": false,
  "message": "Cannot submit loan. Your profile is incomplete. Please complete all required fields: address, NIK, KTP document, phone number, account number, and bank name. Update your profile at /api/user-profiles",
  "data": null
}
```

**Resolution:** User must update their profile with complete bank account information before submitting a loan.

```bash
# Fix by updating profile
curl -X PUT http://localhost:8081/api/user-profiles \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "address": "Jl. Sudirman No. 123",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp.jpg",
    "phoneNumber": "+628123456789",
    "accountNumber": "1234567890",
    "bankName": "Bank BCA"
  }'
```

### Scenario 2: Profile Does Not Exist

**Situation:** User has no profile record in the database

**Response:** Same as Scenario 1 - the system treats a non-existent profile as incomplete.

**Resolution:** User must create a profile with all required fields via `POST /api/user-profiles`.

```bash
# Create profile
curl -X POST http://localhost:8081/api/user-profiles \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "address": "Jl. Sudirman No. 123",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp.jpg",
    "phoneNumber": "+628123456789",
    "accountNumber": "1234567890",
    "bankName": "Bank BCA"
  }'
```

### Scenario 3: Empty Fields

**Situation:** Profile exists but some fields are null or empty strings

**Example Profile:**
```json
{
  "address": "Jl. Sudirman No. 123",
  "nik": "3174012345678901",
  "ktpPath": "/uploads/ktp.jpg",
  "phoneNumber": "+628123456789",
  "accountNumber": "",  // Empty string
  "bankName": null      // Null value
}
```

**Response:** Profile validation fails, loan submission is rejected

**Resolution:** Update all empty/null fields with valid values.

### Scenario 4: IDOR Attempt (Security Test)

**Situation:** Malicious user tries to update another user's profile by including userId in request

**Request:** `PUT /api/user-profiles`
```
Authorization: Bearer {user1_token}
```
```json
{
  "userId": 999,  // Attempting to modify another user's profile
  "address": "Malicious Address",
  "accountNumber": "hack123"
}
```

**Result:** The `userId` in the request body is **IGNORED**. The system extracts userId from the JWT token, so only the authenticated user's profile (user1) is updated. User 999's profile remains unchanged.

**Security:** ✅ IDOR vulnerability prevented!

## Implementation Details

### Backend Service Layer

#### UserProfileService.isProfileComplete()

```java
@Transactional(readOnly = true)
public boolean isProfileComplete(Long userId) {
  return userProfileRepository
      .findById(userId)
      .map(
          profile ->
              isNotBlank(profile.getAddress())
                  && isNotBlank(profile.getNik())
                  && isNotBlank(profile.getKtpPath())
                  && isNotBlank(profile.getPhoneNumber())
                  && isNotBlank(profile.getAccountNumber())
                  && isNotBlank(profile.getBankName()))
      .orElse(false);
}

private boolean isNotBlank(String str) {
  return str != null && !str.trim().isEmpty();
}
```

**Logic:**
- Returns `true` only if profile exists AND all required fields contain non-blank values
- Returns `false` if profile doesn't exist or any field is null/empty
- Uses `isNotBlank()` helper to check for null, empty, or whitespace-only values

#### LoanWorkflowService.submitLoan()

The validation is performed early in the loan submission flow:

```java
@Transactional
public LoanApplicationDTO submitLoan(LoanSubmitRequest request, Long userId) {
  // ... user and tier product validations ...

  // Validate user profile is complete before allowing loan submission
  if (!userProfileService.isProfileComplete(userId)) {
    throw new IllegalStateException(
        "Cannot submit loan. Your profile is incomplete. "
            + "Please complete all required fields: address, NIK, KTP document, phone number, account number, and bank name. "
            + "Update your profile at /api/user-profiles/" + userId);
  }

  // ... rest of loan submission logic ...
}
```

## Testing Guide

### Test Case 1: Complete Profile Loan Submission

**Setup:**
1. Create/update user profile with all required fields
2. Submit loan application

**Expected Result:** Loan submission succeeds

**Steps:**
```bash
# 1. Update profile (JWT token required)
curl -X PUT http://localhost:8081/api/user-profiles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "address": "Jl. Sudirman No. 123",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp.jpg",
    "phoneNumber": "+628123456789",
    "accountNumber": "1234567890",
    "bankName": "Bank BCA"
  }'

# 2. Submit loan (userId extracted from JWT token)
curl -X POST http://localhost:8081/api/loan-workflow/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "amount": 5000000.00,
    "tenureMonths": 12
  }'
```

### Test Case 2: Incomplete Profile Loan Submission

**Setup:**
1. Create profile with missing bank information
2. Attempt loan submission

**Expected Result:** Loan submission fails with clear error message

**Steps:**
```bash
# 1. Update profile WITHOUT bank info (userId from JWT)
curl -X PUT http://localhost:8081/api/user-profiles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "address": "Jl. Sudirman No. 123",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp.jpg",
    "phoneNumber": "+628123456789"
  }'

# 2. Attempt loan submission
curl -X POST http://localhost:8081/api/loan-workflow/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "amount": 5000000.00,
    "tenureMonths": 12
  }'

# Expected: Error message about incomplete profile
```

### Test Case 3: Profile Completion After Initial Failure

**Setup:**
1. Attempt loan with incomplete profile (fails)
2. Complete profile
3. Retry loan submission

**Expected Result:** Second submission succeeds

**Steps:**
```bash
# 1. First attempt (fails - profile incomplete)
curl -X POST http://localhost:8081/api/loan-workflow/submit \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000000.00, "tenureMonths": 12}'

# 2. Get current profile to see what's missing
curl -X GET http://localhost:8081/api/user-profiles/me \
  -H "Authorization: Bearer {token}"

# 3. Complete profile with bank info
curl -X PUT http://localhost:8081/api/user-profiles \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "address": "Jl. Sudirman No. 123",
    "nik": "3174012345678901",
    "ktpPath": "/uploads/ktp.jpg",
    "phoneNumber": "+628123456789",
    "accountNumber": "1234567890",
    "bankName": "Bank BCA"
  }'

# 4. Retry submission (succeeds)
curl -X POST http://localhost:8081/api/loan-workflow/submit \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000000.00, "tenureMonths": 12}'
```

### Test Case 4: IDOR Security Test

**Setup:**
1. Create two user accounts with different JWT tokens
2. Attempt to access/modify other user's profile

**Expected Result:** Each user can only access their own profile

**Steps:**
```bash
# User1 creates their profile
curl -X POST http://localhost:8081/api/user-profiles \
  -H "Authorization: Bearer {user1_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "address": "User1 Address",
    "nik": "1234567890123456",
    "ktpPath": "/uploads/user1_ktp.jpg",
    "phoneNumber": "+628111111111",
    "accountNumber": "111111111",
    "bankName": "Bank A"
  }'

# User2 attempts to include userId in request to modify User1's profile
# This should FAIL - the userId in body is IGNORED
curl -X PUT http://localhost:8081/api/user-profiles \
  -H "Authorization: Bearer {user2_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "address": "Hacked Address",
    "accountNumber": "99999999"
  }'

# Verify User1's profile is unchanged
curl -X GET http://localhost:8081/api/user-profiles/me \
  -H "Authorization: Bearer {user1_token}"

# Result: User1's profile remains intact, User2 only modified their own profile (or got error if no profile exists)
```

## Database Schema

### user_profile Table Structure

```sql
CREATE TABLE user_profile (
  user_id BIGINT PRIMARY KEY,
  address VARCHAR(500),
  nik VARCHAR(20) UNIQUE,
  ktp_path VARCHAR(255),
  phone_number VARCHAR(20),
  account_number VARCHAR(50),      -- NEW FIELD
  bank_name VARCHAR(100),          -- NEW FIELD
  updated_at DATETIME NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Migration Notes

> [!IMPORTANT]
> **Database Migration Required**
> 
> When deploying this feature to an existing database, you need to add the new columns to the `user_profile` table:
>
> ```sql
> ALTER TABLE user_profile 
> ADD COLUMN account_number VARCHAR(50),
> ADD COLUMN bank_name VARCHAR(100);
> ```
>
> **Important:** Existing users will have `NULL` values for these fields and will need to update their profiles before submitting new loan applications.

## Business Rules

1. **Mandatory Fields:** All 6 profile fields must be filled before loan submission
2. **Validation Timing:** Validation occurs before checking credit limits or active loans
3. **Error Clarity:** Error messages clearly specify which fields are required
4. **Data Integrity:** Bank account information is essential for loan disbursement
5. **User Experience:** Helpful error message includes direct link to update profile

## Security Considerations

1. **Authorization:** All profile updates require valid JWT authentication
2. **Ownership:** Users can only update their own profiles (enforced by JWT userId)
3. **Data Privacy:** Bank account information is sensitive and must be protected
4. **Audit Trail:** Profile updates are timestamped via `updated_at` field

## Future Enhancements

Potential improvements for future iterations:

1. **Bank Validation:** Integrate with bank APIs to verify account numbers
2. **Bank List:** Provide dropdown of supported banks instead of free text
3. **Profile Completeness Indicator:** Show users % completion of their profile
4. **Reminder Notifications:** Notify users to complete profiles before loan application
5. **Field-Specific Errors:** Return which specific fields are missing in the error response
6. **Profile Wizard:** Guided multi-step form for completing user profiles

## Related Documentation

- [Loan Workflow Documentation](./LOAN_WORKFLOW_DOCUMENTATION.md) - Complete loan approval process
- [JWT Authentication Guide](./JWT_AUTHENTICATION_GUIDE.md) - Authentication and authorization
- [API Documentation](./DOCUMENTATION.md) - General API reference

## Support

For issues or questions regarding profile validation:

1. Check that all required fields are filled with valid values
2. Verify database schema includes new `account_number` and `bank_name` columns
3. Ensure Spotless formatting has been applied: `mvn spotless:apply`
4. Review application logs for detailed error messages

---

**Document Version:** 1.0  
**Last Updated:** January 5, 2026  
**Author:** Antigravity AI Assistant
