# Implementation Plan: Code Improvements

## Overview

This document provides a detailed implementation plan for the recommendations from the Code Analysis. Each recommendation is broken down into specific files that need to be changed.

---

## Phase 1: Immediate Actions (High Priority)

### 1.1 Add @Transactional to Multi-Operation Service Methods

**Rationale:** Methods performing multiple database operations need `@Transactional` to ensure data consistency.

| File | Method | Change Required |
|------|--------|-----------------|
| `ProductService.java` | `createProduct()` | Add `@Transactional` |
| `ProductService.java` | `updateProductStatus()` | Add `@Transactional` |
| `ProductService.java` | `deleteProduct()` | Add `@Transactional` |
| `RoleService.java` | `createRole()` | Add `@Transactional` |
| `RoleService.java` | `deleteRole()` | Add `@Transactional` |
| `UserService.java` | `createUser()` | Add `@Transactional` |
| `UserService.java` | `updateUser()` | Add `@Transactional` |
| `UserService.java` | `deleteUser()` | Add `@Transactional` |
| `LoanWorkflowService.java` | `getAllowedActions()` | Add `@Transactional(readOnly = true)` |

#### Detailed Changes:

**File: `src/main/java/com/example/demo/service/ProductService.java`**
```java
// Add import
import org.springframework.transaction.annotation.Transactional;

// Change 1: Line 21 - Add @Transactional
@Transactional
@CacheEvict(value = {"products", "activeProducts"}, allEntries = true)
public Product createProduct(Product product) { }

// Change 2: Line 54 - Add @Transactional
@Transactional
@CacheEvict(value = {"products", "activeProducts", "productByCode"}, allEntries = true)
public Product updateProductStatus(Long id, Boolean isActive) { }

// Change 3: Line 73 - Add @Transactional
@Transactional
@CacheEvict(value = {"products", "activeProducts", "productByCode"}, allEntries = true)
public void deleteProduct(Long id) { }
```

**File: `src/main/java/com/example/demo/service/RoleService.java`**
```java
// Add import
import org.springframework.transaction.annotation.Transactional;

// Change 1: Line 15 - Add @Transactional
@Transactional
public Role createRole(Role role) { }

// Change 2: Line 23 - Add @Transactional
@Transactional
public void deleteRole(Long id) { }
```

**File: `src/main/java/com/example/demo/service/UserService.java`**
```java
// Change 1: Line 30 - Add @Transactional
@Transactional
public User createUser(User user) { }

// Change 2: Line 45 - Add @Transactional
@Transactional
public User updateUser(Long id, User userDetails) { }

// Change 3: Line 56 - Add @Transactional
@Transactional
public void deleteUser(Long id) { }
```

**File: `src/main/java/com/example/demo/service/LoanWorkflowService.java`**
```java
// Change: Add @Transactional(readOnly = true) to getAllowedActions method (around line 399)
@Transactional(readOnly = true)
public List<String> getAllowedActions(String currentStatus, Long userId) { }
```

---

### 1.2 Replace RuntimeException with Domain-Specific Exceptions

**Rationale:** Using specific exceptions allows better error handling and more informative API responses.

**Files to Modify:**

| File | Lines | Current Code | New Code |
|------|-------|--------------|----------|
| `UserService.java` | 42 | `new RuntimeException("User not found...")` | `new ResourceNotFoundException("User", "id", id)` |
| `UserService.java` | 121, 163, 171 | `new RuntimeException("Role not found...")` | `new ResourceNotFoundException("Role", "name", roleName)` |
| `RoleService.java` | 28 | `new RuntimeException("Role not found...")` | `new ResourceNotFoundException("Role", "id", id)` |
| `BranchService.java` | 43, 78, 108, 125, 163 | `new RuntimeException("Branch not found...")` | `new ResourceNotFoundException("Branch", "id", id)` |
| `BranchService.java` | 120, 151 | `new RuntimeException("User not found...")` | `new ResourceNotFoundException("User", "id", userId)` |
| `LoanApplicationService.java` | 29, 35, 58, 91 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `LoanWorkflowService.java` | 49, 93, 106, 161, 167 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `LoanEligibilityService.java` | 96, 137, 210 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `LoanHistoryService.java` | 35, 42, 64, 96 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `NotificationService.java` | 29, 45, 62, 98 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `MenuService.java` | 36, 50 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `RbacService.java` | 149, 188 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `RoleMenuService.java` | 29, 34, 75 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `UserProductService.java` | 31, 37, 68, 99 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `UserProfileService.java` | 32 | `new RuntimeException(...)` | `new ResourceNotFoundException(...)` |
| `FileValidationService.java` | 20, 25, 35, 41 | `new RuntimeException(...)` | `new BusinessException(...)` or `new IllegalArgumentException(...)` |

