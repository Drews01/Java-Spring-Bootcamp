# DTO Validation Audit

## Summary
This document tracks the validation status of all DTOs in the application.

## DTOs with Good Validation ✓
These DTOs already have comprehensive validation:
- `AuthRequest.java` - Has @NotBlank on required fields
- `RegisterRequest.java` - Has @NotBlank, @Email, @Size on all fields
- `ForgotPasswordRequest.java` - Has @Email, @NotBlank
- `ResetPasswordRequest.java` - Has @NotBlank, @Size
- `RefreshTokenRequest.java` - Has @NotBlank
- `GoogleLoginRequest.java` - Has @NotBlank
- `AdminCreateUserRequest.java` - Has @NotBlank, @Email, @Size
- `UpdateUserRolesRequest.java` - Has @NotEmpty
- `UpdateUserStatusRequest.java` - Has @NotNull
- `CreateBranchRequest.java` - Has @NotBlank, @Size
- `PushNotificationRequest.java` - Has @NotNull, @NotBlank

## DTOs Needing Validation Updates ⚠️

### High Priority (Request DTOs with missing validation)

1. **LoanSubmitRequest.java**
   - Missing: @NotNull on productId (if required)
   - Missing: @NotNull on amount
   - Missing: @NotNull on tenureMonths
   - Missing: @NotNull on branchId
   - Missing: @Positive or @Min validation on numeric fields
   - Missing: @Size on all fields

2. **LoanActionRequest.java**
   - Missing: @NotNull on loanApplicationId
   - Missing: @NotBlank on action
   - Missing: @Size on action and comment fields

3. **ActionHistoryRequestDTO.java**
   - Missing: @Min, @Max on month (1-12)
   - Missing: @Min on year
   - Missing: @Min on page (>= 0)
   - Missing: @Min, @Max on size

4. **BulkRoleMenuUpdateRequest.java**
   - Missing: @NotNull on roleId
   - Missing: @NotEmpty on menuIds

5. **UpdateBranchRequest.java**
   - Has @Size but missing @NotBlank on name (if required for updates)

### Medium Priority (Response/Data DTOs)

6. **UserProfileDTO.java**
   - This is primarily a response DTO, but if used for updates:
   - Missing: @Size on address, nik, phoneNumber, accountNumber, bankName
   - Missing: @Pattern for phoneNumber format
   - Missing: @Pattern for nik format (if Indonesian NIK)

### Response DTOs (Lower Priority)
These are primarily response DTOs and may not need validation:
- `AuthResponse.java`
- `AdminLoanApplicationDTO.java`
- `BranchDropdownDTO.java`
- `BranchDTO.java`
- `CsrfTokenDTO.java`
- `CurrentUserDTO.java`
- `LoanApplicationDTO.java`
- `LoanHistoryDTO.java`
- `LoanMilestoneDTO.java`
- `LoanQueueItemDTO.java`
- `MenuDTO.java`
- `MenuGroupDTO.java`
- `MenuItemDTO.java`
- `NotificationDTO.java`
- `PagedResponse.java`
- `ProductDTO.java`
- `RoleAccessDTO.java`
- `RoleAccessSummaryDTO.java`
- `RoleDTO.java`
- `RoleMenuDTO.java`
- `StaffDashboardDTO.java`
- `UploadImageResponse.java`
- `UserBranchDTO.java`
- `UserListDTO.java`
- `UserProductDTO.java`
- `UserTierLimitDTO.java`
- `ActionHistoryDTO.java`

## Validation Requirements Summary

Based on Requirements 6.1-6.4:
1. All string fields should have @NotBlank or @NotNull as appropriate
2. All string fields should have @Size constraints
3. All email fields should have @Email annotations
4. All required non-string fields should have @NotNull
5. Numeric fields should have @Min, @Max, @Positive, @PositiveOrZero as appropriate

## Next Steps
1. Add validation annotations to the 6 DTOs identified above
2. Verify @Valid usage in all controller methods
3. Write integration tests for validation
