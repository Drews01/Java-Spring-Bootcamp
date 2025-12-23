package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.LoanHistoryDTO;
import com.example.demo.service.LoanHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-history")
@RequiredArgsConstructor
public class LoanHistoryController {

    private final LoanHistoryService loanHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanHistoryDTO>> createLoanHistory(@RequestBody LoanHistoryDTO dto) {
        LoanHistoryDTO created = loanHistoryService.createLoanHistory(dto);
        return ResponseUtil.created(created, "Loan history created successfully");
    }

    @GetMapping("/{loanHistoryId}")
    public ResponseEntity<ApiResponse<LoanHistoryDTO>> getLoanHistory(@PathVariable Long loanHistoryId) {
        LoanHistoryDTO loanHistory = loanHistoryService.getLoanHistory(loanHistoryId);
        return ResponseUtil.ok(loanHistory, "Loan history retrieved successfully");
    }

    @GetMapping("/loan/{loanApplicationId}")
    public ResponseEntity<ApiResponse<List<LoanHistoryDTO>>> getLoanHistoryByLoanApplicationId(
            @PathVariable Long loanApplicationId) {
        List<LoanHistoryDTO> loanHistories = loanHistoryService.getLoanHistoryByLoanApplicationId(loanApplicationId);
        return ResponseUtil.ok(loanHistories, "Loan history retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanHistoryDTO>>> getAllLoanHistories() {
        List<LoanHistoryDTO> loanHistories = loanHistoryService.getAllLoanHistories();
        return ResponseUtil.ok(loanHistories, "Loan histories retrieved successfully");
    }

    @DeleteMapping("/{loanHistoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteLoanHistory(@PathVariable Long loanHistoryId) {
        loanHistoryService.deleteLoanHistory(loanHistoryId);
        return ResponseUtil.okMessage("Loan history deleted successfully");
    }
}
