package com.example.demo.repository;

import com.example.demo.entity.LoanHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanHistoryRepository extends JpaRepository<LoanHistory, Long> {

  List<LoanHistory> findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(
      Long loanApplicationId);

  List<LoanHistory> findByActorUser_Id(Long actorUserId);

  List<LoanHistory> findByAction(String action);

  List<LoanHistory> findByLoanApplication_LoanApplicationIdAndAction(
      Long loanApplicationId, String action);

  // Parse latest comment
  LoanHistory findTopByLoanApplication_LoanApplicationIdAndCommentIsNotNullOrderByCreatedAtDesc(
      Long loanApplicationId);

  // Dashboard Trends
  @Query(
      "SELECT YEAR(lh.createdAt) as year, SUM(la.amount) as total "
          + "FROM LoanHistory lh "
          + "JOIN lh.loanApplication la "
          + "WHERE lh.action = 'DISBURSE' "
          + "GROUP BY YEAR(lh.createdAt) "
          + "ORDER BY YEAR(lh.createdAt)")
  List<Object[]> findYearlyDisbursementTrend();

  @Query(
      "SELECT MONTH(lh.createdAt) as month, SUM(la.amount) as total "
          + "FROM LoanHistory lh "
          + "JOIN lh.loanApplication la "
          + "WHERE lh.action = 'DISBURSE' AND YEAR(lh.createdAt) = :year "
          + "GROUP BY MONTH(lh.createdAt) "
          + "ORDER BY MONTH(lh.createdAt)")
  List<Object[]> findMonthlyDisbursementStats(@Param("year") Integer year);
}
