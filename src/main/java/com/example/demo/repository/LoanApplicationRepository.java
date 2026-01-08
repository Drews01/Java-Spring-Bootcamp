package com.example.demo.repository;

import com.example.demo.entity.LoanApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

  List<LoanApplication> findByUser_Id(Long userId);

  List<LoanApplication> findByProduct_Id(Long productId);

  List<LoanApplication> findByCurrentStatus(String currentStatus);

  List<LoanApplication> findByUser_IdAndCurrentStatus(Long userId, String currentStatus);

  List<LoanApplication> findByUser_IdOrderByCreatedAtDesc(Long userId);

  List<LoanApplication> findByCurrentStatusInOrderByCreatedAtDesc(List<String> statuses);

  // Tier system queries
  List<LoanApplication> findByUser_IdAndIsPaidFalse(Long userId);

  List<LoanApplication> findByUser_IdAndIsPaidTrue(Long userId);

  @Query(
      "SELECT COALESCE(SUM(la.amount), 0) FROM LoanApplication la "
          + "WHERE la.user.id = :userId AND la.isPaid = false "
          + "AND la.currentStatus = 'DISBURSED'")
  Double findTotalUnpaidAmountByUserId(@Param("userId") Long userId);

  @Query(
      "SELECT COALESCE(SUM(la.amount), 0) FROM LoanApplication la"
          + " WHERE la.user.id = :userId AND la.isPaid = true")
  Double findTotalPaidAmountByUserId(@Param("userId") Long userId);

  // Dashboard Stats
  Long countByCurrentStatus(String currentStatus);

  @Query(
      "SELECT COALESCE(SUM(la.amount), 0) FROM LoanApplication la WHERE la.currentStatus = :status")
  Double sumAmountByCurrentStatus(@Param("status") String status);

  @Query(
      "SELECT COALESCE(SUM(la.totalAmountToPay), 0) FROM LoanApplication la WHERE"
          + " la.currentStatus = :status")
  Double sumTotalAmountToPayByCurrentStatus(@Param("status") String status);

  @Query("SELECT COUNT(la) FROM LoanApplication la")
  Long countTotalApplications();

  // Dashboard Stats (Filtered by Year)
  @Query(
      "SELECT COUNT(la) FROM LoanApplication la WHERE la.currentStatus = :status AND YEAR(la.createdAt) = :year")
  Long countByCurrentStatusAndYear(@Param("status") String currentStatus, @Param("year") int year);

  @Query(
      "SELECT COALESCE(SUM(la.amount), 0) FROM LoanApplication la WHERE la.currentStatus = :status AND YEAR(la.createdAt) = :year")
  Double sumAmountByCurrentStatusAndYear(@Param("status") String status, @Param("year") int year);

  @Query(
      "SELECT COALESCE(SUM(la.totalAmountToPay), 0) FROM LoanApplication la WHERE"
          + " la.currentStatus = :status AND YEAR(la.createdAt) = :year")
  Double sumTotalAmountToPayByCurrentStatusAndYear(
      @Param("status") String status, @Param("year") int year);

  @Query("SELECT COUNT(la) FROM LoanApplication la WHERE YEAR(la.createdAt) = :year")
  Long countTotalApplicationsByYear(@Param("year") int year);

  // Check if user has any active loans (not in final status)
  @Query(
      "SELECT COUNT(la) > 0 FROM LoanApplication la"
          + " WHERE la.user.id = :userId"
          + " AND la.currentStatus NOT IN ('DISBURSED', 'PAID', 'REJECTED')")
  boolean hasActiveLoan(@Param("userId") Long userId);
}
