package com.example.demo.service;

import com.example.demo.dto.AdminLoanApplicationDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.UserProfile;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.UserProfileRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin-specific loan application operations. Provides read-only access to all loan
 * applications with enhanced information for administrative purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLoanService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final UserProfileRepository userProfileRepository;

  /**
   * Get all loan applications with full details for admin view.
   *
   * @param pageable pagination parameters
   * @return Page of AdminLoanApplicationDTO with loan, user, profile, status bucket, and branch
   *     information
   */
  @Transactional(readOnly = true)
  public Page<AdminLoanApplicationDTO> getAllLoanApplications(Pageable pageable) {
    log.info("Admin fetching all loan applications with pagination: {}", pageable);

    Page<LoanApplication> loanApplications = loanApplicationRepository.findAll(pageable);

    return loanApplications.map(this::convertToAdminDTO);
  }

  /**
   * Get all loan applications without pagination (for smaller datasets).
   *
   * @return List of AdminLoanApplicationDTO
   */
  @Transactional(readOnly = true)
  public List<AdminLoanApplicationDTO> getAllLoanApplications() {
    log.info("Admin fetching all loan applications");

    List<LoanApplication> loanApplications = loanApplicationRepository.findAll();

    return loanApplications.stream().map(this::convertToAdminDTO).collect(Collectors.toList());
  }

  /**
   * Convert LoanApplication entity to AdminLoanApplicationDTO.
   *
   * @param loanApplication the loan application entity
   * @return AdminLoanApplicationDTO with all admin-relevant information
   */
  private AdminLoanApplicationDTO convertToAdminDTO(LoanApplication loanApplication) {
    Long userId = loanApplication.getUser().getId();

    // Fetch user profile (may be null if not created yet)
    Optional<UserProfile> userProfile = userProfileRepository.findById(userId);

    return AdminLoanApplicationDTO.builder()
        // Loan Application Info
        .loanApplicationId(loanApplication.getLoanApplicationId())
        .amount(loanApplication.getAmount())
        .tenureMonths(loanApplication.getTenureMonths())
        .interestRateApplied(loanApplication.getInterestRateApplied())
        .totalAmountToPay(loanApplication.getTotalAmountToPay())
        .productName(loanApplication.getProduct().getName())
        .createdAt(loanApplication.getCreatedAt())
        .updatedAt(loanApplication.getUpdatedAt())
        // User Info
        .userId(userId)
        .userName(loanApplication.getUser().getUsername())
        .userEmail(loanApplication.getUser().getEmail())
        // Profile Info
        .profileId(userId) // UserProfile uses @MapsId, so profile ID = user ID
        .nik(userProfile.map(UserProfile::getNik).orElse(null))
        .phoneNumber(userProfile.map(UserProfile::getPhoneNumber).orElse(null))
        .bankName(userProfile.map(UserProfile::getBankName).orElse(null))
        .accountNumber(userProfile.map(UserProfile::getAccountNumber).orElse(null))
        // Status Info
        .currentStatus(loanApplication.getCurrentStatus())
        .displayStatus(mapToDisplayStatus(loanApplication.getCurrentStatus()))
        .currentBucket(determineBucket(loanApplication.getCurrentStatus()))
        // Branch Info
        .branchId(loanApplication.getBranch() != null ? loanApplication.getBranch().getId() : null)
        .branchName(
            loanApplication.getBranch() != null ? loanApplication.getBranch().getName() : null)
        // Location Info
        .latitude(loanApplication.getLatitude())
        .longitude(loanApplication.getLongitude())
        .build();
  }

  /**
   * Determine which bucket/queue the loan application belongs to based on its current status.
   *
   * @param status the current status of the loan application
   * @return the bucket name: "MARKETING", "BRANCH_MANAGER", "BACK_OFFICE", or "COMPLETED"
   */
  public String determineBucket(String status) {
    if (LoanStatus.isMarketingQueue(status)) {
      return "MARKETING";
    }
    if (LoanStatus.isBranchManagerQueue(status)) {
      return "BRANCH_MANAGER";
    }
    if (LoanStatus.isBackOfficeQueue(status)) {
      return "BACK_OFFICE";
    }
    if ("DISBURSED".equals(status) || "PAID".equals(status) || "REJECTED".equals(status)) {
      return "COMPLETED";
    }
    return "UNKNOWN";
  }

  /**
   * Map raw status to human-readable display status.
   *
   * @param status the raw status string
   * @return human-readable display status
   */
  private String mapToDisplayStatus(String status) {
    if (status == null) {
      return "Unknown";
    }
    switch (status) {
      case "SUBMITTED":
        return "Submitted";
      case "IN_REVIEW":
        return "In Review";
      case "WAITING_APPROVAL":
        return "Waiting Approval";
      case "APPROVED_WAITING_DISBURSEMENT":
        return "Approved - Waiting Disbursement";
      case "DISBURSED":
        return "Disbursed";
      case "PAID":
        return "Paid";
      case "REJECTED":
        return "Rejected";
      default:
        return status;
    }
  }
}
