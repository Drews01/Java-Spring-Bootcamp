package com.example.demo.service;

import com.example.demo.dto.LoanHistoryDTO;
import com.example.demo.dto.LoanMilestoneDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.User;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanHistoryService {

  private final LoanHistoryRepository loanHistoryRepository;
  private final LoanApplicationRepository loanApplicationRepository;
  private final UserRepository userRepository;

  @Transactional
  public LoanHistoryDTO createLoanHistory(LoanHistoryDTO dto) {
    LoanApplication loanApplication =
        loanApplicationRepository
            .findById(dto.getLoanApplicationId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "LoanApplication not found with id: " + dto.getLoanApplicationId()));

    User actorUser =
        userRepository
            .findById(dto.getActorUserId())
            .orElseThrow(
                () -> new RuntimeException("User not found with id: " + dto.getActorUserId()));

    LoanHistory loanHistory =
        LoanHistory.builder()
            .loanApplication(loanApplication)
            .actorUser(actorUser)
            .action(dto.getAction())
            .comment(dto.getComment())
            .fromStatus(dto.getFromStatus())
            .toStatus(dto.getToStatus())
            .build();

    LoanHistory saved = loanHistoryRepository.save(loanHistory);
    return convertToDTO(saved);
  }

  @Transactional(readOnly = true)
  public LoanHistoryDTO getLoanHistory(Long loanHistoryId) {
    LoanHistory loanHistory =
        loanHistoryRepository
            .findById(loanHistoryId)
            .orElseThrow(
                () -> new RuntimeException("LoanHistory not found with id: " + loanHistoryId));
    return convertToDTO(loanHistory);
  }

  @Transactional(readOnly = true)
  public List<LoanHistoryDTO> getLoanHistoryByLoanApplicationId(Long loanApplicationId) {
    return loanHistoryRepository
        .findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(loanApplicationId)
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LoanHistoryDTO> getAllLoanHistories() {
    return loanHistoryRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteLoanHistory(Long loanHistoryId) {
    loanHistoryRepository.deleteById(loanHistoryId);
  }

  @Transactional(readOnly = true)
  public List<LoanMilestoneDTO> getLoanMilestones(Long loanApplicationId) {
    LoanApplication loanApplication =
        loanApplicationRepository
            .findById(loanApplicationId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "LoanApplication not found with id: " + loanApplicationId));

    List<LoanHistory> historyList =
        loanHistoryRepository.findByLoanApplication_LoanApplicationIdOrderByCreatedAtAsc(
            loanApplicationId);

    String currentStatus = loanApplication.getCurrentStatus();

    List<LoanMilestoneDTO> milestones = new ArrayList<>();

    // 1. Submitted - use LoanApplication creation time
    milestones.add(
        LoanMilestoneDTO.builder()
            .name("Submitted")
            .status("COMPLETED")
            .timestamp(loanApplication.getCreatedAt())
            .order(1)
            .build());

    // 2. Marketing - first transition to IN_REVIEW
    LocalDateTime marketingTimestamp =
        historyList.stream()
            .filter(h -> LoanStatus.IN_REVIEW.name().equals(h.getToStatus()))
            .findFirst()
            .map(LoanHistory::getCreatedAt)
            .orElse(null);
    milestones.add(
        LoanMilestoneDTO.builder()
            .name("Marketing")
            .status(getMilestoneStatus(marketingTimestamp, currentStatus, LoanStatus.IN_REVIEW))
            .timestamp(marketingTimestamp)
            .order(2)
            .build());

    // 3. Branch Manager - first transition to WAITING_APPROVAL
    LocalDateTime branchManagerTimestamp =
        historyList.stream()
            .filter(h -> LoanStatus.WAITING_APPROVAL.name().equals(h.getToStatus()))
            .findFirst()
            .map(LoanHistory::getCreatedAt)
            .orElse(null);
    milestones.add(
        LoanMilestoneDTO.builder()
            .name("Branch Manager")
            .status(
                getMilestoneStatus(
                    branchManagerTimestamp, currentStatus, LoanStatus.WAITING_APPROVAL))
            .timestamp(branchManagerTimestamp)
            .order(3)
            .build());

    // 4. Back Office - first transition to APPROVED_WAITING_DISBURSEMENT
    LocalDateTime backOfficeTimestamp =
        historyList.stream()
            .filter(h -> LoanStatus.APPROVED_WAITING_DISBURSEMENT.name().equals(h.getToStatus()))
            .findFirst()
            .map(LoanHistory::getCreatedAt)
            .orElse(null);
    milestones.add(
        LoanMilestoneDTO.builder()
            .name("Back Office")
            .status(
                getMilestoneStatus(
                    backOfficeTimestamp, currentStatus, LoanStatus.APPROVED_WAITING_DISBURSEMENT))
            .timestamp(backOfficeTimestamp)
            .order(4)
            .build());

    // 5. Disbursed - first transition to DISBURSED
    LocalDateTime disbursedTimestamp =
        historyList.stream()
            .filter(h -> LoanStatus.DISBURSED.name().equals(h.getToStatus()))
            .findFirst()
            .map(LoanHistory::getCreatedAt)
            .orElse(null);
    milestones.add(
        LoanMilestoneDTO.builder()
            .name("Disbursed")
            .status(getMilestoneStatus(disbursedTimestamp, currentStatus, LoanStatus.DISBURSED))
            .timestamp(disbursedTimestamp)
            .order(5)
            .build());

    return milestones;
  }

  private String getMilestoneStatus(
      LocalDateTime timestamp, String currentLoanStatus, LoanStatus milestoneStatus) {
    if (milestoneStatus.name().equals(currentLoanStatus)) {
      return "CURRENT";
    }
    if (timestamp != null) {
      return "COMPLETED";
    }
    return "PENDING";
  }

  private LoanHistoryDTO convertToDTO(LoanHistory loanHistory) {
    return LoanHistoryDTO.builder()
        .loanHistoryId(loanHistory.getLoanHistoryId())
        .loanApplicationId(loanHistory.getLoanApplication().getLoanApplicationId())
        .actorUserId(loanHistory.getActorUser().getId())
        .action(loanHistory.getAction())
        .comment(loanHistory.getComment())
        .fromStatus(loanHistory.getFromStatus())
        .toStatus(loanHistory.getToStatus())
        .createdAt(loanHistory.getCreatedAt())
        .build();
  }
}
