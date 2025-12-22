package com.example.demo.service;

import com.example.demo.dto.LoanHistoryDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.User;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanHistoryService {

    private final LoanHistoryRepository loanHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public LoanHistoryDTO createLoanHistory(LoanHistoryDTO dto) {
        LoanApplication loanApplication = loanApplicationRepository.findById(dto.getLoanApplicationId())
                .orElseThrow(
                        () -> new RuntimeException("LoanApplication not found with id: " + dto.getLoanApplicationId()));

        User actorUser = userRepository.findById(dto.getActorUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getActorUserId()));

        LoanHistory loanHistory = LoanHistory.builder()
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
        LoanHistory loanHistory = loanHistoryRepository.findById(loanHistoryId)
                .orElseThrow(() -> new RuntimeException("LoanHistory not found with id: " + loanHistoryId));
        return convertToDTO(loanHistory);
    }

    @Transactional(readOnly = true)
    public List<LoanHistoryDTO> getLoanHistoryByLoanApplicationId(Long loanApplicationId) {
        return loanHistoryRepository.findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(loanApplicationId)
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
