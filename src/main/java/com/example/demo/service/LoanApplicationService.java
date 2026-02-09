package com.example.demo.service;

import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loan Application Service.
 *
 * <p>Provides CRUD operations for loan applications. This service handles:
 *
 * <ul>
 *   <li>Creating new loan applications
 *   <li>Retrieving loan applications by ID, user, or status
 *   <li>Updating loan application details
 *   <li>Deleting loan applications
 * </ul>
 *
 * <p>Note: For loan submission workflow operations, use {@link LoanWorkflowService} instead.
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 * @see LoanWorkflowService
 */
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  @Transactional
  public LoanApplicationDTO createLoanApplication(LoanApplicationDTO dto) {
    User user =
        userRepository
            .findById(dto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));

    Product product =
        productRepository
            .findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

    LoanApplication loanApplication =
        LoanApplication.builder()
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
    LoanApplication loanApplication =
        loanApplicationRepository
            .findById(loanApplicationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("LoanApplication", "id", loanApplicationId));
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
    LoanApplication loanApplication =
        loanApplicationRepository
            .findById(loanApplicationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("LoanApplication", "id", loanApplicationId));

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
        .displayStatus(mapToDisplayStatus(loanApplication.getCurrentStatus()))
        .productName(loanApplication.getProduct().getName())
        .totalAmountToPay(loanApplication.getTotalAmountToPay())
        .createdAt(loanApplication.getCreatedAt())
        .updatedAt(loanApplication.getUpdatedAt())
        .build();
  }

  private String mapToDisplayStatus(String status) {
    if (status == null) {
      return "Unknown";
    }
    switch (status) {
      case "SUBMITTED":
      case "IN_REVIEW":
        return "Review Marketing";
      case "WAITING_APPROVAL":
        return "Review Branch Manager";
      case "APPROVED_WAITING_DISBURSEMENT":
        return "Waiting Disbursement";
      case "DISBURSED":
        return "Active";
      case "PAID":
        return "Completed";
      case "REJECTED":
        return "Rejected";
      default:
        return status;
    }
  }
}