#### Enhanced ResourceNotFoundException

**File: `src/main/java/com/example/demo/exception/ResourceNotFoundException.java`** (Refactor)
```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
  
  public ResourceNotFoundException(String message) {
    super(message);
  }
  
  // Add new constructor for entity + field + value
  public ResourceNotFoundException(String entityName, String fieldName, Object fieldValue) {
    super(String.format("%s not found with %s: '%s'", entityName, fieldName, fieldValue));
  }
  
  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
```

#### Example Changes:

**File: `src/main/java/com/example/demo/service/UserService.java`**
```java
// Line 42: Change from
.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
// To
.orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

// Line 121: Change from
.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
// To
.orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
```

**File: `src/main/java/com/example/demo/service/FileValidationService.java`**
```java
// Line 20: Change from
throw new RuntimeException("File is empty");
// To
throw new IllegalArgumentException("File is empty");

// Line 41: Change from
throw new RuntimeException("Failed to validate file", e);
// To
throw new BusinessException("Failed to validate file", "FILE_VALIDATION_ERROR");
```

---

### 1.3 Update Tests for New Exceptions

**Files to Modify:**

| File | Lines | Change Required |
|------|-------|-----------------|
| `UserServiceTest.java` | 81 | Change `RuntimeException.class` to `ResourceNotFoundException.class` |
| `LoanApplicationServiceTest.java` | 100 | Change `RuntimeException.class` to `ResourceNotFoundException.class` |
| `AuthServiceTest.java` | 133 | Change `RuntimeException.class` to appropriate exception |

---

## Phase 2: Short-term Improvements (Medium Priority)

### 2.1 Create DTOs for All Entity Responses

**Problem:** Controllers returning entities directly can expose sensitive data and create tight coupling.

#### Step 1: Create New DTOs

**Create: `src/main/java/com/example/demo/dto/RoleDTO.java`**
```java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
  private Long id;
  private String name;
  private Boolean isActive;
  
  public static RoleDTO fromEntity(com.example.demo.entity.Role role) {
    return RoleDTO.builder()
        .id(role.getId())
        .name(role.getName())
        .isActive(role.getIsActive())
        .build();
  }
}
```

**Create: `src/main/java/com/example/demo/dto/RoleMenuDTO.java`**
```java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuDTO {
  private Long roleId;
  private String roleName;
  private Long menuId;
  private String menuName;
  private String menuCode;
  
  public static RoleMenuDTO fromEntity(com.example.demo.entity.RoleMenu roleMenu) {
    return RoleMenuDTO.builder()
        .roleId(roleMenu.getRoleId())
        .menuId(roleMenu.getMenuId())
        // Note: Need to fetch role/menu names in service
        .build();
  }
}
```

**Create: `src/main/java/com/example/demo/dto/ProductDTO.java`**
```java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
  private Long id;
  private String code;
  private String name;
  private String description;
  private Double minAmount;
  private Double maxAmount;
  private Integer minTenureMonths;
  private Integer maxTenureMonths;
  private Double interestRate;
  private Boolean isActive;
  
  public static ProductDTO fromEntity(com.example.demo.entity.Product product) {
    return ProductDTO.builder()
        .id(product.getId())
        .code(product.getCode())
        .name(product.getName())
        .description(product.getDescription())
        .minAmount(product.getMinAmount())
        .maxAmount(product.getMaxAmount())
        .minTenureMonths(product.getMinTenureMonths())
        .maxTenureMonths(product.getMaxTenureMonths())
        .interestRate(product.getInterestRate())
        .isActive(product.getIsActive())
        .build();
  }
}
```

