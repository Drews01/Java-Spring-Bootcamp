package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> createLoanApplication(@RequestBody LoanApplicationDTO dto) {
        LoanApplicationDTO created = loanApplicationService.createLoanApplication(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Loan application created successfully"));
    }

    @GetMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getLoanApplication(@PathVariable Long loanApplicationId) {
        LoanApplicationDTO loanApplication = loanApplicationService.getLoanApplication(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(loanApplication, "Loan application retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getLoanApplicationsByUserId(
            @PathVariable Long userId) {
        List<LoanApplicationDTO> loanApplications = loanApplicationService.getLoanApplicationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(loanApplications, "Loan applications retrieved successfully"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getLoanApplicationsByStatus(
            @PathVariable String status) {
        List<LoanApplicationDTO> loanApplications = loanApplicationService.getLoanApplicationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(loanApplications, "Loan applications retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getAllLoanApplications() {
        List<LoanApplicationDTO> loanApplications = loanApplicationService.getAllLoanApplications();
        return ResponseEntity.ok(ApiResponse.success(loanApplications, "Loan applications retrieved successfully"));
    }

    @PutMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> updateLoanApplication(
            @PathVariable Long loanApplicationId,
            @RequestBody LoanApplicationDTO dto) {
        LoanApplicationDTO updated = loanApplicationService.updateLoanApplication(loanApplicationId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Loan application updated successfully"));
    }

    @DeleteMapping("/{loanApplicationId}")
    public ResponseEntity<ApiResponse<Void>> deleteLoanApplication(@PathVariable Long loanApplicationId) {
        loanApplicationService.deleteLoanApplication(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Loan application deleted successfully"));
    }
}
