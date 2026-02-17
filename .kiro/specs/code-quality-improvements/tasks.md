# Implementation Plan: Code Quality Improvements

## Overview

This plan outlines the step-by-step refactoring of the Java Spring Boot application to improve code quality, maintainability, and testability. The refactoring is organized into logical phases, with each task building on previous work. All changes maintain existing API contracts and functionality.

## Tasks

- [x] 1. Create enums and constants infrastructure
  - Create RoleName enum with all system roles (USER, ADMIN, MARKETING, BRANCH_MANAGER, BACK_OFFICE)
  - Create ErrorMessage class with all error message constants
  - Create ApiMessage class with all success message constants
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 1.1 Write unit tests for enums and constants
  - Test RoleName enum contains all expected values
  - Test ErrorMessage constants are non-null and properly formatted
  - Test ApiMessage constants are non-null and properly formatted
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 2. Create BaseController and PagedResponse
  - [x] 2.1 Create PagedResponse DTO class
    - Define PagedResponse with content, pageNumber, pageSize, totalElements, totalPages, last fields
    - Add Lombok annotations (@Data, @Builder)
    - _Requirements: 2.4_
  
  - [x] 2.2 Create BaseController abstract class
    - Implement getCurrentUserId() method to extract user ID from security context
    - Implement buildPageResponse() method to construct paginated responses
    - Add proper exception handling for missing security context
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [x] 2.3 Write unit tests for BaseController
    - Test getCurrentUserId with valid authentication
    - Test getCurrentUserId throws IllegalStateException when no user in context
    - Test buildPageResponse constructs correct PagedResponse structure
    - _Requirements: 2.2, 2.3, 2.4_
  
  - [x] 2.4 Write property test for buildPageResponse
    - **Property 1: Page Response Structure Completeness**
    - **Validates: Requirements 2.5**

- [x] 3. Checkpoint - Verify base infrastructure
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Create service interfaces
  - [x] 4.1 Create IAuthService interface
    - Define interface with register, login, refreshAccessToken, logout methods
    - _Requirements: 5.1, 5.2_
  
  - [x] 4.2 Create IOAuthService interface
    - Define interface with loginWithGoogle method
    - _Requirements: 5.1, 5.2_
  
  - [x] 4.3 Create IUserService interface
    - Define interface with all public methods from UserService
    - _Requirements: 5.1, 5.2_
  
  - [x] 4.4 Create additional service interfaces as needed
    - Create interfaces for LoanWorkflowService, UserProfileService, etc.
    - _Requirements: 5.1, 5.2_

- [x] 5. Extract OAuthService from AuthService
  - [x] 5.1 Create OAuthService class implementing IOAuthService
    - Move loginWithGoogle method from AuthService to OAuthService
    - Move createGoogleUser helper method to OAuthService
    - Inject required dependencies (UserRepository, RoleRepository, JwtService, etc.)
    - Use RoleName enum instead of "USER" string literal
    - _Requirements: 1.2, 1.3, 3.4_
  
  - [x] 5.2 Refactor AuthService to implement IAuthService
    - Remove loginWithGoogle and createGoogleUser methods
    - Keep register, login, refreshAccessToken, logout methods
    - Ensure AuthService implements IAuthService interface
    - Use RoleName enum instead of "USER" string literal
    - _Requirements: 1.1, 1.4, 5.3_
  
  - [x] 5.3 Update AuthController to use IOAuthService
    - Inject IOAuthService for Google login endpoint
    - Keep IAuthService injection for other endpoints
    - _Requirements: 1.2, 5.4_
  
  - [x] 5.4 Write unit tests for OAuthService
    - Test Google token verification
    - Test user creation for new Google users
    - Test user lookup for existing Google users
    - Test token generation
    - _Requirements: 1.2, 1.3_
  
  - [x] 5.5 Write integration tests for authentication endpoints
    - **Property 2: Authentication Response Format Preservation**
    - Test register endpoint returns correct AuthResponse format
    - Test login endpoint returns correct AuthResponse format
    - Test Google login endpoint returns correct AuthResponse format
    - **Validates: Requirements 1.5, 1.6**