#### Step 2: Update Services to Return DTOs

**File: `src/main/java/com/example/demo/service/RoleService.java`**
```java
// Change return types to RoleDTO
public RoleDTO createRole(Role role) {
    Role saved = roleRepository.save(role);
    return RoleDTO.fromEntity(saved);
}

public List<RoleDTO> getAllRoles() {
    return roleRepository.findByDeletedFalse().stream()
        .map(RoleDTO::fromEntity)
        .collect(Collectors.toList());
}
```

**File: `src/main/java/com/example/demo/service/ProductService.java`**
```java
// Change return types to ProductDTO
public ProductDTO createProduct(Product product) {
    // ... validation logic
    Product saved = productRepository.save(product);
    return ProductDTO.fromEntity(saved);
}

public List<ProductDTO> getAllProducts() {
    return productRepository.findByDeletedFalse().stream()
        .map(ProductDTO::fromEntity)
        .collect(Collectors.toList());
}
// ... similar changes for other methods
```

**File: `src/main/java/com/example/demo/service/RoleMenuService.java`**
```java
// Change return types to RoleMenuDTO
// Add method to fetch role name and menu name
```

#### Step 3: Update Controllers

**File: `src/main/java/com/example/demo/controller/RoleController.java`**
```java
@GetMapping
public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
    List<RoleDTO> roles = roleService.getAllRoles();
    return ResponseUtil.ok(roles, "Roles retrieved successfully");
}

@PostMapping
public ResponseEntity<ApiResponse<RoleDTO>> createRole(@RequestBody Role role) {
    RoleDTO createdRole = roleService.createRole(role);
    return ResponseUtil.created(createdRole, "Role created successfully");
}
```

**File: `src/main/java/com/example/demo/controller/ProductController.java`**
```java
// Change all ResponseEntity<ApiResponse<Product>> to ResponseEntity<ApiResponse<ProductDTO>>
// Change all ResponseEntity<ApiResponse<List<Product>>> to ResponseEntity<ApiResponse<List<ProductDTO>>>
```

**File: `src/main/java/com/example/demo/controller/RoleMenuController.java`**
```java
// Change all ResponseEntity<ApiResponse<RoleMenu>> to ResponseEntity<ApiResponse<RoleMenuDTO>>
// Change all ResponseEntity<ApiResponse<List<RoleMenu>>> to ResponseEntity<ApiResponse<List<RoleMenuDTO>>>
```

**File: `src/main/java/com/example/demo/controller/UserController.java`**
```java
// Line 32: Change ResponseEntity<ApiResponse<List<User>>> to ResponseEntity<ApiResponse<List<UserListDTO>>>
// Line 38: Change ResponseEntity<ApiResponse<User>> to ResponseEntity<ApiResponse<UserListDTO>>
// Line 44: Change ResponseEntity<ApiResponse<User>> to ResponseEntity<ApiResponse<UserListDTO>>
// Line 50: Change ResponseEntity<ApiResponse<User>> to ResponseEntity<ApiResponse<UserListDTO>>
```

---

### 2.2 Refactor Large Services (LoanWorkflowService)

**Rationale:** `LoanWorkflowService` (485 lines) violates SRP. Break it into smaller services.

#### Step 1: Create New Service Classes

**Create: `src/main/java/com/example/demo/service/LoanStateMachineService.java`**
```java
@Service
@RequiredArgsConstructor
public class LoanStateMachineService {
  
  public void validateTransition(String currentStatus, String action) {
    // Move validation logic from LoanWorkflowService
  }
  
  public String getNextStatus(String currentStatus, String action) {
    // Move status transition logic
  }
  
  public List<String> getAllowedActions(String currentStatus) {
    // Move allowed actions logic (without access control check)
  }
}
```

