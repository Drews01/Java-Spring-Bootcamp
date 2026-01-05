package com.example.demo.repository;

import com.example.demo.entity.LoanHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanHistoryRepository extends JpaRepository<LoanHistory, Long> {

  List<LoanHistory> findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(
      Long loanApplicationId);

  List<LoanHistory> findByActorUser_Id(Long actorUserId);

  List<LoanHistory> findByAction(String action);

  List<LoanHistory> findByLoanApplication_LoanApplicationIdAndAction(
      Long loanApplicationId, String action);
}