- [x] 6. Checkpoint - Verify service refactoring
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Refactor controllers to extend BaseController
  - [x] 7.1 Refactor UserController
    - Extend BaseController
    - Replace getCurrentUserId implementation with call to super.getCurrentUserId()
    - Update paginated endpoints to use buildPageResponse()
    - _Requirements: 2.6_
  
  - [x] 7.2 Refactor LoanWorkflowController
    - Extend BaseController
    - Replace getCurrentUserId implementation with call to super.getCurrentUserId()
    - Update paginated endpoints to use buildPageResponse()
    - _Requirements: 2.6_
  
  - [x] 7.3 Refactor LoanApplicationController
    - Extend BaseController
    - Replace getCurrentUserId implementation with call to super.getCurrentUserId()
    - _Requirements: 2.6_
  
  - [x] 7.4 Refactor UserProfileController
    - Extend BaseController
    - Replace getCurrentUserId implementation with call to super.getCurrentUserId()
    - _Requirements: 2.6_
  
  - [x] 7.5 Refactor remaining controllers
    - Apply same pattern to any other controllers with getCurrentUserId
    - _Requirements: 2.6_

- [x] 8. Replace magic strings with enums and constants
  - [x] 8.1 Replace role name strings in services
    - Update AuthService to use RoleName.USER instead of "USER"
    - Update OAuthService to use RoleName.USER instead of "USER"
    - Update UserService to use RoleName.ADMIN instead of "ADMIN"
    - Update all other services with role references
    - _Requirements: 3.4_
  
  - [x] 8.2 Replace error messages with ErrorMessage constants
    - Update all services to use ErrorMessage constants
    - Update exception messages to use constants
    - _Requirements: 3.5_
  
  - [x] 8.3 Replace success messages with ApiMessage constants
    - Update all controllers to use ApiMessage constants in ResponseUtil calls
    - _Requirements: 3.6_

- [x] 9. Update service implementations to use interfaces
  - [x] 9.1 Update UserService to implement IUserService
    - Add implements IUserService to class declaration
    - Verify all interface methods are implemented
    - _Requirements: 5.3_
  
  - [x] 9.2 Update controller dependencies to use interfaces
    - Change all @Autowired service fields to use interface types
    - Update UserController to inject IUserService instead of UserService
    - Update AuthController to inject IAuthService instead of AuthService
    - Update all other controllers similarly
    - _Requirements: 5.4_
  
  - [x] 9.3 Update service-to-service dependencies to use interfaces
    - Update any services that depend on other services to use interface types
    - _Requirements: 5.4_
  
  - [x] 9.4 Write integration tests for service interfaces
    - Test dependency injection works with interface types
    - Test all service methods work through interfaces
    - **Validates: Requirements 5.5**

- [x] 10. Checkpoint - Verify interface refactoring
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Audit and enhance input validation
  - [x] 11.1 Audit all DTOs for validation annotations
    - Review all DTO classes in dto package
    - Identify missing @NotBlank, @NotNull, @Size, @Email annotations
    - Document DTOs that need validation updates
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [x] 11.2 Add validation annotations to DTOs
    - Add @NotBlank to required string fields
    - Add @Size constraints to all string fields
    - Add @Email to email fields
    - Add @NotNull to required non-string fields
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [x] 11.3 Verify @Valid usage in controllers
    - Ensure all controller methods use @Valid on request parameters
    - Add @Valid where missing
    - _Requirements: 6.6_
  
  - [x] 11.4 Write integration tests for validation
    - **Property 3: Validation Error Response Format**
    - Test invalid DTOs trigger 400 Bad Request
    - Test error responses contain field-level details
    - Test valid DTOs pass validation
    - **Validates: Requirements 6.5**

- [x] 12. Remove test fallback code (if any found)
  - [x] 12.1 Scan codebase for test-only code paths
    - Search for test mode checks, isDevelopment flags, etc.
    - Document any test fallback code found
    - _Requirements: 4.1_
  
  - [x] 12.2 Remove or externalize test fallback logic
    - Remove inline test conditionals from production code
    - Move test-specific behavior to test configurations or Spring profiles
    - _Requirements: 4.2, 4.3, 4.4_

- [ ] 13. Final checkpoint and regression testing
  - [ ] 13.1 Run all existing tests
    - Execute full test suite
    - Verify all tests pass
    - _Requirements: 5.5_
  
  - [ ] 13.2 Verify API compatibility
    - Test all authentication endpoints
    - Verify response formats match pre-refactoring behavior
    - Verify HTTP status codes unchanged
    - _Requirements: 1.6_
  
  - [ ] 13.3 Code review and cleanup
    - Review all refactored code
    - Remove unused imports
    - Ensure consistent formatting
    - Verify all magic strings replaced

## Notes

- Tasks marked with `*` are optional and can be skipped for faster completion
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation and provide opportunities to address issues
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Integration tests ensure refactoring maintains existing behavior
- This is a refactoring spec - no new features are added, only code structure improvements