**Create: `src/main/java/com/example/demo/service/LoanPermissionService.java`**
```java
@Service
@RequiredArgsConstructor
public class LoanPermissionService {
  
  private final AccessControlService accessControl;
  
  public void validateActorPermission(String currentStatus) {
    // Move permission validation logic
  }
  
  public List<String> getAllowedActionsWithPermission(String currentStatus) {
    // Combine state machine with permission check
  }
  
  public boolean canPerformAction(String currentStatus, String action) {
    // Check if user can perform specific action
  }
}
```

**Create: `src/main/java/com/example/demo/service/LoanCalculationService.java`**
```java
@Service
public class LoanCalculationService {
  
  public Double calculateTotalAmountToPay(Double principal, Double annualInterestRate, Integer tenureMonths) {
    // Move EMI calculation logic
  }
  
  public Double calculateEMI(Double principal, Double annualInterestRate, Integer tenureMonths) {
    // Calculate monthly EMI
  }
}
```

#### Step 2: Refactor LoanWorkflowService

**File: `src/main/java/com/example/demo/service/LoanWorkflowService.java`** (Refactored - ~200 lines)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanWorkflowService {
  
  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanHistoryRepository loanHistoryRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final BranchRepository branchRepository;
  private final NotificationService notificationService;
  private final LoanEligibilityService loanEligibilityService;
  private final UserProfileService userProfileService;
  private final LoanNotificationService loanNotificationService;
  
  // New dependencies
  private final LoanStateMachineService stateMachineService;
  private final LoanPermissionService permissionService;
  private final LoanCalculationService calculationService;
  
  @Transactional
  public LoanApplicationDTO submitLoan(LoanSubmitRequest request, Long userId) {
    // Simplified - business logic only
    // Delegate calculations to LoanCalculationService
    // Delegate validation to respective services
  }
  
  @Transactional
  public LoanApplicationDTO performAction(LoanActionRequest request, Long actorUserId) {
    // Simplified - orchestration only
  }
  
  // Remove: validateTransition() → moved to LoanStateMachineService
  // Remove: validateActorPermission() → moved to LoanPermissionService
  // Remove: getNextStatus() → moved to LoanStateMachineService
  // Remove: calculateTotalAmountToPay() → moved to LoanCalculationService
}
```

---

### 2.3 Extract Constants for Magic Strings

**File: `src/main/java/com/example/demo/enums/MenuCode.java`** (Create New)
```java
package com.example.demo.enums;

import lombok.Getter;

@Getter
public enum MenuCode {
  // Admin Module
  ADMIN_DASHBOARD("Admin Module"),
  ADMIN_SYSTEM_LOGS("Admin Module"),
  
  // User Management
  USER_LIST("User Management"),
  USER_GET("User Management"),
  USER_CREATE("User Management"),
  USER_UPDATE("User Management"),
  USER_DELETE("User Management"),
  ADMIN_USER_LIST("User Management"),
  ADMIN_USER_CREATE("User Management"),
  ADMIN_USER_STATUS("User Management"),
  ADMIN_USER_ROLES("User Management"),
  
  // Role Management
  ROLE_LIST("Role Management"),
  ROLE_CREATE("Role Management"),
  ROLE_DELETE("Role Management"),
  
  // Menu Management
  MENU_LIST("Menu Management"),
  MENU_GET("Menu Management"),
  MENU_CREATE("Menu Management"),
  MENU_UPDATE("Menu Management"),
  MENU_DELETE("Menu Management"),
  
  // Loan Workflow
  LOAN_SUBMIT("Loan Workflow"),
  LOAN_ACTION("Loan Workflow"),
  LOAN_ALLOWED_ACTIONS("Loan Workflow"),
  LOAN_QUEUE_MARKETING("Loan Workflow"),
  LOAN_QUEUE_BRANCH_MANAGER("Loan Workflow"),
  LOAN_QUEUE_BACK_OFFICE("Loan Workflow"),
  LOAN_REVIEW("Loan Workflow"),
  LOAN_APPROVE("Loan Workflow"),
  LOAN_REJECT("Loan Workflow"),
  LOAN_DISBURSE("Loan Workflow"),
  
  // Add remaining menu codes...
  
  private final String category;
  
  MenuCode(String category) {
    this.category = category;
  }
  
