package com.example.demo.repository;

import com.example.demo.entity.LoanApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

  List<LoanApplication> findByUser_Id(Long userId);

  List<LoanApplication> findByProduct_Id(Long productId);

  List<LoanApplication> findByCurrentStatus(String currentStatus);

  List<LoanApplication> findByUser_IdAndCurrentStatus(Long userId, String currentStatus);

  List<LoanApplication> findByUser_IdOrderByCreatedAtDesc(Long userId);

  List<LoanApplication> findByCurrentStatusInOrderByCreatedAtDesc(List<String> statuses);
}
