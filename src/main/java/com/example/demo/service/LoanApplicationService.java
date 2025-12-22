package com.example.demo.service;

import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public LoanApplicationDTO createLoanApplication(LoanApplicationDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));

        LoanApplication loanApplication = LoanApplication.builder()
                .user(user)
                .product(product)
                .amount(dto.getAmount())
                .tenureMonths(dto.getTenureMonths())
                .interestRateApplied(dto.getInterestRateApplied())
                .currentStatus(dto.getCurrentStatus() != null ? dto.getCurrentStatus() : "DRAFT")
                .build();

        LoanApplication saved = loanApplicationRepository.save(loanApplication);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public LoanApplicationDTO getLoanApplication(Long loanApplicationId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new RuntimeException("LoanApplication not found with id: " + loanApplicationId));
        return convertToDTO(loanApplication);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoanApplicationsByUserId(Long userId) {
        return loanApplicationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoanApplicationsByStatus(String status) {
        return loanApplicationRepository.findByCurrentStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getAllLoanApplications() {
        return loanApplicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanApplicationDTO updateLoanApplication(Long loanApplicationId, LoanApplicationDTO dto) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new RuntimeException("LoanApplication not found with id: " + loanApplicationId));

        loanApplication.setAmount(dto.getAmount());
        loanApplication.setTenureMonths(dto.getTenureMonths());
        loanApplication.setInterestRateApplied(dto.getInterestRateApplied());
        loanApplication.setCurrentStatus(dto.getCurrentStatus());

        LoanApplication updated = loanApplicationRepository.save(loanApplication);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteLoanApplication(Long loanApplicationId) {
        loanApplicationRepository.deleteById(loanApplicationId);
    }

    private LoanApplicationDTO convertToDTO(LoanApplication loanApplication) {
        return LoanApplicationDTO.builder()
                .loanApplicationId(loanApplication.getLoanApplicationId())
                .userId(loanApplication.getUser().getId())
                .productId(loanApplication.getProduct().getId())
                .amount(loanApplication.getAmount())
                .tenureMonths(loanApplication.getTenureMonths())
                .interestRateApplied(loanApplication.getInterestRateApplied())
                .currentStatus(loanApplication.getCurrentStatus())
                .createdAt(loanApplication.getCreatedAt())
                .updatedAt(loanApplication.getUpdatedAt())
                .build();
    }
}