  public static String getCategory(String code) {
    try {
      return valueOf(code).getCategory();
    } catch (IllegalArgumentException e) {
      return "Other";
    }
  }
}
```

**File: `src/main/java/com/example/demo/service/RbacService.java`** (Refactor)
```java
// Remove static MENU_CATEGORY_MAP block

// Replace usage:
String category = MenuCode.getCategory(menu.getCode());
```

**File: `src/main/java/com/example/demo/enums/LoanStatus.java`** (Check/Update)
```java
// Add if not exists:
public enum LoanStatus {
  SUBMITTED,
  IN_REVIEW,
  WAITING_APPROVAL,
  APPROVED_WAITING_DISBURSEMENT,
  DISBURSED,
  REJECTED,
  PAID;
  
  public static boolean isTerminal(String status) {
    return DISBURSED.name().equals(status) || 
           REJECTED.name().equals(status) || 
           PAID.name().equals(status);
  }
}
```

**File: `src/main/java/com/example/demo/service/LoanWorkflowService.java`** (Update to use enums)
```java
// Change from: LoanStatus.SUBMITTED.name()
// To: LoanStatus.SUBMITTED (when comparing with enum)
// Or keep .name() when storing to entity
```

---

## Phase 3: Testing Improvements

### 3.1 Increase Unit Test Coverage

**Priority Services to Test:**

| Service | Priority | Test File |
|---------|----------|-----------|
| `LoanWorkflowService` | Critical | `LoanWorkflowServiceTest.java` (exists, expand) |
| `LoanEligibilityService` | High | `LoanEligibilityServiceTest.java` (exists, expand) |
| `LoanCalculationService` (new) | High | Create `LoanCalculationServiceTest.java` |
| `LoanStateMachineService` (new) | High | Create `LoanStateMachineServiceTest.java` |
| `LoanPermissionService` (new) | High | Create `LoanPermissionServiceTest.java` |
| `BranchService` | Medium | Create `BranchServiceTest.java` |
| `RbacService` | Medium | Create `RbacServiceTest.java` |
| `RoleMenuService` | Medium | Create `RoleMenuServiceTest.java` |

**Example Test Template:**

**Create: `src/test/java/com/example/demo/service/LoanCalculationServiceTest.java`**
```java
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanCalculationServiceTest {
  
  @InjectMocks
  private LoanCalculationService calculationService;
  
  @Test
  void calculateTotalAmountToPay_ValidInput_ReturnsCorrectAmount() {
    // Given
    Double principal = 1000000.0;
    Double interestRate = 12.0;
    Integer tenure = 12;
    
    // When
    Double result = calculationService.calculateTotalAmountToPay(principal, interestRate, tenure);
    
    // Then
    assertNotNull(result);
    assertTrue(result > principal); // Total should include interest
  }
  
  @Test
  void calculateTotalAmountToPay_ZeroInterest_ReturnsPrincipal() {
    // Given
    Double principal = 1000000.0;
    Double interestRate = 0.0;
    Integer tenure = 12;
    
    // When
    Double result = calculationService.calculateTotalAmountToPay(principal, interestRate, tenure);
    
    // Then
    assertEquals(principal, result);
  }
  
  @Test
  void calculateEMI_ValidInput_ReturnsCorrectEMI() {
    // Given
    Double principal = 1000000.0;
    Double interestRate = 12.0;
    Integer tenure = 12;
    
    // When
    Double result = calculationService.calculateEMI(principal, interestRate, tenure);
    
    // Then
    assertNotNull(result);
    // EMI for 1M at 12% for 12 months ≈ 88,848
    assertEquals(88848.79, result, 0.01);
  }
}
```

---

## Implementation Timeline

| Week | Phase | Tasks | Files Changed |
|------|-------|-------|---------------|
| **Week 1** | 1.1 | Add @Transactional annotations | 8 service files |
| **Week 1** | 1.2 | Replace RuntimeException | 15+ service files + ResourceNotFoundException |
| **Week 1** | 1.3 | Update tests | 3 test files |
| **Week 2** | 2.1 | Create DTOs (RoleDTO, ProductDTO, RoleMenuDTO) | 3 new DTOs |
| **Week 2** | 2.1 | Update RoleService, ProductService, RoleMenuService | 3 service files |
| **Week 2** | 2.1 | Update RoleController, ProductController, RoleMenuController, UserController | 4 controller files |
| **Week 3** | 2.2 | Create LoanStateMachineService, LoanPermissionService, LoanCalculationService | 3 new service files |
| **Week 3** | 2.2 | Refactor LoanWorkflowService | 1 major service refactor |
| **Week 4** | 2.3 | Create MenuCode enum | 1 new enum file |
| **Week 4** | 2.3 | Refactor RbacService | 1 service file |
| **Week 4** | 3.1 | Create new unit tests | 4+ new test files |

---

## Summary of Files to Change

### Service Files (15 files)
1. `ProductService.java` - Add @Transactional, return ProductDTO
2. `RoleService.java` - Add @Transactional, return RoleDTO, use ResourceNotFoundException
3. `UserService.java` - Add @Transactional, use ResourceNotFoundException, return DTOs
4. `BranchService.java` - Use ResourceNotFoundException
5. `LoanApplicationService.java` - Use ResourceNotFoundException
6. `LoanWorkflowService.java` - Use ResourceNotFoundException, refactor to smaller services
7. `LoanEligibilityService.java` - Use ResourceNotFoundException
8. `LoanHistoryService.java` - Use ResourceNotFoundException
9. `NotificationService.java` - Use ResourceNotFoundException
10. `MenuService.java` - Use ResourceNotFoundException
11. `RbacService.java` - Use ResourceNotFoundException, use MenuCode enum
12. `RoleMenuService.java` - Use ResourceNotFoundException, return RoleMenuDTO
13. `UserProductService.java` - Use ResourceNotFoundException
14. `UserProfileService.java` - Use ResourceNotFoundException
15. `FileValidationService.java` - Use appropriate exceptions

### Controller Files (5 files)
1. `UserController.java` - Return UserListDTO instead of User
2. `RoleController.java` - Return RoleDTO instead of Role
3. `ProductController.java` - Return ProductDTO instead of Product
4. `RoleMenuController.java` - Return RoleMenuDTO instead of RoleMenu

### New Files to Create
1. `RoleDTO.java` - DTO for Role entity
2. `RoleMenuDTO.java` - DTO for RoleMenu entity
3. `ProductDTO.java` - DTO for Product entity (if not exists)
4. `MenuCode.java` - Enum for menu codes
5. `LoanStateMachineService.java` - State transition logic
6. `LoanPermissionService.java` - Permission validation logic
7. `LoanCalculationService.java` - EMI calculation logic

### Exception Files (1 file)
1. `ResourceNotFoundException.java` - Add constructor with entity + field + value

### Test Files (4+ new, 3 updated)
1. `UserServiceTest.java` - Update exception assertions
2. `LoanApplicationServiceTest.java` - Update exception assertions
3. `AuthServiceTest.java` - Update exception assertions
4. `LoanCalculationServiceTest.java` - New test file
5. `LoanStateMachineServiceTest.java` - New test file
6. `LoanPermissionServiceTest.java` - New test file
7. `BranchServiceTest.java` - New test file

---

## Risk Assessment

| Change | Risk Level | Mitigation |
|--------|------------|------------|
| Adding @Transactional | Low | Standard Spring practice, well-tested |
| Replacing RuntimeException | Medium | Ensure GlobalExceptionHandler catches ResourceNotFoundException |
| Creating DTOs | Medium | Update both service and controller layers together |
| Refactoring LoanWorkflowService | High | Comprehensive testing, gradual migration |
| Extracting Constants | Low | Compile-time safety with enums |

---

## Verification Checklist

After implementation, verify:

- [ ] All service methods with multiple DB operations have @Transactional
- [ ] No RuntimeException for "not found" scenarios
- [ ] All controllers return DTOs, never entities
- [ ] LoanWorkflowService < 200 lines after refactoring
- [ ] No magic strings for menu codes in RbacService
- [ ] All new services have unit tests
- [ ] Existing tests pass with new exception types
- [ ] API responses remain consistent (use ApiResponse wrapper)
