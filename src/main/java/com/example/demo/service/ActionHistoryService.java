package com.example.demo.service;

import com.example.demo.dto.ActionHistoryDTO;
import com.example.demo.dto.ActionHistoryPageDTO;
import com.example.demo.dto.ActionHistoryRequestDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.repository.LoanHistoryRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActionHistoryService {

  private final LoanHistoryRepository loanHistoryRepository;

  // Define actions per role
  private static final List<String> MARKETING_ACTIONS =
      Arrays.asList("SUBMIT", "COMMENT_FORWARD", "COMMENT", "REJECT");

  private static final List<String> BRANCH_MANAGER_ACTIONS =
      Arrays.asList("APPROVE", "REJECT", "RETURN_TO_MARKETING", "COMMENT");

  private static final List<String> BACK_OFFICE_ACTIONS = Arrays.asList("DISBURSE", "REJECT");

  // Action display names
  private static final Map<String, String> ACTION_DISPLAY_NAMES = new HashMap<>();

  static {
    ACTION_DISPLAY_NAMES.put("SUBMIT", "Submitted");
    ACTION_DISPLAY_NAMES.put("COMMENT_FORWARD", "Forwarded with Comment");
    ACTION_DISPLAY_NAMES.put("COMMENT", "Commented");
    ACTION_DISPLAY_NAMES.put("APPROVE", "Approved");
    ACTION_DISPLAY_NAMES.put("REJECT", "Rejected");
    ACTION_DISPLAY_NAMES.put("RETURN_TO_MARKETING", "Returned to Marketing");
    ACTION_DISPLAY_NAMES.put("DISBURSE", "Disbursed");
  }

  @Transactional(readOnly = true)
  public ActionHistoryPageDTO getMarketingHistory(Long actorId, ActionHistoryRequestDTO request) {
    return getActionHistory(actorId, MARKETING_ACTIONS, request);
  }

  @Transactional(readOnly = true)
  public ActionHistoryPageDTO getBranchManagerHistory(
      Long actorId, ActionHistoryRequestDTO request) {
    return getActionHistory(actorId, BRANCH_MANAGER_ACTIONS, request);
  }

  @Transactional(readOnly = true)
  public ActionHistoryPageDTO getBackOfficeHistory(Long actorId, ActionHistoryRequestDTO request) {
    return getActionHistory(actorId, BACK_OFFICE_ACTIONS, request);
  }

  private ActionHistoryPageDTO getActionHistory(
      Long actorId, List<String> actions, ActionHistoryRequestDTO request) {

    int page = request.getPage() != null ? request.getPage() : 0;
    int size = request.getSize() != null ? request.getSize() : 20;
    Integer month = request.getMonth();
    Integer year = request.getYear();

    Pageable pageable = PageRequest.of(page, size);
    Page<LoanHistory> historyPage;

    if (month != null && year != null) {
      historyPage =
          loanHistoryRepository.findByActorAndActionsAndMonthAndYear(
              actorId, actions, month, year, pageable);
    } else if (year != null) {
      historyPage =
          loanHistoryRepository.findByActorAndActionsAndYear(actorId, actions, year, pageable);
    } else {
      historyPage = loanHistoryRepository.findByActorAndActions(actorId, actions, pageable);
    }

    List<ActionHistoryDTO> content =
        historyPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());

    return ActionHistoryPageDTO.builder()
        .content(content)
        .page(historyPage.getNumber())
        .size(historyPage.getSize())
        .totalElements(historyPage.getTotalElements())
        .totalPages(historyPage.getTotalPages())
        .build();
  }

  private ActionHistoryDTO convertToDTO(LoanHistory loanHistory) {
    LoanApplication loan = loanHistory.getLoanApplication();
    User applicant = loan.getUser();
    UserProfile profile = applicant.getUserProfile();
    User actor = loanHistory.getActorUser();

    return ActionHistoryDTO.builder()
        .loanApplicationId(loan.getLoanApplicationId())
        .action(loanHistory.getAction())
        .actionDisplayName(
            ACTION_DISPLAY_NAMES.getOrDefault(loanHistory.getAction(), loanHistory.getAction()))
        .actionDate(loanHistory.getCreatedAt())
        // Loan Info
        .productName(loan.getProduct() != null ? loan.getProduct().getName() : null)
        .amount(loan.getAmount() != null ? BigDecimal.valueOf(loan.getAmount()) : null)
        .tenureMonths(loan.getTenureMonths())
        // Applicant Info
        .userId(applicant.getId())
        .username(applicant.getUsername())
        .userEmail(applicant.getEmail())
        .userNik(profile != null ? profile.getNik() : null)
        .userPhoneNumber(profile != null ? profile.getPhoneNumber() : null)
        .userAddress(profile != null ? profile.getAddress() : null)
        .userAccountNumber(profile != null ? profile.getAccountNumber() : null)
        .userBankName(profile != null ? profile.getBankName() : null)
        .userKtpPath(profile != null ? profile.getKtpPath() : null)
        // Actor Info
        .actorId(actor.getId())
        .actorUsername(actor.getUsername())
        // Status
        .previousStatus(loanHistory.getFromStatus())
        .resultingStatus(loanHistory.getToStatus())
        // Comment
        .comment(loanHistory.getComment())
        .build();
  }
}
