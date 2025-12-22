package com.example.demo.repository;

import com.example.demo.entity.LoanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanHistoryRepository extends JpaRepository<LoanHistory, Long> {

    List<LoanHistory> findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(Long loanApplicationId);

    List<LoanHistory> findByActorUser_Id(Long actorUserId);

    List<LoanHistory> findByAction(String action);

    List<LoanHistory> findByLoanApplication_LoanApplicationIdAndAction(Long loanApplicationId, String action);
}
