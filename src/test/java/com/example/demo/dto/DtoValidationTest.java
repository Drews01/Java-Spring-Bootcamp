package com.example.demo.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DTO validation annotations.
 *
 * <p>Tests that: - Invalid DTOs trigger validation errors - Error messages are present - Valid DTOs
 * pass validation
 *
 * <p>**Property 3: Validation Error Response Format** **Validates: Requirements 6.5**
 */
class DtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  // ==================== RegisterRequest Validation Tests ====================

  @Test
  @DisplayName("RegisterRequest with missing username should fail validation")
  void registerRequest_MissingUsername_ShouldFailValidation() {
    RegisterRequest request = new RegisterRequest(null, "test@example.com", "password123");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
  }

  @Test
  @DisplayName("RegisterRequest with invalid email should fail validation")
  void registerRequest_InvalidEmail_ShouldFailValidation() {
    RegisterRequest request = new RegisterRequest("testuser", "invalid-email", "password123");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
  }

  @Test
  @DisplayName("RegisterRequest with short password should fail validation")
  void registerRequest_ShortPassword_ShouldFailValidation() {
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "short");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
  }

  @Test
  @DisplayName("RegisterRequest with short username should fail validation")
  void registerRequest_ShortUsername_ShouldFailValidation() {
    RegisterRequest request = new RegisterRequest("ab", "test@example.com", "password123");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
  }

  @Test
  @DisplayName("RegisterRequest with valid data should pass validation")
  void registerRequest_ValidData_ShouldPassValidation() {
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  // ==================== LoanSubmitRequest Validation Tests ====================

  @Test
  @DisplayName("LoanSubmitRequest with missing amount should fail validation")
  void loanSubmitRequest_MissingAmount_ShouldFailValidation() {
    LoanSubmitRequest request = LoanSubmitRequest.builder().tenureMonths(12).branchId(1L).build();

    Set<ConstraintViolation<LoanSubmitRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
  }

  @Test
  @DisplayName("LoanSubmitRequest with negative amount should fail validation")
  void loanSubmitRequest_NegativeAmount_ShouldFailValidation() {
    LoanSubmitRequest request =
        LoanSubmitRequest.builder().amount(-1000.0).tenureMonths(12).branchId(1L).build();

    Set<ConstraintViolation<LoanSubmitRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
  }

  @Test
  @DisplayName("LoanSubmitRequest with missing branchId should fail validation")
  void loanSubmitRequest_MissingBranchId_ShouldFailValidation() {
    LoanSubmitRequest request = LoanSubmitRequest.builder().amount(5000.0).tenureMonths(12).build();

    Set<ConstraintViolation<LoanSubmitRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("branchId")));
  }

  @Test
  @DisplayName("LoanSubmitRequest with zero tenure should fail validation")
  void loanSubmitRequest_ZeroTenure_ShouldFailValidation() {
    LoanSubmitRequest request =
        LoanSubmitRequest.builder().amount(5000.0).tenureMonths(0).branchId(1L).build();

    Set<ConstraintViolation<LoanSubmitRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tenureMonths")));
  }

  @Test
  @DisplayName("LoanSubmitRequest with valid data should pass validation")
  void loanSubmitRequest_ValidData_ShouldPassValidation() {
    LoanSubmitRequest request =
        LoanSubmitRequest.builder().amount(5000.0).tenureMonths(12).branchId(1L).build();

    Set<ConstraintViolation<LoanSubmitRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  // ==================== LoanActionRequest Validation Tests ====================

  @Test
  @DisplayName("LoanActionRequest with missing loanApplicationId should fail validation")
  void loanActionRequest_MissingLoanId_ShouldFailValidation() {
    LoanActionRequest request =
        LoanActionRequest.builder().action("APPROVE").comment("Looks good").build();

    Set<ConstraintViolation<LoanActionRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("loanApplicationId")));
  }

  @Test
  @DisplayName("LoanActionRequest with missing action should fail validation")
  void loanActionRequest_MissingAction_ShouldFailValidation() {
    LoanActionRequest request =
        LoanActionRequest.builder().loanApplicationId(1L).comment("Looks good").build();

    Set<ConstraintViolation<LoanActionRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("action")));
  }

  @Test
  @DisplayName("LoanActionRequest with valid data should pass validation")
  void loanActionRequest_ValidData_ShouldPassValidation() {
    LoanActionRequest request =
        LoanActionRequest.builder()
            .loanApplicationId(1L)
            .action("APPROVE")
            .comment("Looks good")
            .build();

    Set<ConstraintViolation<LoanActionRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  // ==================== UserProfileDTO Validation Tests ====================

  @Test
  @DisplayName("UserProfileDTO with invalid NIK length should fail validation")
  void userProfileDTO_InvalidNikLength_ShouldFailValidation() {
    UserProfileDTO dto =
        UserProfileDTO.builder()
            .nik("123") // Too short, should be 16 digits
            .phoneNumber("081234567890")
            .build();

    Set<ConstraintViolation<UserProfileDTO>> violations = validator.validate(dto);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nik")));
  }

  @Test
  @DisplayName("UserProfileDTO with non-numeric NIK should fail validation")
  void userProfileDTO_NonNumericNik_ShouldFailValidation() {
    UserProfileDTO dto =
        UserProfileDTO.builder()
            .nik("123456789012345A") // Contains letter
            .phoneNumber("081234567890")
            .build();

    Set<ConstraintViolation<UserProfileDTO>> violations = validator.validate(dto);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nik")));
  }

  @Test
  @DisplayName("UserProfileDTO with invalid phone number should fail validation")
  void userProfileDTO_InvalidPhoneNumber_ShouldFailValidation() {
    UserProfileDTO dto =
        UserProfileDTO.builder()
            .nik("1234567890123456")
            .phoneNumber("081-234-567") // Contains dashes
            .build();

    Set<ConstraintViolation<UserProfileDTO>> violations = validator.validate(dto);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
  }

  @Test
  @DisplayName("UserProfileDTO with too long address should fail validation")
  void userProfileDTO_TooLongAddress_ShouldFailValidation() {
    String longAddress = "A".repeat(256); // Exceeds 255 character limit

    UserProfileDTO dto = UserProfileDTO.builder().address(longAddress).build();

    Set<ConstraintViolation<UserProfileDTO>> violations = validator.validate(dto);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
  }

  @Test
  @DisplayName("UserProfileDTO with valid data should pass validation")
  void userProfileDTO_ValidData_ShouldPassValidation() {
    UserProfileDTO dto =
        UserProfileDTO.builder()
            .nik("1234567890123456")
            .phoneNumber("081234567890")
            .address("Valid address")
            .build();

    Set<ConstraintViolation<UserProfileDTO>> violations = validator.validate(dto);

    assertTrue(violations.isEmpty());
  }

  // ==================== ActionHistoryRequestDTO Validation Tests ====================

  @Test
  @DisplayName("ActionHistoryRequestDTO with invalid month should fail validation")
  void actionHistoryRequestDTO_InvalidMonth_ShouldFailValidation() {
    ActionHistoryRequestDTO dto = ActionHistoryRequestDTO.builder().month(13).build();

    Set<ConstraintViolation<ActionHistoryRequestDTO>> violations = validator.validate(dto);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("month")));
  }

  @Test
  @DisplayName("ActionHistoryRequestDTO with invalid page should fail validation")
  void actionHistoryRequestDTO_InvalidPage_ShouldFailValidation() {
    ActionHistoryRequestDTO dto = ActionHistoryRequestDTO.builder().page(-1).build();

    Set<ConstraintViolation<ActionHistoryRequestDTO>> violations = validator.validate(dto);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("page")));
  }

  @Test
  @DisplayName("ActionHistoryRequestDTO with valid data should pass validation")
  void actionHistoryRequestDTO_ValidData_ShouldPassValidation() {
    ActionHistoryRequestDTO dto =
        ActionHistoryRequestDTO.builder().month(6).year(2024).page(0).size(20).build();

    Set<ConstraintViolation<ActionHistoryRequestDTO>> violations = validator.validate(dto);

    assertTrue(violations.isEmpty());
  }

  // ==================== BulkRoleMenuUpdateRequest Validation Tests ====================

  @Test
  @DisplayName("BulkRoleMenuUpdateRequest with missing roleId should fail validation")
  void bulkRoleMenuUpdateRequest_MissingRoleId_ShouldFailValidation() {
    BulkRoleMenuUpdateRequest request =
        new BulkRoleMenuUpdateRequest(null, java.util.List.of(1L, 2L));

    Set<ConstraintViolation<BulkRoleMenuUpdateRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("roleId")));
  }

  @Test
  @DisplayName("BulkRoleMenuUpdateRequest with empty menuIds should fail validation")
  void bulkRoleMenuUpdateRequest_EmptyMenuIds_ShouldFailValidation() {
    BulkRoleMenuUpdateRequest request = new BulkRoleMenuUpdateRequest(1L, java.util.List.of());

    Set<ConstraintViolation<BulkRoleMenuUpdateRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("menuIds")));
  }

  @Test
  @DisplayName("BulkRoleMenuUpdateRequest with valid data should pass validation")
  void bulkRoleMenuUpdateRequest_ValidData_ShouldPassValidation() {
    BulkRoleMenuUpdateRequest request =
        new BulkRoleMenuUpdateRequest(1L, java.util.List.of(1L, 2L, 3L));

    Set<ConstraintViolation<BulkRoleMenuUpdateRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
