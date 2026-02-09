package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.LoanApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

  private final LoanApplicationService loanApplicationService;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<ApiResponse<LoanApplicationDTO>> createLoanApplication(
      @RequestBody LoanApplicationDTO dto) {
    LoanApplicationDTO created = loanApplicationService.createLoanApplication(dto);
    return ResponseUtil.created(created, "Loan application created successfully");
  }

  @GetMapping("/my-history")
  public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getMyLoanHistory() {
    Long userId = getCurrentUserId();
    List<LoanApplicationDTO> history = loanApplicationService.getLoanApplicationsByUserId(userId);
    return ResponseUtil.ok(history, "My loan history retrieved successfully");
  }

  @GetMapping("/{loanApplicationId}")
  public ResponseEntity<ApiResponse<LoanApplicationDTO>> getLoanApplication(
      @PathVariable Long loanApplicationId) {
    LoanApplicationDTO loanApplication =
        loanApplicationService.getLoanApplication(loanApplicationId);
    return ResponseUtil.ok(loanApplication, "Loan application retrieved successfully");
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getLoanApplicationsByUserId(
      @PathVariable Long userId) {
    List<LoanApplicationDTO> loanApplications =
        loanApplicationService.getLoanApplicationsByUserId(userId);
    return ResponseUtil.ok(loanApplications, "Loan applications retrieved successfully");
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getLoanApplicationsByStatus(
      @PathVariable String status) {
    List<LoanApplicationDTO> loanApplications =
        loanApplicationService.getLoanApplicationsByStatus(status);
    return ResponseUtil.ok(loanApplications, "Loan applications retrieved successfully");
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getAllLoanApplications() {
    List<LoanApplicationDTO> loanApplications = loanApplicationService.getAllLoanApplications();
    return ResponseUtil.ok(loanApplications, "Loan applications retrieved successfully");
  }

  @PutMapping("/{loanApplicationId}")
  public ResponseEntity<ApiResponse<LoanApplicationDTO>> updateLoanApplication(
      @PathVariable Long loanApplicationId, @RequestBody LoanApplicationDTO dto) {
    LoanApplicationDTO updated =
        loanApplicationService.updateLoanApplication(loanApplicationId, dto);
    return ResponseUtil.ok(updated, "Loan application updated successfully");
  }

  @DeleteMapping("/{loanApplicationId}")
  public ResponseEntity<ApiResponse<Void>> deleteLoanApplication(
      @PathVariable Long loanApplicationId) {
    loanApplicationService.deleteLoanApplication(loanApplicationId);
    return ResponseUtil.okMessage("Loan application deleted successfully");
  }

  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getId();
    }

    // Fallback for testing - return first user
    return userRepository.findAll().stream()
        .findFirst()
        .map(User::getId)
        .orElseThrow(() -> new ResourceNotFoundException("No users found in the system"));
  }
}
